/**
 * 小票数据结构 —— escpos 直控与驱动打印两条路吃同一份数据，
 * printer.mode 切换时业务层调用方式完全不变。
 * （渲染进程 types/electron.d.ts 里有对应的镜像定义）
 */

export interface ReceiptItemData {
  goodsName?: string
  skuName?: string
  barcode?: string
  unitPrice?: number
  quantity?: number
  subtotal?: number
  /** 克重/工费类商品（黄金珠宝场景）可选字段 */
  goldWeight?: number
  silverWeight?: number
  laborCost?: number
}

export interface ReceiptData {
  /** 小票标题（如"收银小票"/"退货小票"/"打印机自检"） */
  title?: string
  shopName?: string
  orderNo?: string
  /** 缺省用当前时间 */
  time?: string
  memberName?: string
  salespersonName?: string
  items?: ReceiptItemData[]
  totalAmount?: number
  total?: number
  discountAmount?: number
  finalAmount?: number
  payMethod?: string
  receivedAmount?: number
}

/** IPC 统一返回结构：所有 handler 都不抛异常到渲染进程，避免页面白屏 */
export interface IpcResult<T = unknown> {
  ok: boolean
  data?: T
  reason?: string
  hint?: string
}
