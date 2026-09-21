/**
 * 后端连通性引导（仅桌面端）：
 * 进入登录页 / 首次完成鉴权时探测一次 backendUrl，
 * 连不上就弹框询问，确认后跳「服务托管」页去安装/启动后端。
 *
 * 每次进程启动只打扰用户一次（checked 标记），取消弹框后不再重复弹。
 */

import { ElMessageBox } from 'element-plus'
import router from '@/router'
import { launcherAPI } from '@/api/launcher'
import { isElectron } from '@/api/hardware'

let checked = false

/** 已提醒过一次就不再探测/弹框（如需要重新检测，刷新或重启应用即可） */
export function resetBackendGuide(): void {
  checked = false
}

export async function ensureBackendOrGuide(): Promise<void> {
  if (!isElectron() || checked) return
  checked = true
  let reachable = true
  let url = ''
  try {
    const r = await launcherAPI.checkBackend()
    // 探测通道本身失败（异常/降级）不打扰用户，避免误报
    if (!r?.ok || !r.data) return
    reachable = r.data.reachable
    url = r.data.url
  } catch {
    return
  }
  if (reachable) return

  try {
    await ElMessageBox.confirm(
      `检测到后端服务连接不上：${url || '未知地址'} 无响应。` +
        '可以去「服务托管」页检查环境并一键启动后端；也可以稍后在网络恢复后重试。',
      '后端未启动',
      {
        confirmButtonText: '去启动后端',
        cancelButtonText: '稍后重试',
        type: 'warning',
      }
    )
  } catch {
    return // 用户取消，不再打扰
  }

  const from = router.currentRoute.value.fullPath
  if (from.startsWith('/system/services')) return
  router.push({ path: '/system/services', query: { from } })
}
