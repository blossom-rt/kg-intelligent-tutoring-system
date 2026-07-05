<template>
  <aside class="sidebar" :class="{ collapsed: isCollapsed }">
    <!-- 用户信息 + 折叠 -->
    <div class="user-area" @click="toggle" :title="isCollapsed ? '展开菜单' : '收起菜单'">
      <div class="user-avatar">{{ roleLabel.charAt(0) }}</div>
      <span v-if="!isCollapsed" class="user-role">{{ roleLabel }}</span>
      <el-icon v-if="!isCollapsed" class="toggle-icon" :size="14"><Fold /></el-icon>
    </div>

    <!-- 菜单 -->
    <nav class="nav-list">
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ active: isActive(item) }"
        :title="isCollapsed ? item.label : ''"
      >
        <el-icon :size="20"><component :is="item.icon" /></el-icon>
        <span v-if="!isCollapsed" class="nav-label">{{ item.label }}</span>
      </router-link>
    </nav>

    <!-- 底部退出 -->
    <div class="sidebar-footer">
      <button class="nav-item logout" @click="logout" :title="isCollapsed ? '退出登录' : ''">
        <el-icon :size="20"><SwitchButton /></el-icon>
        <span v-if="!isCollapsed" class="nav-label">退出登录</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useSidebar } from '../composables/useSidebar'
import {
  Fold, SwitchButton,
  HomeFilled, Grid, Connection, MapLocation, EditPen, Tickets,
  Notebook, DataAnalysis, School, Share, User, Lock, Bell, Files, Monitor
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const { isCollapsed, toggle } = useSidebar()

const role = computed(() => localStorage.getItem('role') || 'student')
const roleLabel = computed(() => ({ student: '学生端', teacher: '教师端', admin: '管理端' }[role.value] || ''))

const menuItems = computed(() => {
  const menus = {
    student: [
      { path: '/student', label: '学习首页', icon: HomeFilled },
      { path: '/student/knowledge', label: '知识图谱', icon: Grid },
      { path: '/student/themes', label: '跨学科主题', icon: Connection },
      { path: '/student/path', label: '学习路径', icon: MapLocation },
      { path: '/student/exams', label: '测评中心', icon: Tickets },
      { path: '/student/wrong', label: '错题本', icon: Notebook },
    ],
    teacher: [
      { path: '/teacher', label: '工作台', icon: HomeFilled },
      { path: '/teacher/courses', label: '课程管理', icon: School },
      { path: '/teacher/nodes', label: '知识点管理', icon: Grid },
      { path: '/teacher/edges', label: '图谱编辑', icon: Share },
      { path: '/teacher/questions', label: '题库管理', icon: EditPen },
      { path: '/teacher/exams', label: '测评管理', icon: Tickets },
      { path: '/teacher/themes', label: '跨学科主题', icon: Connection },
      { path: '/teacher/analysis', label: '学情分析', icon: DataAnalysis },
    ],
    admin: [
      { path: '/admin', label: '管理后台', icon: HomeFilled },
      { path: '/admin/users', label: '用户管理', icon: User },
      { path: '/admin/roles', label: '角色管理', icon: Lock },
      { path: '/admin/courses', label: '课程管理', icon: School },
      { path: '/admin/notices', label: '公告管理', icon: Bell },
      { path: '/admin/logs', label: '操作日志', icon: Files },
      { path: '/admin/ai-logs', label: 'AI调用日志', icon: Monitor },
    ]
  }
  return menus[role.value] || menus.student
})

const isActive = (item) => {
  if (item.path === '/student' || item.path === '/teacher' || item.path === '/admin') {
    return route.path === item.path
  }
  return route.path.startsWith(item.path)
}

const logout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.sidebar {
  position: fixed; left: 0; top: 0; bottom: 0;
  width: 200px;
  background: #fffdf9;
  border-right: 1px solid #e8e3db;
  display: flex; flex-direction: column;
  z-index: 100;
  transition: width 0.25s ease;
  overflow: hidden;
}
.sidebar.collapsed { width: 60px; }

/* 用户区 — 点击折叠 */
.user-area {
  padding: 20px 16px;
  display: flex; align-items: center; gap: 12px;
  border-bottom: 1px solid #e8e3db;
  cursor: pointer; user-select: none;
  transition: padding 0.25s ease;
}
.collapsed .user-area { padding: 20px 0; justify-content: center; }
.user-avatar {
  width: 36px; height: 36px; border-radius: 10px;
  background: linear-gradient(135deg, #ff7b3d, #e06830);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 15px; flex-shrink: 0;
  transition: width 0.25s ease, height 0.25s ease;
}
.collapsed .user-avatar { width: 32px; height: 32px; font-size: 13px; }
.user-role { font-size: 14px; font-weight: 600; color: #2d2a26; flex: 1; }
.toggle-icon { color: #a09a92; flex-shrink: 0; transition: transform 0.25s ease; }
.user-area:hover .toggle-icon { color: #ff7b3d; }

/* 菜单 */
.nav-list {
  flex: 1; padding: 8px;
  display: flex; flex-direction: column; gap: 2px;
  overflow-y: auto;
}
.nav-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 12px; border-radius: 10px;
  color: #6b655e; text-decoration: none;
  font-size: 14px; font-weight: 500;
  transition: all 0.2s ease;
  white-space: nowrap;
}
.nav-item:hover { background: #f3efe8; color: #2d2a26; }
.nav-item.active { background: #fff3e8; color: #ff7b3d; }
.collapsed .nav-item { justify-content: center; padding: 10px 0; }
.nav-label { flex: 1; }

/* 底部 */
.sidebar-footer {
  padding: 8px; border-top: 1px solid #e8e3db;
}
.nav-item.logout {
  width: 100%; border: none; background: none; cursor: pointer;
  color: #a09a92; font-size: 13px;
}
.nav-item.logout:hover { color: #e74c3c; background: #fef0f0; }
</style>
