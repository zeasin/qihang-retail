<template>
  <div class="pos-header">
    <div class="header-left">
      <span class="page-title">{{ currentTitle }}</span>
    </div>
    <div class="header-center">
      <span class="time">{{ currentTime }}</span>
      <span class="date">{{ currentDate }}</span>
    </div>
    <div class="header-right">
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
import { useRoute } from 'vue-router'
import { getTodayStats } from '@/api/pos/pos'

const route = useRoute()
const currentPath = computed(() => route.path)

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
  '/pos/reports': '交班结算',
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
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
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
  .page-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
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
  gap: 24px;

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
