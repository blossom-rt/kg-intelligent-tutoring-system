<template>
  <div class="admin-home">
    <header class="top-bar">
      <div class="user-info">
        <div class="avatar-widget">
          <el-icon :size="22"><Setting /></el-icon>
        </div>
        <div>
          <h2>{{ greeting }}，管理员</h2>
          <p>系统运行数据一览</p>
        </div>
      </div>
      <el-button class="logout-btn" @click="logout">退出登录</el-button>
    </header>

    <div class="content">
      <!-- 统计 -->
      <div class="stats-row">
        <div class="stat-card" v-for="s in statsList" :key="s.key">
          <div class="stat-icon" :style="{ background: s.color }">
            <el-icon :size="22"><component :is="s.icon" /></el-icon>
          </div>
          <div>
            <p class="stat-num">{{ s.value }}</p>
            <span>{{ s.label }}</span>
          </div>
        </div>
      </div>

      <!-- 管理入口 -->
      <div class="card">
        <div class="card-header">
          <el-icon class="card-head-icon"><Grid /></el-icon>
          管理入口
        </div>
        <div class="entry-grid">
          <div v-for="e in entries" :key="e.key" class="entry-item" @click="$router.push(e.path)">
            <div class="entry-icon" :style="{ background: e.bg, color: e.color }">
              <el-icon :size="20"><component :is="e.icon" /></el-icon>
            </div>
            <span class="entry-label">{{ e.label }}</span>
          </div>
        </div>
      </div>

      <!-- 公告 -->
      <div class="card">
        <div class="card-header">
          <el-icon class="card-head-icon"><Bell /></el-icon>
          系统公告
        </div>
        <div v-if="notices.length" class="notice-list">
          <div v-for="n in notices" :key="n.id" class="notice-item" @click="showNotice(n)">
            <el-tag size="small" type="warning">公告</el-tag>
            <span class="notice-title">{{ n.title }}</span>
          </div>
        </div>
        <div v-else class="empty-hint">暂无公告</div>
      </div>

      <el-dialog v-model="noticeDialog" :title="currentNotice?.title || '公告详情'" width="560px">
        <div class="notice-body">{{ currentNotice?.content }}</div>
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
  Setting, User, Lock, School, Bell, Files, Monitor, Grid
} from '@element-plus/icons-vue'
import { getNoticeList, getUserList, getRoleList, getOperLogs } from '../api/admin'

const router = useRouter()
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const notices = ref([])
const noticeDialog = ref(false)
const currentNotice = ref(null)

const statsList = ref([
  { key: 'users', icon: User, label: '注册用户', value: 0, color: 'linear-gradient(135deg, #ff7b3d, #e06830)' },
  { key: 'roles', icon: Lock, label: '角色数量', value: 0, color: 'linear-gradient(135deg, #d4a853, #b89030)' },
  { key: 'notices', icon: Bell, label: '系统公告', value: 0, color: 'linear-gradient(135deg, #5eaf83, #3d8a5e)' },
  { key: 'logs', icon: Files, label: '操作日志', value: 0, color: 'linear-gradient(135deg, #f5a623, #d4881a)' }
])

const entries = [
  { key: 'users', icon: User, label: '用户管理', path: '/admin/users', bg: '#fff3e8', color: '#e06830' },
  { key: 'roles', icon: Lock, label: '角色管理', path: '/admin/roles', bg: '#f0e8f5', color: '#7a5dba' },
  { key: 'courses', icon: School, label: '课程管理', path: '/admin/courses', bg: '#e8f5ec', color: '#2d8a4e' },
  { key: 'notices', icon: Bell, label: '公告管理', path: '/admin/notices', bg: '#eef2f8', color: '#3d5a8e' },
  { key: 'logs', icon: Files, label: '操作日志', path: '/admin/logs', bg: '#e8f0f8', color: '#5a7dba' },
  { key: 'ai-logs', icon: Monitor, label: 'AI 调用日志', path: '/admin/ai-logs', bg: '#fef3e8', color: '#c07830' }
]

