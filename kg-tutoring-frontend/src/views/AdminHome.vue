<template>
  <div class="admin-home">
    <header class="top-bar">
      <div class="user-info">
        <div class="avatar-widget">
          <el-icon :size="22"><Setting /></el-icon>
        </div>
        <div>
          <h2>{{ greeting }}，管理员</h2>
          <p>系统管理后台</p>
        </div>
      </div>
      <el-button class="logout-btn" @click="logout">退出登录</el-button>
    </header>

    <div class="content">
      <div class="stats-row">
        <div class="stat-card" v-for="s in statsList" :key="s.key">
          <div class="stat-icon" :style="{ background: s.color }">
            <el-icon :size="22"><component :is="s.icon"></component></el-icon>
          </div>
          <div>
            <p class="stat-num">{{ s.value }}</p>
            <span>{{ s.label }}</span>
          </div>
        </div>
      </div>

      <el-card class="section-card">
        <template #header><span class="section-title">管理入口</span></template>
        <div class="entry-grid">
          <el-button v-for="e in entries" :key="e.key" class="entry-btn" @click="$router.push(e.path)">
            <el-icon :size="18"><component :is="e.icon"></component></el-icon>
            <span>{{ e.label }}</span>
          </el-button>
        </div>
      </el-card>

      <el-card class="section-card">
        <template #header><span class="section-title">系统公告</span></template>
        <div v-if="notices.length">
          <div v-for="n in notices" :key="n.id" class="notice-item" style="padding:8px 0;cursor:pointer;" @click="showNotice(n)">
            <el-tag size="small" type="warning">公告</el-tag>
            <span style="margin-left:8px;color:#3670e8;">{{ n.title }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无公告" :image-size="60" />
      </el-card>

      <el-dialog v-model="noticeDialog" :title="currentNotice?.title || '公告详情'" width="560px">
        <div style="font-size:14px;line-height:1.8;color:#333;white-space:pre-wrap;">{{ currentNotice?.content }}</div>
        <template #footer>
          <el-button @click="noticeDialog = false">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Setting, User, Lock, School, Bell, Files, Monitor,
  TrendCharts, Finished, EditPen
} from '@element-plus/icons-vue'
import { getNoticeList, getUserList, getRoleList, getOperLogs } from '../api/admin'

const router = useRouter()
const greeting = computed(() => {
  const h = new Date().getHours()
  return h < 12 ? '上午好' : h < 18 ? '下午好' : '晚上好'
})

const notices = ref([])
const noticeDialog = ref(false)
const currentNotice = ref(null)

const statsList = ref([
  { key: 'users', icon: User, label: '用户管理', value: 0, color: '#409eff' },
  { key: 'roles', icon: Lock, label: '角色管理', value: 0, color: '#7b1fa2' },
  { key: 'notices', icon: Bell, label: '公告管理', value: 0, color: '#f57c00' },
  { key: 'logs', icon: Files, label: '日志审计', value: 0, color: '#c62828' }
])

const entries = [
  { key: 'users', icon: User, label: '用户管理', path: '/admin/users' },
  { key: 'roles', icon: Lock, label: '角色管理', path: '/admin/roles' },
  { key: 'courses', icon: School, label: '课程管理', path: '/admin/courses' },
  { key: 'notices', icon: Bell, label: '公告管理', path: '/admin/notices' },
  { key: 'logs', icon: Files, label: '操作日志', path: '/admin/logs' },
  { key: 'ai-logs', icon: Monitor, label: 'AI日志', path: '/admin/ai-logs' }
]

const showNotice = (n) => {
  currentNotice.value = n
  noticeDialog.value = true
}

const logout = () => {
  localStorage.clear()
  router.push('/login')
}

/**
 * 从 API 响应中安全提取数组
 */
const extractArray = (data) => {
  if (Array.isArray(data)) return data
  if (data && typeof data === 'object') {
    if (Array.isArray(data.records)) return data.records
    if (Array.isArray(data.data)) return data.data
    if (Array.isArray(data.list)) return data.list
  }
  return []
}

onMounted(async () => {
  // 获取公告与统计数据（并行请求，避免重复调用）
  const [noticeRes, userRes, roleRes, logRes] = await Promise.allSettled([
    getNoticeList(),
    getUserList(),
    getRoleList(),
    getOperLogs()
  ])

  // 公告列表（可正常显示）
  if (noticeRes.status === 'fulfilled' && noticeRes.value) {
    const list = extractArray(noticeRes.value)
    notices.value = list
    statsList.value[2].value = list.length
  }

  // 用户数
  if (userRes.status === 'fulfilled' && userRes.value) {
    const list = extractArray(userRes.value)
    statsList.value[0].value = list.length
  }

  // 角色数
  if (roleRes.status === 'fulfilled' && roleRes.value) {
    const list = extractArray(roleRes.value)
    statsList.value[1].value = list.length
  }

  // 操作日志数
  if (logRes.status === 'fulfilled' && logRes.value) {
    const list = extractArray(logRes.value)
    statsList.value[3].value = list.length
  }
})
</script>

<style scoped>
.admin-home { min-height: 100vh; background: #f5f7fa; }
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 32px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.user-info { display: flex; align-items: center; gap: 14px; }
.avatar-widget {
  width: 44px; height: 44px; border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
}
.user-info h2 { margin: 0; font-size: 18px; color: #303133; }
.user-info p { margin: 2px 0 0; font-size: 13px; color: #909399; }
.logout-btn { flex-shrink: 0; }
.content { padding: 24px 32px; display: flex; flex-direction: column; gap: 20px; }

.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-card {
  background: #fff; border-radius: 12px; padding: 20px 24px;
  display: flex; align-items: center; gap: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.stat-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
}
.stat-card .stat-num { margin: 0; font-size: 22px; font-weight: 700; color: #303133; }
.stat-card span { font-size: 13px; color: #909399; }

.section-card { border-radius: 12px; }
.section-title { font-weight: 600; color: #333; }

.entry-grid { display: flex; flex-wrap: wrap; gap: 12px; }
.entry-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 16px 20px; border-radius: 10px; border: 1px solid #ebeef5;
  background: #fafafa; cursor: pointer; font-size: 14px; color: #303133;
  transition: all 0.2s ease;
}
.entry-btn:hover { background: #ecf5ff; border-color: #b3d8ff; color: #409eff; transform: translateY(-1px); }

.notice-item { display: flex; align-items: center; gap: 10px; padding: 6px 0; font-size: 14px; color: #666; }
</style>
