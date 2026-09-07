import { app, BrowserWindow, Menu, shell, nativeImage } from 'electron'
import { join } from 'path'
import { registerIPCHandlers } from './ipc/handlers'

let mainWindow: BrowserWindow | null = null

// 开发模式判断（vite-plugin-electron 注入 dev server 地址）
const isDev = process.env.VITE_DEV_SERVER_URL

function getWindowIcon(): nativeImage {
  if (process.platform === 'win32') {
    return nativeImage.createFromPath(join(__dirname, '../../build/icon.ico'))
  }
  return nativeImage.createFromPath(join(__dirname, '../../resources/icon.png'))
}

function createWindow(): void {
  const icon = getWindowIcon()
  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 1200,
    minHeight: 700,
    title: '启航零售ERP',
    icon,
    autoHideMenuBar: true,
    backgroundColor: '#f5f7fa',
    show: false,
    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      nodeIntegration: false,
      contextIsolation: true,
      sandbox: false
    }
  })

  // 开发模式加载Vite开发服务器
  if (isDev) {
    mainWindow.loadURL(process.env.VITE_DEV_SERVER_URL!)
    mainWindow.webContents.openDevTools()
  } else {
    mainWindow.loadFile(join(__dirname, '../renderer/index.html'))
  }

  mainWindow.once('ready-to-show', () => mainWindow?.show())

  // 打开外部链接时使用系统浏览器
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url)
    return { action: 'deny' }
  })

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

// 注册IPC处理器
registerIPCHandlers()

// 应用准备就绪
app.whenReady().then(() => {
  // Windows/Linux 去掉默认菜单栏
  if (process.platform !== 'darwin') {
    Menu.setApplicationMenu(null)
  }

  createWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

// 所有窗口关闭时退出应用
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

// 导出mainWindow供其他模块使用
export { mainWindow }
