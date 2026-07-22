import { defineStore } from 'pinia'
import router, { constantRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import type { MenuRecord } from '@/api/menu'

const viewsMap: Record<string, () => Promise<any>> = {
  'Layout': () => import('@/layout/index.vue'),
  'ParentView': () => import('@/components/ParentView/index.vue'),
  'system/user/index': () => import('@/views/system/user/index.vue'),
  'system/user/authRole': () => import('@/views/system/user/authRole.vue'),
  'system/user/profile/index': () => import('@/views/system/user/profile/index.vue'),
  'system/role/index': () => import('@/views/system/role/index.vue'),
  'system/role/authUser': () => import('@/views/system/role/authUser.vue'),
  'system/role/selectUser': () => import('@/views/system/role/selectUser.vue'),
  'system/menu/index': () => import('@/views/system/menu/index.vue'),
  'system/dict/index': () => import('@/views/system/dict/index.vue'),
  'system/dict/data': () => import('@/views/system/dict/data.vue'),
  'system/config/index': () => import('@/views/system/config/index.vue'),
  'system/task/index': () => import('@/views/system/task/index.vue'),
  'system/alertChannel/index': () => import('@/views/system/alertChannel/index.vue'),
  'goods/index': () => import('@/views/goods/index.vue'),
  'goods/goods_list': () => import('@/views/goods/goods_list.vue'),
  'goods/create_new': () => import('@/views/goods/create_new.vue'),
  'goods/spec/index': () => import('@/views/goods/spec/index.vue'),
  'goods/spec/add': () => import('@/views/goods/spec/add.vue'),
  'goods/category/index': () => import('@/views/goods/category/index.vue'),
  'goods/category/attribute': () => import('@/views/goods/category/categoryAttribute.vue'),
  'goods/category/attributeValue': () => import('@/views/goods/category/categoryAttributeValue.vue'),
  'goods/category/categoryAttribute': () => import('@/views/goods/category/categoryAttribute.vue'),
  'goods/category/categoryAttributeValue': () => import('@/views/goods/category/categoryAttributeValue.vue'),
  'goods/brand/index': () => import('@/views/goods/brand/index.vue'),
  'goods/product_lib/index': () => import('@/views/goods/product_lib/index.vue'),
  'goods/product_lib/goods_list': () => import('@/views/goods/product_lib/goods_list.vue'),
  'goods/product_lib/create_new': () => import('@/views/goods/product_lib/create_new.vue'),
  'goods/product_lib/add_sku': () => import('@/views/goods/product_lib/add_sku.vue'),
  'goods/product_lib/sku_list': () => import('@/views/goods/product_lib/sku_list.vue'),
  'goods/create': () => import('@/views/goods/create.vue'),
  'goods/PopupSkuList': () => import('@/views/goods/PopupSkuList.vue'),
  'purchase/order': () => import('@/views/purchase/order/index.vue'),
  'purchase/order/index': () => import('@/views/purchase/order/index.vue'),
  'purchase/order/list': () => import('@/views/purchase/order/list.vue'),
  'purchase/order/create': () => import('@/views/purchase/order/create.vue'),
  'purchase/order/detail': () => import('@/views/purchase/order/detail.vue'),
  'purchase/order/item': () => import('@/views/purchase/order/item.vue'),
  'purchase/order/stock_in': () => import('@/views/purchase/order/stock_in.vue'),
  'purchase/ship': () => import('@/views/purchase/ship/index.vue'),
  'purchase/ship/index': () => import('@/views/purchase/ship/index.vue'),
  'purchase/ship/create_stock_in_entry': () => import('@/views/purchase/ship/create_stock_in_entry.vue'),
  'purchase/shipper': () => import('@/views/purchase/shipper/index.vue'),
  'purchase/shipper/index': () => import('@/views/purchase/shipper/index.vue'),
  'order/index': () => import('@/views/order/index.vue'),
  'order/order_list': () => import('@/views/order/order_list.vue'),
  'order/item_list': () => import('@/views/order/item_list.vue'),
  'stock/index': () => import('@/views/stock/index.vue'),
  'wms/warehouse/index': () => import('@/views/wms/warehouse/index.vue'),
  'wms/warehouse/position': () => import('@/views/wms/warehouse/position.vue'),
  'wms/stockIn/index': () => import('@/views/wms/stockIn/index.vue'),
  'wms/stockIn/create': () => import('@/views/wms/stockIn/create.vue'),
  'wms/stockIn/stock_in': () => import('@/views/wms/stockIn/stock_in.vue'),
  'wms/stockOut/index': () => import('@/views/wms/stockOut/index.vue'),
  'wms/stockOut/create': () => import('@/views/wms/stockOut/create.vue'),
  'wms/stockOut/stock_out': () => import('@/views/wms/stockOut/stock_out.vue'),
  'library/logistics_company/index': () => import('@/views/library/logistics_company/index.vue'),
  'components/icons/index': () => import('@/views/components/icons/index.vue'),
}

function loadView(view: string) {
  // 兼容带 .vue 后缀和不带后缀的情况
  const key = view.replace(/\.vue$/, '')
  if (viewsMap[key]) return viewsMap[key]
  console.error(`[permission.ts] 找不到组件: ${view}，请在 viewsMap 中添加该映射`)
  return () => import('@/views/error/ComponentNotFound.vue')
}

function cleanPath(path: string): string {
  if (!path) return ''
  let result = path
  while (result.includes('//')) {
    result = result.replace('//', '/')
  }
  if (result.endsWith('/')) {
    result = result.slice(0, -1)
  }
  return result
}

function resolveRoutePath(path: string, parentPath: string): string {
  if (!path) return parentPath || ''
  if (path.startsWith('/')) return cleanPath(path)
  if (!parentPath || parentPath === '/') {
    return cleanPath('/' + path)
  }
  let result = parentPath
  if (!result.endsWith('/')) result += '/'
  result += path
  return cleanPath(result)
}

const EXCLUDED_PATHS = ['/pos', 'pos']
const EXCLUDED_TITLES = ['POS', '收银']

function isExcludedRoute(route: MenuRecord): boolean {
  const path = route.path?.toLowerCase() || ''
  const title = route.meta?.title?.toLowerCase() || ''
  if (EXCLUDED_PATHS.some(p => path.includes(p))) return true
  if (EXCLUDED_TITLES.some(t => title.includes(t.toLowerCase()))) return true
  return false
}

function filterAsyncRouter(routes: MenuRecord[], parentPath: string = ''): any[] {
  return routes
    .filter((route) => !isExcludedRoute(route))
    .filter((route) => route.component || route.children?.length)
    .map((route) => {
      const r: Record<string, any> = { ...route }
      r.path = resolveRoutePath(r.path, parentPath)
      if (r.component) {
        r.component = loadView(r.component)
      }
      if (r.children?.length) {
        r.children = filterAsyncRouter(r.children, r.path)
      } else {
        delete r.children
        delete r.redirect
      }
      return r
    })
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [] as any[],
    addRoutes: [] as any[],
  }),
  actions: {
    async GenerateRoutes() {
      const res = await getRouters()
      const rewriteRoutes = filterAsyncRouter(res.data || [])
      rewriteRoutes.push({ path: '/:pathMatch(.*)*', redirect: '/404', hidden: true })
      rewriteRoutes.forEach((route) => router.addRoute(route))
      this.addRoutes = rewriteRoutes
      this.routes = constantRoutes.concat(rewriteRoutes)
      return rewriteRoutes
    },
    setSidebarRouters(routes: any[]) {
      this.routes = routes
    },
  },
})
