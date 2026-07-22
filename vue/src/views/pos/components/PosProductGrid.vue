<template>
  <div class="pos-product-grid">
    <div class="grid-header">
      <div class="header-left">
        <span class="current-filter">
          {{ filterLabel }}
        </span>
        <span class="product-count">共 {{ products.length }} 件商品</span>
      </div>
      <div class="header-right">
        <el-radio-group v-model="viewMode" size="default" @change="emitViewMode">
          <el-radio-button value="grid">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
          <el-radio-button value="list">
            <el-icon><List /></el-icon>
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>
    
    <div v-if="loading" class="loading-state">
      <el-icon class="loading-icon" :size="32" color="#B4471D"><Loading /></el-icon>
      <span>加载商品中...</span>
    </div>
    
    <div v-else-if="products.length === 0" class="empty-state">
      <el-icon :size="64" color="#5E6266"><Box /></el-icon>
      <p>暂无商品</p>
    </div>
    
    <div v-else class="product-container" :class="'view-' + viewMode">
      <div
        v-for="product in products"
        :key="product.id"
        class="product-card"
        :class="{ 'has-stock': product.stock > 0, 'out-of-stock': product.stock === 0 }"
        @click="$emit('select', product)"
      >
        <div class="product-image">
          <el-image
            v-if="product.image"
            :src="product.image"
            fit="cover"
            class="image-content"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon :size="32"><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div v-else class="image-placeholder">
            <el-icon :size="32"><Picture /></el-icon>
          </div>
          
          <div v-if="product.stock <= 0" class="stock-badge out">缺货</div>
          <div v-else-if="product.stock <= 10" class="stock-badge low">库存 {{ product.stock }}</div>
        </div>
        
        <div class="product-info">
          <h4 class="product-name">{{ product.name }}</h4>
          <div class="product-meta">
            <span class="product-code">{{ product.goodsNum }}</span>
          </div>
          <div class="product-footer">
            <span class="product-price">
              <em>¥</em>{{ product.retailPrice?.toFixed(2) || '0.00' }}
            </span>
            <el-button class="add-btn" type="primary" size="small" circle>
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="hasMore" class="load-more">
      <el-button @click="loadMore">
        <el-icon><Refresh /></el-icon>
        加载更多
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Grid, List, Loading, Box, Picture, Plus, Refresh } from '@element-plus/icons-vue'

defineProps<{
  products: any[]
  loading: boolean
  hasMore: boolean
  filterLabel: string
}>()

const emit = defineEmits<{
  select: [product: any]
  loadMore: []
  viewModeChange: [mode: string]
}>()

const viewMode = ref<'grid' | 'list'>('grid')

function emitViewMode() {
  emit('viewModeChange', viewMode.value)
}
</script>

<style lang="scss" scoped>
.pos-product-grid {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--pos-bg-primary);
  overflow: hidden;
}

.grid-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--pos-border);
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .current-filter {
      font-size: 16px;
      font-weight: 600;
      color: var(--pos-text-primary);
    }
    
    .product-count {
      font-size: 13px;
      color: var(--pos-text-muted);
    }
  }
  
  .header-right {
    :deep(.el-radio-group) {
      background: var(--pos-bg-card);
      border-radius: 8px;
      padding: 2px;
      
      .el-radio-button {
        border: none;
        background: transparent;
        padding: 6px 12px;
        border-radius: 6px;
        
        &.is-active {
          background: var(--pos-accent-secondary);
          
          .el-icon {
            color: white;
          }
        }
        
        .el-icon {
          font-size: 16px;
          color: var(--pos-text-muted);
        }
      }
    }
  }
}

.loading-state,
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: var(--pos-text-muted);
  
  .loading-icon {
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.product-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  gap: 16px;
  
  &.view-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
  
  &.view-list {
    display: flex;
    flex-direction: column;
  }
}

.product-card {
  background: var(--pos-bg-card);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  
  &:hover {
    transform: translateY(-2px);
    border-color: var(--pos-accent-secondary);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  }
  
  &.out-of-stock {
    opacity: 0.6;
    pointer-events: none;
  }
  
  &.view-list-item {
    display: flex;
    padding: 12px;
  }
}

.product-image {
  position: relative;
  width: 100%;
  height: 160px;
  background: var(--pos-bg-input);
  
  .image-content {
    width: 100%;
    height: 100%;
  }
  
  .image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--pos-text-muted);
  }
  
  .stock-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    
    &.out {
      background: rgba(245, 108, 108, 0.9);
      color: white;
    }
    
    &.low {
      background: rgba(230, 162, 60, 0.9);
      color: white;
    }
  }
}

.product-info {
  padding: 12px;
  
  .product-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--pos-text-primary);
    margin: 0 0 6px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .product-meta {
    margin-bottom: 12px;
    
    .product-code {
      font-size: 12px;
      color: var(--pos-text-muted);
    }
  }
  
  .product-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .product-price {
      font-size: 18px;
      font-weight: 700;
      color: var(--pos-accent-secondary);
      
      em {
        font-style: normal;
        font-size: 14px;
      }
    }
    
    .add-btn {
      background: var(--pos-accent-secondary);
      border-color: var(--pos-accent-secondary);
      
      &:hover {
        background: var(--pos-accent-hover);
        border-color: var(--pos-accent-hover);
      }
    }
  }
}

.load-more {
  padding: 16px;
  text-align: center;
  
  :deep(.el-button) {
    background: transparent;
    border: 1px solid var(--pos-border);
    color: var(--pos-text-secondary);
    
    &:hover {
      border-color: var(--pos-accent-secondary);
      color: var(--pos-accent-secondary);
    }
  }
}
</style>
