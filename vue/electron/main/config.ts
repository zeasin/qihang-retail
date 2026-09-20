/**
 * 桌面端运行时配置（移植自参考项目 electron/config.js）
 *
 * 存放位置：<userData>/config.json，固定目录名 qihang-retail-pos（见 index.ts 的 app.setPath）
 * Windows 路径：C:\Users\<用户>\AppData\Roaming\qihang-retail-pos\config.json
 *
 * 职责划分：
 *   本文件（config.ts + config.json） —— 「每台收银机各不相同」的配置：窗口、串口、纸宽
 *   deploy.config.ts                 —— 「整个部署环境一套」的配置：后端地址、API 前缀
 *
 * 注意：config.json 是机器相关的，不进 git；程序首次启动会自动生成默认值。
 */

import fs from 'fs'
import path from 'path'
import { app } from 'electron'
import { BACKEND_URL, API_PREFIX } from './deploy.config'

export type PrinterMode = 'escpos' | 'driver'

export interface PrinterSettings {
  enabled: boolean
  /**
   * 打印方式：
   *   'escpos' 自己拼 ESC/POS 字节写串口 —— 快、能控切纸，但只认「支持 ESC/POS + 有 COM 口」的机器
   *   'driver' 渲染 HTML 交给 Windows 打印驱动 —— 任何装了驱动的打印机都能用（含 USB 打印类、网口、标签机）
   */
  mode: PrinterMode
  path: string
  baudRate: number
  /** 纸宽 mm：58 或 80。escpos 模式决定排版列数，driver 模式决定页面宽度 */
  paperWidth: number
  encoding: string
  copies: number
  /** mode='driver' 时指定打印机名（要和系统里显示的完全一致）；留空 = 用系统默认打印机 */
  driverName: string
  /** 二维码前缀，最终内容 = qrBaseUrl + '/' + 订单号；留空 = 不打印二维码 */
  qrBaseUrl: string
  /** 打小票时顺带弹钱箱（钱箱接在打印机上的常见接法，仅 escpos 模式有效） */
  openDrawerOnPrint: boolean
  headerText: string
  footerText: string
}

export interface CashDrawerSettings {
  enabled: boolean
  /** 钱箱挂在打印机上时保持 true，独立串口接钱箱时置 false 并填 path */
  followPrinter: boolean
  path: string
  baudRate: number
}

export interface CustomerDisplaySettings {
  enabled: boolean
  path: string
  baudRate: number
}

export interface ScaleSettings {
  enabled: boolean
  path: string
  baudRate: number
  /** 行分隔符，常见 \r\n，部分秤为 \n */
  delimiter: string
}

export interface WindowSettings {
  width: number
  height: number
  fullscreen: boolean
  frame: boolean
  devtools: boolean
}

export interface AppConfig {
  /** 本地服务端口，0 = 随机分配空闲端口（推荐，避免和本机其它服务冲突） */
  serverPort: number
  window: WindowSettings
  printer: PrinterSettings
  cashDrawer: CashDrawerSettings
  customerDisplay: CustomerDisplaySettings
  scale: ScaleSettings
  /** 以下两项由 deploy.config.ts 强制覆盖，不落盘 */
  backendUrl: string
  apiPrefix: string
}

type PersistableConfig = Omit<AppConfig, 'backendUrl' | 'apiPrefix'>

export const DEFAULT_CONFIG: Omit<PersistableConfig, never> = {
  serverPort: 0,
  window: {
    width: 1400,
    height: 900,
    fullscreen: false,
    frame: true,
    devtools: false
  },
  // ---- 硬件（全部默认关闭，配置串口后置 true 才启用）----
  printer: {
    enabled: false,
    mode: 'escpos',
    path: 'COM1',
    baudRate: 9600,
    paperWidth: 80,
    encoding: 'gbk',
    copies: 1,
    driverName: '',
    qrBaseUrl: '',
    openDrawerOnPrint: false,
    headerText: '启航零售',
    footerText: '谢谢惠顾，欢迎再次光临'
  },
  cashDrawer: {
    enabled: false,
    followPrinter: true,
    path: 'COM2',
    baudRate: 9600
  },
  customerDisplay: {
    enabled: false,
    path: 'COM3',
    baudRate: 9600
  },
  scale: {
    enabled: false,
    path: 'COM4',
    baudRate: 9600,
    delimiter: '\r\n'
  }
}

/** 深合并：以 defaults 为底，用 override 覆盖（只覆盖已存在的键，保留结构） */
function merge<T>(defaults: T, override: unknown): T {
  if (override === null || override === undefined) return defaults
  if (typeof defaults !== 'object' || Array.isArray(defaults)) return override as T
  const out: Record<string, unknown> = { ...(defaults as unknown as Record<string, unknown>) }
  const src = override as Record<string, unknown>
  for (const key of Object.keys(src)) {
    if (key in out) out[key] = merge(out[key], src[key])
    else out[key] = src[key]
  }
  return out as T
}

export function getConfigPath(): string {
  return path.join(app.getPath('userData'), 'config.json')
}

export function loadConfig(): AppConfig {
  const file = getConfigPath()
  let userConfig: Record<string, unknown> = {}
  try {
    if (fs.existsSync(file)) {
      userConfig = JSON.parse(fs.readFileSync(file, 'utf8'))
    }
  } catch (e) {
    console.error('[config] 解析失败，使用默认配置：', (e as Error).message)
  }
  const config = merge<AppConfig>({ ...DEFAULT_CONFIG, backendUrl: '', apiPrefix: '' } as AppConfig, userConfig)

  // 部署配置一律以代码为准，无条件覆盖 config.json 里的同名值。
  // 老版本把 backendUrl/apiPrefix 写进过 config.json，客户端升级后那些残值必须被盖掉，
  // 否则会出现「代码里改了地址、客户端还连老地址」这种极难排查的问题。
  config.backendUrl = BACKEND_URL
  config.apiPrefix = API_PREFIX

  // 首次启动、老版本残留字段、或新版本加了配置项时，都把当前完整配置落盘。
  // 原因：merge() 只在内存里补齐默认值，文件不会自动更新 ——
  // 那样新加的配置项（比如 printer.mode）在 config.json 里根本看不到，现场就没法改。
  saveConfigIfChanged(config)

  return config
}

/** 内容有变化才写盘，避免每次启动都无谓地改文件时间戳 */
export function saveConfigIfChanged(config: AppConfig): boolean {
  const file = getConfigPath()
  try {
    const { backendUrl: _b, apiPrefix: _p, ...persist } = config
    const next = JSON.stringify(persist, null, 2)
    if (fs.existsSync(file) && fs.readFileSync(file, 'utf8') === next) return true
    return saveConfig(config)
  } catch {
    return saveConfig(config)
  }
}

export function saveConfig(config: AppConfig): boolean {
  const file = getConfigPath()
  try {
    // 部署配置不落盘：它不属于这个文件管辖，写进去只会让下次升级时产生"改代码没用"的错觉
    const { backendUrl: _b, apiPrefix: _p, ...persist } = config
    fs.mkdirSync(path.dirname(file), { recursive: true })
    fs.writeFileSync(file, JSON.stringify(persist, null, 2), 'utf8')
    return true
  } catch (e) {
    console.error('[config] 写入失败：', (e as Error).message)
    return false
  }
}
