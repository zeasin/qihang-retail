/**
 * 小票 HTML 模板 —— 走打印驱动那条路用（移植自参考项目 electron/hardware/receiptHtml.js）
 *
 * 和 escpos.ts 的 buildReceipt() 吃**同一份数据**（ReceiptData），
 * 这样 printer.mode 在 'escpos' / 'driver' 之间切换时，业务层不用改任何东西。
 *
 * 排版约定（参考项目踩过的坑）：
 *   - 用 <table> 而不是 flex：flex + 驱动默认 A4 宽度会把金额裁到纸外
 *   - 宽度写死像素，不用 100%：打印时 Chromium 会按纸张重新布局，百分比会飘
 *   - 全部内联样式，不引外部 CSS：打印窗口加载 data URL，拿不到外部资源
 */

import type { ReceiptData } from './receiptTypes'
import { fmt, payMethodText } from './escpos'

/* eslint-disable @typescript-eslint/no-explicit-any */

// qrcode 为可选依赖：没装就不打二维码，不阻断小票输出
let QRCode: any = null
try {
  QRCode = eval('require')('qrcode')
} catch {
  console.warn('[receiptHtml] 未安装 qrcode，小票将不打印二维码')
}

/** 纸张宽度(mm) → 可打印宽度(mm)。热敏机打印头通常比纸窄 8mm（80 纸用 72mm 打印头） */
export function printableWidth(paperWidthMm: number): number {
  return Math.max(paperWidthMm - 8, 20)
}

/** mm → CSS 像素（Chromium 按 96dpi 排版） */
export function mm2px(mm: number): number {
  return Math.round((mm * 96) / 25.4 * 100) / 100
}

