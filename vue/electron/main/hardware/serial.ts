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
 * @param timeoutMs 打开超时：端口被占用/虚拟端口/设备未就绪时 open 回调可能永不触发，
 *                  超时后 reject；若之后真的打开了，仍放回连接池供后续复用。
 */
export function open(portPath: string, baudRate = 9600, timeoutMs = 5000): Promise<any> {
  const sp = getSerialport()
  if (!sp) return Promise.reject(new Error(unavailableReason() || 'serialport 不可用'))
  if (!portPath) return Promise.reject(new Error('未配置串口'))

  const key = `${portPath}@${baudRate}`
  const exist = pool.get(key)
  if (exist && exist.isOpen) return Promise.resolve(exist)

  return new Promise((resolve, reject) => {
    let settled = false
    const timer = setTimeout(() => {
      if (settled) return
      settled = true
      reject(new Error(`打开串口 ${portPath} 超时（${Math.round(timeoutMs / 1000)}s），端口可能被占用或设备未就绪`))
    }, timeoutMs)
    const port = new sp.SerialPort({ path: portPath, baudRate, autoOpen: false })
    port.open((err: Error | null | undefined) => {
      if (err) {
        if (!settled) {
          settled = true
          clearTimeout(timer)
          reject(new Error(`打开串口 ${portPath} 失败：${err.message}`))
        }
        return
      }
      pool.set(key, port)
      // 串口被拔出时清掉缓存，下次调用会重新打开
      port.on('close', () => pool.delete(key))
      port.on('error', (e: Error) => console.error(`[serial] ${portPath} 异常：`, e.message))
      if (!settled) {
        settled = true
        clearTimeout(timer)
        resolve(port)
      }
      // 超时后才打开成功的：已入池，调用方可直接复用
    })
  })
}

/** 向串口写数据，写完 + drain 后 resolve（整体带超时，防止 drain 回调不触发挂死） */
export function write(
  portPath: string,
  baudRate = 9600,
  buffer: Buffer,
  timeoutMs = 8000
): Promise<{ path: string; bytes: number }> {
  const task = open(portPath, baudRate).then(
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
  return Promise.race([
    task,
    new Promise<never>((_, reject) =>
      setTimeout(() => reject(new Error(`串口 ${portPath} 写入超时（${Math.round(timeoutMs / 1000)}s），设备可能未就绪`)), timeoutMs)
    )
  ])
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
