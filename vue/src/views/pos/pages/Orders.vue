<template>
  <div class="orders-page">
    <div class="page-header">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索订单号/会员..."
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
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="orderList" stripe>
      <el-table-column prop="orderNo" label="订单号" width="180">
        <template #default="{ row }">
          <span class="order-no" @click="viewDetail(row)">{{ row.orderNo || row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180" />
      <el-table-column prop="memberName" label="会员" width="120">
        <template #default="{ row }">
          {{ row.memberName || '散客' }}
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="120" align="right">
        <template #default="{ row }">
          <span class="amount">¥{{ formatAmount(row.totalAmount || row.payAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="payMethod" label="支付方式" width="100">
        <template #default="{ row }">
          <el-tag :type="getPayMethodType(row.payMethod)">{{ getPayMethodName(row.payMethod) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="viewDetail(row)">查看</el-button>
          <el-button
            v-if="canRefund(row)"
            type="warning"
            link
            size="small"
            @click="handleRefund(row)"
          >
            退款
          </el-button>
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
        @size-change="loadOrders"
        @current-change="loadOrders"
      />
    </div>

    <el-dialog v-model="showDetailDialog" title="订单详情" width="600px">
      <div class="order-detail" v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo || currentOrder.id }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="会员">{{ currentOrder.memberName || '散客' }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ getPayMethodName(currentOrder.payMethod) }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">
            <span class="amount">¥{{ formatAmount(currentOrder.totalAmount || currentOrder.payAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusName(currentOrder.status) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <h4 class="items-title">商品明细</h4>
        <el-table :data="currentOrder.items || currentOrder.orderItems" size="small">
          <el-table-column prop="goodsName" label="商品名称" />
          <el-table-column prop="price" label="单价" width="100" align="right">
            <template #default="{ row }">¥{{ formatAmount(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column label="小计" width="100" align="right">
            <template #default="{ row }">¥{{ formatAmount((row.price || 0) * row.quantity) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog v-model="showRefundDialog" title="退款" width="400px">
      <el-form :model="refundForm" label-width="100px">
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.amount" :min="0" :max="refundForm.maxAmount" :precision="2" />
        </el-form-item>
        <el-form-item label="退款方式">
          <el-select v-model="refundForm.method">
            <el-option label="原路退回" value="ORIGINAL" />
            <el-option label="现金" value="CASH" />
            <el-option label="余额" value="BALANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款原因">
          <el-input v-model="refundForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRefundDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getOrderList, getOrder, refundOrder } from '@/api/pos/pos'

const loading = ref(false)
const orderList = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const searchKeyword = ref('')
const dateRange = ref<string[]>([])

const showDetailDialog = ref(false)
const showRefundDialog = ref(false)
const currentOrder = ref<any>(null)

const refundForm = ref({
  amount: 0,
  method: 'ORIGINAL',
  reason: '',
  orderId: null as number | null,
  maxAmount: 0,
})

onMounted(() => {
  loadOrders()
})

async function loadOrders() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getOrderList(params)
    orderList.value = res.rows || res.data || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadOrders()
}

function handleReset() {
  searchKeyword.value = ''
  dateRange.value = []
  pageNum.value = 1
  loadOrders()
}

async function viewDetail(row: any) {
  try {
    const res = await getOrder(row.id)
    currentOrder.value = res.data || row
    showDetailDialog.value = true
  } catch (e) {
    console.error(e)
    currentOrder.value = row
    showDetailDialog.value = true
  }
}

function canRefund(row: any) {
  return row.status === 1 || row.status === 'PAID' || row.status === 'COMPLETED'
}

function handleRefund(row: any) {
  refundForm.value = {
    amount: row.totalAmount || row.payAmount || 0,
    method: 'ORIGINAL',
    reason: '',
    orderId: row.id,
    maxAmount: row.totalAmount || row.payAmount || 0,
  }
  showRefundDialog.value = true
}

async function confirmRefund() {
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
    })
    ElMessage.success('退款成功')
    showRefundDialog.value = false
    loadOrders()
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

function getPayMethodType(method: string) {
  const map: Record<string, string> = {
    CASH: 'success',
    WECHAT: 'primary',
    ALIPAY: 'warning',
    BANK_CARD: 'info',
    MEMBER_BALANCE: '',
  }
  return map[method] || 'info'
}

function getStatusName(status: number | string) {
  const map: Record<string, string> = {
    0: '待支付',
    1: '已支付',
    2: '已完成',
    3: '已取消',
    4: '已退款',
    PENDING: '待支付',
    PAID: '已支付',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REFUNDED: '已退款',
  }
  return map[String(status)] || status || '-'
}

function getStatusType(status: number | string) {
  const map: Record<string, string> = {
    0: 'warning',
    1: 'success',
    2: '',
    3: 'info',
    4: 'danger',
    PENDING: 'warning',
    PAID: 'success',
    COMPLETED: '',
    CANCELLED: 'info',
    REFUNDED: 'danger',
  }
  return map[String(status)] || 'info'
}
</script>

<style lang="scss" scoped>
.orders-page {
  padding: 0;
}

.page-header {
  margin-bottom: 16px;

  .search-bar {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-wrap: wrap;

    .el-input {
      width: 240px;
    }

    .el-date-picker {
      width: 260px;
    }
  }
}

.order-no {
  color: #B4471D;
  cursor: pointer;
  font-weight: 500;

  &:hover {
    text-decoration: underline;
  }
}

.amount {
  color: #B4471D;
  font-weight: 600;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.order-detail {
  .items-title {
    margin: 20px 0 12px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }
}
</style>
