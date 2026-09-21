import router from './router'
import { useUserStore } from './store/modules/user'
import { usePermissionStore } from './store/modules/permission'
import { useSettingsStore } from './store/modules/settings'
import { getToken } from './utils/auth'
import { defaultLandingPath } from './utils/desktop'
import { ensureBackendOrGuide } from './utils/backendGuide'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

// 服务托管/服务状态页只走本机 IPC，不依赖登录态；后端起不来时恰恰需要在未登录时打开它们，必须放行
const whiteList = ['/login', '/401', '/404', '/system/services', '/system/status']

router.beforeEach(async (to, _from, next) => {
  NProgress.start()

  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  const settingsStore = useSettingsStore()

  if (settingsStore.dynamicTitle) document.title = (to.meta.title as string) || import.meta.env.VITE_APP_TITLE

  if (getToken()) {
    if (to.path === '/login') {
      next({ path: defaultLandingPath() })
      NProgress.done()
    } else {
      if (!userStore.roles.length) {
        try {
          await userStore.GetInfo()
          await permissionStore.GenerateRoutes()
          next({ ...to, replace: true })
          // 桌面端 token 仍有效时不会经过登录页，这里补一次后端探测引导
          ensureBackendOrGuide()
        } catch (e) {
          await userStore.FedLogOut()
          next(`/login?redirect=${to.path}`)
          NProgress.done()
        }
      } else {
        next()
      }
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
