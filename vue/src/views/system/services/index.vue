<template>
  <div class="page-container">
    <div class="page-title-bar">
      <h2>服务托管</h2>
      <div class="title-actions">
        <el-button v-if="fromPath" @click="goBack">
          <el-icon><Back /></el-icon>返回上一页
        </el-button>
        <el-button @click="router.push(desktop ? '/pos/cashier' : '/index')">
          <el-icon><HomeFilled /></el-icon>{{ desktop ? '回到收银台' : '回到首页' }}
        </el-button>
      </div>
    </div>

    <el-alert v-if="!desktop" type="warning" :closable="false" class="mb"
      title="当前运行在浏览器环境"
      description="服务托管（后端/数据库环境检测与启动）仅在 Electron 桌面端可用。" />

    <!-- 总览 -->
    <div class="card mb" v-if="desktop">
      <div class="card-header">
        <h3>
          后端服务
          <el-tag :type="backendReachable ? 'success' : 'danger'" size="small" class="ml">
            {{ backendReachable ? '可达' : '未启动' }}
          </el-tag>
        </h3>
        <div>
          <el-button type="primary" :loading="startingAll" @click="handleStartAll">
            <el-icon><VideoPlay /></el-icon>一键启动全部
          </el-button>
          <el-button @click="handleStopAll">
            <el-icon><VideoPause /></el-icon>停止(本程序启动的)
          </el-button>
          <el-button @click="handleOpenDir">
            <el-icon><FolderOpened /></el-icon>打开运行目录
          </el-button>
          <el-button @click="refresh">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </div>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="后端地址">{{ status?.backendUrl || '-' }}</el-descriptions-item>
        <el-descriptions-item label="运行时目录">{{ status?.runtimeDir || '-' }}（绿色便携，全部组件装在这里，不写注册表）</el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 四个组件卡片 -->
    <el-row :gutter="16" class="mb" v-if="desktop">
      <el-col :span="6" v-for="svc in services" :key="svc.name">
        <div class="svc-card">
          <div class="svc-title">
            <span>{{ nameMap[svc.name] }}</span>
            <el-tag :type="svcTagType(svc)" size="small">{{ svcTagText(svc) }}</el-tag>
          </div>
          <div class="svc-meta">
            <div class="svc-line" :title="svc.installPath">路径：{{ svc.installPath || '—' }}</div>
            <div class="svc-line">{{ svc.message }}</div>
          </div>
          <el-progress
            v-if="progress[svc.name]"
            :percentage="percentOf(svc.name)"
            :status="progress[svc.name]?.phase === 'extract' ? 'success' : undefined"
            :indeterminate="progress[svc.name]?.phase === 'extract'"
            :duration="2"
            class="svc-progress"
          />
          <div class="svc-actions">
            <el-button
              v-if="!svc.installed"
              size="small"
              type="primary"
              plain
              :loading="busy[svc.name]"
              @click="handleInstall(svc.name)"
            >
              下载安装
            </el-button>
            <el-button
              size="small"
              type="success"
              plain
              :disabled="svc.running || svc.ownedExternally"
              :loading="busy[svc.name] && !progress[svc.name]"
              @click="handleStart(svc.name)"
              v-if="svc.name !== 'jdk'"
            >
              启动
            </el-button>
            <el-button
              size="small"
              type="warning"
              plain
              :disabled="!svc.running"
              @click="handleStop(svc.name)"
              v-if="svc.name !== 'jdk'"
            >
              停止
            </el-button>
            <el-button size="small" link type="primary" @click="openLog(svc.name)">日志</el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 服务配置 -->
    <div class="card mb" v-if="config">
      <div class="card-header">
        <h3>服务配置</h3>
        <el-button type="primary" :loading="saving" @click="handleSave">
          <el-icon><Check /></el-icon>保存配置
        </el-button>
      </div>
      <el-form label-width="120px" size="default">
        <el-divider content-position="left">JDK</el-divider>
        <el-row :gutter="16">
          <el-col :span="14">
            <el-form-item label="本地 JDK 目录">
              <el-input v-model="config.launcher.jdkDir" placeholder="本机已装 JDK 的目录（其下有 bin\java.exe），留空 = 用运行时目录自动下载安装的 JDK">
                <template #append>
                  <el-button @click="handlePickJdkDir">选择…</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label-width="0">
              <el-button :loading="testingJdk" @click="handleTestJdk">测试 JDK</el-button>
              <span class="form-hint">配置后启动后端直接用这里的 java，无需再下载 JDK</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">后端</el-divider>
        <el-row :gutter="16">
          <el-col :span="16">
            <el-form-item label="jar 路径">
              <el-input v-model="config.launcher.backend.jarPath" placeholder="留空 = 自动在运行时目录/apps 下查找">
                <template #append>
                  <el-button @click="handlePickJar">选择…</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="profile">
              <el-input v-model="config.launcher.backend.profile" placeholder="如 demo" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="JVM 参数">
              <el-input v-model="config.launcher.backend.javaOptions" placeholder="如 -Xmx512m" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">MySQL</el-divider>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="端口">
              <el-input-number v-model="config.launcher.mysql.port" :min="1" :max="65535" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="用户名">
              <el-input v-model="config.launcher.mysql.username" placeholder="root" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="root 密码">
              <el-input v-model="config.launcher.mysql.rootPassword" show-password placeholder="初始化 portable MySQL 时设置" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="数据库名">
              <el-input v-model="config.launcher.mysql.database" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="初始化数据库">
          <el-button :loading="importing" type="warning" plain @click="handleInitDb">
            <el-icon><Upload /></el-icon>导入 SQL 建表/初始数据
          </el-button>
          <span class="form-hint">新库只有空数据库时后端接口会全部 500（表不存在），导入项目 SQL 后重启后端即可</span>
        </el-form-item>
        <el-form-item label-width="0">
          <el-switch v-model="config.launcher.backend.applyLocalConfig" active-text="启动后端时按上述 MySQL/Redis 配置覆盖后端连接参数" />
          <div class="form-hint-block">
            开启后 java -jar 会追加 --spring.datasource.* / --spring.data.redis.* 命令行参数（优先级高于 jar 内配置），
            保证页面改了密码、端口、库名后后端立刻按新值连接；关闭则沿用 jar 自带配置。
          </div>
        </el-form-item>

        <el-divider content-position="left">Redis / 运行时目录</el-divider>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="Redis 端口">
              <el-input-number v-model="config.launcher.redis.port" :min="1" :max="65535" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="运行时目录">
              <el-input v-model="config.launcher.runtimeDir" placeholder="留空 = %APPDATA%\qihang-retail-pos\runtime" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">下载源（与客户网络环境相关，可整体替换）</el-divider>
        <el-form-item label="JDK 17 ZIP">
          <el-input v-model="config.launcher.urls.jdk" />
        </el-form-item>
        <el-form-item label="MySQL 8 ZIP">
          <el-input v-model="config.launcher.urls.mysql" />
        </el-form-item>
        <el-form-item label="Redis ZIP">
          <el-input v-model="config.launcher.urls.redis" />
        </el-form-item>
        <el-form-item label="后端 jar">
          <el-input v-model="config.launcher.urls.backendJar" placeholder="留空 = 只能手动指定本机 jar" />
        </el-form-item>
      </el-form>
    </div>

    <!-- 日志抽屉 -->
    <el-dialog v-model="logVisible" :title="`${nameMap[logName] || ''} 日志（最近 300 行）`" width="760px">
      <pre class="log-box">{{ logText || '（暂无日志）' }}</pre>
      <template #footer>
        <el-button @click="refreshLog">刷新</el-button>
        <el-button type="primary" @click="logVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Back, HomeFilled, Refresh, Check, VideoPlay, VideoPause, FolderOpened, Upload
} from '@element-plus/icons-vue'
import { isElectron, hardwareAPI } from '@/api/hardware'
import { launcherAPI } from '@/api/launcher'

