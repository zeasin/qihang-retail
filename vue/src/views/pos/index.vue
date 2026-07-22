<template>
  <div class="pos-root">
    <div class="pos-body">
      <div class="main-area">
        <div class="toolbar">
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品/扫码..."
              clearable
              size="large"
            >
              <template #prefix>
                <el-icon :size="18"><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="categories">
            <div
              class="cat-item"
              :class="{ active: selectedCategory === 'all' }"
              @click="selectCategory('all')"
            >全部</div>
            <div
              v-for="cat in categories"
              :key="cat.id"
              class="cat-item"
              :class="{ active: selectedCategory === cat.id }"
              @click="selectCategory(cat.id)"
            >{{ cat.name }}</div>
          </div>
        </div>

        <div class="product-grid">
          <div v-if="loading" class="loading">
            <el-icon class="is-loading" :size="32" color="#B4471D"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="filteredProducts.length === 0" class="empty">
            <el-icon :size="48" color="#4a4a4a"><Goods /></el-icon>
            <span>暂无商品</span>
          </div>
          <template v-else>
            <div
              v-for="product in filteredProducts"
              :key="product.id"
              class="product-card"
              @click="addToCart(product)"
            >
              <div class="product-image">
                <img
                  v-if="product.image"
                  :src="product.image"
                  :alt="product.name"
                  @error="handleImageError"
                />
                <div v-else class="img-fallback">
                  <el-icon :size="28"><Picture /></el-icon>
                </div>
              </div>
              <div class="product-info">
                <div class="product-name" :title="product.name">{{ product.name }}</div>
                <div class="product-code">{{ product.goodsNum || '-' }}</div>
                <div class="product-bottom">
                  <span class="price">¥{{ formatPrice(product.retailPrice) }}</span>
                  <el-button
                    class="add-btn"
                    type="primary"
                    size="small"
                    circle
                  >
                    <el-icon><Plus /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </template>
        </div>

        <div v-if="hasMore && filteredProducts.length > 0" class="load-more">
          <el-button @click="loadMore">加载更多</el-button>
        </div>
      </div>

      <div class="cart-area">
        <div class="cart-header">
          <span class="cart-title">当前订单</span>
          <el-tooltip content="清空" placement="top">
            <el-icon :size="16" class="clear-btn" @click="clearCart"><Delete /></el-icon>
          </el-tooltip>
        </div>

        <div v-if="cartItems.length === 0" class="cart-empty">
          <el-icon :size="48" color="#4a4a4a"><ShoppingCart /></el-icon>
          <span>购物车为空</span>
        </div>

        <div v-else class="cart-items">
          <div
            v-for="(item, idx) in cartItems"
            :key="idx"
            class="cart-item"
          >
            <div class="item-img">
              <el-image v-if="item.image" :src="item.image" fit="cover" />
              <div v-else class="img-placeholder"><el-icon><Picture /></el-icon></div>
            </div>
            <div class="item-detail">
              <div class="item-name">{{ item.name }}</div>
              <div class="item-price">¥{{ item.price.toFixed(2) }}</div>
            </div>
            <div class="item-qty">
              <el-button size="small" circle @click="decreaseQty(idx)" :disabled="item.quantity <= 1">
                <el-icon><Minus /></el-icon>
              </el-button>
              <span class="qty-num">{{ item.quantity }}</span>
              <el-button size="small" circle @click="increaseQty(idx)">
                <el-icon><Plus /></el-icon>
              </el-button>
            </div>
            <el-icon :size="14" class="remove-btn" @click="removeItem(idx)"><Close /></el-icon>
          </div>
        </div>

        <div class="cart-summary">
          <div class="sum-row">
            <span>商品数量</span>
            <span>{{ cartCount }} 件</span>
          </div>
          <div class="sum-row">
            <span>商品金额</span>
            <span>¥{{ subtotal.toFixed(2) }}</span>
          </div>
          <div class="sum-row total">
            <span>应付金额</span>
            <span class="total-amount">¥{{ finalAmount.toFixed(2) }}</span>
          </div>
        </div>

        <div class="cart-footer">
          <el-button
            class="hold-btn"
            :disabled="cartItems.length === 0"
            @click="handleHold"
          >
            挂单
          </el-button>
          <el-button
            class="checkout-btn"
            type="primary"
            :disabled="cartItems.length === 0"
            @click="openCheckout"
          >
            结账 ¥{{ finalAmount.toFixed(2) }}
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="showPaymentDialog"
      title="确认结账"
      width="480px"
      :close-on-click-modal="false"
      class="payment-dialog"
    >
      <div class="dialog-body">
        <div class="amount-display">
          <span class="label">应付金额</span>
          <span class="amount">¥{{ finalAmount.toFixed(2) }}</span>
        </div>
        
        <div class="pay-methods">
          <div
            v-for="m in payMethods"
            :key="m.key"
            class="pay-item"
            :class="{ active: selectedPay === m.key }"
            @click="selectedPay = m.key"
          >
            <el-icon :size="24"><component :is="m.icon" /></el-icon>
            <span>{{ m.name }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPaymentDialog = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedPay" @click="confirmPay">
          确认支付
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showSuccessDialog"
      title=""
      width="360px"
      :show-close="false"
      class="success-dialog"
    >
      <div class="success-body">
        <el-icon :size="64" color="#67C23A"><CircleCheckFilled /></el-icon>
        <h3>支付成功</h3>
        <div class="order-info">
          <div class="info-row">
            <span>订单号</span>
            <span>{{ lastOrderNo }}</span>
          </div>
          <div class="info-row">
            <span>金额</span>
            <span class="amount">¥{{ lastOrderAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button class="full-btn" type="primary" @click="showSuccessDialog = false">
          继续收银
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Loading, Goods, Picture, Plus, Minus, Close,
  ShoppingCart, Delete, Wallet, CreditCard, Money, CircleCheckFilled
} from '@element-plus/icons-vue'
import { listGoods } from '@/api/goods/goods'
import { listCategory } from '@/api/goods/category'
import { addOrder } from '@/api/order/salesOrder'

const products = ref<any[]>([])
const categories = ref<any[]>([])
const loading = ref(false)
const hasMore = ref(false)
const pageNum = ref(1)
const searchKeyword = ref('')
const selectedCategory = ref<string | number>('all')

const cartItems = ref<any[]>([])
const showPaymentDialog = ref(false)
const showSuccessDialog = ref(false)
const selectedPay = ref('')
const lastOrderNo = ref('')
const lastOrderAmount = ref(0)

const payMethods = [
  { key: 'wechat', name: '微信支付', icon: Wallet },
  { key: 'alipay', name: '支付宝', icon: CreditCard },
  { key: 'cash', name: '现金', icon: Money },
  { key: 'card', name: '银行卡', icon: CreditCard },
]

const filteredProducts = computed(() => {
  let result = products.value
  if (selectedCategory.value !== 'all') {
    result = result.filter(p => p.categoryId === selectedCategory.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(p =>
      p.name?.toLowerCase().includes(kw) ||
      p.goodsNum?.toLowerCase().includes(kw) ||
      p.barCode?.toLowerCase().includes(kw)
    )
  }
  return result
})

const cartCount = computed(() => cartItems.value.reduce((s, i) => s + i.quantity, 0))
const subtotal = computed(() => cartItems.value.reduce((s, i) => s + i.price * i.quantity, 0))
const finalAmount = computed(() => Math.max(0, subtotal.value))

function formatPrice(price: any) {
  if (price === null || price === undefined) return '0.00'
  return Number(price).toFixed(2)
}

function handleImageError(event: Event) {
  const target = event.target as HTMLImageElement
  target.style.display = 'none'
}

async function loadProducts(reset = true) {
  if (reset) {
    pageNum.value = 1
    products.value = []
  }
  loading.value = true
  try {
    const res = await listGoods({ pageNum: pageNum.value, pageSize: 24, status: 1 })
    const list = res.rows || []
    products.value = reset ? list : [...products.value, ...list]
    hasMore.value = list.length >= 24
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const res = await listCategory({})
    categories.value = res.rows || []
  } catch (e) {
    console.error(e)
  }
}

function selectCategory(id: string | number) {
  selectedCategory.value = id
}

function addToCart(product: any) {
  const idx = cartItems.value.findIndex(i => i.goodsId === product.id)
  if (idx > -1) {
    cartItems.value[idx].quantity++
  } else {
    cartItems.value.push({
      goodsId: product.id,
      name: product.name,
      image: product.image,
      price: product.retailPrice || 0,
      quantity: 1,
    })
  }
  ElMessage.success(`已添加: ${product.name}`)
}

function increaseQty(idx: number) {
  cartItems.value[idx].quantity++
}

function decreaseQty(idx: number) {
  if (cartItems.value[idx].quantity > 1) {
    cartItems.value[idx].quantity--
  } else {
    removeItem(idx)
  }
}

function removeItem(idx: number) {
  cartItems.value.splice(idx, 1)
}

async function clearCart() {
  if (cartItems.value.length === 0) return
  try {
    await ElMessageBox.confirm('确定清空购物车？', '提示', { type: 'warning' })
    cartItems.value = []
  } catch {}
}

function openCheckout() {
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  selectedPay.value = ''
  showPaymentDialog.value = true
}

function handleHold() {
  ElMessage.info('挂单功能开发中...')
}

async function confirmPay() {
  showPaymentDialog.value = false
  const orderNo = 'POS' + Date.now()
  try {
    await addOrder({
      orderNo,
      items: cartItems.value.map(i => ({
        goodsId: i.goodsId,
        name: i.name,
        price: i.price,
        quantity: i.quantity,
      })),
      payAmount: finalAmount.value,
      payMethod: selectedPay.value,
    })
    lastOrderNo.value = orderNo
    lastOrderAmount.value = finalAmount.value
    cartItems.value = []
    showSuccessDialog.value = true
  } catch (e) {
    console.error(e)
    ElMessage.error('下单失败')
  }
}

function loadMore() {
  if (hasMore.value) {
    pageNum.value++
    loadProducts(false)
  }
}

onMounted(() => {
  loadProducts()
  loadCategories()
})
</script>

<style lang="scss" scoped>
.pos-root {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 56px);
  background: #f5f7fa;
  color: #303133;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', sans-serif;
}

.pos-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f7fa;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;

  .search-box {
    width: 280px;
    flex-shrink: 0;

    :deep(.el-input__wrapper) {
      background: #f5f7fa;
      box-shadow: none;
      border: 1px solid #e4e7ed;
      border-radius: 8px;

      &:hover {
        border-color: #c0c4cc;
      }

      &.is-focus {
        border-color: #B4471D;
      }
    }

    :deep(.el-input__inner) {
      color: #303133;

      &::placeholder {
        color: #c0c4cc;
      }
    }

    :deep(.el-input__prefix-inner) {
      color: #909399;
    }
  }

  .categories {
    display: flex;
    gap: 6px;
    overflow-x: auto;
    flex: 1;

    &::-webkit-scrollbar {
      height: 0;
    }

    .cat-item {
      padding: 8px 18px;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 500;
      color: #606266;
      background: #f5f7fa;
      border: 1px solid #e4e7ed;
      cursor: pointer;
      white-space: nowrap;
      transition: all 0.2s;

      &:hover {
        color: #303133;
        background: #ecf5ff;
        border-color: #B4471D;
      }

      &.active {
        color: #ffffff;
        background: #B4471D;
        border-color: #B4471D;
      }
    }
  }
}

.product-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
  padding: 16px;
  overflow-y: auto;
  align-content: start;

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: #f5f7fa;
  }

  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 4px;

    &:hover {
      background: #c0c4cc;
    }
  }

  .loading,
  .empty {
    grid-column: 1 / -1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 60px 0;
    color: #909399;
  }
}

