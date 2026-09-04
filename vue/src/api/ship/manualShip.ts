import request from '@/utils/request'

export function getManualShipList(query?: Record<string, any>) {
  return request({ url: '/erp-api/manual/ship/list', method: 'get', params: query })
}

export function getManualShipDetail(id: string) {
  return request({ url: '/erp-api/manual/ship/' + id, method: 'get' })
}

export function getManualShipStats() {
  return request({ url: '/erp-api/manual/ship/stats', method: 'get' })
}

export function confirmPickup(orderId: string) {
  return request({ url: '/erp-api/manual/ship/confirm/' + orderId, method: 'post' })
}

export function batchConfirmPickup(orderIds: string[]) {
  return request({ url: '/erp-api/manual/ship/batchConfirm', method: 'post', data: orderIds })
}
