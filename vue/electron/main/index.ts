/**
 * Electron 主进程入口（架构移植自参考项目 electron/main.js）
 *
 * 启动流程：
 *   开发态（VITE_DEV_SERVER_URL，由 scripts/electron-dev.mjs 注入）→ 直接连 Vite devServer（自带 /api 代理）
 *   打包态                  → 起内置本地服务（静态 dist + 反代 <apiPrefix>），再加载 http://127.0.0.1:<port>/
 */

import { app, BrowserWindow, Menu, session, net } from 'electron'
import path from 'path'
import { loadConfig } from './config'
import { registerIpc, broadcast } from './ipc/handlers'
import { createWindow, getMainWindow } from './window'
import hardware from './hardware'
import launcher from './launcher/services'
import { startServer } from './server'
import type { LocalServer } from './server'
import type { AppConfig } from './config'

const isDev = !!process.env.VITE_DEV_SERVER_URL

// 固定配置目录名。
// 不这么写的话 Electron 会取 package.json 的 name/productName，
// 结果是 %APPDATA%\qihang-retail-electron —— 既跟产品名对不上，改个包名现场配置就"丢"了。
// 必须在任何 getPath('userData') 调用之前执行。
app.setPath('userData', path.join(app.getPath('appData'), 'qihang-retail-pos'))

let localServer: LocalServer | null = null
let networkTimer: NodeJS.Timeout | null = null
let lastOnline: boolean | null = null

// 收银机只允许开一个实例，避免两台「收银台」抢同一个钱箱
if (!app.requestSingleInstanceLock()) {
  app.quit()
} else {
  app.on('second-instance', () => {
    const win = getMainWindow()
    if (win) {
      if (win.isMinimized()) win.restore()
      win.focus()
    }
  })
}

/** 关闭所有原生权限弹窗，收银端不需要摄像头/麦克风/定位 */
function hardenSession(): void {
  session.defaultSession.setPermissionRequestHandler((_webContents, _permission, callback) => {
    callback(false)
  })
}

async function resolveEntryUrl(config: AppConfig): Promise<string> {
  if (isDev) {
    const url = process.env.VITE_DEV_SERVER_URL!
    console.log('[main] 开发模式，加载', url)
    return url
  }
  localServer = await startServer(config)
  console.log('[main] 本地服务已启动', localServer.url, '→ 后端', config.backendUrl)
  return localServer.url
}

/** 网络状态探测：用主进程的 net 模块（渲染进程断网时页面自己也能感知，这里做双保险推送） */
function startNetworkWatcher(): void {
  const check = () => {
    // net.isOnline() 只代表网卡有链路，后端可达性由页面请求自己判断
    const online = net.isOnline()
    if (online !== lastOnline) {
      lastOnline = online
      broadcast('network-status', online)
    }
  }
  check()
  networkTimer = setInterval(check, 10000)
}

app.whenReady().then(async () => {
  hardenSession()
  registerIpc()

  // Windows/Linux 去掉默认菜单栏
  if (process.platform !== 'darwin') {
    Menu.setApplicationMenu(null)
  }

  const config = loadConfig()
  const url = await resolveEntryUrl(config)
  createWindow(url, config)

  // 设置应用用户模型ID（Windows 任务栏图标分组必需）
  if (process.platform === 'win32') {
    app.setAppUserModelId('com.qihang.retail')
  }

  hardware.init(config)
  hardware.probeAll().catch((e) => console.warn('[main] 硬件自检异常：', e.message))

  startNetworkWatcher()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow(url, config)
    }
  })
})

app.on('window-all-closed', () => {
  console.log('[main] window-all-closed，退出')
  if (process.platform !== 'darwin') app.quit()
})

app.on('quit', (_e, code) => console.log('[main] quit, code =', code))

app.on('before-quit', () => {
  console.log('[main] before-quit 触发')
  if (networkTimer) clearInterval(networkTimer)
  hardware.closeAll()
  if (localServer) {
    console.log('[main] 关闭本地服务')
    localServer.close()
  }
})

// 本程序拉起过服务（MySQL/Redis/后端）时走异步优雅停机，再兜底 killAll 防孤儿进程占端口
app.on('will-quit', (e) => {
  if (launcher.hasManaged()) {
    e.preventDefault()
    launcher
      .stopAll()
      .catch(() => {})
      .finally(() => {
        launcher.killAll()
        app.exit(0)
      })
  } else {
    launcher.killAll()
  }
})

process.on('uncaughtException', (err) => {
  console.error('[main] 未捕获异常：', err)
})
