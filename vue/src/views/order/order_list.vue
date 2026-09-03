<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryFormRef" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="订单编号" prop="orderNum">
        <el-input v-model="queryParams.orderNum" placeholder="请输入订单编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="订单状态" prop="orderStatus">
        <el-select v-model="queryParams.orderStatus" placeholder="全部" clearable>
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="收件人" prop="receiverName">
        <el-input v-model="queryParams.receiverName" placeholder="收件人姓名" clearable />
      </el-form-item>
      <el-form-item label="手机号" prop="receiverMobile">
        <el-input v-model="queryParams.receiverMobile" placeholder="收件人手机号" clearable />
      </el-form-item>
      <el-form-item label="下单时间">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
        <el-button @click="resetQuery"><el-icon><Refresh /></el-icon>重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain size="small" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain size="small" :disabled="single" @click="handleUpdate">
          <el-icon><Edit /></el-icon>修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain size="small" :disabled="multiple" @click="handleDelete">
          <el-icon><Delete /></el-icon>删除
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单编号" align="center" prop="orderNum" width="220" />
      <el-table-column label="收件人" align="center" prop="receiverName" />
      <el-table-column label="手机号" align="center" prop="receiverMobile" />
      <el-table-column label="配送方式" align="center" prop="deliveryMethod" width="100">
        <template #default="scope">
          {{ getDeliveryLabel(scope.row.deliveryMethod) }}
        </template>
      </el-table-column>
      <el-table-column label="商品金额" align="center" prop="goodsAmount" width="100">
        <template #default="scope">
          {{ scope.row.goodsAmount?.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="实付金额" align="center" prop="payment" width="100">
        <template #default="scope">
          {{ scope.row.payment?.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="订单状态" align="center" prop="orderStatus" width="100">
        <template #default="scope">
          <el-tag size="small" :type="getStatusType(scope.row.orderStatus)">{{ getStatusLabel(scope.row.orderStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" align="center" prop="orderTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="scope">
          <el-button size="small" type="text" @click="handleUpdate(scope.row)">
            <el-icon><Edit /></el-icon>修改
          </el-button>
          <el-button size="small" type="text" @click="handleDetail(scope.row)">
            <el-icon><View /></el-icon>详情
          </el-button>
          <el-button size="small" type="text" @click="handleDelete(scope.row)">
            <el-icon><Delete /></el-icon>删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="850px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收件人" prop="receiverName">
              <el-input v-model="form.receiverName" placeholder="请输入收件人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="receiverMobile">
              <el-input v-model="form.receiverMobile" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="配送方式" prop="deliveryMethod">
              <el-select v-model="form.deliveryMethod" placeholder="请选择配送方式" style="width: 100%">
                <el-option v-for="item in deliveryOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售员" prop="salesmanName">
              <el-input v-model="form.salesmanName" placeholder="请输入销售员" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="省市区" prop="provinces">
              <el-cascader v-model="form.provinces" :options="pcaTextArr" placeholder="请选择省市区" style="width: 100%" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="收货地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入详细地址" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="实付金额" prop="payment">
              <el-input-number v-model="form.payment" :precision="2" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">订单明细</el-divider>
        <el-button type="primary" plain size="small" @click="openSkuDialog" style="margin-bottom: 10px">
          <el-icon><Plus /></el-icon>选择商品
        </el-button>
        <el-table :data="form.itemList" border size="small">
          <el-table-column label="商品名称" prop="goodsTitle" min-width="150" />
          <el-table-column label="规格" prop="goodsSpec" width="100" />
          <el-table-column label="商品编码" prop="goodsNum" width="100" />
          <el-table-column label="SKU编码" prop="skuNum" width="100" />
          <el-table-column label="单价" width="100">
            <template #default="scope">
              <el-input-number v-model="scope.row.goodsPrice" :precision="2" :min="0" size="small" controls-position="right" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="数量" width="90">
            <template #default="scope">
              <el-input-number v-model="scope.row.quantity" :min="1" size="small" controls-position="right" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="90" align="center">
            <template #default="scope">
              {{ ((scope.row.goodsPrice || 0) * (scope.row.quantity || 0)).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60" align="center">
            <template #default="scope">
              <el-button size="small" type="text" text @click="handleDeleteItem(scope.$index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="订单详情" v-model="detailOpen" width="750px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">{{ detailData.orderNum }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag size="small" :type="getStatusType(detailData.orderStatus)">{{ getStatusLabel(detailData.orderStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收件人">{{ detailData.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailData.receiverMobile }}</el-descriptions-item>
        <el-descriptions-item label="配送方式">{{ getDeliveryLabel(detailData.deliveryMethod) }}</el-descriptions-item>
        <el-descriptions-item label="销售员">{{ detailData.salesmanName }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ detailData.address }}</el-descriptions-item>
        <el-descriptions-item label="商品金额">{{ detailData.goodsAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">{{ detailData.payment?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detailData.orderTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">订单明细</el-divider>
      <el-table :data="detailData.itemList || detailData.itemVoList || []" border size="small">
        <el-table-column label="商品名称" prop="goodsTitle" min-width="150" />
        <el-table-column label="规格" prop="goodsSpec" width="100" />
        <el-table-column label="商品编码" prop="goodsNum" width="100" />
        <el-table-column label="单价" prop="goodsPrice" width="90" align="right">
          <template #default="scope">{{ scope.row.goodsPrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="数量" prop="quantity" width="70" align="center" />
        <el-table-column label="小计" width="90" align="right">
          <template #default="scope">{{ scope.row.itemAmount?.toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- SKU选择弹窗 -->
    <PopupSkuList ref="popupSkuRef" @data-from-select="handleSkuDataFromSelect" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, View } from '@element-plus/icons-vue'
import { listSaleOrder, getSaleOrder, delSaleOrder, addSaleOrder, updateSaleOrder } from '@/api/order/saleOrder'
import { pcaTextArr } from '@/utils/chinaAreaData'
import PopupSkuList from '@/views/goods/PopupSkuList.vue'
import Pagination from '@/components/Pagination/index.vue'
import RightToolbar from '@/components/RightToolbar/index.vue'
import type { FormInstance } from 'element-plus'

const loading = ref(true)
const ids = ref<number[]>([])
const single = ref(true)
const multiple = ref(true)
const showSearch = ref(true)
const total = ref(0)
const orderList = ref<any[]>([])
const title = ref('')
const open = ref(false)
const detailOpen = ref(false)
const dateRange = ref<string[]>([])
const queryFormRef = ref<FormInstance>()
const formRef = ref<FormInstance>()
const detailData = ref<any>({})
const popupSkuRef = ref()

const statusOptions = [
  { label: '新订单', value: 0 },
  { label: '待发货', value: 1 },
  { label: '已发货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 11 },
  { label: '退款中', value: 12 },
  { label: '已关闭', value: 13 },
  { label: '待付款', value: 21 }
]

const deliveryOptions = [
  { label: '现结', value: 1 },
  { label: '到店自提', value: 2 },
  { label: '商家配送', value: 3 },
  { label: '骑手配送', value: 4 }
]

const form = reactive<Record<string, any>>({
  id: null,
  receiverName: null,
  receiverMobile: null,
  deliveryMethod: null,
  provinces: [],
  address: null,
  payment: null,
  salesmanName: null,
  remark: null,
  itemList: []
})

const queryParams = reactive<Record<string, any>>({
  pageNum: 1,
  pageSize: 10,
  orderNum: null,
  orderStatus: null,
  receiverName: null,
  receiverMobile: null,
  startTime: null,
  endTime: null
})

const rules = {
  receiverName: [{ required: true, message: '收件人不能为空', trigger: 'blur' }],
  receiverMobile: [{ required: true, message: '手机号不能为空', trigger: 'blur' }],
  deliveryMethod: [{ required: true, message: '请选择配送方式', trigger: 'change' }]
}

onMounted(() => { getList() })

function getList() {
  loading.value = true
  const params = { ...queryParams }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  } else {
    params.startTime = null
    params.endTime = null
  }
  listSaleOrder(params).then((response: any) => {
    orderList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.id = null
  form.receiverName = null
  form.receiverMobile = null
  form.deliveryMethod = null
  form.provinces = []
  form.address = null
  form.payment = null
  form.salesmanName = null
  form.remark = null
  form.itemList = []
  formRef.value?.resetFields()
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  queryFormRef.value?.resetFields()
  handleQuery()
}

function handleSelectionChange(selection: any[]) {
  ids.value = selection.map((item: any) => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增销售订单'
}

function handleUpdate(row: any) {
  reset()
  const id = row.id || ids.value[0]
  getSaleOrder(id).then((response: any) => {
    const data = response.data || {}
    form.id = data.id
    form.receiverName = data.receiverName
    form.receiverMobile = data.receiverMobile
    form.deliveryMethod = data.deliveryMethod
    form.provinces = data.province && data.city && data.town ? [data.province, data.city, data.town] : []
    form.address = data.address
    form.payment = data.payment
    form.salesmanName = data.salesmanName
    form.remark = data.remark
    form.itemList = (data.itemList || []).map((item: any) => ({
      id: item.id,
      goodsTitle: item.goodsTitle,
      goodsSpec: item.goodsSpec,
      goodsPrice: item.goodsPrice,
      quantity: item.quantity,
      goodsId: item.goodsId,
      goodsSkuId: item.goodsSkuId,
      goodsNum: item.goodsNum,
      skuNum: item.skuNum,
      barcode: item.barcode
    }))
    open.value = true
    title.value = '修改销售订单'
  })
}

function handleDetail(row: any) {
  getSaleOrder(row.id).then((response: any) => {
    detailData.value = response.data || {}
    detailOpen.value = true
  })
}

function openSkuDialog() {
  popupSkuRef.value?.openDialog()
}

function handleSkuDataFromSelect(data: any[]) {
  if (data && data.length > 0) {
    data.forEach((item: any) => {
      const exists = form.itemList.find((x: any) => x.goodsSkuId === item.id)
      if (!exists) {
        form.itemList.push({
          goodsTitle: item.goodsName,
          goodsSpec: item.skuName,
          goodsPrice: item.retailPrice || 0,
          quantity: 1,
          goodsId: item.goodsId,
          goodsSkuId: item.id,
          goodsNum: item.goodsNum,
          skuNum: item.skuCode,
          barcode: item.barcode
        })
      }
    })
  }
}

function handleDeleteItem(index: number) {
  form.itemList.splice(index, 1)
}

function submitForm() {
  formRef.value?.validate((valid: boolean) => {
    if (valid) {
      if (!form.itemList || form.itemList.length === 0) {
        ElMessage.error('请至少添加一个订单明细')
        return
      }
      const submitData = {
        ...form,
        province: form.provinces?.[0] || null,
        city: form.provinces?.[1] || null,
        town: form.provinces?.[2] || null,
        itemList: form.itemList.map((item: any) => ({
          ...item,
          goodsPrice: Number(item.goodsPrice) || 0,
          quantity: Number(item.quantity) || 1
        }))
      }
      if (form.id != null) {
        updateSaleOrder(submitData).then(() => {
          ElMessage.success('修改成功')
          open.value = false
          getList()
        })
      } else {
        addSaleOrder(submitData).then(() => {
          ElMessage.success('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row: any) {
  const idArr = row.id || ids.value
  ElMessageBox.confirm('是否确认删除选中的销售订单？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return delSaleOrder(idArr)
  }).then(() => {
    getList()
    ElMessage.success('删除成功')
  }).catch(() => {})
}

function getStatusLabel(status: number) {
  const item = statusOptions.find(i => i.value === status)
  return item ? item.label : '未知'
}

function getStatusType(status: number) {
  const map: Record<number, string> = {
    0: 'info', 1: 'warning', 2: 'primary', 3: 'success',
    11: 'danger', 12: 'warning', 13: 'danger', 21: 'info'
  }
  return map[status] || 'info'
}

function getDeliveryLabel(method: number) {
  const item = deliveryOptions.find(i => i.value === method)
  return item ? item.label : '-'
}
</script>
