<template>
  <el-dialog
    v-model="visible"
    title="订单完成"
    width="400px"
    :close-on-click-modal="false"
    class="pos-success-dialog"
  >
    <div class="success-content">
      <div class="success-icon">
        <el-icon :size="64" color="#67C23A"><CircleCheckFilled /></el-icon>
      </div>
      
      <h2 class="success-title">支付成功！</h2>
      
      <div class="order-details">
        <div class="detail-row">
          <span>订单编号</span>
          <span class="detail-value">{{ orderNo }}</span>
        </div>
        <div class="detail-row">
          <span>支付金额</span>
          <span class="detail-value amount">¥{{ amount.toFixed(2) }}</span>
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button class="print-btn" @click="$emit('print')">
          <el-icon><Printer /></el-icon>
          打印小票
        </el-button>
        <el-button class="continue-btn" type="primary" @click="$emit('continue')">
          继续收银
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CircleCheckFilled, Printer } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: boolean
  orderNo: string
  amount: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'continue'): void
  (e: 'print'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})
</script>

<style lang="scss" scoped>
.pos-success-dialog {
  :deep(.el-dialog) {
    background: var(--pos-bg-secondary);
    border-radius: 16px;
    
    .el-dialog__header {
      display: none;
    }
    
    .el-dialog__body {
      padding: 32px 24px 24px;
    }
    
    .el-dialog__footer {
      padding: 0 24px 24px;
    }
  }
}

.success-content {
  text-align: center;
  
  .success-icon {
    margin-bottom: 16px;
    animation: scaleIn 0.3s ease-out;
  }
  
  .success-title {
    font-size: 24px;
    font-weight: 700;
    color: var(--pos-text-primary);
    margin: 0 0 24px;
  }
  
  .order-details {
    background: var(--pos-bg-card);
    border-radius: 12px;
    padding: 20px;
    
    .detail-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      span:first-child {
        font-size: 14px;
        color: var(--pos-text-muted);
      }
      
      .detail-value {
        font-size: 14px;
        font-weight: 600;
        color: var(--pos-text-primary);
        
        &.amount {
          font-size: 20px;
          font-weight: 700;
          color: var(--pos-accent-secondary);
        }
      }
    }
  }
}

@keyframes scaleIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.dialog-footer {
  display: flex;
  gap: 12px;
  
  .print-btn,
  .continue-btn {
    flex: 1;
    height: 48px;
    font-size: 15px;
    font-weight: 600;
    border-radius: 10px;
    
    .el-icon {
      margin-right: 6px;
    }
  }
  
  .print-btn {
    background: var(--pos-bg-card);
    border: 1px solid var(--pos-border);
    color: var(--pos-text-primary);
    
    &:hover {
      border-color: var(--pos-accent-secondary);
      color: var(--pos-accent-secondary);
    }
  }
  
  .continue-btn {
    background: var(--pos-accent-secondary);
    border: none;
    color: white;
    
    &:hover {
      background: var(--pos-accent-hover);
    }
  }
}
</style>
