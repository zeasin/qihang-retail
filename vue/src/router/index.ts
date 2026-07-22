import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/index.vue'

export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue'),
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    hidden: true,
    meta: { title: '登录' },
  },
  {
    path: '/401',
    name: 'Page401',
    component: () => import('@/views/error/401.vue'),
    hidden: true,
    meta: { title: '401' },
  },
  {
    path: '/404',
    name: 'Page404',
    component: () => import('@/views/error/404.vue'),
    hidden: true,
    meta: { title: '404' },
  },
  {
    path: '/pos',
    name: 'POS',
    component: () => import('@/views/pos/layout/PosLayout.vue'),
    redirect: '/pos/cashier',
    hidden: true,
    meta: { title: 'POS收银台', icon: 'shopping', requiresAuth: true },
    children: [
      {
        path: 'cashier',
        name: 'PosCashier',
        component: () => import('@/views/pos/index.vue'),
        meta: { title: '收银台', icon: 'shopping' },
      },
      {
        path: 'orders',
        name: 'PosOrders',
        component: () => import('@/views/pos/pages/Orders.vue'),
        meta: { title: '订单查询', icon: 'list' },
      },
      {
        path: 'members',
        name: 'PosMembers',
        component: () => import('@/views/pos/pages/Members.vue'),
        meta: { title: '会员管理', icon: 'user' },
      },
      {
        path: 'refund',
        name: 'PosRefund',
        component: () => import('@/views/pos/pages/Refund.vue'),
        meta: { title: '退款管理', icon: 'refund' },
      },
      {
        path: 'reports',
        name: 'PosReports',
        component: () => import('@/views/pos/pages/Reports.vue'),
        meta: { title: '交班结算', icon: 'data-analysis' },
      },
    ],
  },
  {
    path: '/',
    name: 'Index',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: 'index',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'dashboard' },
      },
      {
        path: 'user/profile',
        name: 'Profile',
        component: () => import('@/views/system/user/profile/index.vue'),
        hidden: true,
        meta: { title: '个人中心' },
      },
    ],
  },
] as RouteRecordRaw[]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
})

export default router
