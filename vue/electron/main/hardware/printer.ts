/**
 * 小票打印机 —— 两条打印路径的统一入口（移植自参考项目 electron/hardware/printer.js）
 *
 * 由 config.json 的 printer.mode 决定走哪条：
 *   "escpos"  直控串口，自己拼 ESC/POS 字节
 *             ✅ 快，能直控切纸/钱箱
 *             ❌ 只认「支持 ESC/POS + 能出 COM 口」的机器
 *
 *   "driver"  渲染 HTML 交给 Windows 打印驱动
 *             ✅ 任何装了驱动的打印机都能用（USB 打印类、网口、标签机都行）
 *             ✅ 排版用 CSS，二维码直接内联 SVG
 *             ❌ 慢一点，精度受驱动影响
 *
 * 两条路吃**同一份 ReceiptData**，所以业务层调用方式完全一样：print(data)。
 */

import * as serial from './serial'
import { buildReceipt } from './escpos'
import DriverPrinter from './driverPrinter'
import type { PrinterSettings } from '../config'
import type { ReceiptData } from './receiptTypes'

export interface PrintResult {
  ok: boolean
  reason?: string
  hint?: string
  bytes?: number
  copies?: number
  mode?: string
}

export class ReceiptPrinter {
  mode: 'escpos' | 'driver'
  driver: DriverPrinter
  ready = false
  message: string

  constructor(private config: Partial<PrinterSettings> = {}) {
    this.mode = config.mode === 'driver' ? 'driver' : 'escpos'
    this.driver = new DriverPrinter(config)
    this.message = config.enabled ? '未连接' : '未启用（config.json 中 printer.enabled=false）'
  }

  async probe(): Promise<ReturnType<ReceiptPrinter['status']>> {
    if (!this.config.enabled) {
      this.ready = false
      return this.status()
    }

    if (this.mode === 'driver') {
      const st = await this.driver.probe()
      this.ready = st.ready
      this.message = st.message
      return this.status()
    }

    // ---- ESC/POS 直控 ----
    if (!serial.isAvailable()) {
      this.ready = false
      this.message = serial.unavailableReason() || 'serialport 不可用'
      return this.status()
    }
    if (!this.config.path) {
      this.ready = false
      this.message = '未配置串口（printer.path）'
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

  status(): { ready: boolean; message: string; path: string; mode: string } {
    const base =
      this.mode === 'driver'
        ? this.driver.status()
        : { ready: this.ready, message: this.message, path: this.config.path || '' }
    return { ...base, mode: this.mode }
  }

  /** 打印小票 */
  async print(data: ReceiptData): Promise<PrintResult> {
    if (!this.config.enabled) {
      return {
        ok: false,
        reason: '打印机未启用',
        hint: '打开 系统设置-硬件自检，在「打印设置」里打开"启用打印"；或改 config.json 的 printer.enabled 后重启应用'
      }
    }

    if (this.mode === 'driver') return this.driver.print(data)

    // ---- ESC/POS 直控 ----
    if (!serial.isAvailable()) {
      return {
        ok: false,
        reason: serial.unavailableReason() || 'serialport 不可用',
        hint: '需要安装 serialport 并针对 Electron 重新编译'
      }
    }

    const buffer = buildReceipt(data, {
      paperWidth: this.config.paperWidth,
      encoding: this.config.encoding,
      headerText: this.config.headerText,
      footerText: this.config.footerText
    })

    const copies = Math.max(Number(this.config.copies) || 1, 1)
    try {
      let bytes = 0
      for (let i = 0; i < copies; i++) {
        const r = await serial.write(this.config.path!, this.config.baudRate, buffer)
        bytes += r.bytes
      }
      this.ready = true
      this.message = `已连接 ${this.config.path}`
      return { ok: true, bytes, copies, mode: this.mode }
    } catch (e) {
      this.ready = false
      this.message = (e as Error).message
      return { ok: false, reason: this.message }
    }
  }

  /** 打印一张自检小票 */
  async test(): Promise<PrintResult> {
    if (this.mode === 'driver' && this.config.enabled) return this.driver.testPrint()
    return this.print({
      title: '打印机自检',
      orderNo: 'TEST-' + Date.now(),
      items: [
        { goodsName: '自检商品 A（中文测试）', unitPrice: 12.5, quantity: 2, subtotal: 25 },
        { goodsName: 'Test Item B', unitPrice: 3.5, quantity: 1, subtotal: 3.5 }
      ],
      totalAmount: 28.5,
      discountAmount: 3.5,
      finalAmount: 25,
      payMethod: 'cash',
      receivedAmount: 50
    })
  }
}

export default ReceiptPrinter
