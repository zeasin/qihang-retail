/**
 * 渲染进程硬件能力桥接 —— 封装 window.electronAPI，浏览器环境下自动降级。
 * 用法与《Electron桌面端设计文档》5.3 一致，所有方法返回 Promise，
 * 非桌面端时返回 { ok: false, reason: '...' }，调用方无需判断运行环境。
 *
 * IpcResult / ReceiptData / HardwareStatus 等类型来自 types/electron.d.ts（全局声明）。
 */

/** 是否运行在 Electron 桌面端（浏览器里 electronAPI 不存在） */
export function isElectron(): boolean {
  return typeof window !== 'undefined' && !!window.electronAPI?.isElectron
}

const notDesktop = <T,>(): Promise<IpcResult<T>> =>
  Promise.resolve({ ok: false, reason: '当前运行在浏览器环境，桌面硬件能力不可用' })

const api = () => window.electronAPI

/**
 * IPC 走结构化克隆，Vue 的响应式 Proxy 对象会报 "An object could not be cloned"。
 * 所有要过 IPC 的入参先 JSON 深拷贝一份，剥离 Proxy / 不可克隆值。
 */
const plain = <T,>(v: T): T => JSON.parse(JSON.stringify(v)) as T

export const hardwareAPI = {
  // ---- 打印机 ----
  printReceipt: (data: ReceiptData): Promise<IpcResult> =>
    api()?.printReceipt(plain(data)) ?? notDesktop(),
  printTest: (): Promise<IpcResult> => api()?.printTest() ?? notDesktop(),
  listPrinters: (): Promise<IpcResult<PrinterListItem[]>> => api()?.listPrinters() ?? notDesktop(),

  // ---- 钱箱 ----
  openDrawer: (): Promise<IpcResult> => api()?.openCashDrawer() ?? notDesktop(),

  // ---- 客显 ----
  displayAmount: (text: string): Promise<IpcResult> => api()?.displayAmount(text) ?? notDesktop(),

  // ---- 电子秤 ----
  readScale: (): Promise<ScaleReadResult> => api()?.readScale() ?? Promise.resolve({ ok: false, reason: '当前运行在浏览器环境，桌面硬件能力不可用' }),

  // ---- 硬件状态 / 自检 ----
  getStatus: (): Promise<IpcResult<HardwareStatus>> => api()?.getHardwareStatus() ?? notDesktop(),
  probe: (): Promise<IpcResult<HardwareStatus>> => api()?.probeHardware() ?? notDesktop(),
  listSerialPorts: (): Promise<IpcResult<SerialPortInfo[]>> => api()?.listSerialPorts() ?? notDesktop(),

  // ---- 运行时配置（每台收银机一套）----
  getConfig: (): Promise<IpcResult<AppConfig>> => api()?.getConfig() ?? notDesktop(),
  getConfigPath: (): Promise<IpcResult<string>> => api()?.getConfigPath() ?? notDesktop(),
  setConfig: (patch: Record<string, any>): Promise<IpcResult<AppConfig>> =>
    api()?.setConfig(plain(patch)) ?? notDesktop(),
  openConfigFile: (): Promise<IpcResult> => api()?.openConfigFile() ?? notDesktop(),
  relaunch: (): Promise<IpcResult> => api()?.relaunch() ?? notDesktop(),

  // ---- 应用信息 / 窗口 ----
  getVersion: (): Promise<IpcResult<AppVersions>> => api()?.getVersion() ?? notDesktop(),
  minimize: () => api()?.minimize(),
  toggleMaximize: () => api()?.toggleMaximize(),
  toggleFullscreen: () => api()?.toggleFullscreen(),
  closeWindow: () => api()?.closeWindow(),

  // ---- 主进程推送事件 ----
  /** 订阅扫码枪条码；非桌面端返回空的取消函数 */
  onBarcode(callback: (code: string) => void): () => void {
    return api()?.onBarcode(callback) ?? (() => {})
  },
  /** 订阅主进程网络状态推送；非桌面端用浏览器 online/offline 事件兜底 */
  onNetworkStatus(callback: (online: boolean) => void): () => void {
    const desktop = api()?.onNetworkStatus(callback)
    if (desktop) return desktop
    const on = () => callback(true)
    const off = () => callback(false)
    window.addEventListener('online', on)
    window.addEventListener('offline', off)
    return () => {
      window.removeEventListener('online', on)
      window.removeEventListener('offline', off)
    }
  }
}
