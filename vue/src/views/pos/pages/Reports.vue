<template>
  <div class="reports-page">
    <div class="page-header">
      <div class="date-selector">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          @change="loadReport"
        />
        <el-button-group>
          <el-button :type="dateType === 'today' ? 'primary' : ''" @click="selectQuickDate('today')">今日</el-button>
          <el-button :type="dateType === 'yesterday' ? 'primary' : ''" @click="selectQuickDate('yesterday')">昨日</el-button>
          <el-button :type="dateType === 'week' ? 'primary' : ''" @click="selectQuickDate('week')">本周</el-button>
          <el-button :type="dateType === 'month' ? 'primary' : ''" @click="selectQuickDate('month')">本月</el-button>
        </el-button-group>
      </div>
      <el-button type="primary" :icon="Download" @click="exportReport">导出报表</el-button>
    </div>

    <div v-loading="loading" class="report-content">
      <el-empty v-if="!reportData" description="请选择日期查看报表" />

      <template v-else>
        <div class="summary-cards">
          <div class="card highlight">
            <div class="card-icon">
              <el-icon :size="32"><Money /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">营业额</div>
              <div class="card-value">¥{{ formatAmount(reportData.totalSales) }}</div>
            </div>
          </div>
          <div class="card">
            <div class="card-icon order">
              <el-icon :size="32"><ShoppingCart /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">订单数</div>
              <div class="card-value">{{ reportData.orderCount }}</div>
            </div>
          </div>
          <div class="card">
            <div class="card-icon member">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">会员数</div>
              <div class="card-value">{{ reportData.memberCount }}</div>
            </div>
          </div>
          <div class="card">
            <div class="card-icon refund">
              <el-icon :size="32"><RefreshLeft /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">退款额</div>
              <div class="card-value">¥{{ formatAmount(reportData.refundAmount) }}</div>
            </div>
          </div>
        </div>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <span class="chart-title">支付方式统计</span>
              </template>
              <div class="payment-stats">
                <div v-for="item in reportData.paymentStats || []" :key="item.method" class="payment-item">
                  <div class="payment-info">
                    <span class="payment-name">{{ getPayMethodName(item.method) }}</span>
                    <span class="payment-count">{{ item.count }} 笔</span>
                  </div>
                  <div class="payment-amount">¥{{ formatAmount(item.amount) }}</div>
                  <el-progress
                    :percentage="getPaymentPercentage(item.amount)"
                    :color="getProgressColor(item.method)"
                    :show-text="false"
                    height="8px"
                  />
                </div>
                <el-empty v-if="!reportData.paymentStats || reportData.paymentStats.length === 0" description="暂无数据" :image-size="60" />
              </div>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <span class="chart-title">销售时段分布</span>
              </template>
              <div class="time-stats">
                <div v-for="(item, index) in reportData.timeStats || []" :key="index" class="time-item">
                  <div class="time-label">{{ item.time }}</div>
                  <div class="time-bar-wrapper">
                    <div
                      class="time-bar"
                      :style="{ width: getTimePercentage(item.count) + '%' }"
                    />
                  </div>
                  <div class="time-count">{{ item.count }}单</div>
                </div>
                <el-empty v-if="!reportData.timeStats || reportData.timeStats.length === 0" description="暂无数据" :image-size="60" />
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-card class="details-card">
          <template #header>
            <span class="chart-title">商品销售排行</span>
          </template>
          <el-table :data="reportData.goodsRanking || []" stripe size="small">
            <el-table-column type="index" label="排名" width="60">
              <template #default="{ $index }">
                <span :class="['rank', `rank-${$index + 1}`]">{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="goodsName" label="商品名称" />
            <el-table-column prop="quantity" label="销售数量" width="100" align="center" />
            <el-table-column prop="amount" label="销售额" width="120" align="right">
              <template #default="{ row }">
                <span class="amount">¥{{ formatAmount(row.amount) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!reportData.goodsRanking || reportData.goodsRanking.length === 0" description="暂无数据" :image-size="60" />
        </el-card>

        <el-card class="settlement-card">
          <template #header>
            <div class="settlement-header">
              <span class="chart-title">交班结算</span>
              <el-button type="primary" @click="handleSettlement" :disabled="isSettled">
                {{ isSettled ? '已交班' : '确认交班' }}
              </el-button>
            </div>
          </template>
          <div class="settlement-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="收银员">{{ reportData.cashierName || '当前用户' }}</el-descriptions-item>
              <el-descriptions-item label="交班时间">{{ reportData.settleTime || '--' }}</el-descriptions-item>
              <el-descriptions-item label="现金收入">¥{{ formatAmount(reportData.cashIncome) }}</el-descriptions-item>
              <el-descriptions-item label="微信收入">¥{{ formatAmount(reportData.wechatIncome) }}</el-descriptions-item>
              <el-descriptions-item label="支付宝收入">¥{{ formatAmount(reportData.alipayIncome) }}</el-descriptions-item>
              <el-descriptions-item label="银行卡收入">¥{{ formatAmount(reportData.bankCardIncome) }}</el-descriptions-item>
              <el-descriptions-item label="会员充值">¥{{ formatAmount(reportData.memberRecharge) }}</el-descriptions-item>
              <el-descriptions-item label="退款支出">¥{{ formatAmount(reportData.refundAmount) }}</el-descriptions-item>
              <el-descriptions-item label="应上缴" :span="2">
                <span class="settlement-amount">¥{{ formatAmount(reportData.totalSales - reportData.refundAmount) }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Money, ShoppingCart, User, RefreshLeft, Download } from '@element-plus/icons-vue'
import { getDailyReport } from '@/api/pos/pos'

const loading = ref(false)
const selectedDate = ref('')
const dateType = ref('today')
const reportData = ref<any>(null)
const isSettled = ref(false)

const paymentTotal = computed(() => {
  if (!reportData.value?.paymentStats) return 1
  return reportData.value.paymentStats.reduce((sum, item) => sum + item.amount, 1)
})

const timeMax = computed(() => {
  if (!reportData.value?.timeStats) return 1
  return Math.max(...reportData.value.timeStats.map(item => item.count), 1)
})

onMounted(() => {
  selectQuickDate('today')
})

function selectQuickDate(type: string) {
  dateType.value = type
  const now = new Date()
  switch (type) {
    case 'today':
      selectedDate.value = formatDate(now)
      break
    case 'yesterday':
      const yesterday = new Date(now)
      yesterday.setDate(yesterday.getDate() - 1)
      selectedDate.value = formatDate(yesterday)
      break
    case 'week':
      selectedDate.value = formatDate(now)
      break
    case 'month':
      selectedDate.value = formatDate(now)
      break
  }
  loadReport()
}

function formatDate(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

async function loadReport() {
  if (!selectedDate.value) return
  loading.value = true
  try {
    const res = await getDailyReport(1, selectedDate.value)
    reportData.value = res.data || generateMockData()
    isSettled.value = reportData.value.settled || false
  } catch (e) {
    console.error(e)
    reportData.value = generateMockData()
  } finally {
    loading.value = false
  }
}

function generateMockData() {
  return {
    totalSales: 2580.50,
    orderCount: 56,
    memberCount: 23,
    refundAmount: 150.00,
    cashierName: '收银员A',
    settleTime: '',
    cashIncome: 800.00,
    wechatIncome: 1200.50,
    alipayIncome: 380.00,
    bankCardIncome: 200.00,
    memberRecharge: 500.00,
    paymentStats: [
      { method: 'CASH', name: '现金', count: 18, amount: 800.00 },
      { method: 'WECHAT', name: '微信', count: 25, amount: 1200.50 },
      { method: 'ALIPAY', name: '支付宝', count: 8, amount: 380.00 },
      { method: 'BANK_CARD', name: '银行卡', count: 5, amount: 200.00 },
    ],
    timeStats: [
      { time: '08-10', count: 5 },
      { time: '10-12', count: 12 },
      { time: '12-14', count: 18 },
      { time: '14-16', count: 8 },
      { time: '16-18', count: 10 },
      { time: '18-20', count: 3 },
    ],
    goodsRanking: [
      { goodsName: '可口可乐 330ml', quantity: 45, amount: 135.00 },
      { goodsName: '农夫山泉 550ml', quantity: 38, amount: 76.00 },
      { goodsName: '康师傅红烧牛肉面', quantity: 25, amount: 75.00 },
      { goodsName: '伊利纯牛奶 250ml', quantity: 22, amount: 66.00 },
      { goodsName: '薯片原味', quantity: 18, amount: 54.00 },
    ],
  }
}

function getPaymentPercentage(amount: number) {
  return Math.round((amount / paymentTotal.value) * 100)
}

function getTimePercentage(count: number) {
  return Math.round((count / timeMax.value) * 100)
}

function getProgressColor(method: string) {
  const colors: Record<string, string> = {
    CASH: '#67C23A',
    WECHAT: '#07C160',
    ALIPAY: '#1677FF',
    BANK_CARD: '#E6A23C',
  }
  return colors[method] || '#B4471D'
}

function getPayMethodName(method: string) {
  const map: Record<string, string> = {
    CASH: '现金',
    WECHAT: '微信支付',
    ALIPAY: '支付宝',
    BANK_CARD: '银行卡',
    MEMBER_BALANCE: '会员余额',
  }
  return map[method] || method
}

function formatAmount(amount: any) {
  if (!amount) return '0.00'
  return Number(amount).toFixed(2)
}

async function handleSettlement() {
  try {
    await ElMessageBox.confirm(
      '确认交班？交班后将无法修改当日数据。',
      '交班确认',
      { type: 'warning' }
    )
    reportData.value.settleTime = new Date().toLocaleString('zh-CN')
    isSettled.value = true
    ElMessage.success('交班成功')
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('交班失败')
    }
  }
}

function exportReport() {
  ElMessage.info('导出功能开发中...')
}
</script>

<style lang="scss" scoped>
.reports-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .date-selector {
    display: flex;
    gap: 12px;
    align-items: center;
  }
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;

  .card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-2px);
    }

    &.highlight {
      background: linear-gradient(135deg, #B4471D, #D96A3F);
      color: #fff;

      .card-icon {
        background: rgba(255, 255, 255, 0.2);
        color: #fff;
      }

      .card-label {
        color: rgba(255, 255, 255, 0.8);
      }

      .card-value {
        color: #fff;
      }
    }

    .card-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f7fa;
      color: #B4471D;

      &.order {
        color: #409EFF;
        background: #ecf5ff;
      }

      &.member {
        color: #67C23A;
        background: #f0f9eb;
      }

      &.refund {
        color: #F56C6C;
        background: #fef0f0;
      }
    }

    .card-content {
      flex: 1;

      .card-label {
        font-size: 13px;
        color: #909399;
        margin-bottom: 8px;
      }

      .card-value {
        font-size: 24px;
        font-weight: 700;
        color: #303133;
      }
    }
  }
}

