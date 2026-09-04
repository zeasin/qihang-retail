import request from '@/utils/request'

export function listPlatform() {
  return request({ url: '/erp-api/shop/platform/list', method: 'get' })
}

export function getPlatform(id: number) {
  return request({ url: '/erp-api/shop/platform/' + id, method: 'get' })
}

export function addPlatform(data: Record<string, any>) {
  return request({ url: '/erp-api/shop/platform', method: 'post', data })
}

export function updatePlatform(data: Record<string, any>) {
  return request({ url: '/erp-api/shop/platform', method: 'put', data })
}

export function delPlatform(ids: number | string) {
  return request({ url: '/erp-api/shop/platform/' + ids, method: 'delete' })
}
