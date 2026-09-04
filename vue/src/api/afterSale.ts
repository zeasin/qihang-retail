import request from '@/utils/request'

export function getAfterSaleList(query?: Record<string, any>) {
  return request({ url: '/erp-api/afterSale/list', method: 'get', params: query })
}

export function getAfterSaleDetail(id: number | string) {
  return request({ url: '/erp-api/afterSale/' + id, method: 'get' })
}

export function applyAfterSale(data: Record<string, any>) {
  return request({ url: '/erp-api/afterSale/apply', method: 'post', data })
}

export function auditAfterSale(id: number | string, data: Record<string, any>) {
  return request({ url: '/erp-api/afterSale/audit/' + id, method: 'put', data })
}

export function receiveReturnGoods(id: number | string) {
  return request({ url: '/erp-api/afterSale/receive/' + id, method: 'put' })
}

export function processAfterSale(id: number | string, data: Record<string, any>) {
  return request({ url: '/erp-api/afterSale/process/' + id, method: 'put', data })
}

export function shipExchange(id: number | string) {
  return request({ url: '/erp-api/afterSale/shipExchange/' + id, method: 'put' })
}

export function cancelAfterSale(id: number | string) {
  return request({ url: '/erp-api/afterSale/cancel/' + id, method: 'put' })
}

export function getAfterSaleStats() {
  return request({ url: '/erp-api/afterSale/stats', method: 'get' })
}

export function getRefundableOrders(query?: Record<string, any>) {
  return request({ url: '/erp-api/afterSale/refundableOrders', method: 'get', params: query })
}

export function getAfterSaleConfig() {
  return request({ url: '/erp-api/afterSale/config', method: 'get' })
}
