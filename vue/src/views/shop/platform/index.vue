<template>
  <div class="app-container">
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
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="平台名称" align="center" prop="name" min-width="140" />
      <el-table-column label="平台编码" align="center" prop="code" width="120" />
      <el-table-column label="AppKey" align="center" prop="appKey" min-width="180" show-overflow-tooltip />
      <el-table-column label="AppSecret" align="center" prop="appSecret" min-width="180" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.appSecret ? '******' : '' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="回调URI" align="center" prop="redirectUri" min-width="240" show-overflow-tooltip />
      <el-table-column label="接口地址" align="center" prop="serverUrl" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">{{ scope.row.status === 0 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="small" type="text" @click="handleUpdate(scope.row)"><el-icon><Edit /></el-icon>修改</el-button>
          <el-button size="small" type="text" @click="handleDelete(scope.row)"><el-icon><Delete /></el-icon>删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="平台名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入平台名称" />
        </el-form-item>
        <el-form-item label="平台编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入平台编码" />
        </el-form-item>
        <el-form-item label="AppKey" prop="appKey">
          <el-input v-model="form.appKey" placeholder="请输入AppKey" />
        </el-form-item>
        <el-form-item label="AppSecret" prop="appSecret">
          <el-input v-model="form.appSecret" placeholder="请输入AppSecret" show-password />
        </el-form-item>
        <el-form-item label="回调URI" prop="redirectUri">
          <el-input v-model="form.redirectUri" placeholder="请输入回调URI" />
        </el-form-item>
        <el-form-item label="接口地址" prop="serverUrl">
          <el-input v-model="form.serverUrl" placeholder="请输入接口访问地址" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">启用</el-radio>
            <el-radio :label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
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
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { listPlatform, getPlatform, addPlatform, updatePlatform, delPlatform } from '@/api/shop/platform'
import type { FormInstance } from 'element-plus'

const loading = ref(true)
const ids = ref<number[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const list = ref<any[]>([])
const title = ref('')
const open = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<Record<string, any>>({
  id: undefined,
  name: undefined,
  code: undefined,
  appKey: undefined,
  appSecret: undefined,
  redirectUri: undefined,
  serverUrl: undefined,
  status: 0,
  sort: 0
})

const rules = {
  name: [{ required: true, message: '请输入平台名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入平台编码', trigger: 'blur' }]
}

onMounted(() => { getList() })

function getList() {
  loading.value = true
  listPlatform().then((res: any) => {
    list.value = res.data || []
    total.value = list.value.length
    loading.value = false
  }).catch(() => { loading.value = false })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.id = undefined
  form.name = undefined
  form.code = undefined
  form.appKey = undefined
  form.appSecret = undefined
  form.redirectUri = undefined
  form.serverUrl = undefined
  form.status = 0
  form.sort = 0
  formRef.value?.resetFields()
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增电商平台'
}

function handleSelectionChange(selection: any[]) {
  ids.value = selection.map((item: any) => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleUpdate(row: any) {
  reset()
  const id = row.id || ids.value[0]
  getPlatform(id).then((res: any) => {
    Object.assign(form, res.data || {})
    open.value = true
    title.value = '修改电商平台'
  })
}

function submitForm() {
  formRef.value?.validate((valid: boolean) => {
    if (valid) {
      if (form.id != undefined) {
        updatePlatform({ ...form }).then(() => {
          ElMessage.success('修改成功')
          open.value = false
          getList()
        })
      } else {
        addPlatform({ ...form }).then(() => {
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
    return delPlatform(idsArr)
  }).then(() => {
    getList()
    ElMessage.success('删除成功')
  }).catch(() => {})
}
</script>
