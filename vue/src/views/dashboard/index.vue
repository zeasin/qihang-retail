<template>
  <div class="page-container">
    <!-- 欢迎栏 -->
    <div class="welcome-card card">
      <div class="welcome-left">
        <h2>{{ greeting }}，{{ userStore.name || '管理员' }}</h2>
        <p>{{ currentDate }} {{ currentTime }} · 欢迎使用启航零售ERP</p>
      </div>
      <div class="welcome-right">
        <el-button type="primary" size="large" @click="goPath('/pos/cashier')">
          <el-icon><Money /></el-icon>
          <span>开始收银</span>
        </el-button>
        <el-button :icon="Refresh" size="large" @click="loadStats" :loading="loading" />
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row" v-loading="loading">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #409eff">
            <el-icon :size="28"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatAmount(stats.todaySalesAmount) }}</div>
            <div class="stat-label">今日销售额</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #67c23a">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayOrderCount ?? 0 }}</div>
            <div class="stat-label">今日订单数</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #e6a23c">
            <el-icon :size="28"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.memberCount ?? 0 }}</div>
            <div class="stat-label">会员总数</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #f56c6c">
            <el-icon :size="28"><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value" :class="{ 'text-danger': (stats.lowStockCount ?? 0) > 0 }">
              {{ stats.lowStockCount ?? 0 }}
            </div>
            <div class="stat-label">库存预警</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 近7天销售趋势 -->
      <el-col :span="16">
        <div class="card trend-card">
          <div class="card-header">
            <h3 class="card-title">近7天销售趋势</h3>
            <span class="card-sub">单位：元</span>
          </div>
          <div class="chart-container" v-loading="loading">
            <template v-if="trendBars.length">
              <div class="bar" v-for="item in trendBars" :key="item.date">
                <div class="bar-amount">¥{{ formatAmount(item.amount) }}</div>
                <div class="bar-fill" :style="{ height: item.height + '%' }"></div>
                <div class="bar-label">{{ item.label }}</div>
              </div>
            </template>
            <el-empty v-else description="暂无销售数据" :image-size="80" />
          </div>
        </div>
      </el-col>

      <!-- 快捷操作 -->
      <el-col :span="8">
        <div class="card quick-card">
          <h3 class="card-title">快捷操作</h3>
          <div class="quick-grid">
            <div class="quick-item" @click="goPath('/pos/cashier')">
              <div class="quick-icon" style="background-color: #409eff"><el-icon><Money /></el-icon></div>
              <span>POS收银</span>
            </div>
            <div class="quick-item" @click="goPath('/pos/orders')">
              <div class="quick-icon" style="background-color: #909399"><el-icon><List /></el-icon></div>
              <span>订单查询</span>
            </div>
            <div class="quick-item" @click="goPath('/pos/members')">
              <div class="quick-icon" style="background-color: #f56c6c"><el-icon><User /></el-icon></div>
              <span>会员管理</span>
            </div>
            <div class="quick-item" @click="goPath('/pos/refund')">
              <div class="quick-icon" style="background-color: #e6a23c"><el-icon><RefreshLeft /></el-icon></div>
              <span>退款管理</span>
            </div>
            <div class="quick-item" @click="goPath('/pos/reports')">
              <div class="quick-icon" style="background-color: #9c27b0"><el-icon><DataAnalysis /></el-icon></div>
              <span>交班结算</span>
            </div>
            <div class="quick-item" @click="goPath('/system/hardware')">
              <div class="quick-icon" style="background-color: #67c23a"><el-icon><Setting /></el-icon></div>
              <span>硬件设置</span>
            </div>
            <div class="quick-item" @click="goPath('/system/services')">
              <div class="quick-icon" style="background-color: #409eff; opacity: 0.85"><el-icon><Cpu /></el-icon></div>
              <span>服务托管</span>
            </div>
            <div class="quick-item" @click="goPath('/system/status')">
              <div class="quick-icon" style="background-color: #e6a23c"><el-icon><DataAnalysis /></el-icon></div>
              <span>服务状态</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近订单 -->
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">最近POS订单</h3>
        <el-button link type="primary" @click="goPath('/pos/orders')">查看全部</el-button>
      </div>
      <el-table :data="stats.recentOrders" border stripe v-loading="loading" size="default">
        <el-table-column prop="orderNum" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ formatAmount(row.payment ?? row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="orderStatusType(row.orderStatus)" size="small">
              {{ orderStatusText(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.orderTime || row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createBy" label="操作员" width="120" />
      </el-table>
      <el-empty
        v-if="!loading && !stats.recentOrders?.length"
        description="暂无订单，去收银台开一单吧"
        :image-size="60"
      />
    </div>

    <!-- 系统信息 -->
    <div class="card">
      <h3 class="card-title">系统信息</h3>
      <el-descriptions :column="2" border>
        <template v-if="desktop">
          <el-descriptions-item label="应用版本">{{ versions?.app || 'v1.0.0' }}</el-descriptions-item>
          <el-descriptions-item label="运行环境">Electron {{ versions?.electron || '-' }}</el-descriptions-item>
          <el-descriptions-item label="平台">{{ versions?.platform || '-' }}</el-descriptions-item>
          <el-descriptions-item label="数据库">MySQL 8</el-descriptions-item>
        </template>
        <template v-else>
          <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
          <el-descriptions-item label="运行环境">Web 浏览器</el-descriptions-item>
          <el-descriptions-item label="后端接口">{{ backendLabel }}</el-descriptions-item>
          <el-descriptions-item label="数据库">MySQL 8</el-descriptions-item>
        </template>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Refresh,
  RefreshLeft,
  Money,
  Document,
  User,
  Warning,
  List,
  DataAnalysis,
  Setting,
  Cpu
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { getPosDashboardStats } from '@/api/pos/pos'
import { hardwareAPI, isElectron } from '@/api/hardware'

interface TrendItem {
  date: string
  amount: number
  count: number
}

interface RecentOrder {
  id: string
  orderNum: string
  payment?: number
  amount?: number
  orderStatus: number
  orderTime?: string
  createTime?: string
  createBy?: string
}

interface DashboardStats {
  todaySalesAmount?: number
  todayOrderCount?: number
  memberCount?: number
  lowStockCount?: number
  salesTrend?: TrendItem[]
  recentOrders?: RecentOrder[]
}

const router = useRouter()
const userStore = useUserStore()
const desktop = isElectron()
const backendLabel = import.meta.env.VITE_APP_BASE_API || '/api'
const loading = ref(false)
const stats = ref<DashboardStats>({})
const versions = ref<AppVersions | null>(null)

const now = ref(new Date())
let timer: ReturnType<typeof setInterval> | null = null

const greeting = computed(() => {
  const h = now.value.getHours()
  if (h < 6) return '凌晨好'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  const d = now.value
  const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${week}`
})

const currentTime = computed(() => {
  const d = now.value
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
})

const trendBars = computed(() => {
  const list = stats.value.salesTrend || []
  const max = Math.max(...list.map((i) => i.amount || 0), 1)
  return list.map((i) => ({
    date: i.date,
    amount: i.amount || 0,
    height: Math.round(((i.amount || 0) / max) * 100),
    label: (i.date || '').slice(5)
  }))
})

function formatAmount(v?: number) {
  return (v ?? 0).toFixed(2)
}

function formatDateTime(t?: string) {
  if (!t) return '-'
  return t.replace('T', ' ').slice(0, 19)
}

function orderStatusText(s: number) {
  const map: Record<number, string> = {
    0: '新订单', 1: '待发货', 2: '已发货', 3: '已完成',
    11: '已取消', 12: '退款中', 13: '已关闭', 21: '待付款'
  }
  return map[s] || '未知'
}

function orderStatusType(s: number): 'success' | 'warning' | 'info' | 'danger' | 'primary' {
  if (s === 3) return 'success'
  if (s === 0 || s === 21) return 'warning'
  if (s === 11 || s === 13) return 'danger'
  if (s === 12) return 'warning'
  if (s === 1) return 'warning'
  return 'primary'
}

function goPath(path: string) {
  router.push(path)
}

async function loadStats() {
  loading.value = true
  try {
    const res: any = await getPosDashboardStats()
    stats.value = res?.data || {}
  } catch {
    stats.value = {}
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
  if (desktop) {
    hardwareAPI.getVersion().then((r) => {
      if (r.ok) versions.value = r.data || null
    })
  }
  timer = setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 16px;
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.welcome-card {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .welcome-left {
    h2 {
      margin: 0 0 8px 0;
      color: #303133;
      font-size: 22px;
    }

    p {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .welcome-right {
    display: flex;
    gap: 12px;
  }
}

.stat-row {
  margin-bottom: 0;

  .el-col {
    margin-bottom: 20px;
  }
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    margin-right: 16px;
  }

  .stat-info {
    flex: 1;

    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
    }
  }
}

.text-danger {
  color: #f56c6c !important;
}

.trend-card {
  height: calc(100% - 20px);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  .card-sub {
    font-size: 12px;
    color: #909399;
  }
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.chart-container {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  gap: 12px;
  height: 240px;
  padding-top: 20px;

  .bar {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-end;
    height: 100%;

    .bar-amount {
      font-size: 12px;
      color: #606266;
      margin-bottom: 6px;
      white-space: nowrap;
    }

    .bar-fill {
      width: 60%;
      min-height: 4px;
      background: linear-gradient(180deg, #409eff 0%, #79bbff 100%);
      border-radius: 4px 4px 0 0;
      transition: height 0.4s ease;
    }

    .bar-label {
      font-size: 12px;
      color: #909399;
      margin-top: 8px;
    }
  }
}

.quick-card {
  height: calc(100% - 20px);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;

  .quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 16px 8px;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.2s;
    gap: 8px;

    &:hover {
      background-color: #f5f7fa;
    }

    .quick-icon {
      width: 44px;
      height: 44px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 22px;
    }

    span {
      font-size: 13px;
      color: #606266;
    }
  }
}

.amount {
  color: #f56c6c;
  font-weight: 500;
}
</style>
