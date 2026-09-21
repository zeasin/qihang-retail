/**
 * 端口 / HTTP 探测（移植自 qihang-launcher src-tauri/src/port.rs 的思路）
 *
 * 约定与参考项目一致：
 *   - TCP 能连上 127.0.0.1:<port> = 服务在跑（不管是谁拉起来的）
 *   - HTTP 有响应（任意状态码）= 服务活着；超时/拒绝 = 不通
 */

import net from 'net'
import http from 'http'
import https from 'https'

/** 端口是否已被占用（能连上 = 占用/在跑） */
export function isPortOpen(port: number, host = '127.0.0.1', timeoutMs = 300): Promise<boolean> {
  return new Promise((resolve) => {
    const socket = net.connect({ port, host })
    const done = (result: boolean) => {
      socket.destroy()
      resolve(result)
    }
    socket.setTimeout(timeoutMs)
    socket.once('connect', () => done(true))
    socket.once('timeout', () => done(false))
    socket.once('error', () => done(false))
  })
}

/** 端口是否空闲（bind 成功 = 空闲），用于启动前冲突检测 */
export function isPortFree(port: number): Promise<boolean> {
  return new Promise((resolve) => {
    const server = net.createServer()
    server.once('error', () => resolve(false))
    server.once('listening', () => {
      server.close(() => resolve(true))
    })
    server.listen(port, '127.0.0.1')
  })
}

/** 轮询等待端口就绪：tries 次 × interval 毫秒 */
export async function waitPort(port: number, tries = 30, intervalMs = 1000): Promise<boolean> {
  for (let i = 0; i < tries; i++) {
    if (await isPortOpen(port)) return true
    await new Promise((r) => setTimeout(r, intervalMs))
  }
  return false
}

export interface UrlProbe {
  reachable: boolean
  status?: number
  error?: string
}

/** 探测 URL：任何 HTTP 响应（含 4xx/5xx）都算可达，与参考项目判定一致 */
export function probeUrl(url: string, timeoutMs = 2000): Promise<UrlProbe> {
  return new Promise((resolve) => {
    let settled = false
    const finish = (r: UrlProbe) => {
      if (!settled) {
        settled = true
        resolve(r)
      }
    }
    try {
      const mod = url.startsWith('https') ? https : http
      const req = mod.get(url, (res) => {
        res.resume() // 丢弃 body，只关心有没有响应
        finish({ reachable: true, status: res.statusCode })
      })
      req.setTimeout(timeoutMs, () => {
        req.destroy()
        finish({ reachable: false, error: 'timeout' })
      })
      req.on('error', (e) => finish({ reachable: false, error: e.message }))
    } catch (e) {
      finish({ reachable: false, error: (e as Error).message })
    }
  })
}

/** 从 http://host:6666 之类的地址取端口，取不到用 fallback */
export function portFromUrl(url: string, fallback: number): number {
  try {
    const u = new URL(url)
    if (u.port) return Number(u.port)
    return u.protocol === 'https:' ? 443 : 80
  } catch {
    return fallback
  }
}
