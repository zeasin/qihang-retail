<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>首页</h2>
    </div>
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card>
          <div class="welcome-content">
            <h2>欢迎使用启航电商零售ERP系统</h2>
            <p>选择左侧菜单开始操作</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 16px;
}
.page-header {
  margin-bottom: 16px;
}
.welcome-content {
  text-align: center;
  padding: 60px 0;
  h2 {
    font-size: 24px;
    margin-bottom: 12px;
  }
  p {
    color: #999;
    font-size: 14px;
  }
}
</style>
