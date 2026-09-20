/**
 * 桌面端（Electron）与 Web 端的差异判断集中在这里。
 * 页面只有一套，靠这些函数在运行时决定跳转/显隐，避免分叉维护。
 */
import { isElectron } from '@/api/hardware'

/** 桌面端登录后/已登录访问登录页时的默认落地页：收银台；Web 端仍是首页看板 */
export function defaultLandingPath(): string {
  return isElectron() ? '/pos/cashier' : '/index'
}
