# AGENTS.md

启航电商ERP — Spring Boot 4.1 + Vue 3 单体应用。

## 构建与运行

**后端（Java 17, Maven）**
```bash
mvn clean install                           # 构建所有 6 个模块
mvn clean package -DskipTests               # 跳过测试
java -jar erp-api/target/erp-api.jar        # 启动，端口 8088
```

profile 通过 `application.yml` 中 `spring.profiles.active` 切换（默认 `demo`）。

**前端（vue/ 目录）**
```bash
cd vue
npm install
npm run dev      # 开发服务器，端口 88，代理 /api → http://localhost:8088
npm run build    # vue-tsc 类型检查 + vite build
```

**登录**: admin / QHerp@23

## 架构要点

- **包名必须用 `cn.qihangerp`**（是 qihang**er**p，不是 qihangp）
- 入口类: `erp-api/.../ErpApi.java`，`@ComponentScan("cn.qihangerp")`
- 6 个 Maven 模块: `erp-api` → `security` → `common` → `mapper` → `model` → `service`
  - `erp-api` 为 Spring Boot 主模块，控制器分 `erp/` 和 `sys/` 两个包
  - `common` 含工具类、`ResultVo`、`PageQuery`、`PageResult`
  - `security` 含 JWT + Spring Security 7.x 配置
  - `model` 含 entity/bo/vo/dto/query
  - open-sdk 已移除（`erp-api/pom.xml` 中标注 "open-sdk removed"）
- 若新增 Service 接口，必须在 `service/.../impl/` 下同步创建 `XxxServiceImpl`（否则启动报 NoSuchBeanDefinitionException）
- 所有平台店铺数据用**统一表**: `ShopOrder`(`oms_shop_order`), `ShopOrderItem`, `ShopRefund`, `ShopGoods`, `ShopGoodsSku`
- 平台枚举: TAO=1, JD=2, PDD=3, DOU=4, WEI=5, KWAI=6, XHS=7, OFFLINE=100, ERP_ORDER=101
- 表前缀: `oms_` 订单, `o_` 业务, `g_` 商品, `s_` 库存
- API 统一返回 `ResultVo<T>`，分页用 `PageQuery` / `PageResult<T>`
- Service 接口以 `I` 开头（`IUserService`），`@Autowired` 注入
- 时间戳字段: `createTime` / `updateTime`（LocalDateTime）

## MyBatis-Plus 使用规范（重要）

### Service 接口
- Service 接口必须继承 `IService<Entity>`，例如：
  ```java
  public interface OShopService extends IService<OShop> { ... }
  ```

### Service 实现
- Service 实现必须继承 `ServiceImpl<Mapper, Entity>`，例如：
  ```java
  public class OShopServiceImpl extends ServiceImpl<OShopMapper, OShop> implements OShopService { ... }
  ```

### IService 常用方法（不要用 MyBatis 原生方法名）
| 正确方法 | 错误方法（不存在） | 说明 |
|---------|------------------|------|
| `getById(id)` | `selectById(id)` | 根据ID查询 |
| `save(entity)` | `insert(entity)` | 新增 |
| `updateById(entity)` | - | 修改 |
| `removeByIds(list)` | `deleteBatchIds(array)` | 批量删除（参数是Collection，不是数组） |
| `list(wrapper)` | `selectList(wrapper)` | 查询列表 |
| `count(wrapper)` | `selectCount(wrapper)` | 计数 |

### 分页查询
- `PageResult` 没有 `(List, long)` 构造函数
- 正确用法：`PageResult.build(page)` 其中 page 是 MyBatis-Plus 的 `IPage` 对象
  ```java
  Page<Entity> page = new Page<>(pageNum, pageSize);
  Page<Entity> result = baseMapper.selectPage(page, wrapper);
  return PageResult.build(result);  // 正确
  // return new PageResult<>(result.getRecords(), result.getTotal());  // 错误
  ```

### Mapper 接口
- Mapper 接口必须继承 `BaseMapper<Entity>`，例如：
  ```java
  public interface OShopMapper extends BaseMapper<OShop> { }
  ```

### Entity 实体类
- 必须使用 `@TableName("表名")` 注解
- 主键使用 `@TableId(type = IdType.AUTO)`
- 非数据库字段使用 `@TableField(exist = false)`

## 零售POS复用策略（零新增表）

**核心原则**：最大化复用现有161张表，仅扩展少量字段，先跑通业务再迭代。

### 门店管理
- 复用 `o_shop`（type=999 表示线下门店）
- 扩展字段: `number`/`opening_hours`/`register_count`/`print_config`/`pay_config`

### 营业员管理
- 复用 `erp_sales_person`
- 扩展字段: `position`/`position_code`

### 会员管理
- 复用 `oms_shop_member`
- 扩展字段: `gender`/`birthday`/`level_id`/`total_consumption`/`visit_count`/`points`/`stored_balance`/`last_visit_time`

### 营销促销
- 复用 `o_marketing_discount_rule`
- 扩展 `discount_type` 值: 3=满减/4=第二件半价/5=限时秒杀

### POS销售单
- 复用 `erp_sales_order` + `erp_sales_order_item`

### 库存/采购/仓储
- 全部复用现有表，无需修改

## 前端要点

- Vue 3.5 + TypeScript + Vite 8 + Element Plus + Pinia
- `<script setup lang="ts">` 组合式 API，缩进 2 空格，单引号，无分号
- 路径别名: `@/` 和 `~/` 均指向 `src/`
- 请求通过 `utils/request.ts`（axios），自动附带 `Bearer` Token
- Token 存于 Cookies，key 为 `Admin-Token`（`utils/auth.ts` 用 js-cookie 管理）
- 路由用 `createWebHistory()`，动态菜单从 `sys_menu` 表加载（`permission.ts` 路由守卫）
- 环境变量前缀 `VITE_`（如 `VITE_APP_BASE_API`），配置文件在 `vue/.env.*`
- 错误提示: 前端 `ElMessage.error()`，后端 `ResultVo.error()`
- 生产部署 SSE 需 Nginx 配置: `proxy_http_version 1.1; proxy_buffering off; proxy_read_timeout 1800s;`

## 数据库与环境

- MySQL 8 + Redis 7，数据库名 `qihang-erp`
- 默认 dev 数据库: `127.0.0.1:3306/qihang-erp`，用户 `root`
- MyBatis-Plus mapper XML 位置: `classpath*:mapper/**/*Mapper.xml`
- type-aliases: `cn.qihangerp.oms.domain;cn.qihangerp.module.domain;cn.qihangerp.security.entity`

## 注意

- `AGENTS.md` 在 `.gitignore` 中，不会提交到仓库
- 目前无 CI 配置，无测试用例
- 本文件由 OpenCode 维护，仅包含已验证的高价值信息。与配置/代码矛盾时以可执行源为准。
