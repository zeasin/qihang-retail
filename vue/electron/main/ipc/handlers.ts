/**
 * IPC 通道注册（移植自参考项目 electron/ipc.js）
 *
 * 渲染进程（Vue）通过 preload 暴露的 window.electronAPI 调用这里的方法。
 * 所有 handler 都返回 { ok, ... } 结构，不抛异常到渲染进程，避免页面白屏。
 */

import { ipcMain, app, shell, BrowserWindow, dialog } from 'electron'
import fs from 'fs'
import hardware from '../hardware'
import launcher from '../launcher/services'
import type { ServiceName } from '../launcher/services'
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

  // ---- 服务托管（借鉴 qihang-launcher：检测/下载安装/启停 JDK、MySQL、Redis、后端）----
  ipcMain.handle(
    'launcher:status',
    safe(() => launcher.status())
  )

  /** 启动阶段后端探活（轻量，只查 backendUrl 可达性） */
  ipcMain.handle(
    'launcher:backend',
    safe(() => launcher.checkBackend())
  )

  ipcMain.handle(
    'launcher:install',
    safe((_e, name: ServiceName) =>
      launcher.install(name, (p) => broadcast('launcher-progress', p))
    )
  )

  ipcMain.handle('launcher:start', safe((_e, name: ServiceName) => launcher.start(name)))
  ipcMain.handle('launcher:stop', safe((_e, name: ServiceName) => launcher.stop(name)))
  ipcMain.handle('launcher:start-all', safe(() => launcher.startAll()))
  ipcMain.handle('launcher:stop-all', safe(() => launcher.stopAll()))
  ipcMain.handle('launcher:log', safe((_e, name: ServiceName) => ({ text: launcher.logTail(name) })))
  ipcMain.handle(
    'launcher:open-dir',
    safe(async () => {
      const dir = launcher.runtimeDir()
      fs.mkdirSync(dir, { recursive: true })
      const err = await shell.openPath(dir)
      if (err) throw new Error(err)
      return { path: dir }
    })
  )

  /** 选择本机已有的后端 jar（客户现场 jar 不在 runtime 下时用） */
  ipcMain.handle(
    'launcher:pick-jar',
    safe(async (e) => {
      const win = BrowserWindow.fromWebContents(e.sender)
      const opts = {
        title: '选择后端 jar',
        filters: [{ name: 'Java 包', extensions: ['jar'] }],
        properties: ['openFile'] as ['openFile']
      }
      const r = win ? await dialog.showOpenDialog(win, opts) : await dialog.showOpenDialog(opts)
      if (r.canceled || !r.filePaths[0]) return { ok: false, reason: '已取消' }
      return { ok: true, data: r.filePaths[0] }
    })
  )

  /** 选择本机已有 JDK 的目录（launcher.jdkDir） */
  ipcMain.handle(
    'launcher:pick-jdk-dir',
    safe(async (e) => {
      const win = BrowserWindow.fromWebContents(e.sender)
      const opts = {
        title: '选择本地 JDK 目录（其下有 bin\\java.exe）',
        properties: ['openDirectory'] as ['openDirectory']
      }
      const r = win ? await dialog.showOpenDialog(win, opts) : await dialog.showOpenDialog(opts)
      if (r.canceled || !r.filePaths[0]) return { ok: false, reason: '已取消' }
      return { ok: true, data: r.filePaths[0] }
    })
  )

  /** 测试 JDK 是否可用（java -version）；参数可传尚未保存的目录，留空按已保存配置解析 */
  ipcMain.handle('launcher:test-jdk', safe((_e, dir?: string) => launcher.testJava(dir)))

  /** 选择本机 SQL 文件（初始化数据库用） */
  ipcMain.handle(
    'launcher:pick-sql',
    safe(async (e) => {
      const win = BrowserWindow.fromWebContents(e.sender)
      const opts = {
        title: '选择初始化 SQL 文件',
        filters: [{ name: 'SQL 文件', extensions: ['sql'] }],
        properties: ['openFile'] as ['openFile']
      }
      const r = win ? await dialog.showOpenDialog(win, opts) : await dialog.showOpenDialog(opts)
      if (r.canceled || !r.filePaths[0]) return { ok: false, reason: '已取消' }
      return { ok: true, data: r.filePaths[0] }
    })
  )

  /** 导入 SQL 建表/初始化数据；file 留空则用配置的 initSql 下载地址 */
  ipcMain.handle('launcher:init-db', safe((_e, file?: string) => launcher.importSql(file)))
}

/** 主进程主动推给渲染进程的事件（网络状态等） */
export function broadcast(channel: string, payload: unknown): void {
  for (const win of BrowserWindow.getAllWindows()) {
    if (!win.isDestroyed()) win.webContents.send(channel, payload)
  }
}
