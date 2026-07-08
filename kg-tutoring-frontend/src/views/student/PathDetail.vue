<template>
  <div class="path-detail-page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="router.push('/student/path')" :icon="ArrowLeft" circle size="small" />
        <h2 class="page-title">学习路径详情</h2>
        <el-button type="primary" size="small" @click="exportMindMap" :loading="exportLoading">
          <el-icon style="margin-right:4px"><Download /></el-icon>导出思维导图
        </el-button>
      </div>
    </div>

    <div v-loading="loading">
      <template v-if="detail">
        <!-- 路径信息 -->
        <el-card class="info-card" shadow="never">
          <div class="info-row">
            <span class="info-label">路径名称：</span>
            <span class="info-value">{{ detail.pathName || detail.name }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">学习目标：</span>
            <span class="info-value">{{ detail.target || detail.goal || '-' }}</span>
          </div>
          <div class="progress-section">
            <span class="info-label">整体进度：</span>
            <el-progress
              :percentage="displayProgress"
              :stroke-width="14"
              :status="displayProgress === 100 ? 'success' : undefined"
              style="flex: 1"
            />
            <span class="progress-text">{{ displayProgress }}%</span>
          </div>
        </el-card>

        <!-- 节点时间线 -->
        <el-card class="timeline-card" shadow="never">
          <template #header>
            <span class="card-title">学习节点</span>
          </template>
          <el-empty v-if="!nodes.length" description="暂无学习节点" :image-size="60" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="(node, index) in computedNodes"
              :key="node.id || index"
              :timestamp="node.completedTime ? formatTime(node.completedTime) : ''"
              placement="top"
              :color="timelineColor(node.displayStatus)"
              :hollow="node.displayStatus !== 'completed' && node.displayStatus !== 'learning'"
            >
              <div
                class="timeline-node"
                :class="{
                  clickable: node.displayStatus === 'learning',
                  'node-locked': node.displayStatus === 'locked'
                }"
                @click="goStudy(node)"
              >
                <div class="node-title-row">
                  <el-icon :size="18" class="status-icon" :class="'icon-' + node.displayStatus">
                    <CircleCheck v-if="node.displayStatus === 'completed'" style="color:#67c23a" />
                    <Loading v-else-if="node.displayStatus === 'learning'" style="color:#ff7b3d" />
                    <Lock v-else style="color:var(--text-muted)" />
                  </el-icon>
                  <span class="node-title" :class="{ 'text-locked': node.displayStatus === 'locked', 'text-completed': node.displayStatus === 'completed' }">{{ node.nodeName || node.name || '未命名节点' }}</span>
                  <el-tag :type="difficultyTagType(node.difficulty)" size="small">
                    {{ difficultyLabel(node.difficulty) }}
                  </el-tag>
                </div>
                <p class="node-desc">{{ truncate(node.description || node.desc || '', 60) }}</p>
                <span class="node-status-text" :class="'status-' + node.displayStatus">{{ statusLabel(node.displayStatus) }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </template>
      <el-empty v-else description="路径不存在或已删除" :image-size="80" />
    </div>

    <!-- 隐藏的思维导图容器 -->
    <div ref="mindMapRef" class="mindmap-container" style="position:fixed;left:-9999px;top:0;width:1800px;height:900px;"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, CircleCheck, Download, Loading, Lock } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import confetti from 'canvas-confetti'
import { getPathDetail } from '../../api/student'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const detail = ref(null)
const nodes = ref([])

// 根据 sortOrder 和完成状态推导每个节点的实际状态
const computedNodes = computed(() => {
  const sorted = [...nodes.value].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  const result = []
  for (let i = 0; i < sorted.length; i++) {
    const node = { ...sorted[i] }
    if (node.status === 'completed') {
      node.displayStatus = 'completed'
    } else if (i === 0) {
      // 第一个未完成的节点 -> 学习中
      node.displayStatus = 'learning'
    } else if (result[i - 1].displayStatus === 'completed') {
      // 前置节点已完成 -> 学习中
      node.displayStatus = 'learning'
    } else {
      // 前置节点未完成 -> 待解锁
      node.displayStatus = 'locked'
    }
    result.push(node)
  }
  return result
})

const progressPercent = computed(() => {
  if (!nodes.value.length) return 0
  const done = nodes.value.filter((n) => n.status === 'completed').length
  return formatProgress((done / nodes.value.length) * 100)
})

const displayProgress = computed(() => {
  return formatProgress(detail.value?.progress ?? progressPercent.value)
})

const formatProgress = (progress) => {
  const value = Number(progress || 0)
  return Math.max(0, Math.min(100, Math.round(value)))
}

const difficultyLabel = (level) => {
  const map = { 1: '入门', 2: '基础', 3: '进阶', 4: '困难', 5: '挑战' }
  return map[level] || '未知'
}

const difficultyTagType = (level) => {
  const map = { 1: 'success', 2: 'info', 3: 'warning', 4: 'danger', 5: 'danger' }
  return map[level] || 'info'
}

const statusLabel = (displayStatus) => {
  const map = { completed: '已完成', learning: '学习中', locked: '待解锁' }
  return map[displayStatus] || '待解锁'
}

const timelineColor = (displayStatus) => {
  const map = { completed: '#67c23a', learning: '#ff7b3d', locked: 'var(--text-muted)' }
  return map[displayStatus] || 'var(--text-muted)'
}

