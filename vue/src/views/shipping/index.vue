<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-label">待提货</div>
        <div class="stat-value primary">{{ stats.pendingCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日已提货</div>
        <div class="stat-value success">{{ stats.todayConfirmed || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日自提单</div>
        <div class="stat-value info">{{ stats.todayTotal || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">全部待提货</div>
        <div class="stat-value warning">{{ stats.totalPending || 0 }}</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-form :model="queryParams" size="small" :inline="true" class="search-form">
      <el-form-item label="订单号">
        <el-input v-model="queryParams.orderNum" placeholder="订单号" clearable @keyup.enter="handleQuery" style="width: 160px" />
      </el-form-item>
      <el-form-item label="提货人">
        <el-input v-model="queryParams.receiverName" placeholder="提货人" clearable @keyup.enter="handleQuery" style="width: 120px" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="queryParams.receiverMobile" placeholder="手机号" clearable @keyup.enter="handleQuery" style="width: 140px" />
      </el-form-item>
      <el-form-item label="提货状态">
        <el-select v-model="queryParams.shipStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="待提货" :value="0" />
          <el-option label="已提货" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
        <el-button @click="resetQuery"><el-icon><Refresh /></el-icon>重置</el-button>
        <el-button type="success" :disabled="!selectedIds.length" @click="handleBatchConfirm">
          <el-icon><Check /></el-icon>批量确认提货 ({{ selectedIds.length }})
        </el-button>
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
      <el-table-column label="提货人" prop="receiverName" width="100" />
      <el-table-column label="手机号" prop="receiverMobile" width="130" />
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
      <el-table-column label="下单时间" prop="createTime" width="160" />
      <el-table-column label="提货状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.shipStatus === 2 ? 'success' : 'warning'" size="small">
            {{ row.shipStatus === 2 ? '已提货' : '待提货' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right" align="center">
        <template #default="{ row }">
          <el-button v-if="row.shipStatus !== 2" type="success" link size="small" @click="handleConfirm(row)">
            <el-icon><Check /></el-icon>确认提货
          </el-button>
          <span v-else class="text-success">已提货</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Check } from '@element-plus/icons-vue'
import { getManualShipList, getManualShipStats, confirmPickup, batchConfirmPickup } from '@/api/ship/manualShip'

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
  shipStatus: undefined as number | undefined,
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
    const res: any = await getManualShipList(queryParams)
    orderList.value = res.rows || res.data?.rows || []
    total.value = res.total || res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    const res: any = await getManualShipStats()
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
  queryParams.shipStatus = undefined
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = []
  handleQuery()
}

function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id)
}

async function handleConfirm(row: any) {
  try {
    await ElMessageBox.confirm(`确认提货订单 ${row.orderNum}？提货人: ${row.receiverName}`, '确认提货', { type: 'success' })
    await confirmPickup(row.id)
    ElMessage.success('提货确认成功')
    getList()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleBatchConfirm() {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm(`确认批量提货 ${selectedIds.value.length} 个订单？`, '批量确认提货', { type: 'success' })
    await batchConfirmPickup(selectedIds.value)
    ElMessage.success('批量提货成功')
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
.stat-value.info { color: #909399; }
.search-form { margin-bottom: 12px; }
.order-no { color: #409eff; cursor: pointer; }
.goods-item { display: flex; align-items: center; gap: 6px; margin: 2px 0; }
.goods-item .qty { color: #606266; margin-left: auto; }
.amount { color: #f56c6c; font-weight: 500; }
.text-muted { color: #c0c4cc; }
.text-success { color: #67c23a; font-size: 12px; }
</style>
