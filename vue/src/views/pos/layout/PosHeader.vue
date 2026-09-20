<template>
  <div class="pos-header">
    <div class="header-left">
      <span class="page-title">{{ currentTitle }}</span>
      <el-tooltip content="硬件设置" placement="bottom" v-if="desktop">
        <el-button class="gear-btn" link @click="goHardware">
          <el-icon :size="18"><Setting /></el-icon>
        </el-button>
      </el-tooltip>
    </div>
    <div class="header-center">
      <span class="time">{{ currentTime }}</span>
      <span class="date">{{ currentDate }}</span>
    </div>
    <div class="header-right">
      <el-tag :type="online ? 'success' : 'danger'" size="small" class="net-tag">
        {{ online ? '网络正常' : '网络断开' }}
      </el-tag>
      <div class="stat" v-if="currentPath === '/pos/cashier'">
        <span class="stat-label">今日营业额</span>
        <span class="stat-value">¥{{ todaySales.toFixed(2) }}</span>
      </div>
      <div class="stat" v-if="currentPath === '/pos/cashier'">
        <span class="stat-label">今日订单</span>
        <span class="stat-value">{{ todayOrders }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Setting } from '@element-plus/icons-vue'
import { getTodayStats } from '@/api/pos/pos'
import { hardwareAPI, isElectron } from '@/api/hardware'

const route = useRoute()
const router = useRouter()
const currentPath = computed(() => route.path)
const desktop = isElectron()

/** 桌面端齿轮入口：带 from 参数，硬件页可返回来源页面 */
function goHardware() {
  router.push({ path: '/system/hardware', query: { from: route.fullPath } })
}

const online = ref(navigator.onLine)
let offNetwork: (() => void) | null = null

const currentTime = ref('')
const currentDate = ref('')
const todaySales = ref(0)
const todayOrders = ref(0)

let timer: number

const titleMap: Record<string, string> = {
  '/pos/cashier': '收银台',
  '/pos/orders': '订单查询',
  '/pos/members': '会员管理',
  '/pos/refund': '退款管理',
  '/pos/reports': '业绩统计',
}

const currentTitle = computed(() => titleMap[currentPath.value] || 'POS系统')

function formatTime() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
  currentDate.value = now.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', weekday: 'short' })
}

async function loadStats() {
  try {
    const res = await getTodayStats(1)
    if (res && res.data) {
      todaySales.value = res.data.totalSales || 0
      todayOrders.value = res.data.orderCount || 0
    }
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

onMounted(() => {
  formatTime()
  timer = window.setInterval(formatTime, 1000)
  loadStats()
  offNetwork = hardwareAPI.onNetworkStatus((v) => (online.value = v))
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  offNetwork?.()
  offNetwork = null
})

defineExpose({
  refreshStats: loadStats
})
</script>

<style lang="scss" scoped>
.pos-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;

  .page-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }

  .gear-btn {
    color: #909399;

    &:hover {
      color: #B4471D;
    }
  }
}

.header-center {
  display: flex;
  align-items: center;
  gap: 16px;

  .time {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    font-variant-numeric: tabular-nums;
  }

  .date {
    font-size: 13px;
    color: #909399;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;

  .net-tag {
    flex-shrink: 0;
  }

  .stat {
    display: flex;
    flex-direction: column;
    align-items: flex-end;

    .stat-label {
      font-size: 12px;
      color: #909399;
    }

    .stat-value {
      font-size: 16px;
      font-weight: 600;
      color: #B4471D;
    }
  }
}
</style>
