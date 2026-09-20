/**
 * 客显屏（顾客显示屏）（移植自参考项目 electron/hardware/customerDisplay.js）
 *
 * 常见两类：
 *   - 串口 VFD/LED 客显（ESC/POS 兼容指令，如 ESC Q A <len> <text> CR）
 *   - 副屏（第二块显示器，浏览器窗口即可，不需要这里驱动）
 * 本类处理第一类。
 */

import * as serial from './serial'
import type { CustomerDisplaySettings } from '../config'

const ESC = 0x1b
const CR = 0x0d

export class CustomerDisplay {
  ready = false
  message: string

  constructor(private config: Partial<CustomerDisplaySettings> = {}) {
    this.message = config.enabled ? '未连接' : '未启用'
  }

  async probe(): Promise<ReturnType<CustomerDisplay['status']>> {
    if (!this.config.enabled) {
      this.ready = false
      this.message = '未启用（config.json 中 customerDisplay.enabled=false）'
      return this.status()
    }
    if (!serial.isAvailable()) {
      this.ready = false
      this.message = serial.unavailableReason() || 'serialport 不可用'
      return this.status()
    }
    if (!this.config.path) {
      this.ready = false
      this.message = '未配置串口（customerDisplay.path）'
      return this.status()
    }
    try {
      await serial.open(this.config.path, this.config.baudRate)
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

  /**
   * 显示一行文本（通常是金额）
   * @param text 建议 <= 20 个 ASCII 字符
   */
  async show(text: string): Promise<{ ok: boolean; reason?: string; bytes?: number }> {
    if (!this.config.enabled) {
      return { ok: false, reason: '客显未启用' }
    }
    if (!serial.isAvailable()) {
      return { ok: false, reason: serial.unavailableReason() || 'serialport 不可用' }
    }
    const s = String(text == null ? '' : text).slice(0, 20)
    // ESC Q A <len> <text> CR：覆盖显示，光标回行首
    const len = Buffer.byteLength(s, 'ascii')
    const cmd = Buffer.concat([Buffer.from([ESC, 0x51, 0x41, len]), Buffer.from(s, 'ascii'), Buffer.from([CR])])
    try {
      const r = await serial.write(this.config.path!, this.config.baudRate, cmd)
      this.ready = true
      return { ok: true, bytes: r.bytes }
    } catch (e) {
      this.ready = false
      this.message = (e as Error).message
      return { ok: false, reason: this.message }
    }
  }

  /** 清屏 */
  async clear(): Promise<{ ok: boolean; reason?: string }> {
    if (!this.config.enabled) return { ok: false, reason: '客显未启用' }
    try {
      await serial.write(this.config.path!, this.config.baudRate, Buffer.from([0x0c]))
      return { ok: true }
    } catch (e) {
      return { ok: false, reason: (e as Error).message }
    }
  }
}

export default CustomerDisplay
