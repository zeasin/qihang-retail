<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-label">待备货</div>
        <div class="stat-value primary">{{ stats.pendingCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日已发</div>
        <div class="stat-value success">{{ stats.todayShipped || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">骑手配送</div>
        <div class="stat-value warning">{{ stats.riderCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">商家配送</div>
        <div class="stat-value info">{{ stats.merchantCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">到店自提</div>
        <div class="stat-value">{{ stats.pickupCount || 0 }}</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-form :model="queryParams" size="small" :inline="true" class="search-form">
      <el-form-item label="订单号">
        <el-input v-model="queryParams.orderNum" placeholder="订单号" clearable @keyup.enter="handleQuery" style="width: 160px" />
      </el-form-item>
      <el-form-item label="收货人">
        <el-input v-model="queryParams.receiverName" placeholder="收货人" clearable @keyup.enter="handleQuery" style="width: 120px" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="queryParams.receiverMobile" placeholder="手机号" clearable @keyup.enter="handleQuery" style="width: 140px" />
      </el-form-item>
      <el-form-item label="配送方式">
        <el-select v-model="queryParams.deliveryMethod" placeholder="全部" clearable style="width: 120px">
          <el-option label="骑手配送" :value="4" />
          <el-option label="商家配送" :value="3" />
          <el-option label="到店自提" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
        <el-button @click="resetQuery"><el-icon><Refresh /></el-icon>重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 订单列表 -->
    <el-table v-loading="loading" :data="orderList" stripe border>
      <el-table-column label="订单号" prop="orderNum" width="160" fixed>
        <template #default="{ row }">
          <span class="order-no">{{ row.orderNum }}</span>
        </template>
      </el-table-column>
      <el-table-column label="收货人" prop="receiverName" width="100" />
      <el-table-column label="手机号" prop="receiverMobile" width="130" />
      <el-table-column label="配送方式" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getDeliveryTagType(row.deliveryMethod)" size="small">
            {{ getDeliveryName(row.deliveryMethod) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="商品" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <div v-if="row.itemList && row.itemList.length">
            <div v-for="item in row.itemList" :key="item.id" class="goods-item">
              <span>{{ item.goodsName }}</span>
              <span v-if="item.goodsSku" class="sku-spec">{{ item.goodsSku }}</span>
              <span class="qty">x{{ item.quantity }}</span>
            </div>
          </div>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="订单金额" width="100" align="right">
        <template #default="{ row }">
          <span class="amount">¥{{ (row.orderTotalAmount || 0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" prop="createTime" width="160" />
      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template #default="{ row }">
          <el-button v-if="row.deliveryMethod === 4" type="primary" link size="small" @click="openDeliverDialog(row)">
            <el-icon><Van /></el-icon>骑手配送
          </el-button>
          <el-button v-else-if="row.deliveryMethod === 3" type="warning" link size="small" @click="openDeliverDialog(row)">
            <el-icon><Box /></el-icon>商家配送
          </el-button>
          <el-button v-else-if="row.deliveryMethod === 2" type="success" link size="small" @click="handlePickup(row)">
            <el-icon><Flag /></el-icon>到店自提
          </el-button>
          <el-button type="info" link size="small" @click="openDetailDialog(row)">
            <el-icon><View /></el-icon>详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 配送对话框 -->
    <el-dialog v-model="showDeliverDialog" title="配送发货" width="500px" @close="resetDeliverForm">
      <el-form :model="deliverForm" label-width="100px">
        <el-form-item label="订单号">
          <span>{{ currentOrder?.orderNum }}</span>
        </el-form-item>
        <el-form-item label="收货人">
          <span>{{ currentOrder?.receiverName }} {{ currentOrder?.receiverMobile }}</span>
        </el-form-item>
        <el-form-item label="配送方式">
          <el-tag :type="getDeliveryTagType(currentOrder?.deliveryMethod)" size="small">
            {{ getDeliveryName(currentOrder?.deliveryMethod) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="承运人">
          <el-input v-model="deliverForm.carrierName" placeholder="骑手/快递员姓名" style="width: 200px" />
        </el-form-item>
        <el-form-item label="运单号">
          <el-input v-model="deliverForm.trackingNumber" placeholder="运单号" style="width: 200px" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="deliverForm.remark" type="textarea" :rows="2" placeholder="备注" style="width: 280px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDeliverDialog = false">取消</el-button>
        <el-button type="primary" @click="handleDeliver" :loading="submitting">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="订单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder?.orderNum }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">{{ getStatusName(currentOrder?.orderStatus) }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ currentOrder?.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentOrder?.receiverMobile }}</el-descriptions-item>
        <el-descriptions-item label="配送方式">{{ getDeliveryName(currentOrder?.deliveryMethod) }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ (currentOrder?.orderTotalAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ currentOrder?.receiverProvince }}{{ currentOrder?.receiverCity }}{{ currentOrder?.receiverArea }}{{ currentOrder?.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ currentOrder?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="发货状态">{{ currentOrder?.shipStatus === 2 ? '已发货' : '待发货' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="currentOrder?.itemList?.length" style="margin-top: 16px">
        <el-divider content-position="left">商品明细</el-divider>
        <el-table :data="currentOrder.itemList" size="small" border>
          <el-table-column label="商品" prop="goodsName" show-overflow-tooltip />
          <el-table-column label="规格" prop="goodsSku" width="150" show-overflow-tooltip />
          <el-table-column label="数量" prop="quantity" width="60" align="center" />
          <el-table-column label="单价" width="80" align="right">
            <template #default="{ row }">¥{{ (row.goodsUnitPrice || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="小计" width="80" align="right">
            <template #default="{ row }">¥{{ (row.goodsTotalAmount || 0).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Van, Box, Flag, View } from '@element-plus/icons-vue'
import { getStockingList, getStockingDetail, executeDelivery, executePickup, getStockingStats } from '@/api/ship/stocking'

const loading = ref(false)
const submitting = ref(false)
const orderList = ref<any[]>([])
const total = ref(0)
const stats = ref<Record<string, any>>({})
const dateRange = ref<string[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNum: '',
  receiverName: '',
  receiverMobile: '',
  deliveryMethod: undefined as number | undefined,
  startTime: '',
  endTime: '',
})

const showDeliverDialog = ref(false)
const showDetailDialog = ref(false)
const currentOrder = ref<any>(null)

const deliverForm = reactive({
  carrierName: '',
  trackingNumber: '',
  remark: '',
})

onMounted(() => {
  getList()
  loadStats()
})

async function getList() {
  loading.value = true
  try {
    if (dateRange.value?.length === 2) {
      queryParams.startTime = dateRange.value[0]
      queryParams.endTime = dateRange.value[1]
    } else {
      queryParams.startTime = ''
      queryParams.endTime = ''
    }
    const res: any = await getStockingList(queryParams)
    orderList.value = res.rows || res.data?.rows || []
    total.value = res.total || res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    const res: any = await getStockingStats()
    stats.value = res.data || res
  } catch (e) {
    // ignore
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.orderNum = ''
  queryParams.receiverName = ''
  queryParams.receiverMobile = ''
  queryParams.deliveryMethod = undefined
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = []
  handleQuery()
}

function getDeliveryName(code?: number) {
  const map: Record<number, string> = { 1: '现结', 2: '到店自提', 3: '商家配送', 4: '骑手配送' }
  return map[code || 0] || '未知'
}

function getDeliveryTagType(code?: number) {
  const map: Record<number, string> = { 2: 'success', 3: 'warning', 4: 'primary' }
  return map[code || 0] || 'info'
}

function getStatusName(code?: number) {
  const map: Record<number, string> = { 0: '待处理', 1: '已确认', 2: '已发货', 3: '已完成', 4: '已取消' }
  return map[code || 0] || '未知'
}

function openDeliverDialog(row: any) {
  currentOrder.value = row
  deliverForm.carrierName = ''
  deliverForm.trackingNumber = ''
  deliverForm.remark = ''
  showDeliverDialog.value = true
}

function resetDeliverForm() {
  deliverForm.carrierName = ''
  deliverForm.trackingNumber = ''
  deliverForm.remark = ''
}

async function handleDeliver() {
  if (!deliverForm.carrierName) {
    ElMessage.warning('请输入承运人姓名')
    return
  }
  submitting.value = true
  try {
    await executeDelivery({
      orderId: currentOrder.value.id,
      deliveryMethod: currentOrder.value.deliveryMethod,
      carrierName: deliverForm.carrierName,
      trackingNumber: deliverForm.trackingNumber,
      remark: deliverForm.remark,
    })
    ElMessage.success('发货成功')
    showDeliverDialog.value = false
    getList()
    loadStats()
  } finally {
    submitting.value = false
  }
}

async function handlePickup(row: any) {
  try {
    await ElMessageBox.confirm(`确认到店自提订单 ${row.orderNum}？`, '确认操作', { type: 'success' })
    submitting.value = true
    await executePickup(row.id)
    ElMessage.success('到店自提确认成功')
    getList()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function openDetailDialog(row: any) {
  try {
    const res: any = await getStockingDetail(row.id)
    currentOrder.value = res.data || res
    showDetailDialog.value = true
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}
</script>

<style scoped>
.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  flex: 1;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}
.stat-value.primary { color: #409eff; }
.stat-value.success { color: #67c23a; }
.stat-value.warning { color: #e6a23c; }
.stat-value.info { color: #909399; }
.search-form { margin-bottom: 12px; }
.order-no { color: #409eff; cursor: pointer; }
.goods-item { display: flex; align-items: center; gap: 6px; margin: 2px 0; }
.goods-item .sku-spec { color: #909399; font-size: 12px; }
.goods-item .qty { color: #606266; margin-left: auto; }
.amount { color: #f56c6c; font-weight: 500; }
.text-muted { color: #c0c4cc; }
</style>
