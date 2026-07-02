<template>
  <div class="teacher-home">
    <header class="top-bar">
      <div>
        <h2>教师工作台</h2>
        <span class="welcome">欢迎回来，{{ teacherName }}</span>
      </div>
      <el-button type="info" size="small" @click="logout">退出登录</el-button>
    </header>

    <div class="content">
      <!-- 统计卡片 -->
      <div class="stats-row">
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.courseCount }}</div>
          <div class="stat-label">负责课程数</div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.studentCount }}</div>
          <div class="stat-label">学生总数</div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.nodeCount }}</div>
          <div class="stat-label">知识点总数</div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.questionCount }}</div>
          <div class="stat-label">题目总数</div>
        </el-card>
      </div>

      <!-- 快捷入口 -->
      <el-card class="section-card">
        <template #header><span class="section-title">快捷入口</span></template>
        <div class="quick-links">
          <el-button type="primary" @click="router.push('/teacher/courses')">课程管理</el-button>
          <el-button type="success" @click="router.push('/teacher/nodes')">知识点管理</el-button>
          <el-button type="warning" @click="router.push('/teacher/themes')">跨学科主题</el-button>
          <el-button type="info" @click="router.push('/teacher/analysis')">学情分析</el-button>
        </div>
      </el-card>

      <!-- 最近测评 -->
      <el-card class="section-card">
        <template #header><span class="section-title">最近发布的测评</span></template>
        <el-table :data="recentExams" v-loading="examLoading" border stripe>
          <el-table-column prop="examName" label="测评名称" min-width="200" />
          <el-table-column prop="courseName" label="所属课程" min-width="160" />
          <el-table-column prop="studentCount" label="参与人数" min-width="100" />
          <el-table-column prop="avgScore" label="平均分" min-width="100" />
          <el-table-column prop="createTime" label="发布时间" min-width="160" />
        </el-table>
        <el-empty v-if="!examLoading && recentExams.length === 0" description="暂无测评数据" :image-size="60" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const teacherName = ref('老师')
const examLoading = ref(false)
const recentExams = ref([])

const stats = reactive({
  courseCount: 0,
  studentCount: 0,
  nodeCount: 0,
  questionCount: 0
})

onMounted(() => {
  loadDashboard()
})

async function loadDashboard() {
  // 尝试从API获取数据，若不可用则使用占位数据
  try {
    const token = localStorage.getItem('token')
    if (token) {
      // 可在此调用 API 获取教师仪表盘数据
      // 因 API 可能尚未就绪，使用占位数据
    }
  } catch { /* ignore */ }

  // 占位示例数据
  teacherName.value = localStorage.getItem('username') || '老师'
  stats.courseCount = 3
  stats.studentCount = 86
  stats.nodeCount = 42
  stats.questionCount = 156
  recentExams.value = [
    { id: 1, examName: '数据结构期末测评', courseName: '数据结构', studentCount: 28, avgScore: 78.5, createTime: '2026-06-28 10:00' },
    { id: 2, examName: '算法基础单元测验', courseName: '算法设计', studentCount: 30, avgScore: 82.3, createTime: '2026-06-20 14:30' },
    { id: 3, examName: '计算机网络综合测试', courseName: '计算机网络', studentCount: 28, avgScore: 75.8, createTime: '2026-06-15 09:00' }
  ]
}

const logout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.teacher-home { min-height: 100vh; background: #f5f7fa; }
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 32px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.top-bar h2 { margin: 0; font-size: 20px; color: #303133; }
.welcome { font-size: 13px; color: #999; margin-left: 12px; }

.content { padding: 24px 32px; }

.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px;
  margin-bottom: 20px;
}
.stat-card { text-align: center; }
.stat-num { font-size: 32px; font-weight: 700; color: #3670e8; }
.stat-label { font-size: 14px; color: #909399; margin-top: 8px; }

.section-card { margin-bottom: 20px; }
.section-title { font-weight: 600; color: #333; }

.quick-links { display: flex; gap: 12px; flex-wrap: wrap; }
</style>
