<template>
  <div class="node-study-page">
    <StudentHeader :title="'知识点学习 - ' + (node?.nodeName || node?.name || '加载中...')" subtitle="仔细阅读内容，认真完成练习">
      <template #actions>
        <el-button type="success" round :loading="marking" :disabled="nodeStatus === 'completed'" @click="markComplete">
          {{ nodeStatus === 'completed' ? '已标记完成' : '标记完成' }}
        </el-button>
      </template>
    </StudentHeader>

    <div v-loading="loading" class="study-body">
      <template v-if="node">
        <el-card class="meta-card" shadow="never">
          <div class="meta-row">
            <span class="meta-label">所属课程：</span>
            <span class="meta-value">{{ node.courseName || '未分类' }}</span>
            <el-tag :type="difficultyTagType(node.difficulty)" size="small" style="margin-left: 12px">
              {{ difficultyLabel(node.difficulty) }}
            </el-tag>
          </div>
        </el-card>

        <el-tabs v-model="activeTab" class="study-tabs">
          <el-tab-pane label="学习内容" name="content">
            <el-card shadow="never" class="content-card">
              <div class="content-body">
                <h3>{{ node.nodeName || node.name }}</h3>
                <div class="desc-section" v-html="formattedContent"></div>
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
        </el-tabs>
      </template>
      <el-empty v-else description="知识点不存在" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import StudentHeader from '../../components/StudentHeader.vue'
import { getNodeById } from '../../api/knowledge'
import { updateStudyRecord } from '../../api/student'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const marking = ref(false)
const node = ref(null)
const nodeStatus = ref('')
const activeTab = ref('content')

const hasExercises = computed(() => {
  return node.value?.exercises && node.value.exercises.length > 0
})

const formattedContent = computed(() => {
  const content = node.value?.content || node.value?.description || node.value?.desc || '暂无学习内容'
  // 保留换行转为 <br>，同时允许后端返回 HTML
  if (/<[^>]+>/.test(content)) {
    return content
  }
  return content.replace(/\n/g, '<br>')
})

const difficultyLabel = (level) => {
  const map = { 1: '入门', 2: '基础', 3: '进阶', 4: '困难', 5: '挑战' }
  return map[level] || '未知'
}

const difficultyTagType = (level) => {
  const map = { 1: 'success', 2: '', 3: 'warning', 4: 'danger', 5: 'danger' }
  return map[level] || 'info'
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
  router.push('/student/practice' + query)
}

const markComplete = async () => {
  marking.value = true
  try {
    const nodeId = node.value?.nodeId || node.value?.id
    await updateStudyRecord({ nodeId, status: 'completed' })
    nodeStatus.value = 'completed'
    ElMessage.success('已标记为完成')
  } catch {
    ElMessage.error('标记失败，请重试')
  } finally {
    marking.value = false
  }
}

const fetchNode = async () => {
  const nodeId = route.params.nodeId
  if (!nodeId) return

  loading.value = true
  try {
    const res = await getNodeById(nodeId)
    node.value = res
    nodeStatus.value = res.studyStatus || res.status || ''
  } catch {
    ElMessage.error('加载知识点失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchNode)
</script>

<style scoped>
.node-study-page {
  min-height: 100vh;
  background: #faf7f2;
  
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
  color: #a09a92;
}

.meta-value {
  font-size: 14px;
  color: #2d2a26;
  font-weight: 500;
}

.study-tabs {
  border-radius: 12px;
}

.study-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
  background: #fffdf9;
  border-radius: 12px 12px 0 0;
  padding: 0 20px;
}

.content-card,
.practice-card {
  border-radius: 0 0 12px 12px;
  min-height: 300px;
}

.content-body h3 {
  margin: 0 0 16px;
  font-size: 20px;
  color: #2d2a26;
}

.desc-section {
  font-size: 15px;
  color: #6b655e;
  line-height: 1.9;
}

.desc-section :deep(br) {
  display: block;
  content: '';
  margin-bottom: 8px;
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
  background: #faf7f2;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.exercise-item:hover {
  background: #f3efe8;
}
</style>
