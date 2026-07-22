<template>
  <div class="pos-layout">
    <PosSidebar />
    <div class="pos-main">
      <PosHeader />
      <div class="pos-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import PosSidebar from './PosSidebar.vue'
import PosHeader from './PosHeader.vue'
</script>

<style lang="scss" scoped>
.pos-layout {
  display: flex;
  height: 100vh;
  background: #f5f7fa;
}

.pos-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.pos-content {
  flex: 1;
  overflow: auto;
  padding: 20px;
}
</style>
