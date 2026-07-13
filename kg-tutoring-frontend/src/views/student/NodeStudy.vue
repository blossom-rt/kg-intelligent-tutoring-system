<template>
  <div class="node-study-page">
    <StudentHeader :title="'知识点学习 - ' + (node?.nodeName || node?.name || '加载中...')" subtitle="仔细阅读内容，认真完成练习">
      <template #actions>
        <el-button v-if="node" :type="isFav ? 'warning' : 'default'" :icon="favIcon" circle size="small" @click="toggleFavorite" />
        <el-tag v-if="nodeStatus === 'completed'" type="success" size="large" effect="dark" round>已掌握 ✓</el-tag>
        <el-tag v-else type="info" size="large" effect="plain" round>待练习</el-tag>
      </template>
    </StudentHeader>

    <div v-loading="loading" class="study-body">
      <template v-if="node">
        <el-card class="meta-card" shadow="never">
          <div class="meta-row">
            <span class="meta-label">所属课程：</span>
            <span class="meta-value">{{ node.courseName || '未分类' }}</span>
            <el-tag size="small" effect="plain" style="margin-left: 12px">
              {{ nodeTypeLabel(node.nodeType) }}
            </el-tag>
            <el-tag :type="difficultyTagType(node.difficulty)" size="small" style="margin-left: 12px">
              {{ difficultyLabel(node.difficulty) }}
            </el-tag>
          </div>
        </el-card>

        <el-tabs v-model="activeTab" class="study-tabs">
          <el-tab-pane label="学习内容" name="content">
            <el-card shadow="never" class="content-card">
              <div class="content-body">
                <div class="content-header-row">
                  <h3>{{ node.nodeName || node.name }}</h3>
                  <el-button type="warning" plain size="small" :loading="aiSummaryLoading" @click="showAiSummary">
                    <el-icon style="margin-right:4px"><Promotion /></el-icon>AI 划重点
                  </el-button>
                </div>
                <div v-if="node.learningGoal || node.keywords || node.exampleHint" class="guide-block">
                  <div v-if="node.learningGoal" class="guide-row">
                    <span>学习目标</span>
                    <p>{{ node.learningGoal }}</p>
                  </div>
                  <div v-if="node.keywords" class="guide-row">
                    <span>关键词</span>
                    <p>{{ node.keywords }}</p>
                  </div>
                  <div v-if="node.exampleHint" class="guide-row">
                    <span>例题提示</span>
                    <p>{{ node.exampleHint }}</p>
                  </div>
                </div>
                <div class="desc-section markdown-body" v-html="formattedContent"></div>
              </div>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="视频资源" name="resources">
            <el-card shadow="never" class="resource-card">
              <el-empty v-if="!resources.length" description="暂无视频资源" :image-size="80">
                <template #default>
                  <p style="color: #a09a92; font-size: 14px">老师还没有为该知识点配置配套视频</p>
                </template>
              </el-empty>
              <div v-else class="resource-list">
                <div v-for="resource in resources" :key="resource.id" class="resource-item">
                  <div v-if="isDirectVideo(resource.url)" class="video-frame">
                    <video :src="resource.url" controls preload="metadata"></video>
                  </div>
                  <div v-else class="video-cover" :style="coverStyle(resource)">
                    <span>{{ resource.source || 'video' }}</span>
                  </div>
                  <div class="resource-info">
                    <div class="resource-title-row">
                      <h4>{{ resource.title }}</h4>
                      <el-tag size="small" effect="plain">{{ resourceLabel(resource.resourceType) }}</el-tag>
                    </div>
                    <p v-if="resource.durationSeconds" class="resource-meta">
                      时长：{{ formatDuration(resource.durationSeconds) }}
                    </p>
                    <p class="resource-url">{{ resource.url }}</p>
                    <el-button type="primary" plain size="small" @click="openResource(resource.url)">
                      打开资源
                    </el-button>
                  </div>
                </div>
              </div>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="配套练习" name="practice">
            <el-card shadow="never" class="practice-card">
              <el-empty v-if="!hasExercises" description="暂无配套练习" :image-size="80">
                <template #default>
                  <p style="color: #a09a92; font-size: 14px">该知识点暂无预设练习，您仍可以去刷题中心进行练习</p>
                </template>
              </el-empty>
              <div v-else class="exercise-list">
                <div
                  v-for="ex in node.exercises"
                  :key="ex.id"
                  class="exercise-item"
                  @click="goPractice(ex.id)"
                >
                  <span>{{ ex.title || ex.name || '练习题' }}</span>
                  <el-tag
                    :type="ex.difficulty ? difficultyTagType(ex.difficulty) : 'info'"
                    size="small"
                  >
                    {{ ex.difficulty ? difficultyLabel(ex.difficulty) : '练习' }}
                  </el-tag>
                </div>
              </div>
              <div style="margin-top: 20px; text-align: center">
                <el-button type="primary" size="large" @click="goPractice()">
                  开始练习
                </el-button>
              </div>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="AI 答疑" name="aiQA">
            <el-card shadow="never" class="qa-card">
              <div class="qa-history" v-if="qaMessages.length">
                <div
                  v-for="(msg, idx) in qaMessages"
                  :key="idx"
                  class="qa-message"
                  :class="msg.role === 'user' ? 'qa-user' : 'qa-assistant'"
                >
                  <div class="qa-avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
                  <div
                    v-if="msg.role === 'assistant'"
                    class="qa-bubble markdown-body"
                    v-html="renderAiMarkdown(msg.content)"
                  ></div>
                  <div v-else class="qa-bubble">{{ msg.content }}</div>
                </div>
                <div v-if="qaLoading" class="qa-message qa-assistant">
                  <div class="qa-avatar">AI</div>
                  <div class="qa-bubble qa-thinking">{{ loadingTip || '思考中...' }}</div>
                </div>
              </div>
              <el-empty v-else description="向 AI 老师提问，获得即时解答" :image-size="60" />
              <div class="qa-input-row">
                <el-input
                  v-model="qaInput"
                  placeholder="输入你的问题..."
                  size="large"
                  :disabled="qaLoading"
                  @keyup.enter="sendQuestion"
                >
                  <template #append>
                    <el-button :loading="qaLoading" @click="sendQuestion">提问</el-button>
                  </template>
                </el-input>
              </div>
            </el-card>
          </el-tab-pane>
        </el-tabs>

        <!-- 前置知识快速回顾 -->
        <el-card v-if="prerequisiteNodes.length" class="prereq-card" shadow="never">
          <template #header><span class="card-title">前置知识</span></template>
          <div class="prereq-list">
            <div v-for="p in prerequisiteNodes" :key="p.id" class="prereq-item" @click="goStudyNode(p)">
              <el-tag :type="difficultyTagType(p.difficulty)" size="small" effect="dark">{{ difficultyLabel(p.difficulty) }}</el-tag>
              <span class="prereq-name">{{ p.name }}</span>
              <el-icon color="var(--text-muted)"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>
      </template>
      <el-empty v-else description="知识点不存在" :image-size="80" />
    </div>

    <!-- AI 划重点弹窗 -->
    <el-dialog v-model="aiSummaryVisible" :title="aiSummaryTitle" width="680px">
      <div style="min-height:100px;">
        <div v-if="aiSummaryLoading" class="loading-tip">
          <el-icon class="is-loading" :size="28" style="margin-bottom:16px"><Loading /></el-icon>
          <div>{{ loadingTip }}</div>
        </div>
        <div v-if="aiSummaryContent" class="ai-summary-body markdown-body" v-html="formattedAiSummary"></div>
      </div>
      <template #footer>
        <el-button @click="aiSummaryVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Promotion, ArrowRight, Star, StarFilled, Loading } from '@element-plus/icons-vue'