const truncate = (str, max) => {
  if (!str) return ''
  return str.length > max ? str.slice(0, max) + '...' : str
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const goStudy = (node) => {
  if (node.displayStatus === 'learning') {
    let url = '/student/study/' + (node.nodeId || node.id)
    if (node.detailId) url += '?detailId=' + node.detailId
    router.push(url)
  }
}

// ── 撒花动画 ──
const smallCelebrate = () => {
  confetti({ particleCount: 50, spread: 50, origin: { x: 0.5, y: 0.6 }, colors: ['#ff7b3d','#f5a623','#5eaf83','#d4a853'], disableForReducedMotion: true })
}
const bigCelebrate = () => {
  const duration = 2500
  const end = Date.now() + duration
  const colors = ['#ff7b3d','#f5a623','#5eaf83','#d4a853','#ff6b6b','#64b5f6']
  ;(function frame() {
    confetti({ particleCount: 4, angle: 60, spread: 70, origin: { x: 0, y: 0.6 }, colors, disableForReducedMotion: true })
    confetti({ particleCount: 4, angle: 120, spread: 70, origin: { x: 1, y: 0.6 }, colors, disableForReducedMotion: true })
    if (Date.now() < end) requestAnimationFrame(frame)
  })()
  setTimeout(() => confetti({ particleCount: 150, spread: 120, origin: { x: 0.5, y: 0.5 }, colors, disableForReducedMotion: true }), 500)
}

// 进度变化时触发庆祝
const lastSeenKey = computed(() => `path_progress_${route.params.id}`)
watch(displayProgress, (newVal, oldVal) => {
  if (newVal === oldVal || newVal === 0) return
  const key = lastSeenKey.value
  const prev = Number(localStorage.getItem(key) || 0)
  if (newVal > prev && newVal < 100) {
    setTimeout(() => smallCelebrate(), 300)
  }
  if (newVal >= 100 && prev < 100) {
    setTimeout(() => bigCelebrate(), 500)
  }
  localStorage.setItem(key, String(newVal))
})

const fetchDetail = async () => {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    const res = await getPathDetail(id)
    detail.value = res
    nodes.value = res.nodes || res.nodeList || []
  } catch {
    ElMessage.error('加载路径详情失败')
  } finally {
    loading.value = false
  }
}

// ---- 思维导图导出 ----
const mindMapRef = ref(null)
const exportLoading = ref(false)

const exportMindMap = async () => {
  if (exportLoading.value || !nodes.value.length) return
  exportLoading.value = true
  await nextTick()

  // 构建树形数据（按排序顺序作为层级）
  const nodeList = computedNodes.value
  const treeData = {
    name: detail.value?.pathName || '学习路径',
    children: nodeList.map((n, i) => ({
      name: n.nodeName || n.name || `节点${i + 1}`,
      itemStyle: {
        color: n.displayStatus === 'completed' ? '#67c23a'
             : n.displayStatus === 'learning' ? '#ff7b3d'
             : '#bbb'
      }
    }))
  }

  const chart = echarts.init(mindMapRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}' },
    series: [{
      type: 'tree',
      data: [treeData],
      top: '3%',
      left: '18%',
      bottom: '3%',
      right: '22%',
      symbolSize: 10,
      label: {
        position: 'left',
        verticalAlign: 'middle',
        align: 'right',
        fontSize: 14,
        color: '#333'
      },
      leaves: {
        label: { position: 'right', verticalAlign: 'middle', align: 'left' }
      },
      edgeShape: 'polyline',
      expandAndCollapse: false,
      animationDuration: 300,
      lineStyle: { color: '#ccc', width: 1.5, curveness: 0 }
    }]
  })

  // 导出为图片下载
  setTimeout(() => {
    try {
      const url = chart.getDataURL({ type: 'png', pixelRatio: 2, backgroundColor: '#fff' })
      const a = document.createElement('a')
      a.href = url
      a.download = (detail.value?.pathName || '学习路径') + '_思维导图.png'
      a.click()
      ElMessage.success('思维导图已导出')
    } catch {
      ElMessage.error('导出失败')
    } finally {
      chart.dispose()
      exportLoading.value = false
    }
  }, 500)
}

onMounted(fetchDetail)
</script>

<style scoped>
.path-detail-page {
  min-height: 100vh;
  background: var(--bg-root);
  padding: 24px 32px;
}

.page-header {
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.info-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.info-label {
  font-size: 14px;
  color: var(--text-muted);
  min-width: 90px;
}

.info-value {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 500;
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-text {
  font-size: 14px;
  color: var(--accent);
  font-weight: 600;
  min-width: 40px;
}

.timeline-card {
  border-radius: 12px;
}

.card-title {
  font-weight: 600;
  color: var(--text-primary);
}

.timeline-node {
  padding: 10px 14px;
  border-radius: 10px;
  background: var(--bg-input);
  transition: background 0.2s ease, transform 0.2s ease;
}

.timeline-node.clickable {
  cursor: pointer;
  background: var(--bg-hover);
  border: 1px solid rgba(255,123,61,0.08);
}

.timeline-node.clickable:hover {
  background: rgba(255,123,61,0.08);
  transform: translateX(4px);
}

.timeline-node.node-locked {
  opacity: 0.55;
  cursor: not-allowed;
  background: #f5f5f0;
}

.node-title.text-locked {
  color: var(--text-muted);
}

.node-title.text-completed {
  color: var(--success);
}

.node-status-text.status-completed {
  color: var(--success);
  font-weight: 500;
}

.node-status-text.status-locked {
  color: var(--text-muted);
}

.node-status-text.status-learning {
  color: var(--accent);
  font-weight: 500;
}

.node-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.status-icon {
  flex-shrink: 0;
}

.node-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
}

.node-desc {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0 0 6px;
}

.node-status-text {
  font-size: 12px;
  color: var(--text-muted);
}
</style>