.product-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e4e7ed;

  &:hover:not(.disabled) {
    transform: translateY(-2px);
    border-color: #B4471D;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .product-image {
    position: relative;
    width: 100%;
    height: 140px;
    background: #f5f7fa;
    overflow: hidden;

    .img-fallback {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #c0c4cc;
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    .stock-badge {
      position: absolute;
      top: 8px;
      right: 8px;
      padding: 2px 8px;
      border-radius: 10px;
      font-size: 11px;
      font-weight: 600;

      &.out {
        background: rgba(245, 108, 108, 0.9);
        color: #fff;
      }

      &.low {
        background: rgba(230, 162, 60, 0.9);
        color: #fff;
      }
    }
  }

  .product-info {
    padding: 10px 12px 12px;

    .product-name {
      font-size: 13px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .product-code {
      font-size: 11px;
      color: #909399;
      margin-bottom: 8px;
    }

    .product-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .price {
        font-size: 16px;
        font-weight: 700;
        color: #B4471D;
      }

      .add-btn {
        background: #B4471D;
        border: none;

        &:hover:not(:disabled) {
          background: #C55A2A;
        }

        &:disabled {
          background: #dcdfe6;
        }
      }
    }
  }
}

.load-more {
  padding: 12px;
  text-align: center;
  flex-shrink: 0;

  :deep(.el-button) {
    background: #ffffff;
    border: 1px solid #e4e7ed;
    color: #606266;

    &:hover {
      border-color: #B4471D;
      color: #B4471D;
    }
  }
}

