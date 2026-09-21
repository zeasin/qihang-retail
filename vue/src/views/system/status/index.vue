<!--
  服务状态（独立整页，仅桌面端有意义）：
  - 免登录可访问（后端起不来时正是要看它），无后台侧边栏框架
  - 四个服务状态 + 启停 + 一键启动全部；下方大面积后端 Java 实时日志（3s 轮询）
  - 右上角互通「服务托管」配置页（安装/下载/参数）
-->
<template>
  <div class="status-page">
    <div class="page-title-bar">
      <h2>
        服务状态
        <el-tag :type="backendUp === null ? 'info' : backendUp ? 'success' : 'danger'" size="small" class="ml">
          后端 {{ backendUp === null ? '探测中' : backendUp ? '运行中' : '未启动' }}
        </el-tag>
      </h2>
      <div class="title-actions">
        <el-button v-if="fromPath" @click="router.push(fromPath)">
          <el-icon><Back /></el-icon>返回上一页
        </el-button>
        <el-button @click="router.push('/system/services?from=' + encodeURIComponent(route.fullPath))">
          <el-icon><Setting /></el-icon>服务托管（安装/配置）
        </el-button>
        <el-button :loading="loading" @click="refreshAll">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-alert v-if="!desktop" type="warning" :closable="false" class="mb"
      title="当前运行在浏览器环境"
      description="服务状态页依赖 Electron 桌面端的本机检测能力，请在桌面端打开。" />

    <template v-if="desktop">
      <div class="card mb">
        <div class="card-header">
          <h3>操作</h3>
          <div>
            <el-button type="primary" :loading="startingAll" @click="handleStartAll">
              <el-icon><VideoPlay /></el-icon>一键启动全部
            </el-button>
            <el-button :disabled="!hasRunning" @click="handleStopAll">
              <el-icon><VideoPause /></el-icon>停止(本程序启动的)
            </el-button>
          </div>
        </div>
        <el-descriptions v-if="status" :column="2" border size="small">
          <el-descriptions-item label="后端地址">{{ status.backendUrl }}</el-descriptions-item>
          <el-descriptions-item label="运行时目录">{{ status.runtimeDir }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-row :gutter="16" class="mb">
        <el-col :span="6" v-for="svc in services" :key="svc.name">
          <div class="svc-card">
            <div class="svc-title">
              <span>{{ nameMap[svc.name] }}</span>
              <el-tag :type="tagType(svc)" size="small">{{ tagText(svc) }}</el-tag>
            </div>
            <div class="svc-meta">
              <div class="svc-line" :title="svc.installPath">路径：{{ svc.installPath || '—' }}</div>
              <div class="svc-line">{{ svc.message }}</div>
            </div>
            <div class="svc-actions">
              <el-button
                v-if="svc.name !== 'jdk' && !svc.running"
                size="small" type="success" plain
                :loading="busy[svc.name]" @click="handleStart(svc.name)"
              >
                启动
              </el-button>
              <el-button
                v-if="svc.name !== 'jdk' && svc.running && !svc.ownedExternally"
                size="small" type="warning" plain
                :loading="busy[svc.name]" @click="handleStop(svc.name)"
              >
                停止
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <div class="card log-card">
        <div class="card-header">
          <h3>后端运行日志（每 3 秒自动刷新）</h3>
          <el-switch v-model="followTail" active-text="滚动到底部" />
        </div>
        <pre ref="logBoxRef" class="log-box">{{ logText || '（暂无日志：后端启动/输出后会实时出现在这里）' }}</pre>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Setting, Refresh, VideoPlay, VideoPause } from '@element-plus/icons-vue'
import { isElectron } from '@/api/hardware'
import { launcherAPI } from '@/api/launcher'

const desktop = isElectron()
const route = useRoute()
const router = useRouter()

const nameMap: Record<LauncherServiceName, string> = {
  jdk: 'JDK 17',
  mysql: 'MySQL 8',
  redis: 'Redis',
  backend: '后端 erp-api',
}

const fromPath = (() => {
  const from = route.query.from as string | undefined
  return from && from.startsWith('/') && !from.startsWith('//') ? from : ''
})()

