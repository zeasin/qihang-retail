<template>
  <div class="pos-sidebar">
    <div class="sidebar-logo">
      <el-icon :size="24" color="#B4471D"><Shop /></el-icon>
      <span class="logo-text">启航 POS</span>
    </div>
    
    <nav class="sidebar-nav">
      <div
        v-for="item in navItems"
        :key="item.key"
        class="nav-item"
        :class="{ active: activeKey === item.key }"
        @click="$emit('navigate', item.key)"
      >
        <el-icon :size="20">
          <component :is="item.icon" />
        </el-icon>
        <span class="nav-label">{{ item.label }}</span>
        <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
      </div>
    </nav>
    
    <div class="sidebar-footer">
      <div class="user-info">
        <el-avatar :size="32" :src="userStore.avatar" />
        <div class="user-detail">
          <span class="user-name">{{ userStore.name }}</span>
          <span class="user-role">收银员</span>
        </div>
      </div>
      <div class="sidebar-actions">
        <el-tooltip content="设置" placement="right">
          <el-icon :size="18" class="action-icon"><Setting /></el-icon>
        </el-tooltip>
        <el-tooltip content="退出" placement="right">
          <el-icon :size="18" class="action-icon" @click="handleLogout"><SwitchButton /></el-icon>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Shop, ShoppingCart, List, User, DataAnalysis, Setting, SwitchButton, Coin, Box } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

defineProps<{
  activeKey: string
}>()

defineEmits<{
  navigate: [key: string]
}>()

const userStore = useUserStore()
const router = useRouter()

const navItems = [
  { key: 'cashier', label: '收银台', icon: ShoppingCart, badge: null },
  { key: 'orders', label: '订单管理', icon: List, badge: null },
  { key: 'members', label: '会员管理', icon: User, badge: null },
  { key: 'goods', label: '商品管理', icon: Box, badge: null },
  { key: 'reports', label: '营业报表', icon: DataAnalysis, badge: null },
  { key: 'refund', label: '退款管理', icon: Coin, badge: null },
]

function handleLogout() {
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.pos-sidebar {
  width: 200px;
  height: 100vh;
  background: var(--pos-bg-secondary);
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--pos-border);
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  border-bottom: 1px solid var(--pos-border);
  
  .logo-text {
    font-size: 16px;
    font-weight: 700;
    color: var(--pos-text-primary);
  }
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
  
  .nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 14px;
    margin-bottom: 4px;
    border-radius: 10px;
    cursor: pointer;
    color: var(--pos-text-secondary);
    transition: all 0.2s ease;
    
    &:hover {
      background: var(--pos-bg-hover);
      color: var(--pos-text-primary);
    }
    
    &.active {
      background: var(--pos-accent-secondary);
      color: white;
      
      .nav-badge {
        background: rgba(255, 255, 255, 0.2);
        color: white;
      }
    }
    
    .nav-label {
      flex: 1;
      font-size: 14px;
      font-weight: 500;
    }
    
    .nav-badge {
      background: var(--pos-accent-primary);
      color: white;
      font-size: 11px;
      padding: 2px 8px;
      border-radius: 10px;
      font-weight: 600;
    }
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--pos-border);
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
    
    .user-detail {
      flex: 1;
      display: flex;
      flex-direction: column;
      
      .user-name {
        font-size: 14px;
        font-weight: 600;
        color: var(--pos-text-primary);
      }
      
      .user-role {
        font-size: 12px;
        color: var(--pos-text-muted);
      }
    }
  }
  
  .sidebar-actions {
    display: flex;
    gap: 8px;
    justify-content: flex-end;
    
    .action-icon {
      color: var(--pos-text-muted);
      cursor: pointer;
      transition: color 0.2s ease;
      
      &:hover {
        color: var(--pos-accent-secondary);
      }
    }
  }
}
</style>