.cart-area {
  width: 360px;
  background: #ffffff;
  border-left: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;

  .cart-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .clear-btn {
    color: #909399;
    cursor: pointer;

    &:hover {
      color: #F56C6C;
    }
  }
}

.cart-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #909399;

  span {
    font-size: 14px;
  }
}

.cart-items {
  flex: 1;
  overflow-y: auto;
  padding: 12px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 3px;
  }

  .cart-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px;
    background: #f5f7fa;
    border-radius: 10px;
    margin-bottom: 8px;
    position: relative;

    &:hover {
      background: #ecf5ff;
    }

    .item-img {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      overflow: hidden;
      flex-shrink: 0;
      background: #e4e7ed;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .img-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #c0c4cc;
      }
    }

    .item-detail {
      flex: 1;
      min-width: 0;

      .item-name {
        font-size: 13px;
        font-weight: 500;
        color: #303133;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .item-price {
        font-size: 12px;
        color: #909399;
        margin-top: 2px;
      }
    }

    .item-qty {
      display: flex;
      align-items: center;
      gap: 6px;

      :deep(.el-button) {
        width: 24px;
        height: 24px;
        padding: 0;
        background: #ffffff;
        border: 1px solid #e4e7ed;
        color: #606266;

        &:hover:not(:disabled) {
          background: #ecf5ff;
          border-color: #B4471D;
          color: #B4471D;
        }

        &:disabled {
          background: #f5f7fa;
          color: #c0c4cc;
        }
      }

      .qty-num {
        min-width: 24px;
        text-align: center;
        font-size: 14px;
        font-weight: 600;
        color: #303133;
      }
    }

    .remove-btn {
      position: absolute;
      top: 4px;
      right: 4px;
      color: #c0c4cc;
      cursor: pointer;
      opacity: 0;
      transition: opacity 0.2s;

      &:hover {
        color: #F56C6C;
      }
    }

    &:hover .remove-btn {
      opacity: 1;
    }
  }
}

