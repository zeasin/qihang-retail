/**
 * IPC 通道注册（移植自参考项目 electron/ipc.js）
 *
 * 渲染进程（Vue）通过 preload 暴露的 window.electronAPI 调用这里的方法。
 * 所有 handler 都返回 { ok, ... } 结构，不抛异常到渲染进程，避免页面白屏。
 */

import { ipcMain, app, shell, BrowserWindow } from 'electron'
import fs from 'fs'
import hardware from '../hardware'
import { getConfigPath, loadConfig, saveConfig } from '../config'
import type { AppConfig } from '../config'

/** 递归合并：把 source 的键覆盖到 target 上（只覆盖传入的键，保留其余配置） */
function deepAssign<T>(target: T, source: unknown): T {
  const out = (Array.isArray(target) ? [...(target as unknown[])] : { ...(target as object) }) as Record<
    string,
    unknown
  >
  const src = (source || {}) as Record<string, unknown>
  for (const key of Object.keys(src)) {
    const v = src[key]
    const t = (target as Record<string, unknown>)?.[key]
    out[key] =
      v && typeof v === 'object' && !Array.isArray(v) && t && typeof t === 'object'
        ? deepAssign(t, v)
        : v
  }
  return out as T
}

type Handler = (event: Electron.IpcMainInvokeEvent, ...args: any[]) => unknown

/** 包装：把同步/异步 handler 的异常统一转成 {ok:false, reason} */
export function safe(fn: Handler) {
  return async (event: Electron.IpcMainInvokeEvent, ...args: any[]) => {
    try {
      const result = await fn(event, ...args)
      const r = result as any
      return r && typeof r === 'object' && 'ok' in r ? r : { ok: true, data: result }
    } catch (e) {
      console.error('[ipc] 调用失败：', e)
      return { ok: false, reason: (e as Error).message || String(e) }
    }
  }
}

export function registerIpc(): void {
  // ---- 应用信息 ----
  ipcMain.handle(
    'app:version',
    safe(() => ({
      app: app.getVersion(),
      electron: process.versions.electron,
      chrome: process.versions.chrome,
      node: process.versions.node,
      name: app.getName(),
      platform: process.platform
    }))
  )

  ipcMain.handle(
    'app:config',
    safe(() => loadConfig())
  )

  ipcMain.handle(
    'app:config:path',
    safe(() => getConfigPath())
  )

  /**
   * 局部改配置并落盘（自检页选打印机用）。
   * patch 是嵌套对象，只覆盖传进来的键，其余保持原样；
   * backendUrl/apiPrefix 由 saveConfig 负责剔除，不会被写进文件。
   */
  ipcMain.handle(
    'app:config:set',
    safe(async (_e, patch: Partial<AppConfig>) => {
      const current = loadConfig()
      const merged = deepAssign(current, patch || {})
      if (!saveConfig(merged)) throw new Error('配置文件写入失败')
      // 硬件配置变了要重新初始化，否则改了打印机名不生效
      hardware.init(loadConfig())
      await hardware.probeAll().catch(() => {})
      return loadConfig()
    })
  )

  /** 用系统默认程序打开 config.json，方便现场改串口 */
  ipcMain.handle(
    'app:config:open',
    safe(async () => {
      const file = getConfigPath()
      if (!fs.existsSync(file)) loadConfig() // 不存在则先落一份默认值
      const err = await shell.openPath(file)
      if (err) throw new Error(err)
      return { path: file }
    })
  )

  /** 改完配置重启应用（部分配置在启动时读取，不重启不生效） */
  ipcMain.handle(
    'app:relaunch',
    safe(() => {
      app.relaunch()
      app.exit(0)
    })
  )

  ipcMain.handle(
    'app:open-external',
    safe((_e, url: string) => shell.openExternal(String(url)))
  )

  // ---- 窗口控制（无边框收银界面用得上）----
  ipcMain.handle(
    'window:minimize',
    safe((e) => BrowserWindow.fromWebContents(e.sender)?.minimize())
  )
  ipcMain.handle(
    'window:toggle-maximize',
    safe((e) => {
      const win = BrowserWindow.fromWebContents(e.sender)
      if (!win) return
      win.isMaximized() ? win.unmaximize() : win.maximize()
    })
  )
  ipcMain.handle(
    'window:close',
    safe((e) => BrowserWindow.fromWebContents(e.sender)?.close())
  )
  ipcMain.handle(
    'window:toggle-fullscreen',
    safe((e) => {
      const win = BrowserWindow.fromWebContents(e.sender)
      if (!win) return
      win.setFullScreen(!win.isFullScreen())
    })
  )

  // ---- 硬件 ----
  ipcMain.handle(
    'hardware:status',
    safe(() => hardware.status())
  )

  ipcMain.handle(
    'hardware:probe',
    safe(() => hardware.probeAll())
  )

  ipcMain.handle(
    'hardware:list-ports',
    safe(() => hardware.listPorts())
  )

  ipcMain.handle(
    'hardware:list-printers',
    safe(() => hardware.listPrinters())
  )

  ipcMain.handle(
    'hardware:print-receipt',
    safe((_e, data) => hardware.printReceipt(data || {}))
  )

  ipcMain.handle(
    'hardware:print-test',
    safe(() => hardware.testPrint())
  )

  ipcMain.handle(
    'hardware:open-drawer',
    safe(() => hardware.openCashDrawer())
  )

  ipcMain.handle(
    'hardware:display-amount',
    safe((_e, text: string) => hardware.displayAmount(text))
  )

  ipcMain.handle(
    'hardware:scale-read',
    safe(() => hardware.readScale())
  )
}

/** 主进程主动推给渲染进程的事件（网络状态等） */
export function broadcast(channel: string, payload: unknown): void {
  for (const win of BrowserWindow.getAllWindows()) {
    if (!win.isDestroyed()) win.webContents.send(channel, payload)
  }
}
