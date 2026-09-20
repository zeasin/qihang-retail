/**
 * window.electronAPI 类型定义 —— 与 src/preload/index.ts 暴露的 API 一一对应。
 * 数据结构镜像主进程 src/main/hardware/receiptTypes.ts 与 config.ts。
 */

// ---------- 硬件/配置数据结构 ----------

interface ReceiptItemData {
  goodsName?: string
  skuName?: string
  barcode?: string
  unitPrice?: number
  quantity?: number
  subtotal?: number
  goldWeight?: number
  silverWeight?: number
  laborCost?: number
}

interface ReceiptData {
  title?: string
  shopName?: string
  orderNo?: string
  time?: string
  memberName?: string
  salespersonName?: string
  items?: ReceiptItemData[]
  totalAmount?: number
  total?: number
  discountAmount?: number
  finalAmount?: number
  payMethod?: string
  receivedAmount?: number
}

/** IPC 统一返回结构：handler 不抛异常，全部包成 {ok, data/reason} */
interface IpcResult<T = any> {
  ok: boolean
  data?: T
  reason?: string
  hint?: string
}

interface PrinterSettings {
  enabled: boolean
  mode: 'escpos' | 'driver'
  path: string
  baudRate: number
  paperWidth: number
  encoding: string
  copies: number
  driverName: string
  qrBaseUrl: string
  openDrawerOnPrint: boolean
  headerText: string
  footerText: string
}

interface CashDrawerSettings {
  enabled: boolean
  followPrinter: boolean
  path: string
  baudRate: number
}

interface CustomerDisplaySettings {
  enabled: boolean
  path: string
  baudRate: number
}

interface ScaleSettings {
  enabled: boolean
  path: string
  baudRate: number
  delimiter: string
}

interface WindowSettings {
  width: number
  height: number
  fullscreen: boolean
  frame: boolean
  devtools: boolean
}

interface AppConfig {
  serverPort: number
  window: WindowSettings
  printer: PrinterSettings
  cashDrawer: CashDrawerSettings
  customerDisplay: CustomerDisplaySettings
  scale: ScaleSettings
  backendUrl: string
  apiPrefix: string
}

interface DeviceStatus {
  ready: boolean
  message: string
  path?: string
  mode?: string
}

interface HardwareStatus {
  serialportAvailable: boolean
  serialportMessage: string | null
  printerMode?: string
  printer: DeviceStatus
  cashDrawer: DeviceStatus
  customerDisplay: DeviceStatus
  scale: DeviceStatus
}

interface SerialPortInfo {
  path: string
  manufacturer: string
  friendlyName: string
}

interface PrinterListItem {
  name: string
  displayName: string
  isDefault: boolean
  status: number | null
}

interface AppVersions {
  app: string
  electron: string
  chrome: string
  node: string
  name: string
  platform: string
}

interface WeightData {
  weight: number
  unit: string
  stable: boolean
}

/** hardware:scale-read 主进程原样透传 {ok, weight/reason}，不走 data 包装 */
interface ScaleReadResult {
  ok: boolean
  weight?: WeightData
  reason?: string
}

// ---------- API 表面 ----------

interface ElectronAPI {
  isElectron: true
  platform: string

  // 应用
  getVersion(): Promise<IpcResult<AppVersions>>
  getConfig(): Promise<IpcResult<AppConfig>>
  getConfigPath(): Promise<IpcResult<string>>
  setConfig(patch: DeepPartial<AppConfig>): Promise<IpcResult<AppConfig>>
  openConfigFile(): Promise<IpcResult<{ path: string }>>
  relaunch(): Promise<IpcResult>
  openExternal(url: string): Promise<IpcResult>

  // 窗口
  minimize(): Promise<IpcResult>
  toggleMaximize(): Promise<IpcResult>
  toggleFullscreen(): Promise<IpcResult>
  closeWindow(): Promise<IpcResult>

  // 硬件
  getHardwareStatus(): Promise<IpcResult<HardwareStatus>>
  probeHardware(): Promise<IpcResult<HardwareStatus>>
  listSerialPorts(): Promise<IpcResult<SerialPortInfo[]>>
  listPrinters(): Promise<IpcResult<PrinterListItem[]>>
  printReceipt(data: ReceiptData): Promise<IpcResult>
  printTest(): Promise<IpcResult>
  openCashDrawer(): Promise<IpcResult>
  displayAmount(text: string): Promise<IpcResult>
  readScale(): Promise<ScaleReadResult>

  // 主进程 → 渲染进程事件（返回取消订阅函数）
  onBarcode(callback: (code: string) => void): () => void
  onNetworkStatus(callback: (online: boolean) => void): () => void
}

type DeepPartial<T> = { [K in keyof T]?: T[K] extends object ? DeepPartial<T[K]> : T[K] }

interface Window {
  electronAPI?: ElectronAPI
}
