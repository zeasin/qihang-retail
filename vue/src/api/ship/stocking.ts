import request from '@/utils/request'

export function getStockingList(query?: Record<string, any>) {
  return request({ url: '/erp-api/ship/stocking/list', method: 'get', params: query })
}

export function getStockingDetail(id: string) {
  return request({ url: '/erp-api/ship/stocking/' + id, method: 'get' })
}

export function executeDelivery(data: Record<string, any>) {
  return request({ url: '/erp-api/ship/stocking/deliver', method: 'post', data })
}

export function executePickup(orderId: string) {
  return request({ url: '/erp-api/ship/stocking/pickup/' + orderId, method: 'post' })
}

export function getStockingStats() {
  return request({ url: '/erp-api/ship/stocking/stats', method: 'get' })
}