const desktop = isElectron()
const route = useRoute()
const router = useRouter()

const nameMap: Record<LauncherServiceName, string> = {
  jdk: 'JDK 17（后端运行环境）',
  mysql: 'MySQL 8',
  redis: 'Redis',
  backend: '后端服务 erp-api'
}

const fromPath = (() => {
  const from = route.query.from as string | undefined
  return from && from.startsWith('/') && !from.startsWith('//') ? from : ''
})()
const goBack = () => router.push(fromPath || '/system/hardware')

const status = ref<LauncherStatus | null>(null)
const config = ref<AppConfig | null>(null)
const busy = ref<Partial<Record<LauncherServiceName, boolean>>>({})
const progress = ref<Partial<Record<LauncherServiceName, LauncherProgressPayload>>>({})
const startingAll = ref(false)
const saving = ref(false)
const testingJdk = ref(false)
const importing = ref(false)

const logVisible = ref(false)
const logName = ref<LauncherServiceName>('backend')
const logText = ref('')

let pollTimer: number | undefined
let logTimer: number | undefined
let offProgress: (() => void) | null = null

const services = computed<LauncherServiceStatus[]>(() => status.value?.services || [])
const backendReachable = computed(() =>
  (status.value?.services || []).some((s) => s.name === 'backend' && s.running)
)