const status = ref<LauncherStatus | null>(null)
const logText = ref('')
const busy = ref<Partial<Record<LauncherServiceName, boolean>>>({})
const startingAll = ref(false)
const loading = ref(false)
const followTail = ref(true)
const logBoxRef = ref<HTMLElement | null>(null)

let statusTimer: number | undefined
let logTimer: number | undefined

const services = computed<LauncherServiceStatus[]>(() => status.value?.services || [])
const hasRunning = computed(() => services.value.some((s) => s.running && !s.ownedExternally))
const backendUp = computed<boolean | null>(() => {
  const be = services.value.find((s) => s.name === 'backend')
  return be ? be.running : null
})

function tagType(svc: LauncherServiceStatus) {
  if (svc.running && !svc.ownedExternally) return 'success'
  if (svc.ownedExternally) return 'primary'
  if (!svc.installed) return 'info'
  return 'warning'
}

function tagText(svc: LauncherServiceStatus) {
  if (svc.ownedExternally) return '外部实例运行中'
  if (svc.running) return '运行中'
  if (!svc.installed) return '未安装'
  return '未运行'
}

async function refreshStatus() {
  const r = await launcherAPI.getStatus()
  if (r.ok && r.data) status.value = r.data
}

async function refreshLog() {
  const r = await launcherAPI.getLog('backend')
  if (!r.ok || !r.data) return
  logText.value = r.data.text || ''
  if (followTail.value) {
    await nextTick()
    const el = logBoxRef.value
    if (el) el.scrollTop = el.scrollHeight
  }
}

async function refreshAll() {
  loading.value = true
  try {
    await Promise.all([refreshStatus(), refreshLog()])
  } finally {
    loading.value = false
  }
}

async function handleStart(name: LauncherServiceName) {
  busy.value[name] = true
  try {
    const r = await launcherAPI.start(name)
    if (r.ok) ElMessage.success(r.message || '已启动')
    else ElMessage.error(r.reason || '启动失败')
  } finally {
    busy.value[name] = false
    refreshStatus()
  }
}

async function handleStop(name: LauncherServiceName) {
  busy.value[name] = true
  try {
    const r = await launcherAPI.stop(name)
    r.ok ? ElMessage.success(r.message || '已停止') : ElMessage.warning(r.reason || '停止失败')
  } finally {
    busy.value[name] = false
    refreshStatus()
    refreshLog()
  }
}

async function handleStartAll() {
  startingAll.value = true
  try {
    const r = await launcherAPI.startAll()
    const bad = (r.results || []).find((x) => !x.result.ok)
    if (!bad) ElMessage.success('全部服务已就绪')
    else ElMessage.error(`${nameMap[bad.name]} 启动失败：${bad.result.reason || '见日志'}`)
  } finally {
    startingAll.value = false
    refreshAll()
  }
}

async function handleStopAll() {
  const r = await launcherAPI.stopAll()
  r.ok ? ElMessage.success('已停止本程序启动的服务') : ElMessage.warning(r.reason || '停止失败')
  refreshStatus()
}

onMounted(() => {
  if (!desktop) return
  refreshAll()
  statusTimer = window.setInterval(refreshStatus, 5000)
  logTimer = window.setInterval(refreshLog, 3000)
})

onUnmounted(() => {
  if (statusTimer) clearInterval(statusTimer)
  if (logTimer) clearInterval(logTimer)
})
</script>

<style scoped lang="scss">
.status-page {
  padding: 16px;
  min-height: 100vh;
  box-sizing: border-box;
  background: #f0f2f5;
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
    display: flex;
    align-items: center;
  }
  .title-actions {
    display: flex;
    gap: 8px;
  }
}
.ml {
  margin-left: 8px;
}
.mb {
  margin-bottom: 16px;
}
.card {
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  h3 {
    margin: 0;
    font-size: 15px;
    color: #303133;
  }
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  > div {
    display: flex;
    gap: 8px;
    align-items: center;
  }
}
.svc-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px 16px;
  height: 150px;
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
  .svc-actions {
    display: flex;
    gap: 6px;
  }
}
.log-card {
  padding-bottom: 20px;
}
.log-box {
  margin: 0;
  height: calc(100vh - 480px);
  min-height: 240px;
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
</style>
