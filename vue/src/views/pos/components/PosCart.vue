<template>
  <div class="pos-cart">
    <div class="cart-header">
      <h3 class="cart-title">当前订单</h3>
      <div class="cart-actions">
        <el-tooltip content="清空购物车" placement="top">
          <el-icon :size="18" class="action-icon" @click="handleClearCart"><Delete /></el-icon>
        </el-tooltip>
      </div>
    </div>
    
    <div v-if="items.length === 0" class="empty-cart">
      <el-icon :size="48" color="#5E6266"><ShoppingCart /></el-icon>
      <p>购物车为空</p>
      <span>请从左侧选择商品</span>
    </div>
    
    <div v-else class="cart-items">
      <div
        v-for="(item, index) in items"
        :key="item.skuId || item.goodsId + '-' + index"
        class="cart-item"
      >
        <div class="item-image">
          <el-image
            v-if="item.image"
            :src="item.image"
            fit="cover"
          >
            <template #error>
              <div class="image-fallback">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div v-else class="image-fallback">
            <el-icon><Picture /></el-icon>
          </div>
        </div>
        
        <div class="item-info">
          <h4 class="item-name">{{ item.name }}</h4>
          <span v-if="item.skuName" class="item-sku">{{ item.skuName }}</span>
          <div class="item-price">¥{{ item.price?.toFixed(2) || '0.00' }}</div>
        </div>
        
        <div class="item-quantity">
          <el-button size="small" circle class="qty-btn" @click="decreaseQty(index)">
            <el-icon><Minus /></el-icon>
          </el-button>
          <span class="qty-value">{{ item.quantity }}</span>
          <el-button size="small" circle class="qty-btn" @click="increaseQty(index)">
            <el-icon><Plus /></el-icon>
          </el-button>
        </div>
        
        <div class="item-subtotal">
          ¥{{ (item.price * item.quantity)?.toFixed(2) || '0.00' }}
        </div>
        
        <el-icon :size="16" class="remove-icon" @click="removeItem(index)"><Close /></el-icon>
      </div>
    </div>
    
    <div class="cart-summary">
      <div class="summary-row">
        <span>商品数量</span>
        <span>{{ totalItems }} 件</span>
      </div>
      <div class="summary-row">
        <span>商品金额</span>
        <span>¥{{ subtotal.toFixed(2) }}</span>
      </div>
      <div class="summary-row discount">
        <span>优惠</span>
        <span>-¥{{ discount.toFixed(2) }}</span>
      </div>
      <div class="summary-row total">
        <span>应付金额</span>
        <span class="total-amount">¥{{ total.toFixed(2) }}</span>
      </div>
    </div>
    
    <div class="cart-footer">
      <el-button
        class="hold-btn"
        :disabled="items.length === 0"
        @click="$emit('hold')"
      >
        <el-icon><Document /></el-icon>
        挂单
      </el-button>
      <el-button
        class="checkout-btn"
        type="primary"
        :disabled="items.length === 0"
        @click="$emit('checkout')"
      >
        <el-icon><Wallet /></el-icon>
        结账
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ShoppingCart, Picture, Minus, Plus, Close, Delete, Document, Wallet } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps<{
  items: any[]
  discount: number
}>()

const emit = defineEmits<{
  removeItem: [index: number]
  updateQuantity: [index: number, quantity: number]
  clearCart: []
  checkout: []
  hold: []
}>()

const totalItems = computed(() => {
  return props.items.reduce((sum, item) => sum + item.quantity, 0)
})