import StudentHeader from '../../components/StudentHeader.vue'
import { getNodeById, getNodeResources } from '../../api/knowledge'
import { getStudyRecords, aiNodeSummary, aiChat, addFavorite, deleteFavorite, getFavoriteList, getPrerequisiteNodes } from '../../api/student'
import { renderMarkdown } from '../../utils/markdown'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const node = ref(null)
const resources = ref([])
const nodeStatus = ref('')
const activeTab = ref('content')

const hasExercises = computed(() => {
  return node.value?.exercises && node.value.exercises.length > 0
})

// 收藏夹
const isFav = ref(false)
const favId = ref(null)
const favIcon = computed(() => isFav.value ? StarFilled : Star)

const toggleFavorite = async () => {
  const nodeId = node.value?.nodeId || node.value?.id
  if (!nodeId) return
  try {
    if (isFav.value) {
      await deleteFavorite(favId.value)
      isFav.value = false
      favId.value = null
      ElMessage.success('已取消收藏')
    } else {
      const res = await addFavorite({ nodeId })
      isFav.value = true
      favId.value = res?.id
      ElMessage.success('已收藏')
    }
  } catch (e) {
    ElMessage.warning(e?.response?.data?.message || '操作失败')
  }
}

const fetchFavStatus = async () => {
  const nodeId = node.value?.nodeId || node.value?.id
  if (!nodeId) return
  try {
    const res = await getFavoriteList({ silent: true })
    const all = Array.isArray(res) ? res : []
    const found = all.find(f => f.nodeId === Number(nodeId))
    isFav.value = !!found
    if (found) favId.value = found.id
  } catch { }
}

