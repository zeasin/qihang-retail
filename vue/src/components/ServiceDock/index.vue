<!--
  桌面端全局「服务坞」：任何页面右下角常驻。
  - 收起态：状态灯（绿=后端可达 / 红=未启动 / 灰=探测中），20s 轻量轮询
  - 展开态：四个服务状态与启停、后端 Java 实时日志尾（3s 轮询）、一键启动全部/停止、跳转完整服务托管页
  仅在 Electron 环境由 App.vue 挂载；服务托管页本身打开时自动隐藏。
-->
<template>
  <div class="svc-dock" :class="{ open: expanded }">
    <!-- 展开面板 -->
    <div v-if="expanded" class="dock-panel">
      <div class="panel-head">
        <span class="panel-title">服务状态</span>
        <el-tag :type="backendUp ? 'success' : 'danger'" size="small" effect="dark">
          后端 {{ backendUp ? '运行中' : '未启动' }}
        </el-tag>
        <div class="panel-head-actions">
          <el-button link title="打开独立服务状态页" @click="openStatusPage">
            <el-icon><DataAnalysis /></el-icon>
          </el-button>
          <el-button link title="打开完整服务托管页" @click="openFullPage">
            <el-icon><FullScreen /></el-icon>
          </el-button>
          <el-button link title="收起" @click="expanded = false">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="panel-toolbar">
        <el-button size="small" type="primary" plain :loading="startingAll" @click="handleStartAll">
          一键启动全部
        </el-button>
        <el-button size="small" plain :disabled="!hasRunning" @click="handleStopAll">
          停止(本程序启动的)
        </el-button>
        <el-button size="small" text :loading="statusLoading" @click="refreshStatus">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>

      <div class="svc-list">
        <div v-for="svc in services" :key="svc.name" class="svc-row">
          <span class="svc-name">{{ nameMap[svc.name] }}</span>
          <el-tag :type="tagType(svc)" size="small">{{ tagText(svc) }}</el-tag>
          <template v-if="svc.name !== 'jdk'">
            <el-button
              v-if="!svc.running"
              size="small"
              link
              type="success"
              :loading="busy[svc.name]"
              @click="handleStart(svc.name)"
            >
              启动
            </el-button>
            <el-button
              v-else-if="!svc.ownedExternally"
              size="small"
              link
              type="warning"
              :loading="busy[svc.name]"
              @click="handleStop(svc.name)"
            >
              停止
            </el-button>
          </template>
        </div>
      </div>

      <div class="log-head">
        <span>后端运行日志（最近 {{ LOG_LINES }} 行，每 3 秒刷新）</span>
        <el-switch v-model="showLog" size="small" />
      </div>
      <pre v-show="showLog" class="log-box">{{ logText || '（暂无日志，启动后端后这里会实时滚动）' }}</pre>
    </div>

    <!-- 收起态悬浮球 -->
    <div class="dock-fab" :title="expanded ? '收起' : '查看服务状态'" @click="toggle">
      <span class="dot" :class="dotClass" />
      <span v-if="!expanded" class="fab-text">服务</span>
      <el-icon v-else><ArrowDown /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Close, Refresh, FullScreen, ArrowDown, DataAnalysis } from '@element-plus/icons-vue'
import { launcherAPI } from '@/api/launcher'

const LOG_LINES = 200

const route = useRoute()
const router = useRouter()

const nameMap: Record<LauncherServiceName, string> = {
  jdk: 'JDK',
  mysql: 'MySQL',
  redis: 'Redis',
  backend: '后端 erp-api',
}

const expanded = ref(false)
const backendUp = ref<boolean | null>(null) // null = 还没探测
const status = ref<LauncherStatus | null>(null)
const logText = ref('')
const showLog = ref(true)
const busy = ref<Partial<Record<LauncherServiceName, boolean>>>({})
const startingAll = ref(false)
const statusLoading = ref(false)

let collapsedTimer: number | undefined
let expandedTimer: number | undefined
let logTimer: number | undefined

const services = computed<LauncherServiceStatus[]>(() => status.value?.services || [])
const hasRunning = computed(() => services.value.some((s) => s.running && !s.ownedExternally))

const dotClass = computed(() =>
  backendUp.value === null ? 'dot-unknown' : backendUp.value ? 'dot-up' : 'dot-down'
)

