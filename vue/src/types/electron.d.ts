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

// ---------- 服务托管（launcher）----------

interface LauncherUrls {
  jdk: string
  jdkSha256: string
  mysql: string
  mysqlSha256: string
  redis: string
  redisSha256: string
  backendJar: string
  initSql: string
}

interface LauncherSettings {
  runtimeDir: string
  /** 本机已有 JDK 目录（其下有 bin/java.exe），配置后优先于 runtime/jdk */
  jdkDir: string
  urls: LauncherUrls
  backend: { jarPath: string; javaOptions: string; profile: string; applyLocalConfig: boolean }
  mysql: { port: number; username: string; rootPassword: string; database: string }
  redis: { port: number }
}

type LauncherServiceName = 'jdk' | 'mysql' | 'redis' | 'backend'

interface LauncherServiceStatus {
  name: LauncherServiceName
  installed: boolean
  installPath: string
  running: boolean
  ownedExternally: boolean
  pid?: number
  port?: number
  message: string
}

interface LauncherStatus {
  runtimeDir: string
  backendUrl: string
  services: LauncherServiceStatus[]
}

/** install/start/stop 返回：{ok, message/reason}（safe() 检测到 ok 字段会原样透传） */
interface LauncherCallResult {
  ok: boolean
  message?: string
  reason?: string
}

interface LauncherStartAllResult {
  ok: boolean
  results: Array<{ name: LauncherServiceName; result: LauncherCallResult }>
}

interface LauncherProgressPayload {
  name: LauncherServiceName
  phase: 'download' | 'extract'
  downloaded: number
  total: number
}

interface AppConfig {
  serverPort: number
  window: WindowSettings
  printer: PrinterSettings
  cashDrawer: CashDrawerSettings
  customerDisplay: CustomerDisplaySettings
  scale: ScaleSettings
  launcher: LauncherSettings
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

  // 服务托管
  launcherStatus(): Promise<IpcResult<LauncherStatus>>
  /** 启动阶段后端探活：handler 返回 {reachable,url,...}，无 ok 键 → safe() 包成 {ok:true, data} */
  launcherCheckBackend(): Promise<IpcResult<{ reachable: boolean; url: string; status?: number; error?: string }>>
  launcherInstall(name: LauncherServiceName): Promise<LauncherCallResult>
  launcherStart(name: LauncherServiceName): Promise<LauncherCallResult>
  launcherStop(name: LauncherServiceName): Promise<LauncherCallResult>
  launcherStartAll(): Promise<LauncherStartAllResult>
  launcherStopAll(): Promise<IpcResult>
  launcherLog(name: LauncherServiceName): Promise<IpcResult<{ text: string }>>
  launcherOpenDir(): Promise<IpcResult<{ path: string }>>
  launcherPickJar(): Promise<IpcResult<string>>
  /** 选择本地 JDK 目录，返回绝对路径（data） */
  launcherPickJdkDir(): Promise<IpcResult<string>>
  /** 测试 JDK（java -version）；dir 可传尚未保存的目录，留空按已保存配置解析。透传 {ok, message/reason, output?} */
  launcherTestJdk(dir?: string): Promise<LauncherCallResult & { output?: string }>
  /** 选择本机 .sql 文件，返回绝对路径（data） */
  launcherPickSql(): Promise<IpcResult<string>>
  /** 导入 SQL 初始化数据库；file 留空则用配置的 initSql 下载地址 */
  launcherInitDb(file?: string): Promise<LauncherCallResult>

  // 主进程 → 渲染进程事件（返回取消订阅函数）
  onBarcode(callback: (code: string) => void): () => void
  onNetworkStatus(callback: (online: boolean) => void): () => void
  onLauncherProgress(callback: (payload: LauncherProgressPayload) => void): () => void
}

type DeepPartial<T> = { [K in keyof T]?: T[K] extends object ? DeepPartial<T[K]> : T[K] }

interface Window {
  electronAPI?: ElectronAPI
}
