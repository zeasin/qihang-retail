/**
 * 部署配置 —— 打包给客户前改这里（移植自参考项目 electron/deploy.config.js）
 *
 * 这两项**不放进 config.json**，因为它属于「整个部署环境一套」的参数，
 * 不是「每台收银机一套」的参数。放进配置文件的话，客户机器上残留的旧
 * config.json 会把新代码里的值盖掉 —— 升级后连的还是老地址，很难查。
 *
 * 每台机器不同的东西（串口、纸宽、钱箱）仍然在 config.json 里，见 config.ts。
 */

/**
 * 客户现场的后端地址（Spring Boot 服务，application.yml 中 server.port=6666）
 *
 * 打包给客户前改这里，或用环境变量临时覆盖，不用改代码：
 *   ERP_BACKEND_URL=http://192.168.1.100:6666 npm run build:win
 */
export const BACKEND_URL = process.env.ERP_BACKEND_URL || 'http://localhost:6666'

/**
 * 前端请求前缀，本地服务按此前缀反代到 BACKEND_URL
 *
 * ⚠️ 必须与渲染进程 axios baseURL（api/http.ts）保持一致。
 *    两处不一致的后果：前端请求 /api/xxx，本地服务不认这个前缀，
 *    当成静态资源去找，最后落到 index.html —— 页面能开但所有接口 404。
 */
export const API_PREFIX = '/api'

/**
 * 反代时是否剥掉 API_PREFIX（对应 vue/vite.config.ts 里 devServer 的
 * rewrite: path.replace(/^\/api/, '')）。后端没有 context-path，必须为 true。
 */
export const API_STRIP_PREFIX = true
