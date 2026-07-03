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
      <!-- 管理入口 -->
      <div class="card full-width">
        <div class="card-header">
          <el-icon class="card-head-icon"><Menu /></el-icon>
          管理入口
        </div>
        <div class="entry-grid">
          <div
            v-for="e in entries" :key="e.key"
            class="entry-item" @click="$router.push(e.path)"
          >
            <div class="entry-icon" :style="{ background: e.bg, color: e.color }">
              <el-icon :size="22"><component :is="e.icon" /></el-icon>
            </div>
            <span class="entry-label">{{ e.label }}</span>
            <span class="entry-desc">{{ e.desc }}</span>
          </div>
        </div>
      </div>

      <!-- 运营区 -->
      <div class="dual-row">
        <div class="card" style="flex:1">
          <div class="card-header">
            <el-icon class="card-head-icon"><DataAnalysis /></el-icon>
            运营数据
          </div>
          <div class="mini-stats">
            <div class="mini-item">
              <span class="mini-num">{{ stats.userCount }}</span>
              <span class="mini-label">注册用户</span>
            </div>
            <div class="mini-item">
              <span class="mini-num">{{ stats.todayLogin }}</span>
              <span class="mini-label">今日登录</span>
            </div>
          </div>
        </div>
        <div class="card" style="flex:1">
          <div class="card-header">
            <el-icon class="card-head-icon"><Document /></el-icon>
            操作日志
          </div>
          <div class="empty-hint">查看详细日志</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Setting, Menu, DataAnalysis, Document,
  User, Lock, School, Bell, Files, Monitor
} from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const stats = ref({ userCount: 0, todayLogin: 0 })

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 9) return '早上好'; if (h < 12) return '上午好'
  if (h < 14) return '中午好'; if (h < 18) return '下午好'
  return '晚上好'
})

const entries = [
  { key: 'users', icon: User, label: '用户管理', desc: '账号创建与维护', path: '/admin/users', bg: '#fff3e8', color: '#e06830' },
  { key: 'roles', icon: Lock, label: '角色管理', desc: '权限与角色配置', path: '/admin/roles', bg: '#f0e8f5', color: '#7a5dba' },
  { key: 'courses', icon: School, label: '课程管理', desc: '全局课程编排', path: '/admin/courses', bg: '#e8f5ec', color: '#2d8a4e' },
  { key: 'notices', icon: Bell, label: '公告管理', desc: '发布系统通知', path: '/admin/notices', bg: '#eef2f8', color: '#3d5a8e' },
  { key: 'logs', icon: Files, label: '操作日志', desc: '用户行为审计', path: '/admin/logs', bg: '#e8f0f8', color: '#5a7dba' },
  { key: 'aiLogs', icon: Monitor, label: 'AI 调用日志', desc: '大模型调用追踪', path: '/admin/ai-logs', bg: '#fef3e8', color: '#c07830' },
]

onMounted(async () => {
  try { const res = await request.get('/admin/dashboard'); if (res) stats.value = res } catch { }
})

const logout = () => { localStorage.clear(); router.push('/login') }
</script>

<style scoped>
.admin-home { min-height: 100vh; background: #faf7f2; }
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 36px;
  background: linear-gradient(135deg, #f5f1ea, #f0ece4); color: #2d2a26;
}
.user-info { display: flex; align-items: center; gap: 16px; }
.avatar-widget {
  width: 44px; height: 44px; border-radius: 12px;
  background: rgba(0,0,0,0.05); display: flex; align-items: center; justify-content: center;
}
.user-info h2 { margin: 0; font-size: 20px; }
.user-info p { margin: 2px 0 0; font-size: 13px; opacity: 0.7; }
.logout-btn {
  background: rgba(0,0,0,0.04); border: 1px solid rgba(0,0,0,0.12);
  color: #6b655e; border-radius: 8px;
}
.logout-btn:hover { background: rgba(0,0,0,0.08); }

.content { padding: 28px 36px; }

/* 管理入口 */
.full-width { margin-bottom: 24px; }
.card {
  background: #fffdf9; border-radius: 16px; padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.card-header {
  font-size: 16px; font-weight: 600; color: #2d2a26; margin-bottom: 20px;
  display: flex; align-items: center; gap: 8px;
}
.card-head-icon { color: #ff7b3d; }
.entry-grid {
  display: grid; grid-template-columns: repeat(6, 1fr); gap: 14px;
}
.entry-item {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 20px 12px 16px; border-radius: 14px;
  background: #f8f5f0; cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}
.entry-item:hover { background: #f3efe8; transform: translateY(-3px); }
.entry-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
}
.entry-label { font-size: 14px; font-weight: 600; color: #2d2a26; }
.entry-desc { font-size: 11px; color: #a09a92; }

/* 底部 */
.dual-row { display: flex; gap: 20px; }
.empty-hint { color: #a09a92; font-size: 14px; text-align: center; padding: 28px 0; }

.mini-stats { display: flex; gap: 24px; justify-content: center; padding: 20px 0; }
.mini-item { text-align: center; }
.mini-num { font-size: 32px; font-weight: 700; color: #d4a853; display: block; }
.mini-label { font-size: 13px; color: #a09a92; }

@media (max-width: 1024px) { .entry-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 640px) { .entry-grid { grid-template-columns: 1fr 1fr; } .dual-row { flex-direction: column; } }
</style>
