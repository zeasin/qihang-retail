/**
 * 电子秤驱动（RS232 串口）——按《Electron桌面端设计文档》4.4 实现，
 * 复用 hardware/serial.ts 的串口池，避免自己管理连接生命周期。
 *
 * 常见电子秤协议输出格式：
 *   ST,GS,+  0.123 kg
 *   S,  0.123 kg
 *   +0.123 kg
 */

import * as serial from './serial'
import type { ScaleSettings } from '../config'

export interface WeightData {
  weight: number
  unit: string
  stable: boolean
}

/** 解析一行重量数据，识别失败返回 null */
export function parseWeight(line: string): WeightData | null {
  const patterns = [/ST,[A-Z],([+-]?\d+\.?\d*)\s*(kg|g|斤)/i, /([+-]?\d+\.?\d*)\s*(kg|g|斤)/i]

  for (const pattern of patterns) {
    const match = line.match(pattern)
    if (match) {
      return {
        weight: parseFloat(match[1]),
        unit: match[2] || 'kg',
        stable: /ST/i.test(line)
      }
    }
  }
  return null
}

export class ScaleDriver {
  ready = false
  message: string
  private listeners = new Set<(w: WeightData) => void>()
  private lineBuf = ''
  private attached = false

  constructor(private config: Partial<ScaleSettings> = {}) {
    this.message = config.enabled ? '未连接' : '未启用'
  }

  /** 挂上串口并开始按行解析（连接由 serial 池复用） */
  async probe(): Promise<ReturnType<ScaleDriver['status']>> {
    if (!this.config.enabled) {
      this.ready = false
      this.message = '未启用（config.json 中 scale.enabled=false）'
      return this.status()
    }
    if (!serial.isAvailable()) {
      this.ready = false
      this.message = serial.unavailableReason() || 'serialport 不可用'
      return this.status()
    }
    if (!this.config.path) {
      this.ready = false
      this.message = '未配置串口（scale.path）'
      return this.status()
    }
    try {
      const port = await serial.open(this.config.path, this.config.baudRate)
      if (!this.attached) {
        const delimiter = this.config.delimiter || '\r\n'
        port.on('data', (chunk: Buffer) => {
          this.lineBuf += chunk.toString('ascii')
          let idx: number
          while ((idx = this.lineBuf.indexOf(delimiter)) >= 0) {
            const line = this.lineBuf.slice(0, idx)
            this.lineBuf = this.lineBuf.slice(idx + delimiter.length)
            const w = parseWeight(line)
            if (w) for (const cb of this.listeners) cb(w)
          }
        })
        this.attached = true
      }
      this.ready = true
      this.message = `已连接 ${this.config.path}`
    } catch (e) {
      this.ready = false
      this.message = (e as Error).message
    }
    return this.status()
  }

  status(): { ready: boolean; message: string; path: string } {
    return { ready: this.ready, message: this.message, path: this.config.path || '' }
  }

  /** 订阅连续重量推送，返回取消订阅函数 */
  onData(callback: (w: WeightData) => void): () => void {
    this.listeners.add(callback)
    return () => this.listeners.delete(callback)
  }

  /** 读一次稳定读数（3 秒超时） */
  read(timeoutMs = 3000): Promise<{ ok: boolean; weight?: WeightData; reason?: string }> {
    return new Promise((resolve) => {
      if (!this.config.enabled) return resolve({ ok: false, reason: '电子秤未启用' })
      let done = false
      const unsub = this.onData((weight) => {
        if (done) return
        done = true
        unsub()
        clearTimeout(timer)
        resolve({ ok: true, weight })
      })
      const timer = setTimeout(() => {
        if (done) return
        done = true
        unsub()
        resolve({ ok: false, reason: '读取超时' })
      }, timeoutMs)

      // 触发一次连接，确保数据流已挂上
      this.probe().catch(() => {})
    })
  }
}

export default ScaleDriver
