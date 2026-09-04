<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-label">待处理</div>
        <div class="stat-value primary">{{ stats.pendingCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日已打印</div>
        <div class="stat-value success">{{ stats.todayPrinted || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日已发货</div>
        <div class="stat-value warning">{{ stats.todayShipped || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">未打单</div>
        <div class="stat-value danger">{{ stats.totalPending || 0 }}</div>
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
      <el-form-item label="打单状态">
        <el-select v-model="queryParams.waybillStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="未打单" :value="0" />
          <el-option label="已取号" :value="1" />
          <el-option label="已打印" :value="2" />
          <el-option label="已发货" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
        <el-button @click="resetQuery"><el-icon><Refresh /></el-icon>重置</el-button>
        <el-button type="warning" :disabled="!selectedIds.length" @click="handleBatchPrint"><el-icon><Printer /></el-icon>批量打单 ({{ selectedIds.length }})</el-button>
        <el-button type="success" :disabled="!selectedIds.length" @click="handleBatchShip"><el-icon><Van /></el-icon>批量发货 ({{ selectedIds.length }})</el-button>
      </el-form-item>
    </el-form>

    <!-- 订单列表 -->
    <el-table v-loading="loading" :data="orderList" stripe border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="45" />
      <el-table-column label="订单号" prop="orderNum" width="160" fixed>
        <template #default="{ row }">
          <span class="order-no">{{ row.orderNum }}</span>
        </template>
      </el-table-column>
      <el-table-column label="收货人" prop="receiverName" width="100" />
      <el-table-column label="手机号" prop="receiverMobile" width="130" />
      <el-table-column label="收货地址" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.receiverProvince }}{{ row.receiverCity }}{{ row.receiverArea }}{{ row.receiverAddress }}
        </template>
      </el-table-column>
      <el-table-column label="商品" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <div v-if="row.itemList && row.itemList.length">
            <div v-for="item in row.itemList" :key="item.id" class="goods-item">
              <span>{{ item.goodsName }}</span>
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
      <el-table-column label="打单状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="getWaybillTagType(row.waybillStatus)" size="small">
            {{ getWaybillStatusName(row.waybillStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发货状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.shipStatus === 2 ? 'success' : 'warning'" size="small">
            {{ row.shipStatus === 2 ? '已发货' : '待发货' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" prop="createTime" width="160" />
    </el-table>

    <!-- 分页 -->
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Printer, Van } from '@element-plus/icons-vue'
import { getRiderDeliveryList, getRiderDeliveryStats, batchPrinted, batchShip } from '@/api/ship/riderDelivery'

const loading = ref(false)
const orderList = ref<any[]>([])
const total = ref(0)
const stats = ref<Record<string, any>>({})
const dateRange = ref<string[]>([])
const selectedIds = ref<string[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNum: '',
  receiverName: '',
  receiverMobile: '',
  waybillStatus: undefined as number | undefined,
  startTime: '',
  endTime: '',
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
    const res: any = await getRiderDeliveryList(queryParams)
    orderList.value = res.rows || res.data?.rows || []
    total.value = res.total || res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    const res: any = await getRiderDeliveryStats()
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
  queryParams.waybillStatus = undefined
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = []
  handleQuery()
}

function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id)
}

function getWaybillStatusName(code?: number) {
  const map: Record<number, string> = { 0: '未打单', 1: '已取号', 2: '已打印', 3: '已发货', 10: '手动发货' }
  return map[code || 0] || '未知'
}

function getWaybillTagType(code?: number) {
  const map: Record<number, string> = { 0: 'danger', 1: 'warning', 2: 'success', 3: 'success' }
  return map[code || 0] || 'info'
}

async function handleBatchPrint() {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm(`确认批量打印 ${selectedIds.value.length} 个订单的面单？`, '批量打单', { type: 'warning' })
    await batchPrinted(selectedIds.value)
    ElMessage.success('批量打单成功')
    getList()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleBatchShip() {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm(`确认批量发货 ${selectedIds.value.length} 个订单？`, '批量发货', { type: 'warning' })
    await batchShip(selectedIds.value)
    ElMessage.success('批量发货成功')
    getList()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
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
.stat-value.danger { color: #f56c6c; }
.search-form { margin-bottom: 12px; }
.order-no { color: #409eff; cursor: pointer; }
.goods-item { display: flex; align-items: center; gap: 6px; margin: 2px 0; }
.goods-item .qty { color: #606266; margin-left: auto; }
.amount { color: #f56c6c; font-weight: 500; }
.text-muted { color: #c0c4cc; }
</style>
