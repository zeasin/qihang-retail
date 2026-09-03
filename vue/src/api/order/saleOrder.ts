import request from '@/utils/request'

export function listSaleOrder(query?: Record<string, any>) {
  return request({ url: '/erp-api/sale/order/list', method: 'get', params: query })
}

export function getSaleOrder(id: number | string) {
  return request({ url: '/erp-api/sale/order/' + id, method: 'get' })
}

export function addSaleOrder(data: Record<string, any>) {
  return request({ url: '/erp-api/sale/order', method: 'post', data })
}

export function updateSaleOrder(data: Record<string, any>) {
  return request({ url: '/erp-api/sale/order', method: 'put', data })
}

export function delSaleOrder(id: number | string) {
  return request({ url: '/erp-api/sale/order/' + id, method: 'delete' })
}