const showNotice = (n) => { currentNotice.value = n; noticeDialog.value = true }

const logout = () => { localStorage.clear(); router.push('/login') }

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
  const [noticeRes, userRes, roleRes, logRes] = await Promise.allSettled([
    getNoticeList(), getUserList(), getRoleList(), getOperLogs()
  ])
  if (noticeRes.status === 'fulfilled' && noticeRes.value) {
    const list = extractArray(noticeRes.value)
    notices.value = list
    statsList.value[2].value = list.length
  }
  if (userRes.status === 'fulfilled' && userRes.value) {
    statsList.value[0].value = extractArray(userRes.value).length
  }
  if (roleRes.status === 'fulfilled' && roleRes.value) {
    statsList.value[1].value = extractArray(roleRes.value).length
  }
  if (logRes.status === 'fulfilled' && logRes.value) {
    statsList.value[3].value = extractArray(logRes.value).length
  }
})
</script>

<style scoped>
.admin-home { min-height: 100vh; background: #faf7f2; }

/* 顶栏 */
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 36px;
  background: linear-gradient(135deg, #f5f1ea, #f0ece4); color: #2d2a26;
}
.user-info { display: flex; align-items: center; gap: 16px; }
.avatar-widget {
  width: 44px; height: 44px; border-radius: 12px;
  background: linear-gradient(135deg, #d4a853, #b89030);
  display: flex; align-items: center; justify-content: center; color: #fff;
}
.user-info h2 { margin: 0; font-size: 20px; font-weight: 600; }
.user-info p { margin: 2px 0 0; font-size: 13px; opacity: 0.7; }
.logout-btn {
  background: rgba(0,0,0,0.04); border: 1px solid rgba(0,0,0,0.12);
  color: #6b655e; border-radius: 8px;
}
.logout-btn:hover { background: rgba(0,0,0,0.08); }

/* 内容 */
.content { padding: 24px 36px; display: flex; flex-direction: column; gap: 20px; }

/* 统计 */
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-card {
  background: #fffdf9; border-radius: 16px; padding: 24px;
  display: flex; align-items: center; gap: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.stat-icon {
  width: 48px; height: 48px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0;
}
.stat-num { margin: 0; font-size: 26px; font-weight: 700; color: #2d2a26; line-height: 1.1; }
.stat-card span { font-size: 13px; color: #a09a92; }

/* 卡片 */
.card {
  background: #fffdf9; border-radius: 16px; padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.card-header {
  font-size: 16px; font-weight: 600; color: #2d2a26; margin-bottom: 20px;
  display: flex; align-items: center; gap: 8px;
}
.card-head-icon { color: #ff7b3d; }

/* 入口 */
.entry-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 14px; }
.entry-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 18px 12px; border-radius: 14px; background: #f8f5f0;
  cursor: pointer; transition: background 0.2s ease, transform 0.2s ease;
}
.entry-item:hover { background: #f3efe8; transform: translateY(-3px); }
.entry-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
}
.entry-label { font-size: 14px; font-weight: 600; color: #2d2a26; }

/* 公告 */
.notice-list { display: flex; flex-direction: column; gap: 8px; }
.notice-item {
  display: flex; align-items: center; gap: 12px; padding: 10px 12px;
  border-radius: 8px; cursor: pointer; transition: background 0.2s ease;
}
.notice-item:hover { background: #f8f5f0; }
.notice-title { font-size: 14px; color: #2d2a26; }
.empty-hint { color: #a09a92; font-size: 14px; text-align: center; padding: 20px 0; }
.notice-body { font-size: 14px; line-height: 1.8; color: #2d2a26; white-space: pre-wrap; }

@media (max-width: 1024px) { .entry-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 640px) { .entry-grid { grid-template-columns: 1fr 1fr; } .stats-row { grid-template-columns: repeat(2, 1fr); } }
</style>
