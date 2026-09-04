<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px" class="mb8">
      <el-form-item label="店铺名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入店铺名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="平台类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择平台类型" clearable>
          <el-option v-for="item in platformOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="已删除" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
        <el-button @click="resetQuery"><el-icon><Refresh /></el-icon>重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain size="small" @click="handleAdd"><el-icon><Plus /></el-icon>新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain size="small" :disabled="single" @click="handleUpdate"><el-icon><Edit /></el-icon>修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain size="small" :disabled="multiple" @click="handleDelete"><el-icon><Delete /></el-icon>删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="店铺名称" align="center" prop="name" min-width="160" show-overflow-tooltip />
      <el-table-column label="平台类型" align="center" prop="type" width="120">
        <template #default="scope">
          <el-tag :type="getPlatformTagType(scope.row.type)">{{ getPlatformName(scope.row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="商户编码" align="center" prop="sellerNum" width="120" show-overflow-tooltip />
      <el-table-column label="联系人" align="center" prop="contact" width="100" />
      <el-table-column label="联系电话" align="center" prop="phone" width="120" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '启用' : '已删除' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="共享库存" align="center" prop="allowInventoryShare" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.allowInventoryShare === 1 ? 'success' : 'info'">{{ scope.row.allowInventoryShare === 1 ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="70" />
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="small" type="text" @click="handleUpdate(scope.row)"><el-icon><Edit /></el-icon>修改</el-button>
          <el-button size="small" type="text" @click="handleDelete(scope.row)"><el-icon><Delete /></el-icon>删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="店铺名称" prop="name">
                  <el-input v-model="form.name" placeholder="请输入店铺名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="平台类型" prop="type">
                  <el-select v-model="form.type" placeholder="请选择平台类型" style="width: 100%">
                    <el-option v-for="item in platformOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="商户编码" prop="sellerNum">
                  <el-input v-model="form.sellerNum" placeholder="请输入商户编码" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系人" prop="contact">
                  <el-input v-model="form.contact" placeholder="请输入联系人" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status">
                    <el-radio :label="1">启用</el-radio>
                    <el-radio :label="2">停用</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="共享库存" prop="allowInventoryShare">
                  <el-radio-group v-model="form.allowInventoryShare">
                    <el-radio :label="1">是</el-radio>
                    <el-radio :label="0">否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="排序" prop="sort">
                  <el-input-number v-model="form.sort" :min="0" :max="999" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-tab-pane>
          <el-tab-pane label="API配置" name="api">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="AppKey" prop="appKey">
                  <el-input v-model="form.appKey" placeholder="请输入AppKey" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="AppSecret" prop="appSecret">
                  <el-input v-model="form.appSecret" placeholder="请输入AppSecret" show-password />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="SellerID" prop="sellerId">
                  <el-input v-model="form.sellerId" placeholder="请输入第三方店铺ID" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="店铺URL" prop="url">
                  <el-input v-model="form.url" placeholder="请输入店铺URL" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>
          <el-tab-pane label="地址信息" name="address">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="省" prop="province">
                  <el-input v-model="form.province" placeholder="省" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="市" prop="city">
                  <el-input v-model="form.city" placeholder="市" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="区" prop="district">
                  <el-input v-model="form.district" placeholder="区" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="详细地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入详细地址" />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'
import { listShop, getShop, addShop, updateShop, delShop } from '@/api/shop/shop'
import { parseTime } from '@/utils/zhijian'
import RightToolbar from '@/components/RightToolbar/index.vue'
import Pagination from '@/components/Pagination/index.vue'
import type { FormInstance } from 'element-plus'

const loading = ref(true)
const ids = ref<number[]>([])
const single = ref(true)
const multiple = ref(true)
const showSearch = ref(true)
const total = ref(0)
const list = ref<any[]>([])
const title = ref('')
const open = ref(false)
const activeTab = ref('basic')
const formRef = ref<FormInstance>()
const queryFormRef = ref<FormInstance>()

const platformOptions = [
  { label: '淘宝天猫', value: 100 },
  { label: '京东POP', value: 200 },
  { label: '拼多多', value: 300 },
  { label: '抖店', value: 400 },
  { label: '微信小店', value: 500 },
  { label: '快手小店', value: 600 },
  { label: '小红书', value: 700 },
  { label: '美团闪购', value: 1100 },
  { label: '淘宝闪购', value: 1200 },
  { label: '京东到家', value: 1300 },
  { label: '抖音小时达', value: 1400 },
  { label: '线下门店', value: 999 }
]

const platformMap: Record<number, { name: string; tagType: string }> = {
  100: { name: '淘宝天猫', tagType: '' },
  200: { name: '京东POP', tagType: 'danger' },
  300: { name: '拼多多', tagType: 'warning' },
  400: { name: '抖店', tagType: '' },
  500: { name: '微信小店', tagType: 'success' },
  600: { name: '快手小店', tagType: 'warning' },
  700: { name: '小红书', tagType: 'danger' },
  1100: { name: '美团闪购', tagType: 'warning' },
  1200: { name: '淘宝闪购', tagType: '' },
  1300: { name: '京东到家', tagType: 'danger' },
  1400: { name: '抖音小时达', tagType: '' },
  999: { name: '线下门店', tagType: 'info' }
}

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: undefined,
  type: undefined,
  status: undefined
})

const form = reactive<Record<string, any>>({
  id: undefined,
  name: undefined,
  type: undefined,
  sellerNum: undefined,
  contact: undefined,
  phone: undefined,
  status: 1,
  allowInventoryShare: 0,
  sort: 0,
  remark: undefined,
  appKey: undefined,
  appSecret: undefined,
  sellerId: undefined,
  url: undefined,
  province: undefined,
  city: undefined,
  district: undefined,
  address: undefined
})

const rules = {
  name: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择平台类型', trigger: 'change' }]
}

onMounted(() => { getList() })

function getPlatformName(type: number): string {
  return platformMap[type]?.name || '未知平台'
}

function getPlatformTagType(type: number): string {
  return platformMap[type]?.tagType || 'info'
}

function getList() {
  loading.value = true
  listShop(queryParams).then((res: any) => {
    list.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryFormRef.value?.resetFields()
  handleQuery()
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.id = undefined
  form.name = undefined
  form.type = undefined
  form.sellerNum = undefined
  form.contact = undefined
  form.phone = undefined
  form.status = 1
  form.allowInventoryShare = 0
  form.sort = 0
  form.remark = undefined
  form.appKey = undefined
  form.appSecret = undefined
  form.sellerId = undefined
  form.url = undefined
  form.province = undefined
  form.city = undefined
  form.district = undefined
  form.address = undefined
  activeTab.value = 'basic'
  formRef.value?.resetFields()
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增店铺'
}

function handleSelectionChange(selection: any[]) {
  ids.value = selection.map((item: any) => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleUpdate(row: any) {
  reset()
  const id = row.id || ids.value[0]
  getShop(id).then((res: any) => {
    Object.assign(form, res.data || {})
    open.value = true
    title.value = '修改店铺'
  })
}

function submitForm() {
  formRef.value?.validate((valid: boolean) => {
    if (valid) {
      if (form.id != undefined) {
        updateShop({ ...form }).then(() => {
          ElMessage.success('修改成功')
          open.value = false
          getList()
        })
      } else {
        addShop({ ...form }).then(() => {
          ElMessage.success('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row: any) {
  const idsArr = row.id || ids.value
  ElMessageBox.confirm('是否确认删除"' + (row.name || '') + '"？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return delShop(idsArr)
  }).then(() => {
    getList()
    ElMessage.success('删除成功')
  }).catch(() => {})
}
</script>