// 前置知识
const prerequisiteNodes = ref([])

const fetchPrerequisites = async () => {
  const nodeId = node.value?.nodeId || node.value?.id
  if (!nodeId) return
  try {
    const res = await getPrerequisiteNodes(nodeId, { silent: true })
    prerequisiteNodes.value = Array.isArray(res) ? res : []
  } catch { prerequisiteNodes.value = [] }
}

const goStudyNode = (p) => {
  router.push('/student/study/' + p.id)
}

// AI 划重点
const aiSummaryVisible = ref(false)
const aiSummaryLoading = ref(false)
const aiSummaryTitle = ref('')
const aiSummaryContent = ref('')
const formattedAiSummary = computed(() => renderMarkdown(aiSummaryContent.value))
const renderAiMarkdown = (content) => renderMarkdown(content)
const loadingTip = ref('')

const summaryTips = [
  '正在分析知识点结构...',
  '梳理核心概念中...',
  '提炼学习要点...',
  '整理重点难点...',
  '生成学习建议...',
  '马上就好，再等一下...'
]

const chatTips = [
  '正在思考你的问题...',
  '查阅相关资料中...',
  '组织语言回答...',
  '快要完成了...',
  '马上就好...'
]

let tipTimer = null
const startTipRotation = (tips) => {
  loadingTip.value = tips[0]
  let i = 1
  tipTimer = setInterval(() => {
    loadingTip.value = tips[i % tips.length]
    i++
  }, 2500)
}
const stopTipRotation = () => {
  if (tipTimer) { clearInterval(tipTimer); tipTimer = null }
}

const showAiSummary = async () => {
  const nodeId = node.value?.nodeId || node.value?.id
  if (!nodeId) {
    ElMessage.warning('节点信息不完整')
    return
  }
  aiSummaryLoading.value = true
  aiSummaryVisible.value = true
  aiSummaryTitle.value = 'AI 正在生成总结...'
  aiSummaryContent.value = ''
  startTipRotation(summaryTips)
  try {
    const res = await aiNodeSummary({ nodeId })
    if (res) {
      aiSummaryTitle.value = res.title || 'AI 学习总结'
      aiSummaryContent.value = res.summary || ''
    }
  } catch {
    aiSummaryContent.value = 'AI 总结生成失败，请稍后重试'
    aiSummaryTitle.value = '生成失败'
  } finally {
    stopTipRotation()
    aiSummaryLoading.value = false
  }
}

// AI 答疑
const qaInput = ref('')
const qaLoading = ref(false)
const qaMessages = ref([])

const sendQuestion = async () => {
  const text = qaInput.value.trim()
  if (!text || qaLoading.value) return
  qaMessages.value.push({ role: 'user', content: text })
  qaInput.value = ''
  startTipRotation(chatTips)
  qaLoading.value = true
  const nodeId = node.value?.nodeId || node.value?.id
  try {
    const res = await aiChat({ nodeId, question: text })
    qaMessages.value.push({ role: 'assistant', content: res?.answer || '抱歉，AI 暂时无法回答，请稍后重试。' })
  } catch {
    qaMessages.value.push({ role: 'assistant', content: '请求失败，请检查网络后重试。' })
  } finally {
    stopTipRotation()
    qaLoading.value = false
  }
}

const formattedContent = computed(() => {
  const content = node.value?.content || node.value?.description || node.value?.desc || '暂无学习内容'
  return renderMarkdown(content)
})

