import request from '@/utils/request'

export function listShop(query?: Record<string, any>) {
  return request({ url: '/erp-api/shop/list', method: 'get', params: query })
}

export function getShop(id: number) {
  return request({ url: '/erp-api/shop/' + id, method: 'get' })
}

export function addShop(data: Record<string, any>) {
  return request({ url: '/erp-api/shop', method: 'post', data })
}

export function updateShop(data: Record<string, any>) {
  return request({ url: '/erp-api/shop', method: 'put', data })
}

export function delShop(ids: number | string) {
  return request({ url: '/erp-api/shop/' + ids, method: 'delete' })
}

export function shopOptions(query?: Record<string, any>) {
  return request({ url: '/erp-api/shop/options', method: 'get', params: query })
}