function svcTagType(svc: LauncherServiceStatus) {
  if (svc.running && !svc.ownedExternally) return 'success'
  if (svc.ownedExternally) return 'primary'
  if (!svc.installed) return 'info'
  return 'warning'
}

function svcTagText(svc: LauncherServiceStatus) {
  if (svc.ownedExternally) return '外部实例运行中'
  if (svc.running) return '运行中'
  if (!svc.installed) return '未安装'
  return '未运行'
}

function percentOf(name: LauncherServiceName) {
  const p = progress.value[name]
  if (!p || p.phase === 'extract') return 100
  if (!p.total) return 0
  return Math.min(99, Math.round((p.downloaded / p.total) * 100))
}

async function refresh() {
  const r = await launcherAPI.getStatus()
  if (r.ok && r.data) status.value = r.data
}

async function loadConfig() {
  const r = await hardwareAPI.getConfig()
  if (r.ok && r.data) config.value = r.data
}

async function handleInstall(name: LauncherServiceName) {
  busy.value[name] = true
  progress.value[name] = { name, phase: 'download', downloaded: 0, total: 0 }
  const r = await launcherAPI.install(name)
  busy.value[name] = false
  progress.value[name] = undefined
  if (r.ok) ElMessage.success(`${nameMap[name]} 安装完成`)
  else ElMessage.error(r.reason || '安装失败')
  refresh()
}

async function handleStart(name: LauncherServiceName) {
  busy.value[name] = true
  const r = await launcherAPI.start(name)
  busy.value[name] = false
  if (r.ok) ElMessage.success(r.message || '已启动')
  else {
    ElMessage.error(r.reason || '启动失败')
    // 后端起不来最需要看 java 输出，直接替用户把日志面板打开
    if (name === 'backend') openLog('backend')
  }
  refresh()
}

async function handleStop(name: LauncherServiceName) {
  const r = await launcherAPI.stop(name)
  r.ok ? ElMessage.success(r.message || '已停止') : ElMessage.warning(r.reason || '停止失败')
  refresh()
}

async function handleStartAll() {
  startingAll.value = true
  const r = await launcherAPI.startAll()
  startingAll.value = false
  const bad = (r.results || []).find((x) => !x.result.ok)
  if (!bad) ElMessage.success('全部服务已就绪')
  else {
    ElMessage.error(`${nameMap[bad.name]} 启动失败：${bad.result.reason || '见日志'}`)
    if (bad.name === 'backend') openLog('backend')
  }
  refresh()
}

async function handleStopAll() {
  await launcherAPI.stopAll()
  ElMessage.success('已停止本程序启动的服务')
  refresh()
}

async function handleOpenDir() {
  const r = await launcherAPI.openDir()
  if (!r.ok) ElMessage.error(r.reason || '打开失败')
}

async function handlePickJar() {
  const r = await launcherAPI.pickJar()
  if (r.ok && r.data && config.value) config.value.launcher.backend.jarPath = r.data
}

/** 选本地 JDK 目录：只填表单，仍需点「保存配置」才对启动生效 */
async function handlePickJdkDir() {
  const r = await launcherAPI.pickJdkDir()
  if (r.ok && r.data && config.value) {
    config.value.launcher.jdkDir = r.data
    ElMessage.success('已填入路径，点「保存配置」后生效')
  }
}

