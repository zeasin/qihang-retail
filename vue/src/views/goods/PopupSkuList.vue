<template>
  <el-dialog v-model="dialogVisible" title="选择商品SKU" width="900px" append-to-body>
    <el-form :model="queryParams" ref="queryFormRef" size="small" :inline="true" label-width="80px">
      <el-form-item label="商品名称" prop="goodsName">
        <el-input v-model="queryParams.goodsName" placeholder="请输入商品名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="商品编码" prop="goodsNum">
        <el-input v-model="queryParams.goodsNum" placeholder="请输入商品编码" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="SKU编码" prop="skuCode">
        <el-input v-model="queryParams.skuCode" placeholder="请输入SKU编码" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
        <el-button type="primary" :disabled="multiple" @click="sendDataToParent"><el-icon><Plus /></el-icon>确认添加</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="goodsSpecList" @selection-change="handleSelectionChange" border height="400" size="small">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="规格ID" align="center" prop="id" width="70" />
      <el-table-column label="商品名称" align="left" prop="goodsName" min-width="150" show-overflow-tooltip />
      <el-table-column label="规格" align="center" prop="skuName" width="120" />
      <el-table-column label="商品编码" align="center" prop="goodsNum" width="120" />
      <el-table-column label="SKU编码" align="center" prop="skuCode" width="120" />
      <el-table-column label="零售价" align="center" prop="retailPrice" width="90">
        <template #default="scope">
          {{ scope.row.retailPrice?.toFixed(2) }}
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { listGoodsSpec } from '@/api/goods/goodsSpec'
import Pagination from '@/components/Pagination/index.vue'
import type { FormInstance } from 'element-plus'

const emit = defineEmits<{
  (e: 'data-from-select', data: any[]): void
}>()

const queryFormRef = ref<FormInstance>()
const dialogVisible = ref(false)
const loading = ref(true)
const ids = ref<number[]>([])
const multiple = ref(true)
const total = ref(0)
const goodsSpecList = ref<any[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  goodsId: null as string | null,
  goodsNum: null as string | null,
  skuCode: null as string | null,
  goodsName: null as string | null
})

function openDialog() {
  getList()
  dialogVisible.value = true
}

function sendDataToParent() {
  if (!ids.value || ids.value.length === 0) {
    ElMessage.error('请选择商品')
    return
  }
  const filteredList = goodsSpecList.value.filter((item: any) => ids.value.includes(item.id))
  emit('data-from-select', filteredList)
  queryParams.pageNum = 1
  queryParams.goodsId = null
  queryParams.goodsNum = null
  queryParams.skuCode = null
  queryParams.goodsName = null
  dialogVisible.value = false
}

function handleSelectionChange(selection: any[]) {
  ids.value = selection.map((item: any) => item.id)
  multiple.value = !selection.length
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function getList() {
  loading.value = true
  listGoodsSpec(queryParams).then((response: any) => {
    goodsSpecList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

defineExpose({ openDialog })
</script>