function esc(s: unknown): string {
  return String(s == null ? '' : s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

function money(n: unknown): string {
  return '¥' + fmt(n)
}

function p2(n: number): string {
  return String(n).padStart(2, '0')
}

/**
 * 生成二维码的内联 SVG（用 SVG 不用 PNG：热敏机分辨率低，矢量图打出来更清晰）
 * @returns SVG 字符串；失败返回空串（不让二维码问题阻断打印）
 */
async function qrSvg(text: string): Promise<string> {
  if (!QRCode) return ''
  try {
    const svg: string = await QRCode.toString(String(text), {
      type: 'svg',
      margin: 0,
      errorCorrectionLevel: 'M'
    })
    // qrcode 输出的 svg 带 width/height="100%"，改成固定尺寸才好排版
    return svg.replace(/<svg([^>]*?)width="[^"]*"/, '<svg$1').replace(/<svg([^>]*?)height="[^"]*"/, '<svg$1')
  } catch (e) {
    console.error('[receiptHtml] 二维码生成失败：', (e as Error).message)
    return ''
  }
}

export interface ReceiptHtmlOptions {
  paperWidth?: number
  headerText?: string
  footerText?: string
  qrBaseUrl?: string
  pretty?: boolean
}

/** 组装小票 HTML */
export async function buildReceiptHtml(data: ReceiptData = {}, opts: ReceiptHtmlOptions = {}): Promise<string> {
  const paperWidth = Number(opts.paperWidth) || 80
  const W = printableWidth(paperWidth) // 可打印宽 mm
  const px = mm2px(W) // 同上，CSS 像素
  const now = new Date()
  const time =
    data.time ||
    `${now.getFullYear()}-${p2(now.getMonth() + 1)}-${p2(now.getDate())} ${p2(now.getHours())}:${p2(now.getMinutes())}:${p2(now.getSeconds())}`

  const items = Array.isArray(data.items) ? data.items : []
  const total = data.totalAmount != null ? data.totalAmount : data.total
  const finalAmount = data.finalAmount != null ? data.finalAmount : total

  // ---- 明细行 ----
  // 商品名/单价/小计一行；规格·克重·工费单独占一整行（塞在商品列里会折行，数字会被从中间断开）
  const itemRows = items
    .map((it) => {
      const qty = it.quantity == null ? 1 : it.quantity
      const sub = it.subtotal != null ? it.subtotal : (Number(it.unitPrice) || 0) * qty
      const name = it.goodsName || it.skuName || it.barcode || '商品'

      const detail: string[] = []
      if (it.skuName && it.skuName !== it.goodsName) detail.push(it.skuName)
      if (Number(it.goldWeight)) detail.push(`金重 ${Number(it.goldWeight).toFixed(2)}g`)
      if (Number(it.silverWeight)) detail.push(`银重 ${Number(it.silverWeight).toFixed(2)}g`)
      if (Number(it.laborCost)) detail.push(`工费 ${money(it.laborCost)}`)
      const detailHtml = detail.length
        ? `<tr><td colspan="3" class="sub detail">${esc(detail.join(' · '))}</td></tr>`
        : ''

      return `<tr>
      <td class="item">${esc(name)}</td>
      <td class="num">${money(it.unitPrice)}<div class="sub">× ${qty}</div></td>
      <td class="num">${money(sub)}</td>
    </tr>${detailHtml}`
    })
    .join('')

  // ---- 二维码 ----
  const qrText =
    opts.qrBaseUrl && data.orderNo ? String(opts.qrBaseUrl).replace(/\/$/, '') + '/' + data.orderNo : ''
  const qr = qrText ? await qrSvg(qrText) : ''
  const qrBlock = qr
    ? `<div class="qr"><div class="qrimg" style="width:${mm2px(28)}px;height:${mm2px(28)}px">${qr}</div><div class="sub">扫码查看电子凭证</div></div>`
    : ''

  // ---- 金额区 ----
  const amountRows: string[] = []
  amountRows.push(`<tr><td>合计</td><td class="num">${money(total)}</td></tr>`)
  if (Number(data.discountAmount)) amountRows.push(`<tr><td>优惠</td><td class="num">-${money(data.discountAmount)}</td></tr>`)
  amountRows.push(`<tr class="total"><td>应收</td><td class="num">${money(finalAmount)}</td></tr>`)
  if (data.payMethod) amountRows.push(`<tr><td>支付方式</td><td class="num">${esc(payMethodText(data.payMethod))}</td></tr>`)
  if (Number(data.receivedAmount)) {
    amountRows.push(`<tr><td>实收</td><td class="num">${money(data.receivedAmount)}</td></tr>`)
    amountRows.push(`<tr><td>找零</td><td class="num">${money(Number(data.receivedAmount) - Number(finalAmount))}</td></tr>`)
  }

  const head: string[] = []
  if (data.shopName) head.push(`<div>${esc(data.shopName)}</div>`)
  if (data.title) head.push(`<div class="sub">${esc(data.title)}</div>`)
  if (data.orderNo) head.push(`<div>单号 ${esc(data.orderNo)}</div>`)
  head.push(`<div>${esc(time)}</div>`)
  if (data.memberName) head.push(`<div>会员 ${esc(data.memberName)}</div>`)
  if (data.salespersonName) head.push(`<div>导购 ${esc(data.salespersonName)}</div>`)

  return `<!DOCTYPE html>
<html><head><meta charset="utf-8"><title>receipt</title>
<style>
  /* 纸张宽度写死；@page 去掉浏览器默认页边距 */
  @page { margin: 0; }
  html, body { margin: 0; padding: 0; }
  body {
    /* 宽度写死；居中，让 72mm 内容落在 80mm 纸的中间 */
    width: ${px}px;
    margin: 0 auto;
    font-family: "Microsoft YaHei", "SimSun", monospace;
    font-size: 12px;
    line-height: 1.45;
    color: #000;
    ${opts.pretty ? 'padding: 6px 0;' : ''}
  }
  .hd { text-align: center; font-size: 14px; font-weight: bold; padding-bottom: 2px; }
  .hd.sub { font-weight: normal; font-size: 11px; }
  .sub { font-size: 11px; color: #000; }
  table { width: 100%; border-collapse: collapse; table-layout: fixed; }
  td { vertical-align: top; padding: 1px 0; word-break: break-word; }
  .item { width: 46%; }
  /* 金额和数量不折行：从数字中间断行是最难看的 */
  td.num { width: 27%; text-align: right; white-space: nowrap; }
  /* 规格/克重/工费行：占满整行，左缩进对齐商品名 */
  td.detail { padding: 0 0 3px 0; }
  hr { border: none; border-top: 1px dashed #000; margin: 4px 0; }
  tr.total td { font-size: 15px; font-weight: bold; padding-top: 2px; }
  .ft { text-align: center; padding-top: 4px; font-size: 11px; }
  .qr { text-align: center; padding: 4px 0; }
  .qrimg { display: inline-block; }
  .qrimg svg { width: 100%; height: 100%; display: block; }
</style></head>
<body>
  <div class="hd">${esc(opts.headerText || '启航零售')}</div>
  <div class="hd sub">${head.join('')}</div>
  <hr>
  <table>${itemRows || '<tr><td colspan="3" class="sub" style="text-align:center">（无商品明细）</td></tr>'}</table>
  <hr>
  <table>${amountRows.join('')}</table>
  <hr>
  ${qrBlock}
  <div class="ft">${esc(opts.footerText || '谢谢惠顾，欢迎再次光临')}</div>
</body></html>`
}
