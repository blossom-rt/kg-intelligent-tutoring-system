<template>
  <div class="page">
    <header class="top-bar">
      <div class="top-left">
        <span class="back-btn" @click="$router.push('/student')">&#8592; 返回</span>
        <h2>跨学科主题探究</h2>
      </div>
    </header>
    <div class="content">
      <div v-if="!themes.length" class="empty-full">
        <div class="empty-circle">T</div>
        <p>暂无跨学科主题</p>
        <span>教师发布主题后将在这里展示</span>
      </div>
      <div v-else class="theme-grid">
        <div v-for="t in themes" :key="t.id" class="theme-card">
          <div class="theme-badge" :class="t.status===1?'pub':'off'">{{ t.status===1?'已发布':'已下架' }}</div>
          <h3>{{ t.themeName }}</h3>
          <p>{{ t.description || '暂无简介' }}</p>
          <div class="theme-footer">
            <el-tag size="small" round>{{ ['','基础','进阶','困难'][t.difficulty]||'进阶' }}</el-tag>
            <el-button type="primary" size="small" round :disabled="t.status!==1">开始学习</el-button>
          </div>
        </div>
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
.page { min-height: 100vh; background: #faf7f2; }
.top-bar { display: flex; align-items: center; padding: 16px 36px; background: #fffdf9; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.top-left { display: flex; align-items: center; gap: 16px; }
.back-btn { color: #ff7b3d; cursor: pointer; font-size: 14px; }
.top-bar h2 { margin: 0; font-size: 18px; color: #2d2a26; }
.content { padding: 28px 36px; }

.empty-full { text-align: center; padding: 80px 0; }
.empty-circle { width: 72px; height: 72px; border-radius: 50%; background: #f3efe8; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; font-size: 28px; color: #ff7b3d; font-weight: 700; }
.empty-full p { font-size: 16px; color: #6b655e; margin: 0 0 6px; }
.empty-full span { font-size: 13px; color: #a09a92; }

.theme-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 20px; }
.theme-card {
  background: #fffdf9; border-radius: 16px; padding: 24px; position: relative;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); transition: all 0.25s;
}
.theme-card:hover { transform: translateY(-4px); box-shadow: 0 8px 28px rgba(0,0,0,0.08); }
.theme-badge { position: absolute; top: 16px; right: 16px; font-size: 12px; padding: 3px 10px; border-radius: 20px; }
.theme-badge.pub { background: #e8f8f0; color: #2d8a4e; }
.theme-badge.off { background: #fef0f0; color: #c44; }
.theme-card h3 { margin: 0 0 10px; font-size: 17px; color: #2d2a26; }
.theme-card p { color: #777; font-size: 14px; line-height: 1.6; min-height: 40px; margin-bottom: 18px; }
.theme-footer { display: flex; justify-content: space-between; align-items: center; }
</style>
