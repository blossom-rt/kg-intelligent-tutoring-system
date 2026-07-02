<template>
  <div class="page">
    <header class="top-bar">
      <h2 @click="$router.push('/student')" style="cursor:pointer">跨学科主题探究</h2>
    </header>
    <div class="content">
      <el-card v-if="!themes.length"><el-empty description="暂无跨学科主题" :image-size="80" /></el-card>
      <div v-else class="theme-grid">
        <el-card v-for="t in themes" :key="t.id" class="theme-card">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span>{{ t.themeName }}</span>
              <el-tag size="small">{{ ['','基础','进阶','困难'][t.difficulty] || '进阶' }}</el-tag>
            </div>
          </template>
          <p class="theme-desc">{{ t.description || '暂无简介' }}</p>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span style="color:#aaa;font-size:12px">状态：{{ t.status === 1 ? '已发布' : '已下架' }}</span>
            <el-button type="primary" size="small" :disabled="t.status !== 1">开始学习</el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCrossSubjects } from '../api/student'

const themes = ref([])
onMounted(async () => {
  try { themes.value = (await getCrossSubjects()) || [] } catch { }
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fa; }
.top-bar { display: flex; align-items: center; padding: 16px 32px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.top-bar h2 { margin: 0; font-size: 18px; color: #2c5eb5; }
.content { padding: 24px 32px; }
.theme-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(360px, 1fr)); gap: 16px; }
.theme-card { border-radius: 12px; }
.theme-desc { color: #666; font-size: 14px; margin-bottom: 16px; min-height: 40px; }
</style>
