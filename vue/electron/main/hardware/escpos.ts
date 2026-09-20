/**
 * 极简 ESC/POS 指令构造器（移植自参考项目 electron/hardware/escpos.js）
 *
 * 为什么不用现成的库：`esc-printer` 在 npm 上不存在（404），
 * `escpos` 只有 alpha 版本、`node-thermal-printer` 依赖链较重。
 * 收银小票用到的指令就这几条，自己拼字节反而更可控、零依赖风险。
 *
 * 指令参考：ESC/POS Command Manual（EPSON TM-T88 系列通用）
 */

import type { ReceiptData } from './receiptTypes'

export const ESC = 0x1b
export const GS = 0x1d

const ALIGN: Record<string, number> = { left: 0, center: 1, right: 2 }

// iconv-lite 用于把中文转成打印机认的 GBK 编码；没有就退回 utf8（部分机型支持）
let iconv: { encode: (s: string, enc: string) => Buffer; encodingExists: (enc: string) => boolean } | null = null
try {
  iconv = eval('require')('iconv-lite')
} catch {
  console.warn('[escpos] 未安装 iconv-lite，中文将按 UTF-8 输出（部分机型会乱码）')
}

/** 计算字符串在热敏纸上的显示宽度：ASCII 占 1 列，中文/全角占 2 列 */
export function displayWidth(str: string): number {
  let w = 0
  for (const ch of String(str)) {
    w += /[\u0000-\u00ff]/.test(ch) ? 1 : 2
  }
  return w
}

/** 按显示宽度截断，超出补省略号 */
function truncate(str: string, cols: number): string {
  const s = String(str == null ? '' : str)
  if (displayWidth(s) <= cols) return s
  let out = ''
  let w = 0
  for (const ch of s) {
    const cw = /[\u0000-\u00ff]/.test(ch) ? 1 : 2
    if (w + cw > cols - 2) break
    out += ch
    w += cw
  }
  return out + '..'
}

export interface ReceiptBuilderOptions {
  /** 纸宽 mm：58 → 32 列，80 → 48 列 */
  paperWidth?: number
  /** 字符编码，默认 gbk */
  encoding?: string
}

export class ReceiptBuilder {
  cols: number
  encoding: string
  private chunks: Buffer[] = []

  constructor(opts: ReceiptBuilderOptions = {}) {
    const paperWidth = Number(opts.paperWidth) || 58
    this.cols = paperWidth >= 76 ? 48 : 32
    this.encoding = opts.encoding || 'gbk'
  }

  /** 原始字节 */
  raw(bytes: number[]): this {
    this.chunks.push(Buffer.from(bytes))
    return this
  }

  /** 文本（按配置编码） */
  text(str: string): this {
    const s = String(str == null ? '' : str)
    if (iconv && iconv.encodingExists(this.encoding)) {
      this.chunks.push(iconv.encode(s, this.encoding))
    } else {
      this.chunks.push(Buffer.from(s, 'utf8'))
    }
    return this
  }

  /** 一行文本（自动换行） */
  line(str = ''): this {
    return this.text(str + '\n')
  }

  /** 初始化打印机（每次打印开头都发一遍，清掉上一次的样式残留） */
  init(): this {
    return this.raw([ESC, 0x40])
  }

  /** 对齐：left / center / right */
  align(mode: 'left' | 'center' | 'right'): this {
    return this.raw([ESC, 0x61, ALIGN[mode] || 0])
  }

  /** 加粗 */
  bold(on: boolean): this {
    return this.raw([ESC, 0x45, on ? 1 : 0])
  }

  /** 字号：宽高倍率 1~8。GS ! n，高 4 位宽 4 位 */
  size(w = 1, h = 1): this {
    const n = ((Math.min(Math.max(w, 1), 8) - 1) << 4) | (Math.min(Math.max(h, 1), 8) - 1)
    return this.raw([GS, 0x21, n])
  }

  /** 强制换行（走纸 n 行） */
  feed(n = 1): this {
    return this.raw([ESC, 0x64, Math.min(Math.max(n, 0), 255)])
  }

