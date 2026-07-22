import request from '@/utils/request'

export function getGoodsList(query?: Record<string, any>) {
  return request({ url: '/api/erp-api/goods/list', method: 'get', params: query })
}

export function getGoodsDetail(id: number | string) {
  return request({ url: '/api/pos-api/goods/' + id, method: 'get' })
}

export function getGoodsSkuList(goodsId: number | string) {
  return request({ url: '/api/pos-api/goods/' + goodsId + '/sku', method: 'get' })
}

export function searchGoodsByBarcode(barcode: string) {
  return request({ url: '/api/pos-api/goods/barcode/' + barcode, method: 'get' })
}

export function searchGoods(keyword: string) {
  return request({ url: '/api/pos-api/goods/search', method: 'get', params: { keyword } })
}

export function submitOrder(data: Record<string, any>) {
  return request({ url: '/api/pos-api/cashier/submit', method: 'post', data })
}

export function getPosOrderList(query?: Record<string, any>) {
  return request({ url: '/api/pos-api/cashier/order/list', method: 'get', params: query })
}

export function getPosOrder(id: number | string) {
  return request({ url: '/api/pos-api/cashier/order/' + id, method: 'get' })
}

export function refundOrder(data: Record<string, any>) {
  return request({ url: '/api/pos-api/cashier/refund', method: 'post', data })
}

export function getOrderList(query?: Record<string, any>) {
  return request({ url: '/api/pos-api/order/list', method: 'get', params: query })
}

export function getOrder(id: number | string) {
  return request({ url: '/api/pos-api/order/' + id, method: 'get' })
}

export function getTodayStats(shopId: number) {
  return request({ url: '/api/pos-api/order/today', method: 'get', params: { shopId } })
}

export function getDailyReport(shopId: number, date: string) {
  return request({ url: '/api/pos-api/order/daily', method: 'get', params: { shopId, date } })
}

export function getMemberByPhone(phone: string, shopId: number) {
  return request({ url: '/api/pos-api/member/phone/' + phone, method: 'get', params: { shopId } })
}

export function getMember(id: number | string) {
  return request({ url: '/api/pos-api/member/' + id, method: 'get' })
}

export function getMemberList(query?: Record<string, any>) {
  return request({ url: '/api/pos-api/member/list', method: 'get', params: query })
}

export function addMember(data: Record<string, any>) {
  return request({ url: '/api/pos-api/member', method: 'post', data })
}

export function updateMember(data: Record<string, any>) {
  return request({ url: '/api/pos-api/member', method: 'put', data })
}

export function getSkuInventory(skuId: number | string) {
  return request({ url: '/api/pos-api/inventory/sku/' + skuId, method: 'get' })
}

export function getSkuInventoryBatches(skuId: number | string) {
  return request({ url: '/api/pos-api/inventory/sku/' + skuId + '/batches', method: 'get' })
}
