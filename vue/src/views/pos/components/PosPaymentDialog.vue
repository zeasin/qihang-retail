<template>
  <el-dialog
    v-model="visible"
    title="确认结账"
    width="500px"
    :close-on-click-modal="false"
    class="pos-payment-dialog"
  >
    <div class="payment-content">
      <div class="order-info">
        <div class="info-row">
          <span>订单编号</span>
          <span class="order-no">{{ orderNo }}</span>
        </div>
        <div class="info-row">
          <span>商品数量</span>
          <span>{{ totalItems }} 件</span>
        </div>
      </div>

      <div class="amount-section">
        <div class="amount-label">应付金额</div>
        <div class="amount-value">
          <em>¥</em>{{ finalAmount.toFixed(2) }}
        </div>
      </div>

      <div class="payment-methods">
        <h4 class="section-title">选择支付方式</h4>
        <div class="method-list">
          <div
            v-for="method in paymentMethods"
            :key="method.key"
            class="method-item"
            :class="{ active: selectedMethod === method.key }"
            @click="selectMethod(method.key)"
          >
            <el-icon :size="24"><component :is="method.icon" /></el-icon>
            <span class="method-name">{{ method.name }}</span>
            <el-icon v-if="selectedMethod === method.key" class="check-icon"><Check /></el-icon>
          </div>
        </div>
      </div>

      <div class="customer-info">
        <h4 class="section-title">会员信息（可选）</h4>
        <el-input
          v-model="memberCode"
          placeholder="输入会员手机号/会员号"
          clearable
        >
          <template #prefix>
            <el-icon><User /></el-icon>
          </template>
          <template #append>
            <el-button @click="searchMember">查询</el-button>
          </template>
        </el-input>
        <div v-if="memberInfo" class="member-display">
          <el-avatar :size="36" />
          <div class="member-detail">
            <span class="member-name">{{ memberInfo.name }}</span>
            <span class="member-points">积分: {{ memberInfo.points }}</span>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          :disabled="!selectedMethod"
          @click="handleConfirm"
        >
          确认支付 ¥{{ finalAmount.toFixed(2) }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Wallet, CreditCard, Money, Check, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  orderNo: string
  totalItems: number
  finalAmount: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm', paymentInfo: { method: string; memberCode?: string }): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const selectedMethod = ref<string>('')
const memberCode = ref('')
const memberInfo = ref<any>(null)
const submitting = ref(false)

const paymentMethods = [
  { key: 'cash', name: '现金', icon: Money },
  { key: 'wechat', name: '微信支付', icon: Wallet },
  { key: 'alipay', name: '支付宝', icon: CreditCard },
  { key: 'card', name: '银行卡', icon: CreditCard },
]

function selectMethod(key: string) {
  selectedMethod.value = key
}

async function searchMember() {
  if (!memberCode.value) {
    ElMessage.warning('请输入会员信息')
    return
  }
  memberInfo.value = { name: '张三', points: 1280 }
  ElMessage.success('会员查询成功')
}

function handleCancel() {
  visible.value = false
  selectedMethod.value = ''
  memberCode.value = ''
  memberInfo.value = null
}

async function handleConfirm() {
  if (!selectedMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }
  
  submitting.value = true
  try {
    emit('confirm', {
      method: selectedMethod.value,
      memberCode: memberCode.value || undefined
    })
    handleCancel()
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.pos-payment-dialog {
  :deep(.el-dialog) {
    background: var(--pos-bg-secondary);
    border-radius: 16px;
    
    .el-dialog__header {
      border-bottom: 1px solid var(--pos-border);
      padding: 20px 24px;
      
      .el-dialog__title {
        font-size: 18px;
        font-weight: 600;
        color: var(--pos-text-primary);
      }
      
      .el-dialog__headerbtn .el-dialog__close {
        color: var(--pos-text-muted);
        
        &:hover {
          color: var(--pos-text-primary);
        }
      }
    }
    
    .el-dialog__body {
      padding: 24px;
    }
    
    .el-dialog__footer {
      border-top: 1px solid var(--pos-border);
      padding: 16px 24px;
    }
  }
}

.payment-content {
  .order-info {
    display: flex;
    gap: 24px;
    margin-bottom: 20px;
    
    .info-row {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      span:first-child {
        font-size: 13px;
        color: var(--pos-text-muted);
      }
      
      .order-no {
        font-size: 14px;
        font-weight: 600;
        color: var(--pos-text-primary);
      }
    }
  }

  .amount-section {
    background: var(--pos-bg-card);
    border-radius: 12px;
    padding: 20px;
    text-align: center;
    margin-bottom: 24px;
    
    .amount-label {
      font-size: 14px;
      color: var(--pos-text-muted);
      margin-bottom: 8px;
    }
    
    .amount-value {
      font-size: 36px;
      font-weight: 700;
      color: var(--pos-accent-secondary);
      
      em {
        font-style: normal;
        font-size: 24px;
      }
    }
  }

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--pos-text-primary);
    margin: 0 0 12px;
  }

  .payment-methods {
    margin-bottom: 24px;
    
    .method-list {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
    }
    
    .method-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background: var(--pos-bg-card);
      border-radius: 12px;
      cursor: pointer;
      border: 2px solid transparent;
      transition: all 0.2s;
      
      &:hover {
        border-color: var(--pos-border-light);
      }
      
      &.active {
        border-color: var(--pos-accent-secondary);
        background: rgba(180, 71, 29, 0.1);
        
        .el-icon:first-child {
          color: var(--pos-accent-secondary);
        }
        
        .check-icon {
          opacity: 1;
        }
      }
      
      .el-icon:first-child {
        color: var(--pos-text-secondary);
      }
      
      .method-name {
        flex: 1;
        font-size: 14px;
        font-weight: 500;
        color: var(--pos-text-primary);
      }
      
      .check-icon {
        color: var(--pos-accent-secondary);
        opacity: 0;
        transition: opacity 0.2s;
      }
    }
  }

  .customer-info {
    :deep(.el-input) {
      .el-input__wrapper {
        background: var(--pos-bg-input);
        box-shadow: 0 0 0 1px var(--pos-border) inset;
        
        &:hover {
          box-shadow: 0 0 0 1px var(--pos-border-light) inset;
        }
        
        &.is-focus {
          box-shadow: 0 0 0 2px var(--pos-accent-secondary) inset;
        }
      }
      
      .el-input__inner {
        color: var(--pos-text-primary);
      }
      
      .el-input-group__append {
        background: var(--pos-bg-card);
        border-color: var(--pos-border);
        color: var(--pos-text-primary);
      }
    }
    
    .member-display {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-top: 12px;
      padding: 12px;
      background: var(--pos-bg-card);
      border-radius: 10px;
      
      .member-detail {
        .member-name {
          display: block;
          font-size: 14px;
          font-weight: 600;
          color: var(--pos-text-primary);
        }
        
        .member-points {
          font-size: 12px;
          color: var(--pos-text-muted);
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  
  .el-button {
    padding: 12px 24px;
    font-size: 14px;
    font-weight: 600;
    border-radius: 10px;
    
    &.el-button--default {
      background: transparent;
      border-color: var(--pos-border-light);
      color: var(--pos-text-primary);
      
      &:hover {
        border-color: var(--pos-text-primary);
      }
    }
    
    &.el-button--primary {
      background: var(--pos-accent-secondary);
      border: none;
      
      &:hover {
        background: var(--pos-accent-hover);
      }
    }
  }
}
</style>