const difficultyLabel = (level) => {
  const map = { 1: '入门', 2: '基础', 3: '进阶', 4: '困难', 5: '挑战' }
  return map[level] || '未知'
}

const difficultyTagType = (level) => {
  const map = { 1: 'success', 2: 'info', 3: 'warning', 4: 'danger', 5: 'danger' }
  return map[level] || 'info'
}

const nodeTypeLabel = (type) => {
  const map = { concept: '概念理解', skill: '方法技能', application: '应用实践' }
  return map[type] || '概念理解'
}

const resourceLabel = (type) => {
  const map = { video: '视频', article: '文章', pdf: '文档', link: '链接' }
  return map[type] || '资源'
}

const isDirectVideo = (url = '') => {
  return /\.(mp4|webm|ogg)(\?.*)?$/i.test(url)
}

const formatDuration = (seconds) => {
  const total = Number(seconds)
  if (!Number.isFinite(total) || total <= 0) return '-'
  const minutes = Math.floor(total / 60)
  const rest = total % 60
  return `${minutes}:${String(rest).padStart(2, '0')}`
}

const coverStyle = (resource) => {
  if (!resource.coverUrl) return {}
  return { backgroundImage: `url(${resource.coverUrl})` }
}

const openResource = (url) => {
  if (!url) return
  window.open(url, '_blank', 'noopener,noreferrer')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/student/knowledge')
  }
}

const goPractice = (questionId) => {
  const nodeId = node.value?.nodeId || node.value?.id
  let query = `?nodeId=${nodeId}`
  if (questionId) {
    query += `&questionId=${questionId}`
  }
  const detailId = route.query.detailId
  if (detailId) query += `&detailId=${detailId}`
  router.push('/student/practice' + query)
}


const fetchNode = async () => {
  const nodeId = route.params.nodeId
  if (!nodeId) return

  loading.value = true
  try {
    const res = await getNodeById(nodeId)
    node.value = res
    try {
      const resourceRes = await getNodeResources(nodeId, undefined, { silent: true })
      resources.value = Array.isArray(resourceRes) ? resourceRes : []
    } catch {
      resources.value = []
    }
    // 从学习记录中获取当前掌握状态
    try {
      const records = await getStudyRecords()
      const record = Array.isArray(records) ? records.find(r => r.nodeId === Number(nodeId)) : null
      if (record && record.masteryLevel >= 3) nodeStatus.value = 'completed'
    } catch {}
    fetchFavStatus()
    fetchPrerequisites()
  } catch {
    ElMessage.error('加载知识点失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchNode)

onBeforeUnmount(() => { stopTipRotation() })

// 路由参数变化时重新加载（前置知识跳转等）
watch(() => route.params.nodeId, () => {
  fetchNode()
})
</script>

<style scoped>
.node-study-page {
  min-height: 100vh;
  background: var(--bg-root);
  
}

.study-body {
  max-width: 900px;
  margin: 0 auto;
}

.meta-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.meta-row {
  display: flex;
  align-items: center;
}

.meta-label {
  font-size: 14px;
  color: var(--text-muted);
}

.meta-value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.study-tabs {
  border-radius: 12px;
}

.study-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
  background: var(--bg-surface);
  border-radius: 12px 12px 0 0;
  padding: 0 20px;
}

.content-card,
.practice-card,
.resource-card {
  border-radius: 0 0 12px 12px;
  min-height: 300px;
}

.content-body h3 {
  margin: 0 0 16px;
  font-size: 20px;
  color: var(--text-primary);
}

.desc-section {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.9;
}

.desc-section :deep(br) {
  display: block;
  content: '';
  margin-bottom: 8px;
}

.guide-block {
  display: grid;
  gap: 10px;
  margin: 0 0 18px;
  padding: 14px 16px;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  background: var(--bg-root);
}

.guide-row {
  display: grid;
  grid-template-columns: 76px 1fr;
  gap: 12px;
  align-items: start;
}

.guide-row span {
  color: var(--text-muted);
  font-size: 13px;
}

.guide-row p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.exercise-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.exercise-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--bg-root);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.exercise-item:hover {
  background: var(--bg-hover);
}

.resource-list {
  display: grid;
  gap: 16px;
}

.resource-item {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 18px;
  padding: 16px;
  border: 1px solid var(--border-light);
  border-radius: 10px;
  background: var(--bg-root);
}

.video-frame,
.video-cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  border-radius: 8px;
  background: #171717;
}