  /** 分隔线 */
  divider(ch = '-'): this {
    return this.line(ch.repeat(this.cols))
  }

  /** 左右对齐的一行（左标签 + 右数值），按显示宽度补齐 */
  row(left: string, right: string | number, cols = this.cols): this {
    const l = truncate(left, cols)
    const r = String(right == null ? '' : right)
    const pad = Math.max(cols - displayWidth(l) - displayWidth(r), 1)
    return this.line(l + ' '.repeat(pad) + r)
  }

  /** 右对齐的一行 */
  rowRight(str: string): this {
    const s = String(str == null ? '' : str)
    const pad = Math.max(this.cols - displayWidth(s), 0)
    return this.line(' '.repeat(pad) + s)
  }

  /** 切纸（带 1 行走纸的半切） */
  cut(): this {
    return this.raw([GS, 0x56, 0x42, 0x00])
  }

  build(): Buffer {
    return Buffer.concat(this.chunks)
  }
}

export interface BuildReceiptOptions extends ReceiptBuilderOptions {
  headerText?: string
  footerText?: string
}

/**
 * 组装一张收银小票
 * @param data  见 receiptTypes.ts 的 ReceiptData
 * @param opts  { paperWidth, encoding, headerText, footerText }
 */
export function buildReceipt(data: ReceiptData = {}, opts: BuildReceiptOptions = {}): Buffer {
  const b = new ReceiptBuilder(opts)
  const now = new Date()
  const time =
    data.time ||
    `${now.getFullYear()}-${p2(now.getMonth() + 1)}-${p2(now.getDate())} ${p2(now.getHours())}:${p2(now.getMinutes())}:${p2(now.getSeconds())}`

  b.init()
    .align('center')
    .size(1, 1)
    .bold(true)
    .line(opts.headerText || '启航零售')
    .bold(false)
    .size(1, 1)

  if (data.shopName) b.line(data.shopName)
  if (data.title) b.line(data.title)

  b.align('left').divider()

  if (data.orderNo) b.row('订单号', data.orderNo)
  b.row('时间', time)
  if (data.memberName) b.row('会员', data.memberName)
  if (data.salespersonName) b.row('导购', data.salespersonName)

  b.divider()

  // 明细
  const items = Array.isArray(data.items) ? data.items : []
  for (const it of items) {
    b.line(it.goodsName || it.skuName || it.barcode || '商品')
    const name = it.skuName && it.skuName !== it.goodsName ? `  ${it.skuName}` : ''
    const price = fmt(it.unitPrice)
    const qty = it.quantity == null ? 1 : it.quantity
    const sub = fmt(it.subtotal != null ? it.subtotal : (Number(it.unitPrice) || 0) * qty)
    if (name) b.line(name)
    b.row(`  ${price} x ${qty}`, sub)
  }

  b.divider()
  b.row('合计', '¥' + fmt(data.totalAmount != null ? data.totalAmount : data.total))
  if (Number(data.discountAmount)) b.row('优惠', '-¥' + fmt(data.discountAmount))
  b.bold(true).size(2, 2)
  b.row('应收', '¥' + fmt(data.finalAmount != null ? data.finalAmount : data.total))
  b.size(1, 1).bold(false)

  if (data.payMethod) b.row('支付方式', payMethodText(data.payMethod))
  if (Number(data.receivedAmount)) {
    b.row('实收', '¥' + fmt(data.receivedAmount))
    b.row('找零', '¥' + fmt(Number(data.receivedAmount) - Number(data.finalAmount != null ? data.finalAmount : data.total)))
  }

  b.divider()
  b.align('center').line(opts.footerText || '谢谢惠顾，欢迎再次光临')
  b.feed(3).cut()

  return b.build()
}

export function payMethodText(m?: string): string {
  const map: Record<string, string> = { cash: '现金', wechat: '微信', alipay: '支付宝', card: '银行卡', member: '会员余额' }
  return map[m || ''] || m || ''
}

function p2(n: number): string {
  return String(n).padStart(2, '0')
}

export function fmt(n: unknown): string {
  const v = Number(n)
  return isNaN(v) ? '0.00' : v.toFixed(2)
}
