<template>
  <router-view />
  <!-- 桌面端全局服务坞：任意页面右下角查看后端状态、启停服务、看 Java 日志 -->
  <ServiceDock v-if="desktop" />
</template>

<script setup lang="ts">
import ServiceDock from '@/components/ServiceDock/index.vue'
import { isElectron } from '@/api/hardware'

const desktop = isElectron()
</script>
