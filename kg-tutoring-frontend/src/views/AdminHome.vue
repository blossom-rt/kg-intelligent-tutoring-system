<template>
  <div class="admin-home">
    <header class="top-bar">
      <h2>系统管理后台</h2>
      <el-button type="info" size="small" @click="logout">退出登录</el-button>
    </header>
    <div class="content">
      <el-card><template #header>运营数据</template><el-empty description="暂无数据" :image-size="60" /></el-card>
      <el-card><template #header>用户管理</template><el-empty description="暂无数据" :image-size="60" /></el-card>
      <el-card><template #header>操作日志</template><el-empty description="暂无数据" :image-size="60" /></el-card>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const logout = () => { localStorage.clear(); router.push('/login') }
</script>

<style scoped>
.admin-home { min-height: 100vh; background: #f5f7fa; }
.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 16px 32px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.content { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; padding: 24px 32px; }
</style>
