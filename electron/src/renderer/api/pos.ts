import http from './http'

export function getGoodsList(params?: Record<string, any>) {
  return http.get('/erp-api/goods/list', { params })
}

export function searchGoodsByBarcode(barcode: string) {
  return http.get('/pos-api/goods/barcode/' + barcode)
}

export function searchGoods(keyword: string) {
  return http.get('/pos-api/goods/search', { params: { keyword } })
}

export function submitOrder(data: Record<string, any>) {
  return http.post('/pos-api/cashier/submit', data)
}

export function getOrderList(params?: Record<string, any>) {
  return http.get('/pos-api/cashier/order/list', { params })
}

export function getOrder(id: number | string) {
  return http.get('/pos-api/cashier/order/' + id)
}

export function refundOrder(data: Record<string, any>) {
  return http.post('/pos-api/refund', data)
}

export function getRefundableOrders(params?: Record<string, any>) {
  return http.get('/pos-api/refund/list', { params })
}

export function getTodayStats() {
  return http.get('/pos-api/order/today')
}

export function getMemberByPhone(phone: string) {
  return http.get('/pos-api/member/phone/' + phone)
}

export function getMemberList(params?: Record<string, any>) {
  return http.get('/pos-api/member/list', { params })
}

export function addMember(data: Record<string, any>) {
  return http.post('/pos-api/member', data)
}

export function getSkuInventory(skuId: number | string) {
  return http.get('/pos-api/inventory/sku/' + skuId)
}

export function getSkuInventoryBatches(skuId: number | string) {
  return http.get('/pos-api/inventory/sku/' + skuId + '/batches')
}

export function batchSkuInventory(skuIds: (number | string)[]) {
  return http.post('/pos-api/inventory/batch', skuIds)
}

export function listCategory(params?: Record<string, any>) {
  return http.get('/erp-api/goods_category/list', { params })
}
