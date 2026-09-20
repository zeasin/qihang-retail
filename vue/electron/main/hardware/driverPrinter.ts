/**
 * 走打印驱动的打印（HTML → Windows 驱动 → 打印机）
 * （移植自参考项目 electron/hardware/driverPrinter.js）
 *
 * 和 escpos 那条路的区别：
 *   escpos 直控  —— 自己拼 ESC/POS 字节写串口。快、能控切纸/钱箱，但**只认支持 ESC/POS 且能出 COM 口的机器**
 *   驱动打印     —— HTML 交给 Windows 驱动渲染。**任何装了驱动的打印机都能用**，排版用 CSS 随便写，
 *                   二维码直接内联 SVG。代价是慢一点，且精度受驱动影响
 *
 * 关键难点是**页高**：Chromium 打印必须给一个页高，给大了会空走一整页纸。
 * 做法是在隐藏窗口里渲染完、量出真实高度、换算成微米再设置 pageSize —— 让一页正好是一张小票。
 */

import { BrowserWindow } from 'electron'
import type { PrinterSettings } from '../config'
import type { ReceiptData } from './receiptTypes'
import { buildReceiptHtml } from './receiptHtml'

export interface PrinterListItem {
  name: string
  displayName: string
  isDefault: boolean
  status: number | null
}

export interface PrintHtmlResult {
  ok: boolean
  reason?: string
  heightPx?: number
  pageSize?: { width: number; height: number }
}

/** Chromium 按 96dpi 排版 CSS 像素 */
const MICRONS_PER_PX = 25400 / 96 // ≈ 264.583
/** 小票末尾多留一点，避免最后一行被切掉 */
const TAIL_MICRONS = 3000 // 3mm

export class DriverPrinter {
  ready = false
  message = ''

  constructor(private config: Partial<PrinterSettings> = {}) {}

  /** 目标打印机名；配置为空则用系统默认 */
  deviceName(): string {
    return this.config.driverName || ''
  }

  paperWidthMm(): number {
    return Number(this.config.paperWidth) || 80
  }

  /** 拿一个 webContents 用来枚举打印机（Electron 的打印机列表挂在 webContents 上） */
  private probeWebContents(): Electron.WebContents | null {
    const wins = BrowserWindow.getAllWindows().filter((w) => !w.isDestroyed())
    return wins.length ? wins[0].webContents : null
  }

  /** 列出系统打印机 */
  async listPrinters(): Promise<PrinterListItem[]> {
    const wc = this.probeWebContents()
    if (!wc) return []
    try {
      const list = await wc.getPrintersAsync()
      return list.map((p) => ({
        name: p.name,
        displayName: p.displayName || p.name,
        isDefault: !!p.isDefault,
        status: p.status == null ? null : p.status
      }))
    } catch (e) {
      console.error('[driverPrinter] 枚举打印机失败：', (e as Error).message)
      return []
    }
  }

  async probe(): Promise<{ ready: boolean; message: string; path: string }> {
    const list = await this.listPrinters()
    if (!list.length) {
      this.ready = false
      this.message = '系统里没有找到打印机'
      return this.status()
    }
    const target = this.deviceName()
    if (!target) {
      const def = list.find((p) => p.isDefault)
      this.ready = true
      this.message = def ? `使用系统默认：${def.name}` : `未指定打印机，共 ${list.length} 台可选`
      return this.status()
    }
    const hit = list.find((p) => p.name === target)
    this.ready = !!hit
    this.message = hit ? `已找到 ${target}` : `系统里没有名为「${target}」的打印机`
    return this.status()
  }

  status(): { ready: boolean; message: string; path: string } {
    return { ready: this.ready, message: this.message, path: this.deviceName() || '(系统默认)' }
  }

