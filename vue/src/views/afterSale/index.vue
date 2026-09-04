<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-label">今日售后</div>
        <div class="stat-value">{{ stats.todayCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日退款额</div>
        <div class="stat-value refund">¥{{ (stats.todayRefundAmount || 0).toFixed(2) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">待审核</div>
        <div class="stat-value warning">{{ stats.pendingAudit || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">待处理</div>
        <div class="stat-value primary">{{ stats.pendingProcess || 0 }}</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-form :model="queryParams" size="small" :inline="true" class="search-form">
      <el-form-item label="售后单号">
        <el-input v-model="queryParams.refundNum" placeholder="售后单号" clearable @keyup.enter="handleQuery" style="width: 180px" />
      </el-form-item>
      <el-form-item label="订单号">
        <el-input v-model="queryParams.orderNum" placeholder="源订单号" clearable @keyup.enter="handleQuery" style="width: 180px" />
      </el-form-item>
      <el-form-item label="售后类型">
        <el-select v-model="queryParams.refundType" placeholder="全部" clearable style="width: 120px">
          <el-option label="退货退款" :value="10" />
          <el-option label="仅退款" :value="11" />
          <el-option label="换货" :value="20" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.erpStatus" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="商品名称">
        <el-input v-model="queryParams.goodsName" placeholder="商品名称" clearable @keyup.enter="handleQuery" style="width: 150px" />
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
        <el-button @click="resetQuery"><el-icon><Refresh /></el-icon>重置</el-button>
        <el-button type="success" @click="openApplyDialog"><el-icon><Plus /></el-icon>发起售后</el-button>
      </el-form-item>
    </el-form>

    <!-- 售后列表 -->
    <el-table v-loading="loading" :data="afterSaleList" stripe border>
      <el-table-column label="售后单号" prop="refundNum" width="180" fixed>
        <template #default="{ row }">
          <span class="order-no">{{ row.refundNum }}</span>
        </template>
      </el-table-column>
      <el-table-column label="源订单号" prop="orderNum" width="160" show-overflow-tooltip />
      <el-table-column label="商品" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="goods-info">
            <span>{{ row.goodsName }}</span>
            <span v-if="row.goodsSku" class="sku-spec">{{ row.goodsSku }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="getTypeTagType(row.refundType)" size="small">{{ getTypeName(row.refundType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="数量" prop="quantity" width="60" align="center" />
      <el-table-column label="退款金额" width="100" align="right">
        <template #default="{ row }">
          <span class="amount">¥{{ (row.refundFee || 0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.erpStatus)" size="small">{{ getStatusName(row.erpStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="退款方式" width="90" align="center">
        <template #default="{ row }">
          <span v-if="row.refundMethod">{{ getRefundMethodName(row.refundMethod) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="发起人" prop="createBy" width="80" />
      <el-table-column label="发起时间" width="160" align="center">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openDetailDialog(row)">详情</el-button>
          <el-button v-if="row.erpStatus === 0" link type="warning" size="small" @click="openAuditDialog(row)">审核</el-button>
          <el-button v-if="row.erpStatus === 1" link type="success" size="small" @click="handleReceive(row)">收货</el-button>
          <el-button v-if="row.erpStatus === 2" link type="primary" size="small" @click="openProcessDialog(row)">退款</el-button>
          <el-button v-if="row.erpStatus === 3" link type="primary" size="small" @click="handleShipExchange(row)">换发</el-button>
          <el-button v-if="[0,1,2,3].includes(row.erpStatus)" link type="danger" size="small" @click="handleCancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
      />
    </div>

    <!-- 发起售后弹窗 -->
    <el-dialog v-model="showApplyDialog" title="发起售后" width="720px" top="5vh">
      <el-form :model="applyForm" label-width="100px" size="default">
        <el-form-item label="选择订单">
          <div class="order-search-bar">
            <el-input v-model="orderSearchKeyword" placeholder="输入订单号搜索" clearable style="width: 200px" @keyup.enter="loadRefundableOrders">
              <template #append>
                <el-button @click="loadRefundableOrders"><el-icon><Search /></el-icon></el-button>
              </template>
            </el-input>
          </div>
        </el-form-item>
        <el-form-item v-if="refundableOrders.length > 0" label="可售后订单">
          <el-select v-model="applyForm.orderId" placeholder="选择订单" style="width: 100%" @change="onOrderSelected">
            <el-option v-for="o in refundableOrders" :key="o.id" :label="`${o.orderNum} | ¥${(o.amount || 0).toFixed(2)} | ${formatTime(o.createTime)}`" :value="Number(o.id)" />
          </el-select>
        </el-form-item>

        <template v-if="selectedOrder">
          <el-form-item label="订单商品">
            <el-table :data="selectedOrder.itemList || []" border size="small" max-height="200">
              <el-table-column label="商品" min-width="150" show-overflow-tooltip>
                <template #default="{ row }">{{ row.goodsTitle }}<span v-if="row.goodsSpec" class="sku-spec">{{ row.goodsSpec }}</span></template>
              </el-table-column>
              <el-table-column prop="quantity" label="数量" width="60" align="center" />
              <el-table-column label="可退" width="60" align="center">
                <template #default="{ row }">{{ row.quantity - (row.refundCount || 0) }}</template>
              </el-table-column>
              <el-table-column label="选择" width="140" align="center">
                <template #default="{ row }">
                  <el-radio v-if="row.quantity - (row.refundCount || 0) > 0" v-model="applyForm.orderItemId" :label="Number(row.id)">&nbsp;</el-radio>
                  <span v-else class="text-muted">已退完</span>
                </template>
              </el-table-column>
            </el-table>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="售后类型" required>
                <el-radio-group v-model="applyForm.refundType">
                  <el-radio :label="11">仅退款</el-radio>
                  <el-radio :label="10">退货退款</el-radio>
                  <el-radio :label="20">换货</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="退货数量" required>
                <el-input-number v-model="applyForm.quantity" :min="1" :max="maxRefundQty" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="退款金额" required>
                <el-input-number v-model="applyForm.refundFee" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="applyForm.refundType === 20">
              <el-form-item label="换货商品规格ID" required>
                <el-input-number v-model="applyForm.exchangeGoodsSkuId" :min="1" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="退款原因" required>
            <el-select v-model="applyForm.refundReason" style="width: 100%" placeholder="选择原因">
              <el-option v-for="r in returnReasons" :key="r" :label="r" :value="r" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="applyForm.remark" type="textarea" :rows="2" placeholder="选填" />
          </el-form-item>
          <el-alert v-if="needAudit" type="warning" :closable="false" show-icon>
            退款金额超过 ¥{{ auditThreshold.toFixed(0) }} 阈值，提交后需店长审核
          </el-alert>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="confirmApply">确认发起</el-button>
      </template>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog v-model="showAuditDialog" title="售后审核" width="480px">
      <el-descriptions v-if="currentRow" :column="1" border size="small">
        <el-descriptions-item label="售后单号">{{ currentRow.refundNum }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentRow.orderNum }}</el-descriptions-item>
        <el-descriptions-item label="商品">{{ currentRow.goodsName }} {{ currentRow.goodsSku }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">¥{{ (currentRow.refundFee || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ currentRow.createBy }}</el-descriptions-item>
        <el-descriptions-item label="退款原因">{{ currentRow.refundReason }}</el-descriptions-item>
      </el-descriptions>
      <el-divider />
      <el-form label-width="80px">
        <el-form-item label="审核结果" required>
          <el-radio-group v-model="auditForm.approved">
            <el-radio :label="true">通过</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="auditForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAuditDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="confirmAudit">确认审核</el-button>
      </template>
    </el-dialog>

    <!-- 退款弹窗 -->
    <el-dialog v-model="showProcessDialog" title="执行退款" width="480px">
      <el-descriptions v-if="currentRow" :column="1" border size="small">
        <el-descriptions-item label="售后单号">{{ currentRow.refundNum }}</el-descriptions-item>
        <el-descriptions-item label="商品">{{ currentRow.goodsName }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">¥{{ (currentRow.refundFee || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.receiveTime" label="收货时间">{{ formatTime(currentRow.receiveTime) }}</el-descriptions-item>
      </el-descriptions>
      <el-divider />
      <el-form label-width="80px">
        <el-form-item label="退款方式" required>
          <el-select v-model="processForm.refundMethod" style="width: 100%">
            <el-option label="现金退款" value="cash" />
            <el-option label="原路退回" value="original" />
            <el-option label="退到余额" value="balance" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="processForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showProcessDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="confirmProcess">确认退款</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="售后详情" width="600px">
      <el-descriptions v-if="currentRow" :column="2" border size="small">
        <el-descriptions-item label="售后单号">{{ currentRow.refundNum }}</el-descriptions-item>
        <el-descriptions-item label="源订单号">{{ currentRow.orderNum }}</el-descriptions-item>
        <el-descriptions-item label="商品名称" :span="2">{{ currentRow.goodsName }}</el-descriptions-item>
        <el-descriptions-item label="商品规格">{{ currentRow.goodsSku || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退货数量">{{ currentRow.quantity }}</el-descriptions-item>
        <el-descriptions-item label="售后类型">{{ getTypeName(currentRow.refundType) }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">¥{{ (currentRow.refundFee || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusTagType(currentRow.erpStatus)" size="small">{{ getStatusName(currentRow.erpStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="退款方式">{{ currentRow.refundMethod ? getRefundMethodName(currentRow.refundMethod) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款原因" :span="2">{{ currentRow.refundReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ currentRow.createBy }}</el-descriptions-item>
        <el-descriptions-item label="发起时间">{{ formatTime(currentRow.createTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.auditBy" label="审核人">{{ currentRow.auditBy }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.auditTime" label="审核时间">{{ formatTime(currentRow.auditTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.auditRemark" label="审核备注" :span="2">{{ currentRow.auditRemark }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.receiveBy" label="收货人">{{ currentRow.receiveBy }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.receiveTime" label="收货时间">{{ formatTime(currentRow.receiveTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.refundBy" label="退款执行人">{{ currentRow.refundBy }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.refundTime" label="退款时间">{{ formatTime(currentRow.refundTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.exchangeGoodsName" label="换货商品" :span="2">{{ currentRow.exchangeGoodsName }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.exchangeErpGoodsSkuId" label="换货规格ID">{{ currentRow.exchangeErpGoodsSkuId }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.exchangeGoodsNum" label="换货数量">{{ currentRow.exchangeGoodsNum }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.remark" label="备注" :span="2">{{ currentRow.remark }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getAfterSaleList, getAfterSaleStats, applyAfterSale,
  auditAfterSale, receiveReturnGoods, processAfterSale,
  shipExchange, cancelAfterSale, getRefundableOrders, getAfterSaleConfig,
} from '@/api/afterSale'

const loading = ref(false)
const submitting = ref(false)
const afterSaleList = ref<any[]>([])
const total = ref(0)
const stats = ref<Record<string, any>>({})
const dateRange = ref<string[]>([])
const returnReasons = ref<string[]>([])
const auditThreshold = ref(200)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  refundNum: '',
  orderNum: '',
  refundType: undefined as number | undefined,
  erpStatus: undefined as number | undefined,
  goodsName: '',
  startTime: '',
  endTime: '',
})

const statusOptions = [
  { label: '待审核', value: 0 },
  { label: '待退货', value: 1 },
  { label: '待退款', value: 2 },
  { label: '待换发', value: 3 },
  { label: '已完成', value: 10 },
  { label: '已取消', value: 11 },
  { label: '已拒绝', value: 12 },
]

const showApplyDialog = ref(false)
const showAuditDialog = ref(false)
const showProcessDialog = ref(false)
const showDetailDialog = ref(false)
const currentRow = ref<any>(null)

const refundableOrders = ref<any[]>([])
const selectedOrder = ref<any>(null)
const orderSearchKeyword = ref('')

const applyForm = reactive({
  orderId: undefined as number | undefined,
  orderItemId: undefined as number | undefined,
  quantity: 1,
  refundFee: 0,
  refundType: 11,
  refundReason: '',
  remark: '',
  exchangeGoodsSkuId: undefined as number | undefined,
  exchangeQuantity: 1,
})

const auditForm = reactive({
  approved: true,
  remark: '',
})

const processForm = reactive({
  refundMethod: 'cash',
  remark: '',
})

const maxRefundQty = computed(() => {
  if (!selectedOrder.value || !applyForm.orderItemId) return 1
  const item = selectedOrder.value.itemList?.find((i: any) => Number(i.id) === applyForm.orderItemId)
  if (!item) return 1
  return item.quantity - (item.refundCount || 0)
})

const needAudit = computed(() => {
  return applyForm.refundFee > auditThreshold.value
})

onMounted(() => {
  loadConfig()
  getList()
  loadStats()
})

async function loadConfig() {
  try {
    const res: any = await getAfterSaleConfig()
    const cfg = res.data || res
    returnReasons.value = cfg.returnReasons || ['商品质量问题', '不想要了', '商品描述不符', '发错货', '其他原因']
    auditThreshold.value = cfg.refundAuditThreshold || 200
  } catch (e) {
    returnReasons.value = ['商品质量问题', '不想要了', '商品描述不符', '发错货', '其他原因']
  }
}

async function getList() {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startTime = dateRange.value[0]
      queryParams.endTime = dateRange.value[1]
    } else {
      queryParams.startTime = ''
      queryParams.endTime = ''
    }
    const res: any = await getAfterSaleList(queryParams)
    afterSaleList.value = res.rows || res.data?.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
    ElMessage.error('加载列表失败')
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    const res: any = await getAfterSaleStats()
    stats.value = res.data || res || {}
  } catch (e) {
    console.error(e)
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.refundNum = ''
  queryParams.orderNum = ''
  queryParams.refundType = undefined
  queryParams.erpStatus = undefined
  queryParams.goodsName = ''
  dateRange.value = []
  queryParams.pageNum = 1
  getList()
}

function openApplyDialog() {
  showApplyDialog.value = true
  selectedOrder.value = null
  refundableOrders.value = []
  orderSearchKeyword.value = ''
  Object.assign(applyForm, {
    orderId: undefined,
    orderItemId: undefined,
    quantity: 1,
    refundFee: 0,
    refundType: 11,
    refundReason: '',
    remark: '',
    exchangeGoodsSkuId: undefined,
    exchangeQuantity: 1,
  })
  loadRefundableOrders()
}

async function loadRefundableOrders() {
  try {
    const params: Record<string, any> = {
      pageNum: 1,
      pageSize: 50,
    }
    if (orderSearchKeyword.value) {
      params.orderNum = orderSearchKeyword.value
    }
    const res: any = await getRefundableOrders(params)
    refundableOrders.value = res.rows || res.data?.records || []
  } catch (e) {
    console.error(e)
    ElMessage.error('加载订单失败')
  }
}

function onOrderSelected(orderId: number) {
  selectedOrder.value = refundableOrders.value.find((o: any) => Number(o.id) === orderId) || null
  applyForm.orderItemId = undefined
  if (selectedOrder.value?.itemList?.length === 1) {
    const item = selectedOrder.value.itemList[0]
    if (item.quantity - (item.refundCount || 0) > 0) {
      applyForm.orderItemId = Number(item.id)
      applyForm.quantity = 1
      applyForm.refundFee = item.goodsPrice || 0
    }
  }
}

function onItemSelected(itemId: number) {
  const item = selectedOrder.value?.itemList?.find((i: any) => Number(i.id) === itemId)
  if (item) {
    applyForm.refundFee = (item.goodsPrice || 0) * applyForm.quantity
  }
}

async function confirmApply() {
  if (!applyForm.orderId) {
    ElMessage.warning('请选择订单')
    return
  }
  if (!applyForm.orderItemId) {
    ElMessage.warning('请选择售后商品')
    return
  }
  if (!applyForm.refundReason) {
    ElMessage.warning('请选择退款原因')
    return
  }
  if (applyForm.refundType === 20 && !applyForm.exchangeGoodsSkuId) {
    ElMessage.warning('换货必须填写换货商品规格ID')
    return
  }
  try {
    submitting.value = true
    await applyAfterSale({ ...applyForm })
    ElMessage.success('售后申请已发起')
    showApplyDialog.value = false
    getList()
    loadStats()
  } catch (e: any) {
    console.error(e)
    ElMessage.error(e.message || '发起售后失败')
  } finally {
    submitting.value = false
  }
}

function openAuditDialog(row: any) {
  currentRow.value = row
  auditForm.approved = true
  auditForm.remark = ''
  showAuditDialog.value = true
}

async function confirmAudit() {
  if (!currentRow.value) return
  try {
    submitting.value = true
    await auditAfterSale(currentRow.value.id, { ...auditForm })
    ElMessage.success(auditForm.approved ? '审核通过' : '已拒绝')
    showAuditDialog.value = false
    getList()
    loadStats()
  } catch (e: any) {
    console.error(e)
    ElMessage.error(e.message || '审核失败')
  } finally {
    submitting.value = false
  }
}

function openProcessDialog(row: any) {
  currentRow.value = row
  processForm.refundMethod = 'cash'
  processForm.remark = ''
  showProcessDialog.value = true
}

async function confirmProcess() {
  if (!currentRow.value) return
  try {
    submitting.value = true
    await processAfterSale(currentRow.value.id, { ...processForm })
    ElMessage.success('退款已执行')
    showProcessDialog.value = false
    getList()
    loadStats()
  } catch (e: any) {
    console.error(e)
    ElMessage.error(e.message || '退款失败')
  } finally {
    submitting.value = false
  }
}

async function handleReceive(row: any) {
  try {
    await ElMessageBox.confirm('确认已收到顾客退回的商品？', '退货收货', { type: 'warning' })
    await receiveReturnGoods(row.id)
    ElMessage.success('收货成功')
    getList()
    loadStats()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error(e.message || '收货失败')
    }
  }
}

async function handleShipExchange(row: any) {
  try {
    await ElMessageBox.confirm('确认发出换货商品？', '换货发货', { type: 'warning' })
    await shipExchange(row.id)
    ElMessage.success('换货已发货')
    getList()
    loadStats()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error(e.message || '换货发货失败')
    }
  }
}

async function handleCancel(row: any) {
  try {
    await ElMessageBox.confirm('确认取消此售后单？', '取消售后', { type: 'warning' })
    await cancelAfterSale(row.id)
    ElMessage.success('售后已取消')
    getList()
    loadStats()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error(e.message || '取消失败')
    }
  }
}

function openDetailDialog(row: any) {
  currentRow.value = row
  showDetailDialog.value = true
}

function getTypeName(type: number): string {
  const map: Record<number, string> = { 10: '退货退款', 11: '仅退款', 20: '换货' }
  return map[type] || '未知'
}

function getTypeTagType(type: number): string {
  const map: Record<number, string> = { 10: 'warning', 11: 'info', 20: 'primary' }
  return map[type] || 'info'
}

function getStatusName(status: number): string {
  const map: Record<number, string> = {
    0: '待审核', 1: '待退货', 2: '待退款', 3: '待换发',
    10: '已完成', 11: '已取消', 12: '已拒绝',
  }
  return map[status] || '未知'
}

function getStatusTagType(status: number): string {
  const map: Record<number, string> = {
    0: 'warning', 1: 'info', 2: 'info', 3: 'info',
    10: 'success', 11: 'danger', 12: 'danger',
  }
  return map[status] || 'info'
}

function getRefundMethodName(method: string): string {
  const map: Record<string, string> = { cash: '现金', original: '原路退回', balance: '退到余额' }
  return map[method] || method
}

function formatTime(time: any): string {
  if (!time) return '-'
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 19)
  return '-'
}
</script>

<style lang="scss" scoped>
.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;

  .stat-card {
    flex: 1;
    padding: 16px 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    text-align: center;

    .stat-label {
      font-size: 13px;
      color: #909399;
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 22px;
      font-weight: 700;
      color: #303133;

      &.refund { color: #f56c6c; }
      &.warning { color: #e6a23c; }
      &.primary { color: #409eff; }
    }
  }
}

.search-form {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.order-no {
  color: #b4471d;
  font-weight: 500;
  font-size: 13px;
}

.goods-info {
  display: flex;
  flex-direction: column;
  gap: 2px;

  .sku-spec {
    font-size: 12px;
    color: #909399;
  }
}

.sku-spec {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
}

.text-muted {
  color: #c0c4cc;
  font-size: 12px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.order-search-bar {
  display: flex;
  gap: 8px;
}
</style>
