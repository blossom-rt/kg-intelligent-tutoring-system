<template>
  <div class="student-home">
    <header class="top-bar">
      <div class="user-info">
        <!-- 时间感知问候小组件 -->
        <div class="greeting-widget" :class="timePeriod">
          <div class="celestial">
            <div class="sun-moon"></div>
            <div class="orbit-ring"></div>
          </div>
        </div>
        <div>
          <h2>{{ timeGreeting }}，{{ studentName }}</h2>
          <p>{{ motivationTip }}</p>
        </div>
      </div>
    </header>

    <div class="dashboard">
      <div class="main-col">
        <div class="card">
          <div class="card-header"><el-icon class="card-icon"><Clock /></el-icon>快捷续学</div>
          <div v-if="activePaths.length" class="path-list">
            <div v-for="p in activePaths" :key="p.id" class="path-item">
              <div class="path-info">
                <span class="path-title">{{ p.pathName }}</span>
                <el-progress :percentage="p.progress" :stroke-width="6" :show-text="false" />
              </div>
              <el-tag size="small" round>进行中</el-tag>
              <el-button type="primary" size="small" round @click="$router.push('/student/path/' + p.id)">继续</el-button>
            </div>
          </div>
          <div v-else class="empty-hint">暂无进行中的学习，去知识图谱开启一段旅程吧</div>
        </div>

        <div class="card">
          <div class="card-header"><el-icon class="card-icon"><Bell /></el-icon>待办提醒</div>
          <div v-if="todos.length" class="todo-list">
            <div v-for="t in todos" :key="t.id" class="todo-item">
              <el-tag :type="t.tagType || 'info'" size="small" round>{{ t.label }}</el-tag>
              <span>{{ t.content }}</span>
            </div>
          </div>
          <div v-else class="empty-hint">暂无待办，一切尽在掌握</div>
        </div>

        <div class="card">
          <div class="card-header"><el-icon class="card-icon"><Present /></el-icon>个性化推荐</div>
          <div class="empty-hint">完成更多学习后，系统将为你精准推荐</div>
        </div>

        <div class="card">
          <div class="card-header"><el-icon class="card-icon"><Bell /></el-icon>系统公告</div>
          <div v-if="notices.length" class="todo-list">
            <div v-for="n in notices" :key="n.id" class="todo-item" style="cursor:pointer;" @click="showNotice(n)">
              <el-tag size="small" type="warning" round>公告</el-tag>
              <span style="color:#3670e8;">{{ n.title }}</span>
            </div>
          </div>
          <div v-else class="empty-hint">暂无公告</div>
        </div>
      </div>

      <div class="side-col">
        <div class="card">
          <div class="card-header"><el-icon class="card-icon"><DataAnalysis /></el-icon>学情概览</div>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-circle blue">{{ stats.studyDays }}</div>
              <span>学习天数</span>
            </div>
            <div class="stat-item">
              <div class="stat-circle green">{{ stats.totalMinutes }}</div>
              <span>学习分钟</span>
            </div>
            <div class="stat-item">
              <div class="stat-circle purple">{{ stats.masteredNodes }}</div>
              <span>已掌握</span>
            </div>
            <div class="stat-item">
              <div class="stat-circle orange">{{ stats.correctRate }}%</div>
              <span>正确率</span>
            </div>
          </div>
        </div>

        <div class="card" v-if="weakNodes.length">
          <div class="card-header"><el-icon class="card-icon"><Present /></el-icon>薄弱知识点</div>
          <div class="weak-list">
            <div v-for="(item, idx) in weakNodes.slice(0, 3)" :key="idx" class="weak-item">
              <span class="weak-rank">{{ idx + 1 }}</span>
              <span class="weak-name">{{ item.nodeName }}</span>
              <el-progress :percentage="item.correctRate || 0" :stroke-width="4" :show-text="false" />
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header"><el-icon class="card-icon"><Compass /></el-icon>快捷入口</div>
          <div class="quick-links">
            <div v-for="link in quickLinks" :key="link.key" class="quick-item" @click="$router.push(link.path)">
              <span class="quick-icon"><el-icon :size="20"><component :is="link.icon" /></el-icon></span>
              <span class="quick-label">{{ link.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="noticeDialog" :title="currentNotice?.title || '公告详情'" width="560px">
      <div style="font-size:14px;line-height:1.8;color:#333;white-space:pre-wrap;">{{ currentNotice?.content }}</div>
      <template #footer>
        <el-button @click="noticeDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ---- 新手引导弹窗 ---- -->
    <el-dialog v-model="guideDialogVisible" title="欢迎使用智能导学系统" width="600px" :close-on-click-modal="false" :show-close="false">
      <div class="guide-body">
        <div class="guide-item">
          <div class="guide-icon">1</div>
          <div class="guide-text">
            <strong>知识图谱</strong><br/>
            以可视化图谱呈现知识点之间的关联，帮助你构建系统化的知识体系。
          </div>
        </div>
        <div class="guide-item">
          <div class="guide-icon">2</div>
          <div class="guide-text">
            <strong>学习路径</strong><br/>
            系统根据你的学习目标，自动生成个性化的学习路径，按顺序学习更高效。
          </div>
        </div>
        <div class="guide-item">
          <div class="guide-icon">3</div>
          <div class="guide-text">
            <strong>配套练习</strong><br/>
            每个知识点都配有练习题目，学完即练，巩固所学内容。
          </div>
        </div>
        <div class="guide-item">
          <div class="guide-icon">4</div>
          <div class="guide-text">
            <strong>AI 助手</strong><br/>
            AI 划重点、错题讲解、智能答疑，让学习更轻松高效。
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" size="large" style="width:100%;" @click="dismissGuide">我知道了，开始学习</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Clock, Bell, Present, DataAnalysis, Compass,
  Grid, Connection, MapLocation, Notebook
} from '@element-plus/icons-vue'
import { getStudentDashboard, getWeakAnalysis } from '../api/student'
import { getNoticeList } from '../api/admin'

const router = useRouter()
const studentName = ref('同学')
const notices = ref([])
const noticeDialog = ref(false)
const currentNotice = ref(null)
const activePaths = ref([])
const todos = ref([])
const stats = ref({ studyDays: 0, totalMinutes: 0, masteredNodes: 0, correctRate: 0 })
const weakNodes = ref([])

// ── 新手引导 ──
const guideDialogVisible = ref(false)

const dismissGuide = () => {
  localStorage.setItem('guide_done', 'true')
  guideDialogVisible.value = false
}

const quickLinks = [
  { key: 'knowledge', label: '知识图谱', icon: Grid, path: '/student/knowledge' },
  { key: 'themes', label: '跨学科主题', icon: Connection, path: '/student/themes' },
  { key: 'paths', label: '学习路径', icon: MapLocation, path: '/student/path' },
  { key: 'exams', label: '测评中心', icon: Grid, path: '/student/exams' },
  { key: 'wrong', label: '错题本', icon: Notebook, path: '/student/wrong' },
]

// ── 时间感知问候 ──
const tips = [
  '每一步积累，都在靠近目标',
  '今天的学习，是明天的底气',
  '保持好奇心，知识自会生长',
  '学得越深，世界越大',
  '别怕慢，怕的是停下来',
  '温故知新，可以为师矣',
]
const timeGreeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})
const timePeriod = computed(() => {
  const h = new Date().getHours()
  if (h >= 6 && h < 18) return 'day'
  return 'night'
})
const motivationTip = ref(tips[Math.floor(Math.random() * tips.length)])

