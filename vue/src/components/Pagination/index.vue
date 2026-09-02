<template>
  <div :class="{ hidden: hidden }" class="pagination-container">
    <el-pagination
      :background="background"
      :current-page="currentPage"
      :page-size="pageSize"
      :layout="layout"
      :page-sizes="pageSizes"
      :pager-count="pagerCount"
      :total="total"
      v-bind="$attrs"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  total: number
  page?: number
  limit?: number
  pageSizes?: number[]
  pagerCount?: number
  layout?: string
  background?: boolean
  autoScroll?: boolean
  hidden?: boolean
}>(), {
  page: 1,
  limit: 20,
  pageSizes: () => [10, 20, 30, 50],
  pagerCount: document.body.clientWidth < 992 ? 5 : 7,
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true,
  autoScroll: true,
  hidden: false
})

const emit = defineEmits<{
  (e: 'pagination', value: { page: number; limit: number }): void
  (e: 'update:page', value: number): void
  (e: 'update:limit', value: number): void
}>()

const currentPage = ref(props.page)
const pageSize = ref(props.limit)

watch(() => props.page, (val) => { currentPage.value = val })
watch(() => props.limit, (val) => { pageSize.value = val })

function handleSizeChange(val: number) {
  pageSize.value = val
  emit('update:limit', val)
  currentPage.value = 1
  emit('update:page', 1)
  emit('pagination', { page: 1, limit: val })
}

function handleCurrentChange(val: number) {
  currentPage.value = val
  emit('update:page', val)
  emit('pagination', { page: val, limit: pageSize.value })
}
</script>

<style scoped>
.pagination-container {
  background: #fff;
  padding: 32px 16px;
}
.pagination-container.hidden {
  display: none;
}
</style>
