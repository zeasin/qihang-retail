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
      <el-table-column prop="phone" label="手机号" width="130">
        <template #default="{ row }">
          <span class="member-phone" @click="viewDetail(row)">{{ row.phone }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="gender" label="性别" width="80">
        <template #default="{ row }">
          {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="levelName" label="会员等级" width="100">
        <template #default="{ row }">
          <el-tag type="warning">{{ row.levelName || '普通会员' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="points" label="积分" width="100" align="right" />
      <el-table-column prop="balance" label="储值余额" width="120" align="right">
        <template #default="{ row }">
          <span class="balance">¥{{ formatAmount(row.balance) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="totalConsumption" label="累计消费" width="120" align="right">
        <template #default="{ row }">
          <span class="consumption">¥{{ formatAmount(row.totalConsumption) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
          <el-button type="success" link size="small" @click="openRechargeDialog(row)">充值</el-button>
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
      <el-form :model="memberForm" label-width="100px">
        <el-form-item label="手机号" required>
          <el-input v-model="memberForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="memberForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="memberForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker
            v-model="memberForm.birthday"
            type="date"
            placeholder="选择生日"
            value-format="YYYY-MM-DD"
          />
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
            <el-tag type="warning">{{ currentMember.levelName || '普通会员' }}</el-tag>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="性别">{{ currentMember.gender === 1 ? '男' : currentMember.gender === 2 ? '女' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="生日">{{ currentMember.birthday || '-' }}</el-descriptions-item>
          <el-descriptions-item label="积分">{{ currentMember.points || 0 }}</el-descriptions-item>
          <el-descriptions-item label="储值余额">
            <span class="balance">¥{{ formatAmount(currentMember.balance) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="累计消费">
            <span class="consumption">¥{{ formatAmount(currentMember.totalConsumption) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="到店次数">{{ currentMember.visitCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ currentMember.createTime }}</el-descriptions-item>
          <el-descriptions-item label="最后到店">{{ currentMember.lastVisitTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <el-dialog v-model="showRechargeDialog" title="会员充值" width="400px">
      <el-form :model="rechargeForm" label-width="100px">
        <el-form-item label="会员">
          <span>{{ currentMember?.name }} ({{ currentMember?.phone }})</span>
        </el-form-item>
        <el-form-item label="充值金额" required>
          <el-input-number v-model="rechargeForm.amount" :min="1" :max="100000" :precision="2" />
        </el-form-item>
        <el-form-item label="赠送金额">
          <el-input-number v-model="rechargeForm.giftAmount" :min="0" :max="10000" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRechargeDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
const showRechargeDialog = ref(false)
const currentMember = ref<any>(null)

const memberForm = ref({
  phone: '',
  name: '',
  gender: 1,
  birthday: '',
})

const rechargeForm = ref({
  amount: 100,
  giftAmount: 0,
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
    const res = await getMemberList(params)
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
  memberForm.value = {
    phone: '',
    name: '',
    gender: 1,
    birthday: '',
  }
  showAddDialog.value = true
}

async function handleAddMember() {
  if (!memberForm.value.phone || !memberForm.value.name) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    await addMemberApi({
      ...memberForm.value,
      shopId: 1,
      merchantId: 1,
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

function openRechargeDialog(row: any) {
  currentMember.value = row
  rechargeForm.value = {
    amount: 100,
    giftAmount: 0,
  }
  showRechargeDialog.value = true
}

async function confirmRecharge() {
  try {
    await ElMessageBox.confirm(
      `确认充值 ¥${rechargeForm.value.amount.toFixed(2)}？`,
      '充值确认',
      { type: 'warning' }
    )
    ElMessage.success('充值成功（待后端实现）')
    showRechargeDialog.value = false
    loadMembers()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('充值失败')
    }
  }
}

function formatAmount(amount: any) {
  if (!amount) return '0.00'
  return Number(amount).toFixed(2)
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

.balance {
  color: #67C23A;
  font-weight: 600;
}

.consumption {
  color: #B4471D;
  font-weight: 600;
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
