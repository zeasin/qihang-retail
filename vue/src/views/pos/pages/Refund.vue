<template>
  <div class="refund-page">
    <div class="page-header">
      <div class="stats-card">
        <div class="stat-item">
          <div class="stat-label">可退款订单</div>
          <div class="stat-value">{{ refundableCount }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">已退款金额</div>
          <div class="stat-value refund">¥{{ totalRefunded.toFixed(2) }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">待处理</div>
          <div class="stat-value pending">{{ pendingCount }}</div>
        </div>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索订单号..."
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
      />
      <el-select v-model="statusFilter" placeholder="退款状态" clearable>
        <el-option label="待退款" value="PENDING" />
        <el-option label="已退款" value="REFUNDED" />
        <el-option label="退款失败" value="FAILED" />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button @click="handleReset">
        <el-icon><Refresh /></el-icon>
        重置
      </el-button>
    </div>

    <el-table v-loading="loading" :data="refundList" stripe>
      <el-table-column prop="orderNo" label="订单号" width="180">
        <template #default="{ row }">
          <span class="order-no">{{ row.orderNo || row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180" />
      <el-table-column prop="memberName" label="会员" width="120">
        <template #default="{ row }">
          {{ row.memberName || '散客' }}
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="订单金额" width="120" align="right">
        <template #default="{ row }">
          <span class="amount">¥{{ formatAmount(row.totalAmount || row.payAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="refundAmount" label="可退金额" width="120" align="right">
        <template #default="{ row }">
          <span class="refund-amount">¥{{ formatAmount(row.totalAmount || row.payAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="payMethod" label="原支付方式" width="100">
        <template #default="{ row }">
          {{ getPayMethodName(row.payMethod) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="canRefund(row)"
            type="warning"
            size="small"
            @click="openRefundDialog(row)"
          >
            办理退款
          </el-button>
          <span v-else class="text-muted">不可退</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadRefunds"
        @current-change="loadRefunds"
      />
    </div>

    <el-dialog v-model="showRefundDialog" title="办理退款" width="450px">
      <div class="refund-info" v-if="currentOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo || currentOrder.id }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">
            <span class="amount">¥{{ formatAmount(currentOrder.totalAmount || currentOrder.payAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="原支付方式">{{ getPayMethodName(currentOrder.payMethod) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <el-form :model="refundForm" label-width="100px">
          <el-form-item label="退款金额" required>
            <el-input-number
              v-model="refundForm.amount"
              :min="0.01"
              :max="refundForm.maxAmount"
              :precision="2"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="退款方式" required>
            <el-select v-model="refundForm.method" style="width: 100%">
              <el-option label="原路退回" value="ORIGINAL" />
              <el-option label="现金" value="CASH" />
              <el-option label="退回余额" value="BALANCE" />
            </el-select>
          </el-form-item>
          <el-form-item label="退款原因" required>
            <el-select v-model="refundForm.reason" style="width: 100%">
              <el-option label="商品质量问题" value="QUALITY" />
              <el-option label="不想要了" value="NO_NEED" />
              <el-option label="商品描述不符" value="MISMATCH" />
              <el-option label="其他原因" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="refundForm.remark" type="textarea" :rows="2" placeholder="选填" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showRefundDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getOrderList, refundOrder } from '@/api/pos/pos'

const loading = ref(false)
const refundList = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const searchKeyword = ref('')
const dateRange = ref<string[]>([])
const statusFilter = ref('')

const refundableCount = ref(0)
const totalRefunded = ref(0)
const pendingCount = ref(0)

const showRefundDialog = ref(false)
const currentOrder = ref<any>(null)

const refundForm = ref({
  amount: 0,
  method: 'ORIGINAL',
  reason: '',
  remark: '',
  orderId: null as number | null,
  maxAmount: 0,
})

onMounted(() => {
  loadRefunds()
  loadStats()
})

async function loadRefunds() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: 1,
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getOrderList(params)
    refundList.value = res.rows || res.data || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function loadStats() {
  refundableCount.value = refundList.value.length
  totalRefunded.value = 0
  pendingCount.value = refundList.value.filter(o => o.status === 1).length
}

function handleSearch() {
  pageNum.value = 1
  loadRefunds()
}

function handleReset() {
  searchKeyword.value = ''
  dateRange.value = []
  statusFilter.value = ''
  pageNum.value = 1
  loadRefunds()
}

function canRefund(row: any) {
  return row.status === 1 || row.status === 'PAID' || row.status === 'COMPLETED'
}

function openRefundDialog(row: any) {
  const maxAmount = row.totalAmount || row.payAmount || 0
  refundForm.value = {
    amount: maxAmount,
    method: 'ORIGINAL',
    reason: '',
    remark: '',
    orderId: row.id,
    maxAmount,
  }
  currentOrder.value = row
  showRefundDialog.value = true
}

async function confirmRefund() {
  if (!refundForm.value.reason) {
    ElMessage.warning('请选择退款原因')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认退款 ¥${refundForm.value.amount.toFixed(2)}？`,
      '退款确认',
      { type: 'warning' }
    )
    await refundOrder({
      orderId: refundForm.value.orderId,
      refundAmount: refundForm.value.amount,
      refundMethod: refundForm.value.method,
      refundReason: refundForm.value.reason,
      remark: refundForm.value.remark,
    })
    ElMessage.success('退款申请已提交')
    showRefundDialog.value = false
    loadRefunds()
    loadStats()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('退款失败')
    }
  }
}

function formatAmount(amount: any) {
  if (!amount) return '0.00'
  return Number(amount).toFixed(2)
}

function getPayMethodName(method: string) {
  const map: Record<string, string> = {
    CASH: '现金',
    WECHAT: '微信',
    ALIPAY: '支付宝',
    BANK_CARD: '银行卡',
    MEMBER_BALANCE: '会员余额',
  }
  return map[method] || method || '-'
}

function getStatusName(status: number | string) {
  const map: Record<string, string> = {
    0: '待支付',
    1: '已支付',
    2: '已完成',
    3: '已取消',
    4: '已退款',
    PENDING: '待退款',
    PAID: '可退款',
    COMPLETED: '可退款',
    REFUNDED: '已退款',
    FAILED: '退款失败',
  }
  return map[String(status)] || status || '-'
}

function getStatusType(status: number | string) {
  const map: Record<string, string> = {
    0: 'info',
    1: 'warning',
    2: 'warning',
    3: 'info',
    4: 'success',
    PENDING: 'warning',
    PAID: 'warning',
    COMPLETED: 'warning',
    REFUNDED: 'success',
    FAILED: 'danger',
  }
  return map[String(status)] || 'info'
}
</script>

<style lang="scss" scoped>
.refund-page {
  padding: 0;
}

.page-header {
  margin-bottom: 16px;

  .stats-card {
    display: flex;
    gap: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .stat-item {
      flex: 1;
      text-align: center;

      .stat-label {
        font-size: 13px;
        color: #909399;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: #303133;

        &.refund {
          color: #F56C6C;
        }

        &.pending {
          color: #E6A23C;
        }
      }
    }
  }
}

.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;

  .el-input {
    width: 240px;
  }

  .el-select {
    width: 140px;
  }
}

.order-no {
  color: #B4471D;
  font-weight: 500;
}

.amount {
  color: #303133;
  font-weight: 600;
}

.refund-amount {
  color: #F56C6C;
  font-weight: 600;
}

.text-muted {
  color: #c0c4cc;
  font-size: 12px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.refund-info {
  margin-bottom: 16px;
}
</style>
