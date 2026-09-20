/**
 * 窗口管理（设计文档 2.2：main/window.ts）
 *
 * 收银窗口创建 + 扫码枪主进程级挂载。createWindow 接收要加载的 URL
 * （开发态 = vite dev server，打包态 = 内置本地服务），由 index.ts 决定。
 */

import { BrowserWindow, shell } from 'electron'
import path from 'path'
import type { AppConfig } from './config'
import KeyboardWedgeScanner from './hardware/scanner'

let mainWindow: BrowserWindow | null = null

/** 扫码枪：主进程级捕获，不依赖页面焦点 */
export const scanner = new KeyboardWedgeScanner()

export function getMainWindow(): BrowserWindow | null {
  return mainWindow
}

export function createWindow(url: string, config: AppConfig): BrowserWindow {
  const winCfg = config.window || { width: 1400, height: 900 }

  mainWindow = new BrowserWindow({
    width: winCfg.width || 1400,
    height: winCfg.height || 900,
    minWidth: 1024,
    minHeight: 700,
    fullscreen: !!winCfg.fullscreen,
    frame: winCfg.frame !== false,
    autoHideMenuBar: true,
    backgroundColor: '#f5f7fa',
    title: '启航零售ERP-收银端',
    // 打包态任务栏图标来自 exe 内嵌图标（electron-builder 的 win.icon），无需这里指定；
    // 但开发态进程是 electron.exe，不设这个就会顶着 Electron 的默认图标
    icon: path.join(__dirname, '../build/icon.ico'),
    show: false,
    webPreferences: {
      preload: path.join(__dirname, 'preload.cjs'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: false,
      spellcheck: false
    }
  })

  // 首屏渲染完成再显示，避免白屏闪烁
  mainWindow.once('ready-to-show', () => mainWindow?.show())

  // 站外链接走系统浏览器，不在收银窗口里打开
  mainWindow.webContents.setWindowOpenHandler(({ url: target }) => {
    if (/^https?:/i.test(target)) shell.openExternal(target)
    return { action: 'deny' }
  })

  // 扫码枪挂到本窗口
  scanner.attach(mainWindow.webContents)

  mainWindow.on('closed', () => {
    scanner.detach()
    mainWindow = null
  })

  mainWindow.loadURL(url)
    .then(() => console.log('[window] 页面加载完成', url))
    .catch((err) => console.error('[window] 页面加载失败：', err.message))
  mainWindow.webContents.on('did-fail-load', (_e, code, desc) =>
    console.error('[window] did-fail-load', code, desc)
  )
  mainWindow.webContents.on('render-process-gone', (_e, d) =>
    console.error('[window] render-process-gone', JSON.stringify(d))
  )

  if (process.env.VITE_DEV_SERVER_URL || winCfg.devtools) {
    mainWindow.webContents.openDevTools({ mode: 'detach' })
  }

  return mainWindow
}