const subtotal = computed(() => {
  return props.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const total = computed(() => {
  return Math.max(0, subtotal.value - props.discount)
})

function increaseQty(index: number) {
  const item = props.items[index]
  emit('updateQuantity', index, item.quantity + 1)
}

function decreaseQty(index: number) {
  const item = props.items[index]
  if (item.quantity > 1) {
    emit('updateQuantity', index, item.quantity - 1)
  } else {
    removeItem(index)
  }
}

function removeItem(index: number) {
  emit('removeItem', index)
  ElMessage.success('已移除商品')
}

async function handleClearCart() {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    emit('clearCart')
    ElMessage.success('购物车已清空')
  } catch {}
}
</script>

<style lang="scss" scoped>
.pos-cart {
  width: 380px;
  height: 100%;
  background: var(--pos-bg-secondary);
  display: flex;
  flex-direction: column;
  border-left: 1px solid var(--pos-border);
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--pos-border);
  
  .cart-title {
    font-size: 18px;
    font-weight: 700;
    color: var(--pos-text-primary);
    margin: 0;
  }
  
  .action-icon {
    color: var(--pos-text-muted);
    cursor: pointer;
    transition: color 0.2s;
    
    &:hover {
      color: var(--pos-error);
    }
  }
}

.empty-cart {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  
  p {
    font-size: 16px;
    font-weight: 600;
    color: var(--pos-text-secondary);
    margin: 0;
  }
  
  span {
    font-size: 13px;
    color: var(--pos-text-muted);
  }
}

.cart-items {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  
  .cart-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: var(--pos-bg-card);
    border-radius: 10px;
    margin-bottom: 10px;
    position: relative;
    
    &:hover {
      background: var(--pos-bg-hover);
    }
  }
}

.item-image {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--pos-bg-input);
  
  .image-fallback {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--pos-text-muted);
  }
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.item-info {
  flex: 1;
  min-width: 0;
  
  .item-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--pos-text-primary);
    margin: 0 0 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .item-sku {
    display: block;
    font-size: 12px;
    color: var(--pos-text-muted);
    margin-bottom: 4px;
  }
  
  .item-price {
    font-size: 14px;
    font-weight: 600;
    color: var(--pos-text-secondary);
  }
}

.item-quantity {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .qty-btn {
    width: 28px;
    height: 28px;
    background: var(--pos-bg-input);
    border: none;
    color: var(--pos-text-primary);
    
    &:hover {
      background: var(--pos-accent-secondary);
      color: white;
    }
  }
  
  .qty-value {
    min-width: 30px;
    text-align: center;
    font-size: 14px;
    font-weight: 600;
    color: var(--pos-text-primary);
  }
}

.item-subtotal {
  font-size: 14px;
  font-weight: 700;
  color: var(--pos-accent-secondary);
  min-width: 70px;
  text-align: right;
}

.remove-icon {
  position: absolute;
  top: 8px;
  right: 8px;
  color: var(--pos-text-muted);
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
  
  .cart-item:hover & {
    opacity: 1;
    
    &:hover {
      color: var(--pos-error);
    }
  }
}

.cart-summary {
  padding: 16px 20px;
  border-top: 1px solid var(--pos-border);
  background: var(--pos-bg-card);
  
  .summary-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    font-size: 14px;
    color: var(--pos-text-secondary);
    
    &.discount span:last-child {
      color: var(--pos-success);
    }
    
    &.total {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid var(--pos-border);
      
      span {
        font-size: 16px;
        font-weight: 600;
        color: var(--pos-text-primary);
      }
      
      .total-amount {
        font-size: 24px;
        font-weight: 700;
        color: var(--pos-accent-secondary);
      }
    }
  }
}

.cart-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: var(--pos-bg-card);
  border-top: 1px solid var(--pos-border);
  
  .hold-btn,
  .checkout-btn {
    flex: 1;
    height: 48px;
    font-size: 15px;
    font-weight: 600;
    border-radius: 10px;
    
    .el-icon {
      margin-right: 6px;
    }
  }
  
  .hold-btn {
    background: transparent;
    border: 1px solid var(--pos-border-light);
    color: var(--pos-text-secondary);
    
    &:hover:not(:disabled) {
      border-color: var(--pos-accent-secondary);
      color: var(--pos-accent-secondary);
    }
  }
  
  .checkout-btn {
    background: var(--pos-accent-secondary);
    border: none;
    color: white;
    
    &:hover:not(:disabled) {
      background: var(--pos-accent-hover);
    }
    
    &:disabled {
      background: var(--pos-bg-input);
      color: var(--pos-text-muted);
    }
  }
}
</style>
