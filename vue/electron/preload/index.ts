/**
 * 预加载脚本：硬件桥（架构移植自参考项目 electron/preload.js）
 *
 * 渲染进程（Vue）里通过 window.electronAPI 访问本机能力。
 * 页面判断是否运行在桌面端：`if (window.electronAPI)` —— 浏览器里这个对象不存在，逻辑自然降级。
 *
 * 安全约束：contextIsolation=true + nodeIntegration=false，只暴露下面这些白名单方法，
 * 不把 ipcRenderer / require 直接交给页面。
 */

import { contextBridge, ipcRenderer } from 'electron'

const invoke = (channel: string, ...args: unknown[]) => ipcRenderer.invoke(channel, ...args)

contextBridge.exposeInMainWorld('electronAPI', {
  /** 标识：供页面判断运行环境 */
  isElectron: true,
  platform: process.platform,

  // ---- 应用 ----
  getVersion: () => invoke('app:version'),
  getConfig: () => invoke('app:config'),
  getConfigPath: () => invoke('app:config:path'),
  /** 局部保存配置（patch 是嵌套对象，只改传进来的键），返回保存后的完整配置 */
  setConfig: (patch: unknown) => invoke('app:config:set', patch),
  openConfigFile: () => invoke('app:config:open'),
  relaunch: () => invoke('app:relaunch'),
  openExternal: (url: string) => invoke('app:open-external', url),

  // ---- 窗口 ----
  minimize: () => invoke('window:minimize'),
  toggleMaximize: () => invoke('window:toggle-maximize'),
  toggleFullscreen: () => invoke('window:toggle-fullscreen'),
  closeWindow: () => invoke('window:close'),

  // ---- 硬件 ----
  getHardwareStatus: () => invoke('hardware:status'),
  probeHardware: () => invoke('hardware:probe'),
  listSerialPorts: () => invoke('hardware:list-ports'),
  /** 系统打印机列表（走驱动打印那条路用） */
  listPrinters: () => invoke('hardware:list-printers'),
  printReceipt: (data: unknown) => invoke('hardware:print-receipt', data),
  printTest: () => invoke('hardware:print-test'),
  openCashDrawer: () => invoke('hardware:open-drawer'),
  displayAmount: (text: string) => invoke('hardware:display-amount', text),
  readScale: () => invoke('hardware:scale-read'),

  // ---- 服务托管（后端/数据库环境检测、安装、启停）----
  launcherStatus: () => invoke('launcher:status'),
  launcherCheckBackend: () => invoke('launcher:backend'),
  launcherInstall: (name: string) => invoke('launcher:install', name),
  launcherStart: (name: string) => invoke('launcher:start', name),
  launcherStop: (name: string) => invoke('launcher:stop', name),
  launcherStartAll: () => invoke('launcher:start-all'),
  launcherStopAll: () => invoke('launcher:stop-all'),
  launcherLog: (name: string) => invoke('launcher:log', name),
  launcherOpenDir: () => invoke('launcher:open-dir'),
  launcherPickJar: () => invoke('launcher:pick-jar'),
  launcherPickJdkDir: () => invoke('launcher:pick-jdk-dir'),
  launcherTestJdk: (dir?: string) => invoke('launcher:test-jdk', dir),
  launcherPickSql: () => invoke('launcher:pick-sql'),
  launcherInitDb: (file?: string) => invoke('launcher:init-db', file),

  // ---- 主进程 → 渲染进程 事件 ----
  /**
   * 扫码枪回调。返回取消订阅函数。
   */
  onBarcode: (callback: (code: string) => void) => {
    const listener = (_event: unknown, code: string) => callback(code)
    ipcRenderer.on('barcode-scan', listener)
    return () => ipcRenderer.removeListener('barcode-scan', listener)
  },
  /**
   * 网络状态回调（由主进程定时探测推送）
   */
  onNetworkStatus: (callback: (online: boolean) => void) => {
    const listener = (_event: unknown, online: boolean) => callback(online)
    ipcRenderer.on('network-status', listener)
    return () => ipcRenderer.removeListener('network-status', listener)
  },
  /** 服务托管安装进度（name/phase/downloaded/total） */
  onLauncherProgress: (callback: (payload: unknown) => void) => {
    const listener = (_event: unknown, payload: unknown) => callback(payload)
    ipcRenderer.on('launcher-progress', listener)
    return () => ipcRenderer.removeListener('launcher-progress', listener)
  }
})
