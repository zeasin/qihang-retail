package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员实体类
 * 对应表: oms_shop_member
 */
@Data
@TableName("oms_shop_member")
public class OmsShopMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商户id
     */
    private Long merchantId;

    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 店铺类型
     */
    private Integer shopType;

    /**
     * 平台用户id
     */
    private String platformUserId;

    /**
     * 平台用户账号、手机号
     */
    private String platformAccount;

    /**
     * 平台openid
     */
    private String platformOpenid;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别：0未知/1男/2女
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 电话
     */
    private String phone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区
     */
    private String county;

    /**
     * 街道
     */
    private String town;

    /**
     * 地址
     */
    private String address;

    /**
     * 会员等级ID
     */
    private Long levelId;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 累计消费
     */
    private BigDecimal totalConsumption;

    /**
     * 消费次数
     */
    private Integer visitCount;

    /**
     * 当前积分
     */
    private Integer points;

    /**
     * 储值余额
     */
    private BigDecimal storedBalance;

    /**
     * 最后消费时间
     */
    private LocalDateTime lastVisitTime;

    /**
     * 确认状态（0未确认1已确认）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 系统创建时间
     */
    private LocalDateTime createOn;

    /**
     * 系统更新时间
     */
    private LocalDateTime updateOn;
}
