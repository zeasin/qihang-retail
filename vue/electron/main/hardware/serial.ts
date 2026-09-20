/**
 * 串口连接管理（移植自参考项目 electron/hardware/serial.js）
 *
 * serialport 是原生模块（node-gyp 编译 / 预编译二进制），在 Electron 下需要
 * 与 Electron 的 Node ABI 匹配，否则 require 会直接抛错。
 * 这里做成「可选依赖 + 动态加载 + 失败降级」，保证没装/装不上时桌面端仍能正常启动。
 */

/* eslint-disable @typescript-eslint/no-explicit-any */

let serialport: any = null
let loadError: string | null = null
let loaded = false

/** 懒加载 serialport，失败不抛异常 */
function getSerialport(): any {
  if (loaded) return serialport
  loaded = true
  try {
    // 运行时动态加载，打包时不会被静态引入（见 vite.config rollup external）
    serialport = eval('require')('serialport')
  } catch (e) {
    loadError = (e as Error).message
    console.warn('[serial] serialport 不可用，硬件功能降级：', loadError)
  }
  return serialport
}

export function isAvailable(): boolean {
  return !!getSerialport()
}

export function unavailableReason(): string | null {
  if (isAvailable()) return null
  return `serialport 模块不可用：${loadError || '未安装'}`
}

export interface PortInfo {
  path: string
  manufacturer: string
  friendlyName: string
}

/** 列出本机可用串口 */
export async function listPorts(): Promise<PortInfo[]> {
  const sp = getSerialport()
  if (!sp) return []
  try {
    const ports = await sp.SerialPort.list()
    return ports.map((p: any) => ({
      path: p.path,
      manufacturer: p.manufacturer || '',
      friendlyName: p.friendlyName || p.pnpId || ''
    }))
  } catch (e) {
    console.error('[serial] 枚举串口失败：', (e as Error).message)
    return []
  }
}

/** 已打开的连接：key = `${path}@${baudRate}` → SerialPort */
const pool = new Map<string, any>()

/**
 * 打开（或复用）一个串口连接
 * @param portPath 如 COM1
 * @param baudRate 如 9600
 */
export function open(portPath: string, baudRate = 9600): Promise<any> {
  const sp = getSerialport()
  if (!sp) return Promise.reject(new Error(unavailableReason() || 'serialport 不可用'))
  if (!portPath) return Promise.reject(new Error('未配置串口'))

  const key = `${portPath}@${baudRate}`
  const exist = pool.get(key)
  if (exist && exist.isOpen) return Promise.resolve(exist)

  return new Promise((resolve, reject) => {
    const port = new sp.SerialPort({ path: portPath, baudRate, autoOpen: false })
    port.open((err: Error | null | undefined) => {
      if (err) return reject(new Error(`打开串口 ${portPath} 失败：${err.message}`))
      pool.set(key, port)
      // 串口被拔出时清掉缓存，下次调用会重新打开
      port.on('close', () => pool.delete(key))
      port.on('error', (e: Error) => console.error(`[serial] ${portPath} 异常：`, e.message))
      resolve(port)
    })
  })
}

/** 向串口写数据，写完 + drain 后 resolve */
export function write(portPath: string, baudRate = 9600, buffer: Buffer): Promise<{ path: string; bytes: number }> {
  return open(portPath, baudRate).then(
    (port) =>
      new Promise<{ path: string; bytes: number }>((resolve, reject) => {
        port.write(buffer, (err: Error | null | undefined) => {
          if (err) return reject(new Error(`写入 ${portPath} 失败：${err.message}`))
          port.drain((drainErr: Error | null | undefined) => {
            if (drainErr) return reject(new Error(`刷新 ${portPath} 失败：${drainErr.message}`))
            resolve({ path: portPath, bytes: buffer.length })
          })
        })
      })
  )
}

/** 关闭全部连接（退出应用时调用） */
export function closeAll(): void {
  for (const [key, port] of pool.entries()) {
    try {
      if (port.isOpen) port.close()
    } catch {
      /* 忽略 */
    }
    pool.delete(key)
  }
}