.cart-summary {
  padding: 14px 20px;
  background: #ffffff;
  border-top: 1px solid #e4e7ed;
  flex-shrink: 0;

  .sum-row {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    color: #909399;
    margin-bottom: 6px;
    white-space: nowrap;

    &.total {
      padding-top: 10px;
      margin-top: 6px;
      border-top: 1px solid #e4e7ed;
      font-size: 14px;
      font-weight: 500;
      color: #303133;

      .total-amount {
        font-size: 22px;
        font-weight: 700;
        color: #B4471D;
      }
    }
  }
}

.cart-footer {
  display: flex;
  gap: 10px;
  padding: 12px 20px 20px;
  background: #ffffff;

  :deep(.el-button) {
    flex: 1;
    height: 44px;
    font-size: 14px;
    font-weight: 600;
    border-radius: 10px;
  }

  .hold-btn {
    background: #ffffff;
    border: 1px solid #dcdfe6;
    color: #606266;

    &:hover:not(:disabled) {
      border-color: #909399;
      color: #303133;
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  .checkout-btn {
    background: #B4471D;
    border: none;

    &:hover:not(:disabled) {
      background: #C55A2A;
    }

    &:disabled {
      background: #dcdfe6;
    }
  }
}

.payment-dialog {
  :deep(.el-dialog) {
    background: #ffffff;
    border-radius: 16px;

    .el-dialog__header {
      border-bottom: 1px solid #e4e7ed;
      padding: 16px 24px;

      .el-dialog__title {
        color: #303133;
      }

      .el-dialog__headerbtn .el-dialog__close {
        color: #909399;

        &:hover {
          color: #303133;
        }
      }
    }

    .el-dialog__body {
      padding: 24px;
    }

    .el-dialog__footer {
      border-top: 1px solid #e4e7ed;
      padding: 16px 24px;
    }
  }

  .dialog-body {
    .amount-display {
      text-align: center;
      padding: 20px;
      background: #f5f7fa;
      border-radius: 12px;
      margin-bottom: 24px;

      .label {
        display: block;
        font-size: 13px;
        color: #909399;
        margin-bottom: 8px;
      }

      .amount {
        font-size: 36px;
        font-weight: 700;
        color: #B4471D;
      }
    }

    .pay-methods {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;

      .pay-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px;
        background: #f5f7fa;
        border: 2px solid transparent;
        border-radius: 12px;
        cursor: pointer;
        color: #606266;
        transition: all 0.2s;

        &:hover {
          border-color: #dcdfe6;
          color: #303133;
        }

        &.active {
          border-color: #B4471D;
          background: rgba(180, 71, 29, 0.05);
          color: #303133;
        }

        .el-icon {
          color: #B4471D;
        }

        span {
          font-size: 14px;
          font-weight: 500;
        }
      }
    }
  }
}

.success-dialog {
  :deep(.el-dialog) {
    background: #ffffff;
    border-radius: 16px;
  }

  .success-body {
    text-align: center;
    padding: 20px 0;

    h3 {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin: 16px 0 20px;
    }

    .order-info {
      background: #f5f7fa;
      border-radius: 12px;
      padding: 16px 20px;
      text-align: left;

      .info-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        font-size: 14px;

        &:last-child {
          margin-bottom: 0;
        }

        span:first-child {
          color: #909399;
        }

        .amount {
          font-weight: 600;
          color: #B4471D;
        }
      }
    }
  }

  :deep(.el-button.full-btn) {
    width: 100%;
    background: #B4471D;
    border: none;
    height: 44px;

    &:hover {
      background: #C55A2A;
    }
  }
}

:deep(.el-message-box) {
  background: #ffffff;

  .el-message-box__title {
    color: #303133;
  }

  .el-message-box__message {
    color: #606266;
  }
}
</style>
