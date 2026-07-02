<template>
  <div class="admin-home">
    <header class="top-bar">
      <h2>系统管理后台</h2>
      <div>
        <span class="welcome">欢迎回来，{{ adminName }}</span>
        <el-button type="info" size="small" @click="logout">退出登录</el-button>
      </div>
    </header>

    <div class="content">
      <!-- 统计卡片 -->
      <div class="stats-row">
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.userCount }}</div>
          <div class="stat-label">总用户数</div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.courseCount }}</div>
          <div class="stat-label">课程数</div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.todayLogCount }}</div>
          <div class="stat-label">今日操作日志数</div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.aiCallCount }}</div>
          <div class="stat-label">AI调用次数</div>
        </el-card>
      </div>

      <!-- 快捷入口 -->
      <el-card class="section-card">
        <template #header><span class="section-title">快捷入口</span></template>
        <div class="quick-links">
          <el-button type="primary" @click="router.push('/admin/users')">用户管理</el-button>
          <el-button type="success" @click="router.push('/admin/roles')">角色管理</el-button>
          <el-button type="warning" @click="router.push('/admin/courses')">课程管理</el-button>
          <el-button @click="router.push('/admin/notices')">公告管理</el-button>
          <el-button type="info" @click="router.push('/admin/logs')">操作日志</el-button>
          <el-button type="danger" @click="router.push('/admin/ai-logs')">AI日志</el-button>
        </div>
      </el-card>

      <!-- 最近操作日志 -->
      <el-card class="section-card">
        <template #header><span class="section-title">最近操作日志</span></template>
        <el-table :data="recentLogs" border stripe>
          <el-table-column prop="operatorName" label="操作人" min-width="120" />
          <el-table-column prop="module" label="模块" min-width="120" />
          <el-table-column prop="content" label="操作内容" min-width="280" show-overflow-tooltip />
          <el-table-column prop="createTime" label="操作时间" min-width="160" />
        </el-table>
        <el-empty v-if="recentLogs.length === 0" description="暂无操作日志" :image-size="60" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const adminName = ref('管理员')

const stats = reactive({
  userCount: 0,
  courseCount: 0,
  todayLogCount: 0,
  aiCallCount: 0
})

const recentLogs = ref([])

onMounted(() => {
  loadDashboard()
})

function loadDashboard() {
  // 尝试从API获取数据，若不可用则使用占位数据
  adminName.value = localStorage.getItem('username') || '管理员'

  // 占位示例数据
  stats.userCount = 128
  stats.courseCount = 8
  stats.todayLogCount = 47
  stats.aiCallCount = 2156
  recentLogs.value = [
    { operatorName: 'admin', module: '用户管理', content: '新增用户 student_zhang', createTime: '2026-07-02 15:30:22' },
    { operatorName: 'admin', module: '课程管理', content: '修改课程「数据结构」信息', createTime: '2026-07-02 14:18:05' },
    { operatorName: 'admin', module: '公告管理', content: '发布新公告「期末考试安排」', createTime: '2026-07-02 11:45:30' },
    { operatorName: 'admin', module: '角色管理', content: '更新角色「teacher」权限', createTime: '2026-07-02 10:02:18' }
  ]
}

const logout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.admin-home { min-height: 100vh; background: #f5f7fa; }
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 32px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.top-bar h2 { margin: 0; font-size: 20px; color: #303133; }
.welcome { font-size: 13px; color: #999; margin-right: 12px; }

.content { padding: 24px 32px; }

.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px;
  margin-bottom: 20px;
}
.stat-card { text-align: center; }
.stat-num { font-size: 32px; font-weight: 700; color: #5b3e9e; }
.stat-label { font-size: 14px; color: #909399; margin-top: 8px; }

.section-card { margin-bottom: 20px; }
.section-title { font-weight: 600; color: #333; }

.quick-links { display: flex; gap: 12px; flex-wrap: wrap; }
</style>
