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
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button @click="handleReset">
        <el-icon><Refresh /></el-icon>
        重置
      </el-button>
    </div>

    <el-table v-loading="loading" :data="orderList" stripe>
      <el-table-column prop="orderNum" label="订单号" width="180">
        <template #default="{ row }">
          <span class="order-no">{{ row.orderNum }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="170" />
      <el-table-column prop="amount" label="订单金额" width="110" align="right">
        <template #default="{ row }">
          <span class="amount">¥{{ formatAmount(row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="payment" label="实付金额" width="110" align="right">
        <template #default="{ row }">
          <span class="amount">¥{{ formatAmount(row.payment) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="orderStatus" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.orderStatus === 3 ? 'success' : 'info'">{{ getOrderStatusName(row.orderStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createBy" label="操作人" width="100" />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.orderStatus === 3"
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
        @size-change="loadOrders"
        @current-change="loadOrders"
      />
    </div>

    <el-dialog v-model="showRefundDialog" title="办理退款" width="600px">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ currentOrder.orderNum }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ formatAmount(currentOrder.amount) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">选择退款商品</el-divider>

        <el-table :data="currentOrder.itemList || []" stripe size="small" max-height="260">
          <el-table-column type="index" width="50" />
          <el-table-column prop="goodsTitle" label="商品名称" show-overflow-tooltip />
          <el-table-column prop="goodsPrice" label="单价" width="90" align="right">
            <template #default="{ row }">¥{{ formatAmount(row.goodsPrice) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="70" align="center" />
          <el-table-column label="可退" width="70" align="center">
            <template #default="{ row }">
              {{ row.quantity - (row.refundCount || 0) }}
            </template>
          </el-table-column>
          <el-table-column label="选择" width="160" align="center">
            <template #default="{ row }">
              <el-input-number
                v-if="row.quantity - (row.refundCount || 0) > 0"
                v-model="row._refundQty"
                :min="0"
                :max="row.quantity - (row.refundCount || 0)"
                size="small"
                style="width: 130px"
              />
              <span v-else class="text-muted">已全额退</span>
            </template>
          </el-table-column>
        </el-table>

        <el-divider content-position="left">退款信息</el-divider>

        <el-form :model="refundForm" label-width="90px">
          <el-form-item label="退款原因" required>
            <el-select v-model="refundForm.reason" style="width: 100%">
              <el-option label="商品质量问题" value="商品质量问题" />
              <el-option label="不想要了" value="不想要了" />
              <el-option label="商品描述不符" value="商品描述不符" />
              <el-option label="发错货" value="发错货" />
              <el-option label="其他原因" value="其他原因" />
            </el-select>
          </el-form-item>
          <el-form-item label="退货退回">
            <el-radio-group v-model="refundForm.hasGoodReturn">
              <el-radio :label="1">需要退货</el-radio>
              <el-radio :label="0">仅退款</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="refundForm.remark" type="textarea" :rows="2" placeholder="选填" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showRefundDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmRefund" :loading="submitting">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getRefundableOrders, refundOrder } from '@/api/pos/pos'

const loading = ref(false)
const submitting = ref(false)
const orderList = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const searchKeyword = ref('')

const refundableCount = ref(0)
const totalRefunded = ref(0)

const showRefundDialog = ref(false)
const currentOrder = ref<any>(null)

const refundForm = ref({
  reason: '',
  hasGoodReturn: 0,
  remark: '',
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
      params.orderNum = searchKeyword.value
    }
    const res: any = await getRefundableOrders(params)
    const rows = res.rows || res.data || []
    orderList.value = rows.map((o: any) => ({
      ...o,
      itemList: (o.itemList || []).map((item: any) => ({ ...item, _refundQty: 0 })),
    }))
    total.value = res.total || 0
    refundableCount.value = rows.filter((o: any) => o.orderStatus === 3).length
  } catch (e) {
    console.error(e)
    ElMessage.error('加载数据失败')
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
  pageNum.value = 1
  loadOrders()
}

function openRefundDialog(row: any) {
  currentOrder.value = {
    ...row,
    itemList: (row.itemList || []).map((item: any) => ({ ...item, _refundQty: 0 })),
  }
  refundForm.value = { reason: '', hasGoodReturn: 0, remark: '' }
  showRefundDialog.value = true
}

async function confirmRefund() {
  if (!refundForm.value.reason) {
    ElMessage.warning('请选择退款原因')
    return
  }

  const items = currentOrder.value.itemList || []
  const refundItems = items.filter((item: any) => item._refundQty > 0)
  if (refundItems.length === 0) {
    ElMessage.warning('请至少选择一个退款商品')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认退款 ${refundItems.length} 个商品？`,
      '退款确认',
      { type: 'warning' }
    )

    submitting.value = true
    for (const item of refundItems) {
      await refundOrder({
        orderId: currentOrder.value.id,
        orderItemId: item.id,
        quantity: item._refundQty,
        refundFee: item.goodsPrice * item._refundQty,
        refundReason: refundForm.value.reason,
        hasGoodReturn: refundForm.value.hasGoodReturn,
        remark: refundForm.value.remark,
      })
    }
    ElMessage.success('退款成功')
    showRefundDialog.value = false
    loadOrders()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('退款失败')
    }
  } finally {
    submitting.value = false
  }
}

function formatAmount(amount: any) {
  if (!amount) return '0.00'
  return Number(amount).toFixed(2)
}

function getOrderStatusName(status: number) {
  const map: Record<number, string> = {
    0: '新订单',
    1: '待发货',
    2: '已发货',
    3: '已完成',
    11: '已取消',
    12: '退款中',
    13: '已关闭',
    21: '待付款',
  }
  return map[status] || '未知'
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
}

.order-no {
  color: #B4471D;
  font-weight: 500;
}

.amount {
  color: #303133;
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
</style>