function tagType(svc: LauncherServiceStatus) {
  if (svc.running && !svc.ownedExternally) return 'success'
  if (svc.ownedExternally) return 'primary'
  if (!svc.installed) return 'info'
  return 'warning'
}

function tagText(svc: LauncherServiceStatus) {
  if (svc.ownedExternally) return '外部运行'
  if (svc.running) return '运行中'
  if (!svc.installed) return '未安装'
  return '未运行'
}

/** 收起态轻量探测：只查后端可达性 */
async function probe() {
  try {
    const r = await launcherAPI.checkBackend()
    if (r.ok && r.data) backendUp.value = r.data.reachable
  } catch {
    /* 探测失败保持原状态 */
  }
}

async function refreshStatus() {
  statusLoading.value = true
  try {
    const r = await launcherAPI.getStatus()
    if (r.ok && r.data) {
      status.value = r.data
      const be = r.data.services.find((s) => s.name === 'backend')
      if (be) backendUp.value = be.running
    }
  } finally {
    statusLoading.value = false
  }
}

async function refreshLog() {
  const r = await launcherAPI.getLog('backend')
  if (r.ok && r.data) logText.value = (r.data.text || '').split('\n').slice(-LOG_LINES).join('\n')
}

function toggle() {
  expanded.value = !expanded.value
  if (expanded.value) {
    refreshStatus()
    refreshLog()
    startExpandedTimers()
  } else {
    stopExpandedTimers()
    probe()
  }
}

function startExpandedTimers() {
  stopExpandedTimers()
  expandedTimer = window.setInterval(refreshStatus, 5000)
  logTimer = window.setInterval(refreshLog, 3000)
}

function stopExpandedTimers() {
  if (expandedTimer) clearInterval(expandedTimer)
  if (logTimer) clearInterval(logTimer)
  expandedTimer = undefined
  logTimer = undefined
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
    else {
      ElMessage.error(`${nameMap[bad.name]} 启动失败：${bad.result.reason || '见日志'}`)
      showLog.value = true
    }
  } finally {
    startingAll.value = false
    refreshStatus()
    refreshLog()
  }
}

async function handleStopAll() {
  const r = await launcherAPI.stopAll()
  r.ok ? ElMessage.success('已停止本程序启动的服务') : ElMessage.warning(r.reason || '停止失败')
  refreshStatus()
}

function openFullPage() {
  const from = route.fullPath
  router.push(from.startsWith('/system/services') ? '/system/services' : { path: '/system/services', query: { from } })
}

function openStatusPage() {
  const from = route.fullPath
  router.push(from.startsWith('/system/status') ? '/system/status' : { path: '/system/status', query: { from } })
}

/** 完整服务托管页/独立状态页打开时收起悬浮面板，避免重复界面 */
watch(
  () => route.path,
  (p) => {
    if (p.startsWith('/system/services') || p.startsWith('/system/status')) expanded.value = false
  }
)

onMounted(() => {
  probe()
  collapsedTimer = window.setInterval(() => {
    if (!expanded.value) probe()
  }, 20000)
})

onUnmounted(() => {
  if (collapsedTimer) clearInterval(collapsedTimer)
  stopExpandedTimers()
})
</script>

<style scoped lang="scss">
.svc-dock {
  position: fixed;
  right: 18px;
  bottom: 78px;
  z-index: 3000;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}
.dock-fab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.18);
  cursor: pointer;
  user-select: none;
  font-size: 13px;
  color: #303133;
  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.24);
  }
  .dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    flex: none;
  }
  .dot-up {
    background: #22c55e;
    box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.18);
  }
  .dot-down {
    background: #ef4444;
    box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.18);
  }
  .dot-unknown {
    background: #c0c4cc;
  }
}
.dock-panel {
  width: 400px;
  max-height: calc(100vh - 140px);
  overflow: auto;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.22);
  padding: 12px 14px;
}
.panel-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  .panel-title {
    font-weight: 600;
    font-size: 14px;
  }
  .panel-head-actions {
    margin-left: auto;
    display: flex;
    gap: 2px;
  }
}
.panel-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}
.svc-list {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 10px;
  .svc-row {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 7px 10px;
    & + .svc-row {
      border-top: 1px solid #f5f5f5;
    }
    .svc-name {
      font-size: 13px;
      color: #303133;
      flex: 1;
    }
  }
}
.log-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}
.log-box {
  margin: 0;
  height: 200px;
  overflow: auto;
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 8px 10px;
  border-radius: 6px;
  font-size: 11px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
