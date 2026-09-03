<template>
  <div class="members-page">
    <div class="page-header">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索手机号/姓名..."
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
        <el-button type="success" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          新增会员
        </el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="memberList" stripe>
      <el-table-column prop="phone" label="手机号" width="140">
        <template #default="{ row }">
          <span class="member-phone" @click="viewDetail(row)">{{ row.phone }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已确认' : '未确认' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="address" label="地址" min-width="160" show-overflow-tooltip />
      <el-table-column prop="remark" label="备注" width="140" show-overflow-tooltip />
      <el-table-column prop="createOn" label="注册时间" width="170" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
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
        @size-change="loadMembers"
        @current-change="loadMembers"
      />
    </div>

    <el-dialog v-model="showAddDialog" title="新增会员" width="450px">
      <el-form :model="memberForm" label-width="80px">
        <el-form-item label="手机号" required>
          <el-input v-model="memberForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="memberForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="memberForm.remark" type="textarea" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddMember">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" title="会员详情" width="500px">
      <div class="member-detail" v-if="currentMember">
        <div class="detail-header">
          <el-avatar :size="64">{{ currentMember.name?.charAt(0) || '会' }}</el-avatar>
          <div class="detail-info">
            <h3>{{ currentMember.name }}</h3>
            <p class="phone">{{ currentMember.phone }}</p>
            <el-tag :type="currentMember.status === 1 ? 'success' : 'info'">{{ currentMember.status === 1 ? '已确认' : '未确认' }}</el-tag>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="手机号">{{ currentMember.phone }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentMember.name }}</el-descriptions-item>
          <el-descriptions-item label="省份">{{ currentMember.province || '-' }}</el-descriptions-item>
          <el-descriptions-item label="城市">{{ currentMember.city || '-' }}</el-descriptions-item>
          <el-descriptions-item label="区">{{ currentMember.county || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ currentMember.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentMember.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ currentMember.createOn }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentMember.updateOn || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getMemberList, addMember as addMemberApi } from '@/api/pos/pos'

const loading = ref(false)
const memberList = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const searchKeyword = ref('')

const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const currentMember = ref<any>(null)

const memberForm = ref({
  phone: '',
  name: '',
  remark: '',
})

onMounted(() => {
  loadMembers()
})

async function loadMembers() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    const res: any = await getMemberList(params)
    memberList.value = res.rows || res.data || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
    ElMessage.error('加载会员列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadMembers()
}

function handleReset() {
  searchKeyword.value = ''
  pageNum.value = 1
  loadMembers()
}

function openAddDialog() {
  memberForm.value = { phone: '', name: '', remark: '' }
  showAddDialog.value = true
}

async function handleAddMember() {
  if (!memberForm.value.phone || !memberForm.value.name) {
    ElMessage.warning('请填写手机号和姓名')
    return
  }
  try {
    await addMemberApi({
      ...memberForm.value,
      shopId: 1,
      merchantId: 1,
      shopType: 100,
    })
    ElMessage.success('新增会员成功')
    showAddDialog.value = false
    loadMembers()
  } catch (e) {
    console.error(e)
    ElMessage.error('新增会员失败')
  }
}

function viewDetail(row: any) {
  currentMember.value = row
  showDetailDialog.value = true
}
</script>

<style lang="scss" scoped>
.members-page {
  padding: 0;
}

.page-header {
  margin-bottom: 16px;

  .search-bar {
    display: flex;
    gap: 12px;
    align-items: center;

    .el-input {
      width: 240px;
    }
  }
}

.member-phone {
  color: #B4471D;
  cursor: pointer;
  font-weight: 500;

  &:hover {
    text-decoration: underline;
  }
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.member-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 20px;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 12px;

    .detail-info {
      h3 {
        margin: 0 0 8px;
        font-size: 20px;
        color: #303133;
      }

      .phone {
        margin: 0 0 8px;
        color: #909399;
      }
    }
  }
}
</style>
