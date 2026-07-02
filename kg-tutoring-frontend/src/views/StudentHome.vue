<template>
  <div class="student-home">
    <header class="top-bar">
      <h2 class="welcome">欢迎回来，{{ studentName }}</h2>
      <el-button type="info" size="small" @click="logout">退出登录</el-button>
    </header>

    <div class="dashboard">
      <div class="main-col">
        <el-card class="section-card">
          <template #header><span class="section-title">快捷续学</span></template>
          <div v-if="activePaths.length" class="path-list">
            <div v-for="p in activePaths" :key="p.id" class="path-item">
              <span class="path-name">{{ p.pathName }}</span>
              <el-progress :percentage="p.progress" :stroke-width="8" />
              <el-button type="primary" size="small" link>继续学习</el-button>
            </div>
          </div>
          <el-empty v-else description="暂无进行中的学习路径" :image-size="60" />
        </el-card>

        <el-card class="section-card">
          <template #header><span class="section-title">待办提醒</span></template>
          <div v-if="todos.length" class="todo-list">
            <div v-for="t in todos" :key="t.id" class="todo-item">
              <el-tag :type="t.tagType" size="small">{{ t.label }}</el-tag>
              <span>{{ t.content }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无待办事项" :image-size="60" />
        </el-card>

        <el-card class="section-card">
          <template #header><span class="section-title">个性化推荐</span></template>
          <el-empty description="完成更多学习后为您精准推荐" :image-size="60" />
        </el-card>
      </div>

      <div class="side-col">
        <el-card class="section-card">
          <template #header><span class="section-title">学情概览</span></template>
          <div class="stats-grid">
            <div class="stat-item">
              <span class="stat-num">{{ stats.studyDays }}</span>
              <span class="stat-label">累计学习天数</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ stats.totalMinutes }}</span>
              <span class="stat-label">学习时长(分)</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ stats.masteredNodes }}</span>
              <span class="stat-label">已掌握知识点</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ stats.correctRate }}%</span>
              <span class="stat-label">总正确率</span>
            </div>
          </div>
        </el-card>

        <el-card class="section-card">
          <template #header><span class="section-title">快捷入口</span></template>
          <div class="quick-links">
            <el-button v-for="link in quickLinks" :key="link.key" @click="$router.push(link.path)">
              {{ link.label }}
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPersonalAnalysis, getWeakAnalysis, getPathList } from '../api/student'

const router = useRouter()
const studentName = ref('同学')
const activePaths = ref([])
const todos = ref([])
const stats = ref({ studyDays: 0, totalMinutes: 0, masteredNodes: 0, correctRate: 0 })

const quickLinks = [
  { key: 'knowledge', label: '知识图谱', path: '/student/knowledge' },
  { key: 'themes', label: '跨学科主题', path: '/student/themes' },
  { key: 'exams', label: '测评中心', path: '/student/exams' },
  { key: 'wrong', label: '错题本', path: '/student/wrong' },
]

onMounted(async () => {
  // 获取个人分析数据
  try {
    const analysisRes = await getPersonalAnalysis()
    if (analysisRes) {
      stats.value.studyDays = analysisRes.studyDays || stats.value.studyDays
      stats.value.totalMinutes = analysisRes.totalMinutes || stats.value.totalMinutes
      stats.value.masteredNodes = analysisRes.masteredNodes || stats.value.masteredNodes
      stats.value.correctRate = analysisRes.correctRate || stats.value.correctRate
    }
  } catch { /* ignore */ }

  // 获取弱点分析
  try {
    const weakRes = await getWeakAnalysis()
    if (weakRes && weakRes.length) {
      // 弱点数据可用于后续扩展展示
    }
  } catch { /* ignore */ }

  // 获取学习路径列表作为备选
  try {
    const pathsRes = await getPathList()
    if (pathsRes && pathsRes.length && activePaths.value.length === 0) {
      activePaths.value = pathsRes.slice(0, 5).map(p => ({
        id: p.id,
        pathName: p.pathName || p.name,
        progress: p.progress || 0
      }))
    }
  } catch { /* ignore */ }
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/login')
}
</script>

<style scoped>
.student-home { min-height: 100vh; background: #f5f7fa; }
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 32px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.welcome { margin: 0; font-size: 18px; color: #2c5eb5; }
.dashboard { display: flex; gap: 20px; padding: 24px 32px; }
.main-col { flex: 2; display: flex; flex-direction: column; gap: 20px; }
.side-col { flex: 1; display: flex; flex-direction: column; gap: 20px; }
.section-card { border-radius: 12px; }
.section-title { font-weight: 600; color: #333; }

.path-item { display: flex; align-items: center; gap: 12px; padding: 8px 0; }
.path-item:not(:last-child) { border-bottom: 1px solid #f0f0f0; }
.path-name { flex: 1; font-size: 14px; color: #555; }

.todo-item { display: flex; align-items: center; gap: 10px; padding: 6px 0; font-size: 14px; color: #666; }

.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.stat-item { text-align: center; }
.stat-num { display: block; font-size: 24px; font-weight: 700; color: #3670e8; }
.stat-label { font-size: 12px; color: #999; }

.quick-links { display: flex; flex-direction: column; gap: 8px; }
.quick-links .el-button { justify-content: flex-start; }
</style>