  /** 打印一段 HTML */
  async printHtml(html: string): Promise<PrintHtmlResult> {
    let win: BrowserWindow | null = null
    try {
      // 窗口要比可打印宽度再宽一点。
      // 因为视口比窗口窄（滚动条等占位，实测 800px 窗口的 innerWidth 只有 786），
      // 窗口刚好等于内容宽的话右边会被挤出视口，文字多折行、量到的高度就偏大。
      const printablePx = Math.ceil(((this.paperWidthMm() - 8) * 96) / 25.4)
      win = new BrowserWindow({
        show: false,
        width: printablePx + 60,
        height: 200,
        webPreferences: { offscreen: false, javascript: true }
      })

      // 用 base64 data URL：避免中文/特殊字符的编码问题，也不用落临时文件
      const b64 = Buffer.from(html, 'utf8').toString('base64')
      await win.loadURL('data:text/html;charset=utf-8;base64,' + b64)

      // 等字体就绪 + 布局稳定。
      // 光等 fonts.ready 不够：实测同一份 HTML 两次量到 563px / 837px，
      // 差 48%——量小了页面就短，小票底部会被切掉，所以必须再给一段沉降时间。
      await win.webContents.executeJavaScript(
        'document.fonts && document.fonts.ready ? document.fonts.ready.then(() => true) : true'
      )
      const heightPx: number = await win.webContents.executeJavaScript(`(async () => {
        // ⚠️ 只能量 body，不能量 documentElement。
        // documentElement.scrollHeight 在内容比视口短时返回的是**视口高度**，
        // 拿它当内容高度会把页高设成窗口高度，白白多走一段纸（实测踩过）。
        const measure = () => {
          const b = document.body
          if (!b) return 0
          return Math.ceil(Math.max(b.scrollHeight || 0, b.getBoundingClientRect().height || 0))
        }
        const raf = () => new Promise(r => requestAnimationFrame(() => requestAnimationFrame(r)))
        // 连续两次测量一致才认为布局稳定，最多等 20 轮
        let prev = measure()
        for (let i = 0; i < 20; i++) {
          await new Promise(r => setTimeout(r, 50))
          await raf()
          const now = measure()
          if (now === prev && i >= 2) return now
          prev = Math.max(prev, now)
        }
        return prev
      })()`)
      if (!heightPx) throw new Error('量不到内容高度')

      const pageSize = {
        width: Math.round(this.paperWidthMm() * 1000), // 微米
        height: Math.round(heightPx * MICRONS_PER_PX) + TAIL_MICRONS
      }

      interface PrintOpts {
        silent: boolean
        printBackground: boolean
        margins: { marginType: 'none' }
        pageSize: { width: number; height: number }
        deviceName?: string
      }
      const opts: PrintOpts = {
        silent: true,
        printBackground: true,
        margins: { marginType: 'none' },
        pageSize
      }
      const name = this.deviceName()
      if (name) opts.deviceName = name

      const res = await new Promise<{ ok: boolean; reason?: string }>((resolve) => {
        win!.webContents.print(opts, (ok, reason) => resolve({ ok, reason }))
        // 保险：回调有时不触发（驱动挂了/打印机离线）
        setTimeout(() => resolve({ ok: false, reason: '打印超时（30 秒无响应）' }), 30000)
      })

      if (!res.ok) {
        this.ready = false
        this.message = res.reason || '打印失败'
        return { ok: false, reason: this.message }
      }

      this.ready = true
      this.message = `已提交到 ${name || '系统默认打印机'}`
      return { ok: true, heightPx, pageSize }
    } catch (e) {
      this.ready = false
      this.message = (e as Error).message
      return { ok: false, reason: this.message }
    } finally {
      if (win && !win.isDestroyed()) win.destroy()
    }
  }

  /** 打印一张自检小票（不需要业务数据） */
  async testPrint(): Promise<PrintHtmlResult> {
    const html = await buildReceiptHtml(
      {
        title: '驱动打印自检',
        orderNo: 'TEST-' + Date.now(),
        items: [
          { goodsName: '自检商品 A（中文测试）', skuName: '500ml', unitPrice: 12.5, quantity: 2, subtotal: 25 },
          { goodsName: 'Test Item B', unitPrice: 3.5, quantity: 1, subtotal: 3.5 }
        ],
        totalAmount: 28.5,
        discountAmount: 3.5,
        finalAmount: 25,
        payMethod: 'cash',
        receivedAmount: 50
      },
      {
        paperWidth: this.paperWidthMm(),
        headerText: this.config.headerText,
        footerText: this.config.footerText,
        qrBaseUrl: this.config.qrBaseUrl
      }
    )
    return this.printHtml(html)
  }

  /** 打印一张业务小票 */
  async print(data: ReceiptData): Promise<PrintHtmlResult> {
    const html = await buildReceiptHtml(data, {
      paperWidth: this.paperWidthMm(),
      headerText: this.config.headerText,
      footerText: this.config.footerText,
      qrBaseUrl: this.config.qrBaseUrl
    })
    return this.printHtml(html)
  }
}

export default DriverPrinter
