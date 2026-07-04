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
              v-for="(node, index) in nodes"
              :key="node.id || index"
              :timestamp="node.completedTime ? formatTime(node.completedTime) : ''"
              placement="top"
              :color="timelineColor(node.status)"
              :hollow="node.status !== 'completed' && node.status !== 'learning'"
            >
              <div
                class="timeline-node"
                :class="{ clickable: node.status === 'learning' || node.status === 'active' }"
                @click="goStudy(node)"
              >
                <div class="node-title-row">
                  <el-icon :size="18" class="status-icon">
                    <CircleCheck v-if="node.status === 'completed'" />
                    <Loading v-else-if="node.status === 'learning' || node.status === 'active'" />
                    <Lock v-else />
                  </el-icon>
                  <span class="node-title">{{ node.nodeName || node.name || '未命名节点' }}</span>
                  <el-tag :type="difficultyTagType(node.difficulty)" size="small">
                    {{ difficultyLabel(node.difficulty) }}
                  </el-tag>
                </div>
                <p class="node-desc">{{ truncate(node.description || node.desc || '', 60) }}</p>
                <span class="node-status-text">{{ statusLabel(node.status) }}</span>
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

const statusLabel = (status) => {
  const map = { completed: '已完成', learning: '学习中', active: '学习中', locked: '待解锁' }
  return map[status] || '待解锁'
}

const timelineColor = (status) => {
  const map = { completed: '#67c23a', learning: '#ff7b3d', active: '#ff7b3d', locked: '#c0c4cc' }
  return map[status] || '#c0c4cc'
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
  if (node.status === 'learning' || node.status === 'active') {
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
  background: #faf7f2;
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
  color: #2d2a26;
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
  color: #a09a92;
  min-width: 90px;
}

.info-value {
  font-size: 15px;
  color: #2d2a26;
  font-weight: 500;
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-text {
  font-size: 14px;
  color: #ff7b3d;
  font-weight: 600;
  min-width: 40px;
}

.timeline-card {
  border-radius: 12px;
}

.card-title {
  font-weight: 600;
  color: #2d2a26;
}

.timeline-node {
  padding: 10px 14px;
  border-radius: 10px;
  background: #f8f5f0;
  transition: background 0.2s ease, transform 0.2s ease;
}

.timeline-node.clickable {
  cursor: pointer;
  background: #f3efe8;
  border: 1px solid rgba(255,123,61,0.08);
}

.timeline-node.clickable:hover {
  background: rgba(255,123,61,0.08);
  transform: translateX(4px);
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
  color: #2d2a26;
  flex: 1;
}

.node-desc {
  font-size: 13px;
  color: #a09a92;
  margin: 0 0 6px;
}

.node-status-text {
  font-size: 12px;
  color: #bbb6ad;
}
</style>
