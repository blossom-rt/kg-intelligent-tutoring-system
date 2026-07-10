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
          <div v-for="e in entries" :key="e.key" class="entry-item" @click="$router.push(e.path)">
            <div class="entry-icon">
              <el-icon :size="22"><component :is="e.icon"></component></el-icon>
            </div>
            <span class="entry-label">{{ e.label }}</span>
            <span class="entry-desc">{{ e.desc }}</span>
          </div>
        </div>
      </el-card>

      <el-card class="section-card">
        <template #header><span class="section-title">系统公告</span></template>
        <div v-if="notices.length">
          <div v-for="n in notices" :key="n.id" class="notice-item" @click="showNotice(n)">
            <div class="notice-main">
              <el-tag size="small" type="warning">公告</el-tag>
              <span class="notice-title">{{ n.title }}</span>
            </div>
            <span v-if="n.createTime" class="notice-time">{{ n.createTime }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无公告" :image-size="60" />
      </el-card>

      <el-dialog v-model="noticeDialog" :title="currentNotice?.title || '公告详情'" width="560px">
        <div class="notice-content">{{ currentNotice?.content }}</div>
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
import {
  Setting, User, Lock, School, Bell, Files, Monitor
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
  { key: 'users', icon: User, label: '用户管理', value: 0, color: '#ff7b3d' },
  { key: 'roles', icon: Lock, label: '角色管理', value: 0, color: '#b89030' },
  { key: 'notices', icon: Bell, label: '公告管理', value: 0, color: '#d4a853' },
  { key: 'logs', icon: Files, label: '日志审计', value: 0, color: '#8a7a5c' }
])

const entries = [
  { key: 'users', icon: User, label: '用户管理', desc: '账号创建、启停与角色分配', path: '/admin/users' },
  { key: 'roles', icon: Lock, label: '角色管理', desc: '维护系统角色与权限', path: '/admin/roles' },
  { key: 'courses', icon: School, label: '课程管理', desc: '课程信息与开课配置', path: '/admin/courses' },
  { key: 'notices', icon: Bell, label: '公告管理', desc: '发布与维护系统公告', path: '/admin/notices' },
  { key: 'logs', icon: Files, label: '操作日志', desc: '审计用户操作记录', path: '/admin/logs' },
  { key: 'ai-logs', icon: Monitor, label: 'AI日志', desc: '查看 AI 调用明细', path: '/admin/ai-logs' }
]

const showNotice = (n) => {
  currentNotice.value = n
  noticeDialog.value = true
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

  // 公告列表
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
.admin-home { min-height: 100vh; background: var(--bg-root); }

/* 顶栏 */
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 36px; background: var(--bg-surface);
  border-bottom: 1px solid var(--border-subtle);
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
}
.user-info { display: flex; align-items: center; gap: 14px; }
.avatar-widget {
  width: 44px; height: 44px; border-radius: 12px;
  background: linear-gradient(135deg, #d4a853, #b89030);
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(184, 144, 48, 0.22);
}
.user-info h2 { margin: 0; font-size: 18px; font-weight: 700; color: var(--text-primary); }
.user-info p { margin: 2px 0 0; font-size: 13px; color: var(--text-secondary); }

.logout-btn:hover {
  background: var(--accent-gold) !important;
  color: #fff !important;
  border-color: var(--accent-gold) !important;
}

.content { padding: 24px 36px; display: flex; flex-direction: column; gap: 20px; }

/* 统计卡 */
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-card {
  background: var(--bg-surface); border-radius: 14px; padding: 20px 24px;
  display: flex; align-items: center; gap: 16px;
  border: 1px solid var(--border-subtle);
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.06); }
.stat-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
}
.stat-card .stat-num {
  margin: 0; font-size: 24px; font-weight: 800;
  color: var(--text-primary); letter-spacing: 0.5px;
}
.stat-card span { font-size: 13px; color: var(--text-secondary); }

.section-card { border-radius: 14px; }
.section-title { font-weight: 700; color: var(--text-primary); }

/* 管理入口卡片网格 */
.entry-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.entry-item {
  display: flex; flex-direction: column; gap: 8px;
  padding: 18px 20px; border-radius: 12px;
  border: 1px solid var(--border-subtle); background: var(--bg-input);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}
.entry-item:hover {
  transform: translateY(-3px); border-color: var(--accent-gold);
  box-shadow: 0 8px 20px rgba(184, 144, 48, 0.12);
}
.entry-icon {
  width: 40px; height: 40px; border-radius: 10px;
  background: linear-gradient(135deg, #d4a853, #b89030);
  color: #fff; display: flex; align-items: center; justify-content: center;
}
.entry-label { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.entry-desc { font-size: 12px; color: var(--text-muted); line-height: 1.5; }

/* 公告 */
.notice-item {
  display: flex; align-items: center; justify-content: space-between;
  gap: 12px; padding: 10px 4px; cursor: pointer;
  border-bottom: 1px solid var(--border-subtle);
  transition: background 0.2s ease;
}
.notice-item:last-child { border-bottom: none; }
.notice-item:hover { background: var(--bg-hover); }
.notice-main { display: flex; align-items: center; gap: 10px; min-width: 0; }
.notice-title {
  font-size: 14px; font-weight: 500; color: var(--accent-gold);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.notice-time { font-size: 12px; color: var(--text-muted); flex-shrink: 0; }

.notice-content {
  font-size: 14px; line-height: 1.8; color: var(--text-secondary); white-space: pre-wrap;
}

@media (max-width: 768px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .entry-grid { grid-template-columns: repeat(2, 1fr); }
  .top-bar, .content { padding-left: 20px; padding-right: 20px; }
}
</style>
