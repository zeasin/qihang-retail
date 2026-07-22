<template>
  <div class="pos-category-nav">
    <div class="nav-header">
      <h3 class="nav-title">商品分类</h3>
      <el-badge v-if="cartCount > 0" :value="cartCount" class="cart-badge">
        <el-icon :size="20" color="#B4471D"><ShoppingCart /></el-icon>
      </el-badge>
    </div>
    
    <div class="search-box">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品/条码..."
        size="large"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>
    
    <div class="category-list">
      <div
        class="category-item"
        :class="{ active: selectedCategory === 'all' }"
        @click="selectCategory('all')"
      >
        <span class="category-icon">📦</span>
        <span class="category-name">全部商品</span>
        <span class="category-count">{{ totalCount }}</span>
      </div>
      
      <div
        v-for="cat in categories"
        :key="cat.id"
        class="category-item"
        :class="{ active: selectedCategory === cat.id }"
        @click="selectCategory(cat.id)"
      >
        <span class="category-icon">{{ getCategoryIcon(cat.id) }}</span>
        <span class="category-name">{{ cat.name }}</span>
      </div>
    </div>
    
    <div class="nav-footer">
      <el-button class="quick-btn" @click="$emit('quickAction', 'scan')">
        <el-icon><VideoPlay /></el-icon>
        <span>扫码添加</span>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ShoppingCart, Search, VideoPlay } from '@element-plus/icons-vue'
import { listCategory } from '@/api/goods/category'

const props = defineProps<{
  cartCount: number
  totalCount: number
}>()

const emit = defineEmits<{
  selectCategory: [categoryId: string | number]
  search: [keyword: string]
  quickAction: [action: string]
}>()

const searchKeyword = ref('')
const selectedCategory = ref<string | number>('all')
const categories = ref<any[]>([])

const categoryIcons = ['🍎', '🥤', '🍜', '🍰', '☕', '🥗', '🍖', '🥦', '🍚', '🍞', '🍫', '🍬']

function getCategoryIcon(index: number) {
  const categoryIndex = index % categoryIcons.length
  return categoryIcons[categoryIndex]
}

async function loadCategories() {
  try {
    const response = await listCategory({})
    categories.value = response.rows || []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

function selectCategory(id: string | number) {
  selectedCategory.value = id
  emit('selectCategory', id)
}

function handleSearch() {
  emit('search', searchKeyword.value)
}

onMounted(() => {
  loadCategories()
})
</script>

<style lang="scss" scoped>
.pos-category-nav {
  width: 220px;
  height: 100%;
  background: var(--pos-bg-secondary);
  border-right: 1px solid var(--pos-border);
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.nav-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  
  .nav-title {
    font-size: 18px;
    font-weight: 700;
    color: var(--pos-text-primary);
    margin: 0;
  }
}

.search-box {
  margin-bottom: 16px;
  
  :deep(.el-input__wrapper) {
    background: var(--pos-bg-input) !important;
    box-shadow: none !important;
    border: 1px solid var(--pos-border);
    border-radius: 10px;
    
    &:hover {
      border-color: var(--pos-border-light);
    }
    
    &.is-focus {
      border-color: var(--pos-accent-secondary);
      box-shadow: 0 0 0 2px rgba(180, 71, 29, 0.2) !important;
    }
  }
  
  :deep(.el-input__inner) {
    color: var(--pos-text-primary);
    
    &::placeholder {
      color: var(--pos-text-muted);
    }
  }
  
  :deep(.el-input__prefix-inner) {
    color: var(--pos-text-muted);
  }
}

.category-list {
  flex: 1;
  overflow-y: auto;
  
  .category-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 14px;
    margin-bottom: 6px;
    border-radius: 10px;
    cursor: pointer;
    background: transparent;
    transition: all 0.2s ease;
    
    &:hover {
      background: var(--pos-bg-hover);
    }
    
    &.active {
      background: var(--pos-accent-secondary);
      
      .category-icon,
      .category-name,
      .category-count {
        color: white;
      }
    }
    
    .category-icon {
      font-size: 20px;
    }
    
    .category-name {
      flex: 1;
      font-size: 14px;
      font-weight: 500;
      color: var(--pos-text-secondary);
    }
    
    .category-count {
      font-size: 12px;
      font-weight: 600;
      color: var(--pos-text-muted);
      background: var(--pos-bg-input);
      padding: 2px 8px;
      border-radius: 10px;
    }
  }
}

.nav-footer {
  padding-top: 12px;
  border-top: 1px solid var(--pos-border);
  
  .quick-btn {
    width: 100%;
    background: var(--pos-accent-secondary);
    border: none;
    color: white;
    height: 44px;
    font-size: 14px;
    font-weight: 600;
    border-radius: 10px;
    
    &:hover {
      background: var(--pos-accent-hover);
    }
  }
}

.cart-badge {
  :deep(.el-badge__content) {
    background: var(--pos-accent-primary);
  }
}
</style>
