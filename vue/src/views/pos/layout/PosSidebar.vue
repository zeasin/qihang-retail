<template>
  <div class="pos-sidebar">
    <div class="sidebar-logo">
      <div class="logo-icon">
        <el-icon :size="24"><Shop /></el-icon>
      </div>
      <span class="logo-text">POS收银</span>
    </div>
    
    <nav class="sidebar-nav">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        active-class="active"
      >
        <el-icon :size="20">
          <component :is="item.icon" />
        </el-icon>
        <span class="nav-label">{{ item.label }}</span>
      </router-link>
    </nav>
    
    <div class="sidebar-footer">
      <router-link to="/" class="back-link">
        <el-icon :size="16"><Monitor /></el-icon>
        <span>返回管理后台</span>
      </router-link>
      <div class="user-info">
        <el-avatar :size="32" :src="userStore.avatar" />
        <div class="user-detail">
          <span class="user-name">{{ userStore.name }}</span>
          <span class="user-role">收银员</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Shop, Monitor, ShoppingCart, List, User, DataAnalysis, Coin } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

const navItems = [
  { path: '/pos/cashier', label: '收银台', icon: ShoppingCart },
  { path: '/pos/orders', label: '订单查询', icon: List },
  { path: '/pos/members', label: '会员管理', icon: User },
  { path: '/pos/refund', label: '退款管理', icon: Coin },
  { path: '/pos/reports', label: '交班结算', icon: DataAnalysis },
]
</script>

<style lang="scss" scoped>
.pos-sidebar {
  width: 200px;
  height: 100vh;
  background: #1a1a1a;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  border-bottom: 1px solid #333;
  
  .logo-icon {
    width: 36px;
    height: 36px;
    background: linear-gradient(135deg, #B4471D, #D96A3F);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
  }
  
  .logo-text {
    font-size: 16px;
    font-weight: 700;
    color: #fff;
  }
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #444;
    border-radius: 2px;
  }
  
  .nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 14px;
    margin-bottom: 4px;
    border-radius: 10px;
    color: #999;
    text-decoration: none;
    transition: all 0.2s ease;
    
    &:hover {
      background: #2a2a2a;
      color: #fff;
    }
    
    &.active {
      background: linear-gradient(135deg, #B4471D, #D96A3F);
      color: #fff;
      
      .el-icon {
        color: #fff;
      }
    }
    
    .el-icon {
      color: #999;
    }
    
    .nav-label {
      flex: 1;
      font-size: 14px;
      font-weight: 500;
    }
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #333;
  
  .back-link {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 14px;
    margin-bottom: 12px;
    border-radius: 8px;
    color: #666;
    text-decoration: none;
    font-size: 13px;
    transition: all 0.2s;
    
    &:hover {
      background: #2a2a2a;
      color: #fff;
    }
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-top: 12px;
    border-top: 1px solid #333;
    
    .user-detail {
      flex: 1;
      display: flex;
      flex-direction: column;
      
      .user-name {
        font-size: 14px;
        font-weight: 600;
        color: #fff;
      }
      
      .user-role {
        font-size: 12px;
        color: #666;
      }
    }
  }
}
</style>
