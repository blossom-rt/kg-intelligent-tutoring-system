<template>
  <div class="teacher-home">
    <header class="top-bar">
      <div class="user-info">
        <div class="avatar-widget">
          <el-icon :size="22"><Reading /></el-icon>
        </div>
        <div>
          <h2>{{ greeting }}，{{ teacherName }}</h2>
          <p>{{ subtitle }}</p>
        </div>
      </div>
      <el-button class="logout-btn" @click="logout">退出登录</el-button>
    </header>

    <div class="content">
      <!-- 统计卡片 -->
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
      <div class="card section-card">
        <div class="card-header">
          <el-icon class="card-head-icon"><Menu /></el-icon>
          管理入口
        </div>
        <div class="entry-grid">
          <div
            v-for="m in entries" :key="m.key"
            class="entry-item" @click="$router.push(m.path)"
          >
            <div class="entry-icon" :style="{ background: m.bg, color: m.color }">
              <el-icon :size="22"><component :is="m.icon" /></el-icon>
            </div>
            <span class="entry-label">{{ m.label }}</span>
            <span class="entry-desc">{{ m.desc }}</span>
          </div>
        </div>
      </div>

      <!-- 待办 & 薄弱分析 -->
      <div class="bottom-row">
        <div class="card" style="flex:1">
          <div class="card-header">
            <el-icon class="card-head-icon"><Bell /></el-icon>
            待处理事项
          </div>
          <div class="empty-hint">暂无待处理事项</div>
        </div>
        <div class="card" style="flex:1">
          <div class="card-header">
            <el-icon class="card-head-icon"><Warning /></el-icon>
            薄弱知识点 TOP5
          </div>
          <div class="empty-hint">暂无数据</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Reading, User, Timer, Collection, Document,
  Menu, Bell, Warning,
  School, Grid, Share, EditPen, Tickets, Connection, DataAnalysis
} from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const stats = ref({ activeStudents: 0, weekStudy: 0, totalNodes: 0, totalQuestions: 0 })
const teacherName = ref('老师')

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 9) return '早上好'; if (h < 12) return '上午好'
  if (h < 14) return '中午好'; if (h < 18) return '下午好'
  return '晚上好'
})
const subtitle = computed(() => {
  const h = new Date().getHours()
  if (h < 9) return '早起备课，一天好开始'
  if (h < 12) return '把握课堂，关注每个学生'
  if (h < 18) return '学情数据实时更新中'
  return '回顾今天的教学成果'
})

const statsList = computed(() => [
  { key: 'students', icon: User, label: '活跃学生', value: stats.value.activeStudents, color: 'linear-gradient(135deg, #ff7b3d, #e06830)' },
  { key: 'study', icon: Timer, label: '本周学习人次', value: stats.value.weekStudy, color: 'linear-gradient(135deg, #5eaf83, #3d8a5e)' },
  { key: 'nodes', icon: Grid, label: '知识点总数', value: stats.value.totalNodes, color: 'linear-gradient(135deg, #d4a853, #b89030)' },
  { key: 'questions', icon: EditPen, label: '题库总量', value: stats.value.totalQuestions, color: 'linear-gradient(135deg, #e08850, #c06838)' },
])

const entries = [
  { key: 'courses', icon: School, label: '课程管理', desc: '创建与编排课程', path: '/teacher/courses', bg: '#e8f5ec', color: '#2d8a4e' },
  { key: 'nodes', icon: Grid, label: '知识点管理', desc: '知识节点与依赖', path: '/teacher/nodes', bg: '#fff3e8', color: '#e06830' },
  { key: 'edges', icon: Share, label: '图谱编辑', desc: '可视化调整依赖关系', path: '/teacher/edges', bg: '#eef2f8', color: '#3d5a8e' },
  { key: 'questions', icon: EditPen, label: '题库管理', desc: '习题录入与维护', path: '/teacher/questions', bg: '#e8f0f8', color: '#5a7dba' },
  { key: 'exams', icon: Tickets, label: '测评管理', desc: '组卷与阅卷分析', path: '/teacher/exams', bg: '#fef3e8', color: '#c07830' },
  { key: 'themes', icon: Connection, label: '跨学科主题', desc: '多学科融合项目', path: '/teacher/themes', bg: '#f0e8f5', color: '#7a5dba' },
  { key: 'analysis', icon: DataAnalysis, label: '学情分析', desc: '班级与个体报告', path: '/teacher/analysis', bg: '#e8f5f0', color: '#3d7a5e' },
]

onMounted(async () => {
  try { const res = await request.get('/teacher/dashboard'); if (res) stats.value = res } catch { }
})

const logout = () => { localStorage.clear(); router.push('/login') }
</script>

<style scoped>
.teacher-home { min-height: 100vh; background: #faf7f2; }
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 36px;
  background: linear-gradient(135deg, #2d8a4e, #43b878); color: #fff;
}
.user-info { display: flex; align-items: center; gap: 16px; }
.avatar-widget {
  width: 44px; height: 44px; border-radius: 12px;
  background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center;
}
.user-info h2 { margin: 0; font-size: 20px; }
.user-info p { margin: 2px 0 0; font-size: 13px; opacity: 0.8; }
.logout-btn {
  background: rgba(255,255,255,0.15); border: 1px solid rgba(255,255,255,0.3);
  color: #fff; border-radius: 8px;
}
.logout-btn:hover { background: rgba(255,255,255,0.25); }

.content { padding: 24px 36px; }

/* 统计卡片 */
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
.stat-card {
  background: #fffdf9; border-radius: 16px; padding: 24px;
  display: flex; align-items: center; gap: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.stat-icon {
  width: 48px; height: 48px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center; color: #fff;
  flex-shrink: 0;
}
.stat-num { font-size: 26px; font-weight: 700; color: #2d2a26; margin: 0; line-height: 1.1; }
.stat-card span { font-size: 13px; color: #a09a92; }

/* 管理入口 */
.section-card { margin-bottom: 24px; }
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
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
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
  margin-bottom: 2px;
}
.entry-label { font-size: 14px; font-weight: 600; color: #2d2a26; }
.entry-desc { font-size: 11px; color: #a09a92; }

/* 底部双栏 */
.bottom-row { display: flex; gap: 20px; }
.empty-hint { color: #a09a92; font-size: 14px; text-align: center; padding: 28px 0; }

@media (max-width: 1024px) {
  .entry-grid { grid-template-columns: repeat(3, 1fr); }
  .stats-row { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .entry-grid { grid-template-columns: 1fr 1fr; }
  .bottom-row { flex-direction: column; }
  .content { padding: 16px; }
}
</style>
