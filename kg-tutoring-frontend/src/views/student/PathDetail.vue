<template>
  <div class="path-detail-page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="router.push('/student/path')" :icon="ArrowLeft" circle size="small" />
        <h2 class="page-title">学习路径详情</h2>
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
              :percentage="detail.progress || progressPercent"
              :stroke-width="14"
              :status="(detail.progress || progressPercent) === 100 ? 'success' : undefined"
              style="flex: 1"
            />
            <span class="progress-text">{{ detail.progress || progressPercent }}%</span>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, CircleCheck, Loading, Lock } from '@element-plus/icons-vue'
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
  return Math.round((done / nodes.value.length) * 100)
})

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
    router.push('/student/study/' + (node.nodeId || node.id))
  }
}

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
