<template>
  <div class="teacher-home">
    <header class="top-bar">
      <h2>教师工作台</h2>
      <el-button type="info" size="small" @click="logout">退出登录</el-button>
    </header>
    <div class="content">
      <div class="stats-row">
        <el-card class="stat-card"><p class="stat-num">{{ stats.activeStudents }}</p><p class="stat-label">活跃学生</p></el-card>
        <el-card class="stat-card"><p class="stat-num">{{ stats.weekStudy }}</p><p class="stat-label">本周学习人次</p></el-card>
        <el-card class="stat-card"><p class="stat-num">{{ stats.totalNodes }}</p><p class="stat-label">知识点总数</p></el-card>
        <el-card class="stat-card"><p class="stat-num">{{ stats.totalQuestions }}</p><p class="stat-label">题库总量</p></el-card>
      </div>
      <div class="main-grid">
        <el-card><template #header>待处理事项</template><el-empty description="暂无待处理事项" :image-size="60" /></el-card>
        <el-card><template #header>薄弱知识点 TOP5</template><el-empty description="暂无数据" :image-size="60" /></el-card>
        <el-card><template #header>快捷入口</template>
          <div class="quick-btns">
            <el-button @click="$router.push('/teacher/knowledge')">知识点管理</el-button>
            <el-button @click="$router.push('/teacher/questions')">题库管理</el-button>
            <el-button @click="$router.push('/teacher/themes')">跨学科主题</el-button>
            <el-button @click="$router.push('/teacher/exams')">测评管理</el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const stats = ref({ activeStudents: 0, weekStudy: 0, totalNodes: 0, totalQuestions: 0 })

onMounted(async () => {
  try { const res = await request.get('/teacher/dashboard'); if (res) stats.value = res } catch { }
})

const logout = () => { localStorage.clear(); router.push('/login') }
</script>

<style scoped>
.teacher-home { min-height: 100vh; background: #f5f7fa; }
.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 16px 32px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.top-bar h2 { margin: 0; font-size: 18px; color: #2d8a4e; }
.content { padding: 24px 32px; }
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { text-align: center; border-radius: 12px; }
.stat-num { font-size: 28px; font-weight: 700; color: #2d8a4e; margin: 0; }
.stat-label { color: #999; font-size: 13px; margin: 4px 0 0; }
.main-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px; }
.quick-btns { display: flex; flex-direction: column; gap: 8px; }
.quick-btns .el-button { justify-content: flex-start; }
</style>