.video-frame video {
  width: 100%;
  height: 100%;
  display: block;
}

.video-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  background-size: cover;
  background-position: center;
}

.video-cover span {
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.48);
}

.resource-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.resource-title-row {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.resource-title-row h4 {
  margin: 0;
  color: var(--text-primary);
  font-size: 16px;
}

.resource-meta {
  margin: 0;
  color: var(--text-muted);
  font-size: 13px;
}

.resource-url {
  width: 100%;
  margin: 0 0 4px;
  color: var(--text-muted);
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ── 内容头部（标题 + AI 按钮） ── */
.content-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.content-header-row h3 {
  margin: 0;
  font-size: 20px;
  color: var(--text-primary);
}
.content-header-row .el-button {
  flex-shrink: 0;
}

/* ── AI 总结弹窗 ── */
.loading-tip {
  text-align: center; font-size: 16px; color: var(--text-primary);
  padding: 80px 0 60px; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
}
.ai-summary-body {
  font-size: 15px;
  line-height: 1.9;
  color: var(--text-secondary);
}

/* ── AI 答疑 ── */
.qa-card {
  min-height: 350px;
  display: flex;
  flex-direction: column;
}
.qa-history {
  flex: 1;
  max-height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-bottom: 16px;
}
.qa-message {
  display: flex;
  gap: 10px;
  max-width: 85%;
}
.qa-user {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.qa-assistant {
  align-self: flex-start;
}
.qa-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}
.qa-user .qa-avatar {
  background: var(--accent);
  color: #fff;
}
.qa-assistant .qa-avatar {
  background: var(--border-subtle);
  color: var(--text-secondary);
}
.qa-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-primary);
}
.qa-user .qa-bubble {
  background: var(--accent);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.qa-assistant .qa-bubble {
  background: var(--bg-hover);
  border-bottom-left-radius: 4px;
}
.markdown-body :deep(p) {
  margin: 0 0 8px;
}
.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  margin: 12px 0 8px;
  color: var(--text-primary);
  line-height: 1.35;
}
.markdown-body :deep(h1) { font-size: 20px; }
.markdown-body :deep(h2) { font-size: 18px; }
.markdown-body :deep(h3) { font-size: 16px; }
.markdown-body :deep(h4) { font-size: 15px; }
.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 6px 0 10px;
  padding-left: 20px;
}
.markdown-body :deep(li) {
  margin: 4px 0;
}
.markdown-body :deep(code) {
  padding: 2px 5px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.08);
  color: var(--text-primary);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 0.92em;
}
.markdown-body :deep(pre) {
  margin: 8px 0 10px;
  padding: 10px 12px;
  border-radius: 8px;
  overflow-x: auto;
  background: #1f2328;
}
.markdown-body :deep(pre code) {
  padding: 0;
  background: transparent;
  color: #f6f8fa;
}
.markdown-body :deep(blockquote) {
  margin: 8px 0;
  padding: 6px 10px;
  border-left: 3px solid var(--accent);
  color: var(--text-secondary);
  background: var(--bg-root);
}
.qa-user .markdown-body :deep(code),
.qa-user .markdown-body :deep(pre) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}
.qa-thinking {
  color: var(--text-muted);
  font-style: italic;
}
.qa-input-row {
  margin-top: 12px;
}
.qa-input-row :deep(.el-input-group__append) {
  background-color: var(--accent);
  border-color: var(--accent);
}
.qa-input-row :deep(.el-input-group__append .el-button) {
  color: #fff;
}

/* 前置知识卡片 */
.prereq-card { margin-top: 20px; border-radius: 12px; }
.prereq-list { display: flex; flex-direction: column; gap: 8px; }
.prereq-item {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 12px; border-radius: 8px;
  cursor: pointer; transition: background 0.2s;
}
.prereq-item:hover { background: var(--bg-hover); }
.prereq-name { flex: 1; font-size: 14px; color: var(--text-primary); }
.card-title { font-weight: 600; color: var(--text-primary); }

@media (max-width: 720px) {
  .resource-item {
    grid-template-columns: 1fr;
  }
}
</style>
