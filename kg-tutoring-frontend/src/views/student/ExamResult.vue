<template>
  <div class="exam-result-page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="router.push('/student/exams')" :icon="ArrowLeft" circle size="small" />
        <h2 class="page-title">测评结果</h2>
      </div>
    </div>

    <div v-loading="loading">
      <template v-if="result">
        <!-- 分数展示 -->
        <el-card shadow="never" class="score-card">
          <div class="score-display">
            <el-progress
              type="circle"
              :percentage="scorePercent"
              :status="result.passed ? 'success' : 'exception'"
              :width="180"
              :stroke-width="14"
            >
              <template #default>
                <div class="score-inner">
                  <span class="score-num">{{ result.score }}</span>
                  <span class="score-total">/ {{ result.totalScore || result.maxScore || 100 }}</span>
                </div>
              </template>
            </el-progress>
            <div class="score-meta">
              <h3 class="exam-title">{{ result.examName || result.name || '测评' }}</h3>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="考试日期">
                  {{ formatTime(result.examDate || result.createTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="用时">
                  {{ result.duration || result.timeUsed || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="正确数">
                  {{ result.correctCount || result.rightCount || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="错误数">
                  {{ result.wrongCount || result.errorCount || '-' }}
                </el-descriptions-item>
              </el-descriptions>
              <el-tag
                :type="result.passed ? 'success' : 'danger'"
                size="large"
                class="pass-tag"
              >
                {{ result.passed ? '已通过' : '未通过' }}
              </el-tag>
            </div>
          </div>
        </el-card>

        <!-- AI 报告 -->
        <el-card shadow="never" class="report-card" v-if="result.aiReport || result.report">
          <template #header>
            <span class="card-title">AI 学情报告</span>
          </template>
          <div class="report-content markdown-body" v-html="formattedReport"></div>
        </el-card>

        <!-- 错题汇总 -->
        <el-card shadow="never" class="wrong-card" v-if="wrongQuestions.length">
          <template #header>
            <span class="card-title">错题回顾（{{ wrongQuestions.length }} 题）</span>
          </template>
          <el-collapse accordion>
            <el-collapse-item
              v-for="(item, index) in wrongQuestions"
              :key="item.id || index"
              :title="'题目 ' + (index + 1) + '：' + truncate(item.content || item.question || '', 60)"
            >
              <div class="wrong-detail">
                <p class="wrong-question-text">{{ item.content || item.question }}</p>
                <p class="wrong-user-answer">
                  <span class="label">你的答案：</span>
                  <span class="wrong-answer">{{ item.userAnswer || item.myAnswer || '-' }}</span>
                </p>
                <p class="wrong-correct-answer">
                  <span class="label">正确答案：</span>
                  <span class="correct-answer">{{ item.correctAnswer || item.answer || '-' }}</span>
                </p>
                <p v-if="item.explanation" class="wrong-explanation">
                  <span class="label">解析：</span>
                  {{ item.explanation }}
                </p>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-card>

        <!-- 空态 -->
        <el-empty v-if="!wrongQuestions.length && !result.aiReport && !result.report" description="测评详情" :image-size="60" />
      </template>
      <el-empty v-else description="测评记录不存在" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getExamResult } from '../../api/student'
import { renderMarkdown } from '../../utils/markdown'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const result = ref(null)

const wrongQuestions = computed(() => {
  if (!result.value) return []
  return result.value.wrongQuestions || result.value.wrongList || result.value.wrongList || []
})

const scorePercent = computed(() => {
  if (!result.value) return 0
  const total = result.value.totalScore || result.value.maxScore || 100
  return Math.round((result.value.score / total) * 100)
})

const formattedReport = computed(() => {
  const report = result.value?.aiReport || result.value?.report || ''
  return renderMarkdown(report)
})

const truncate = (str, max) => {
  if (!str) return ''
  return str.length > max ? str.slice(0, max) + '...' : str
}

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const fetchResult = async () => {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    const res = await getExamResult(id)
    result.value = res
  } catch {
    ElMessage.error('加载测评结果失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchResult)
</script>

<style scoped>
.exam-result-page {
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

.score-card {
  margin-bottom: 20px;
  border-radius: 14px;
}

.score-display {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 10px 0;
}

.score-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-num {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
}

.score-total {
  font-size: 14px;
  color: var(--text-muted);
}

.score-meta {
  flex: 1;
}

.exam-title {
  margin: 0 0 12px;
  font-size: 18px;
  color: var(--text-primary);
}

.pass-tag {
  margin-top: 12px;
}

.report-card,
.wrong-card {
  margin-bottom: 20px;
  border-radius: 14px;
}

.card-title {
  font-weight: 600;
  color: var(--text-primary);
}

.report-content {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.9;
}

.report-content :deep(br) {
  display: block;
  content: '';
  margin-bottom: 6px;
}

.wrong-detail {
  padding: 8px 0;
}

.wrong-question-text {
  font-size: 15px;
  color: var(--text-primary);
  margin: 0 0 12px;
  line-height: 1.7;
}

.wrong-detail p {
  margin: 6px 0;
  font-size: 14px;
}

.label {
  color: var(--text-muted);
  margin-right: 4px;
}

.wrong-answer {
  color: var(--danger);
  font-weight: 500;
}

.correct-answer {
  color: var(--success);
  font-weight: 500;
}

.wrong-explanation {
  color: var(--text-secondary);
  line-height: 1.6;
}
</style>
