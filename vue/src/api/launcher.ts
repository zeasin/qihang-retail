/**
 * 服务托管桥接 —— 检测/安装/启停 JDK、MySQL、Redis、后端服务（仅 Electron 桌面端）。
 * 浏览器环境下所有方法返回降级结果，与 api/hardware.ts 同一套约定。
 */

const notDesktop = <T,>(): Promise<T> =>
  Promise.resolve({ ok: false, reason: '当前运行在浏览器环境，服务托管仅在桌面端可用' } as unknown as T)

const api = () => window.electronAPI

/** 后端探活结果（主进程 launcher.checkBackend 的 data 部分） */
export interface BackendProbe {
  reachable: boolean
  url: string
  status?: number
  error?: string
}

export const launcherAPI = {
  getStatus: (): Promise<IpcResult<LauncherStatus>> => api()?.launcherStatus() ?? notDesktop(),
  /** 后端探活（轻量，只看 backendUrl 是否有 HTTP 响应）；非桌面端 ok:false */
  checkBackend: (): Promise<IpcResult<BackendProbe>> => api()?.launcherCheckBackend() ?? notDesktop(),
  install: (name: LauncherServiceName): Promise<LauncherCallResult> =>
    api()?.launcherInstall(name) ?? notDesktop(),
  start: (name: LauncherServiceName): Promise<LauncherCallResult> =>
    api()?.launcherStart(name) ?? notDesktop(),
  stop: (name: LauncherServiceName): Promise<LauncherCallResult> =>
    api()?.launcherStop(name) ?? notDesktop(),
  startAll: (): Promise<LauncherStartAllResult> =>
    api()?.launcherStartAll() ?? Promise.resolve({ ok: false, results: [] } as unknown as LauncherStartAllResult),
  stopAll: (): Promise<IpcResult> => api()?.launcherStopAll() ?? notDesktop(),
  getLog: (name: LauncherServiceName): Promise<IpcResult<{ text: string }>> =>
    api()?.launcherLog(name) ?? notDesktop(),
  openDir: (): Promise<IpcResult<{ path: string }>> => api()?.launcherOpenDir() ?? notDesktop(),
  pickJar: (): Promise<IpcResult<string>> => api()?.launcherPickJar() ?? notDesktop(),
  /** 选择本地 JDK 目录（结果只用于填表单，仍需点「保存配置」落盘） */
  pickJdkDir: (): Promise<IpcResult<string>> => api()?.launcherPickJdkDir() ?? notDesktop(),
  /** 测试 JDK（java -version）；dir 传尚未保存的表单值，留空按已保存配置解析 */
  testJdk: (dir?: string): Promise<LauncherCallResult & { output?: string }> =>
    api()?.launcherTestJdk(dir) ?? notDesktop(),
  /** 选择本机 SQL 文件（初始化库用） */
  pickSql: (): Promise<IpcResult<string>> => api()?.launcherPickSql() ?? notDesktop(),
  /** 导入 SQL 初始化数据库；file 留空回退 initSql 下载地址 */
  initDb: (file?: string): Promise<LauncherCallResult> =>
    api()?.launcherInitDb(file) ?? notDesktop(),

  /** 订阅安装进度；非桌面端返回空的取消函数 */
  onProgress(callback: (p: LauncherProgressPayload) => void): () => void {
    return api()?.onLauncherProgress(callback) ?? (() => {})
  }
}
