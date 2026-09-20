/**
 * 钱箱（移植自参考项目 electron/hardware/cashDrawer.js）
 *
 * 两种接法，本类都覆盖：
 *   1. 钱箱接在打印机上（最常见）：开箱指令发给打印机串口
 *   2. 钱箱独立接串口：直接发给钱箱自己的串口
 * 由 config.cashDrawer.followPrinter 决定（默认 true）。
 */

import * as serial from './serial'
import type { CashDrawerSettings, PrinterSettings } from '../config'

// ESC p m t1 t2 —— 标准开钱箱指令
const OPEN_CMD = [0x1b, 0x70, 0x00, 0x19, 0xfa]

export class CashDrawer {
  ready = false
  message: string
  private followPrinter: boolean

  constructor(
    private config: Partial<CashDrawerSettings> = {},
    private printerConfig: Partial<PrinterSettings> = {}
  ) {
    this.followPrinter = config.followPrinter !== false
    this.message = this.enabled() ? '未连接' : '未启用'
  }

  enabled(): boolean {
    return !!(
      this.config.enabled ||
      (this.followPrinter && this.printerConfig.enabled && this.printerConfig.openDrawerOnPrint)
    )
  }

  /** 实际使用的串口 */
  target(): { path?: string; baudRate?: number } {
    if (this.followPrinter) {
      return { path: this.printerConfig.path, baudRate: this.printerConfig.baudRate }
    }
    return { path: this.config.path, baudRate: this.config.baudRate }
  }

  async probe(): Promise<ReturnType<CashDrawer['status']>> {
    if (!this.enabled()) {
      this.ready = false
      this.message = '未启用'
      return this.status()
    }
    if (!serial.isAvailable()) {
      this.ready = false
      this.message = serial.unavailableReason() || 'serialport 不可用'
      return this.status()
    }
    const { path, baudRate } = this.target()
    if (!path) {
      this.ready = false
      this.message = '未配置串口'
      return this.status()
    }
    try {
      await serial.open(path, baudRate)
      this.ready = true
      this.message = `已连接 ${path}`
    } catch (e) {
      this.ready = false
      this.message = (e as Error).message
    }
    return this.status()
  }

  status(): { ready: boolean; message: string; path: string } {
    const { path } = this.target()
    return { ready: this.ready, message: this.message, path: path || '' }
  }

  async openDrawer(): Promise<{ ok: boolean; reason?: string; hint?: string; bytes?: number }> {
    if (!this.enabled()) {
      return {
        ok: false,
        reason: '钱箱未启用',
        hint: 'config.json 里 cashDrawer.enabled=true，或打开 printer.openDrawerOnPrint'
      }
    }
    if (!serial.isAvailable()) {
      return { ok: false, reason: serial.unavailableReason() || 'serialport 不可用' }
    }
    const { path, baudRate } = this.target()
    if (!path) return { ok: false, reason: '未配置钱箱串口' }
    try {
      const r = await serial.write(path, baudRate || 9600, Buffer.from(OPEN_CMD))
      this.ready = true
      this.message = `已连接 ${path}`
      return { ok: true, bytes: r.bytes }
    } catch (e) {
      this.ready = false
      this.message = (e as Error).message
      return { ok: false, reason: this.message }
    }
  }
}

export default CashDrawer