/** 测试按当前输入（未保存也可）解析到的 java.exe */
async function handleTestJdk() {
  if (!config.value) return
  testingJdk.value = true
  try {
    const r = await launcherAPI.testJdk(config.value.launcher.jdkDir)
    if (r.ok) ElMessage.success(`JDK 可用（${r.message}）：${firstLine(r.output)}`)
    else ElMessage.error(r.reason || 'JDK 不可用')
  } finally {
    testingJdk.value = false
  }
}

function firstLine(text?: string) {
  return (text || '').split('\n')[0] || ''
}

/** 导入 SQL：选本机文件（取消则回退用配置的 initSql 下载地址），二次确认后执行 */
async function handleInitDb() {
  const pick = await launcherAPI.pickSql()
  const file = pick.ok ? pick.data : undefined
  if (!pick.ok && pick.reason && pick.reason !== '已取消') {
    ElMessage.error(pick.reason)
    return
  }
  try {
    await ElMessageBox.confirm(
      file ? `将导入：${file}` : '未选择本机文件，将下载「服务配置-下载源-initSql」配置的 SQL 导入。',
      `导入到数据库 ${config.value?.launcher.mysql.database || ''}（已存在的表可能被重建，请确认）`,
      { confirmButtonText: '开始导入', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  importing.value = true
  try {
    const r = await launcherAPI.initDb(file)
    if (r.ok) ElMessage.success(r.message || '导入完成')
    else ElMessage.error(r.reason || '导入失败')
  } finally {
    importing.value = false
  }
}

async function handleSave() {
  if (!config.value) return
  saving.value = true
  try {
    const r = await hardwareAPI.setConfig({ launcher: config.value.launcher })
    if (r.ok) {
      config.value = r.data || config.value
      ElMessage.success('服务配置已保存')
      refresh()
    } else {
      ElMessage.error(r.reason || '保存失败')
    }
  } catch (e: any) {
    ElMessage.error('保存失败：' + (e?.message || e))
  } finally {
    saving.value = false
  }
}

async function openLog(name: LauncherServiceName) {
  logName.value = name
  logVisible.value = true
  refreshLog()
  if (logTimer) clearInterval(logTimer)
  logTimer = window.setInterval(refreshLog, 3000)
}

async function refreshLog() {
  const r = await launcherAPI.getLog(logName.value)
  if (r.ok && r.data) logText.value = r.data.text
}

onMounted(() => {
  if (!desktop) return
  loadConfig()
  refresh()
  offProgress = launcherAPI.onProgress((p) => {
    progress.value[p.name] = p
  })
  pollTimer = window.setInterval(refresh, 5000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
  if (logTimer) clearInterval(logTimer)
  offProgress?.()
  offProgress = null
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 16px;
  /* 独立无框架页面：铺满视口并给个浅灰底，避免路由切换时露出body背景 */
  min-height: 100vh;
  box-sizing: border-box;
  background: #f0f2f5;
}
.card {
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  h3 {
    margin: 0 0 4px;
    font-size: 15px;
    color: #303133;
  }
}
.mb {
  margin-bottom: 16px;
}
.ml {
  margin-left: 8px;
}
.page-title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  h2 {
    margin: 0;
    font-size: 18px;
    color: #303133;
  }
  .title-actions {
    display: flex;
    gap: 8px;
  }
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  h3 {
    margin: 0;
    display: flex;
    align-items: center;
  }
  > div {
    display: flex;
    gap: 8px;
  }
}
.svc-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px 16px;
  height: 190px;
  display: flex;
  flex-direction: column;

  .svc-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    font-size: 14px;
    color: #303133;
    margin-bottom: 8px;
  }
  .svc-meta {
    flex: 1;
    .svc-line {
      font-size: 12px;
      color: #909399;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  .svc-progress {
    margin: 6px 0;
  }
  .svc-actions {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }
}
.log-box {
  max-height: 420px;
  overflow: auto;
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}
.el-divider {
  margin: 8px 0 16px;
}
.el-form-item {
  margin-bottom: 12px;
}
.form-hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
.form-hint-block {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 2px;
}
</style>
