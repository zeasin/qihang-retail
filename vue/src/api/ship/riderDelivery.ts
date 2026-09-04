import request from '@/utils/request'

export function getRiderDeliveryList(query?: Record<string, any>) {
  return request({ url: '/erp-api/rider/delivery/list', method: 'get', params: query })
}

export function getRiderDeliveryDetail(id: string) {
  return request({ url: '/erp-api/rider/delivery/' + id, method: 'get' })
}

export function getRiderDeliveryStats() {
  return request({ url: '/erp-api/rider/delivery/stats', method: 'get' })
}

export function batchPrinted(orderIds: string[]) {
  return request({ url: '/erp-api/rider/delivery/batchPrinted', method: 'post', data: orderIds })
}

export function batchShip(orderIds: string[]) {
  return request({ url: '/erp-api/rider/delivery/batchShip', method: 'post', data: orderIds })
}