onMounted(async () => {
  motivationTip.value = tips[Math.floor(Math.random() * tips.length)]

  // 新手引导：仅首次登录时显示
  if (!localStorage.getItem('guide_done')) {
    guideDialogVisible.value = true
  }

  try {
    const res = await getStudentDashboard()
    if (res) {
      studentName.value = res.realName || '同学'
      activePaths.value = res.activePaths || []
      todos.value = res.todos || []
      stats.value = res.stats || stats.value
    }
  } catch { }

  try {
    const noticeRes = await getNoticeList()
    if (noticeRes && Array.isArray(noticeRes)) notices.value = noticeRes
  } catch { }

  try {
    const weakRes = await getWeakAnalysis()
    if (weakRes && weakRes.weakNodes) weakNodes.value = weakRes.weakNodes
  } catch { }
})

const showNotice = (n) => { currentNotice.value = n; noticeDialog.value = true }


</script>

<style scoped>
.student-home { min-height: 100vh; background: var(--bg-root); }

/* ── 顶栏 ── */
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 36px;
  background: var(--bg-grad);
  color: var(--text-primary);
}
.user-info { display: flex; align-items: center; gap: 16px; }
.user-info h2 { margin: 0; font-size: 20px; font-weight: 600; }
.user-info p { margin: 2px 0 0; font-size: 13px; color: #8a847e; }

/* ── 时间问候小组件 ── */
.greeting-widget {
  width: 48px; height: 48px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  position: relative;
  flex-shrink: 0;
}
.greeting-widget.day { background: linear-gradient(135deg, #ffe0b2, #ffcc80); }
.greeting-widget.night { background: linear-gradient(135deg, #37474f, #263238); }
.celestial { position: relative; width: 28px; height: 28px; }
.sun-moon {
  width: 18px; height: 18px; border-radius: 50%;
  position: absolute; top: 5px; left: 5px;
  transition: background 0.6s ease, box-shadow 0.6s ease;
}
.day .sun-moon {
  background: #ff9800;
  box-shadow: 0 0 8px rgba(255,152,0,0.6), 0 0 16px rgba(255,152,0,0.3);
}
.night .sun-moon {
  background: #e0e0e0;
  box-shadow: 0 0 6px rgba(224,224,224,0.4);
  clip-path: circle(65% at 35% 35%);
}
.orbit-ring {
  width: 26px; height: 26px; border-radius: 50%;
  border: 1.5px dashed rgba(255,255,255,0.35);
  position: absolute; top: 1px; left: 1px;
  animation: orbit 8s linear infinite;
}
@keyframes orbit { to { transform: rotate(360deg); } }

/* ── 卡片 ── */
.dashboard { display: flex; gap: 24px; padding: 24px 36px; }
.main-col { flex: 2; display: flex; flex-direction: column; gap: 20px; }
.side-col { flex: 1; display: flex; flex-direction: column; gap: 20px; }

.card {
  background: var(--bg-surface); border-radius: 16px; padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: box-shadow 0.2s ease;
}
.card:hover { box-shadow: 0 4px 20px rgba(0,0,0,0.06); }
.card-header {
  font-size: 16px; font-weight: 600; color: var(--text-primary);
  margin-bottom: 16px; display: flex; align-items: center; gap: 8px;
}
.card-icon { color: var(--accent); font-size: 16px; flex-shrink: 0; }
.empty-hint { color: var(--text-muted); font-size: 14px; text-align: center; padding: 20px 0; }

/* ── 路径列表 ── */
.path-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; }
.path-item:not(:last-child) { border-bottom: 1px solid var(--border-subtle); }
.path-info { flex: 1; }
.path-title { font-size: 14px; color: var(--text-primary); margin-bottom: 6px; font-weight: 500; display: block; }

/* ── 待办 ── */
.todo-item { display: flex; align-items: center; gap: 10px; padding: 8px 0; font-size: 14px; color: var(--text-secondary); }

/* ── 学情统计 ── */
.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.stat-item { text-align: center; }
.stat-circle {
  width: 56px; height: 56px; border-radius: 50%; margin: 0 auto 8px;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; font-weight: 700; color: #fff;
}
.stat-circle.blue { background: linear-gradient(135deg, var(--accent), #e06830); }
.stat-circle.green { background: linear-gradient(135deg, var(--accent-green), var(--accent-green)); }
.stat-circle.purple { background: linear-gradient(135deg, #d4a853, #b89030); }
.stat-circle.orange { background: linear-gradient(135deg, #f5a623, #d4881a); }
.stat-item span { font-size: 12px; color: var(--text-muted); }

/* ── 快捷入口 ── */
.quick-links { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.weak-list { display: flex; flex-direction: column; gap: 10px; }
.weak-item { display: flex; align-items: center; gap: 8px; }
.weak-rank { width: 20px; height: 20px; border-radius: 50%; background: var(--danger); color: #fff; font-size: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.weak-name { flex: 1; font-size: 13px; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.quick-item {
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  padding: 18px 8px; border-radius: 14px;
  background: var(--bg-input); cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}
.quick-item:hover { background: var(--bg-hover); transform: translateY(-3px); }
.quick-icon {
  width: 40px; height: 40px; border-radius: 12px;
  background: var(--bg-surface); display: flex; align-items: center; justify-content: center;
  color: var(--accent); box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.quick-label { font-size: 13px; font-weight: 500; color: var(--text-secondary); }

/* ── 新手引导 ── */
.guide-body { display: flex; flex-direction: column; gap: 20px; padding: 8px 0; }
.guide-item { display: flex; align-items: flex-start; gap: 14px; }
.guide-icon {
  width: 32px; height: 32px; border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), var(--accent-hover));
  color: #fff; font-size: 16px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; margin-top: 2px;
}
.guide-text { font-size: 14px; line-height: 1.7; color: #555; }
.guide-text strong { color: var(--text-primary); }

/* ── 响应式 ── */
@media (max-width: 900px) {
  .dashboard { flex-direction: column; }
  .user-info h2 { font-size: 17px; }
}
</style>
