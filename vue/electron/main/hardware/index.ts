/**
 * 硬件管理器（移植自参考项目 electron/hardware/index.js）
 *
 * 统一入口，主进程只依赖它。所有设备都遵循「未配置 → 优雅降级返回 {ok:false}」，
 * 保证没接硬件的机器照样能跑起来做界面测试。
 */

import ReceiptPrinter from './printer'
import CashDrawer from './cashDrawer'
import CustomerDisplay from './customerDisplay'
import ScaleDriver from './scale'
import * as serial from './serial'
import type { AppConfig } from '../config'
import type { ReceiptData } from './receiptTypes'
import type { WeightData } from './scale'

export interface DeviceStatus {
  ready: boolean
  message: string
  path?: string
  mode?: string
}

export interface HardwareStatus {
  serialportAvailable: boolean
  serialportMessage: string | null
  printerMode?: string
  printer: DeviceStatus
  cashDrawer: DeviceStatus
  customerDisplay: DeviceStatus
  scale: DeviceStatus
}

class HardwareManager {
  config: AppConfig | null = null
  printer: ReceiptPrinter | null = null
  cashDrawer: CashDrawer | null = null
  customerDisplay: CustomerDisplay | null = null
  scale: ScaleDriver | null = null

  init(config: AppConfig): this {
    this.config = config
    this.printer = new ReceiptPrinter(config.printer)
    this.cashDrawer = new CashDrawer(config.cashDrawer, config.printer)
    this.customerDisplay = new CustomerDisplay(config.customerDisplay)
    this.scale = new ScaleDriver(config.scale)
    return this
  }

  /** 单个探测兜底超时：任何设备卡住都不能拖死保存/自检流程 */
  private static async withTimeout<T>(p: Promise<T>, ms = 8000): Promise<T | null> {
    return Promise.race([p.catch(() => null), new Promise<null>((r) => setTimeout(() => r(null), ms))])
  }

  /** 开机自检：并发探测所有设备，任何一台失败都不影响启动 */
  async probeAll(): Promise<HardwareStatus> {
    if (!this.printer) return this.status()
    const t = HardwareManager.withTimeout
    await Promise.all([
      t(this.printer.probe()),
      t(this.cashDrawer!.probe()),
      t(this.customerDisplay!.probe()),
      t(this.scale!.probe())
    ])
    return this.status()
  }

  status(): HardwareStatus {
    if (!this.printer) {
      return {
        serialportAvailable: serial.isAvailable(),
        serialportMessage: serial.unavailableReason(),
        printer: { ready: false, message: '未初始化' },
        cashDrawer: { ready: false, message: '未初始化' },
        customerDisplay: { ready: false, message: '未初始化' },
        scale: { ready: false, message: '未初始化' }
      }
    }
    return {
      serialportAvailable: serial.isAvailable(),
      serialportMessage: serial.unavailableReason(),
      printerMode: this.printer.mode,
      printer: this.printer.status(),
      cashDrawer: this.cashDrawer!.status(),
      customerDisplay: this.customerDisplay!.status(),
      scale: this.scale!.status()
    }
  }

  /** 可用串口列表（供自检页下拉选择） */
  listPorts() {
    return serial.listPorts()
  }

  /** 系统打印机列表（走驱动打印那条路用；返回 {name, isDefault} 数组） */
  listPrinters() {
    if (!this.printer) return []
    return this.printer.driver.listPrinters()
  }

  async printReceipt(data: ReceiptData) {
    if (!this.printer) return { ok: false, reason: '硬件未初始化' }
    const r = await this.printer.print(data)
    // 打印机打了小票，顺手弹钱箱（绝大多数收银台就是这么接的）
    if (r.ok && this.config && this.config.printer.openDrawerOnPrint && this.printer.mode === 'escpos') {
      await this.cashDrawer!.openDrawer().catch(() => {})
    }
    return r
  }

  async testPrint() {
    if (!this.printer) return { ok: false, reason: '硬件未初始化' }
    return this.printer.test()
  }

  async openCashDrawer() {
    if (!this.cashDrawer) return { ok: false, reason: '硬件未初始化' }
    return this.cashDrawer.openDrawer()
  }

  async displayAmount(text: string) {
    if (!this.customerDisplay) return { ok: false, reason: '硬件未初始化' }
    return this.customerDisplay.show(text)
  }

  /** 读取一次电子秤重量 */
  async readScale(): Promise<{ ok: boolean; weight?: WeightData; reason?: string }> {
    if (!this.scale) return { ok: false, reason: '硬件未初始化' }
    return this.scale.read()
  }

  closeAll(): void {
    serial.closeAll()
  }
}

export default new HardwareManager()