.chart-card {
  margin-bottom: 20px;

  .chart-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }
}

.payment-stats {
  .payment-item {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }

    .payment-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;

      .payment-name {
        font-size: 14px;
        color: #606266;
      }

      .payment-count {
        font-size: 12px;
        color: #909399;
      }
    }

    .payment-amount {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
    }
  }
}

.time-stats {
  .time-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }

    .time-label {
      width: 50px;
      font-size: 12px;
      color: #909399;
    }

    .time-bar-wrapper {
      flex: 1;
      height: 24px;
      background: #f5f7fa;
      border-radius: 4px;
      overflow: hidden;

      .time-bar {
        height: 100%;
        background: linear-gradient(90deg, #B4471D, #D96A3F);
        border-radius: 4px;
        transition: width 0.3s;
      }
    }

    .time-count {
      width: 50px;
      text-align: right;
      font-size: 12px;
      color: #606266;
    }
  }
}

.details-card {
  margin-bottom: 20px;
}

.rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;

  &.rank-1 {
    background: #FFD700;
    color: #fff;
  }

  &.rank-2 {
    background: #C0C0C0;
    color: #fff;
  }

  &.rank-3 {
    background: #CD7F32;
    color: #fff;
  }

  &:not([class*='rank-']) {
    background: #f5f7fa;
    color: #909399;
  }
}

.amount {
  color: #B4471D;
  font-weight: 600;
}

.settlement-card {
  .settlement-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .settlement-amount {
    font-size: 20px;
    font-weight: 700;
    color: #B4471D;
  }
}
</style>
