<template>
  <div class="practice-page">
    <div class="page-header">
      <h2 class="page-title">在线刷题</h2>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-area" v-loading="true" />

    <!-- 已完成：显示成绩总结 -->
    <div v-else-if="finished" class="result-area">
      <el-card shadow="never" class="result-card">
        <div class="result-circle">
          <el-progress
            type="circle"
            :percentage="scorePercent"
            :status="scorePercent >= 60 ? 'success' : 'exception'"
            :width="160"
          >
            <template #default>
              <span class="score-text">{{ correctCount }} / {{ questions.length }}</span>
            </template>
          </el-progress>
        </div>
        <h3 class="result-title">练习结束</h3>
        <p class="result-summary">
          共 {{ questions.length }} 题，答对 {{ correctCount }} 题，
          正确率 {{ scorePercent }}%
        </p>
        <div class="result-actions">
          <el-button type="primary" size="large" @click="restart">再来一次</el-button>
          <el-button size="large" @click="goBack">返回</el-button>
        </div>
      </el-card>
    </div>

    <!-- 答题中 -->
    <div v-else class="quiz-area">
      <div class="quiz-progress">
        <span class="progress-label">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
        <el-progress
          :percentage="Math.round(((currentIndex + 1) / questions.length) * 100)"
          :stroke-width="8"
        />
      </div>

      <el-card shadow="never" class="question-card">
        <template #header>
          <div class="question-header">
            <span class="question-no">题目 {{ currentIndex + 1 }}</span>
            <el-tag v-if="currentQuestion.difficulty" :type="diffTagType(currentQuestion.difficulty)" size="small">
              {{ diffLabel(currentQuestion.difficulty) }}
            </el-tag>
          </div>
        </template>

        <div class="question-body">
          <p class="question-text">{{ currentQuestion.content || currentQuestion.question || currentQuestion.title }}</p>

          <el-radio-group
            v-model="selectedAnswer"
            class="options-group"
            :disabled="answered"
          >
            <div
              v-for="(opt, oi) in parsedOptions"
              :key="oi"
              class="option-item"
              :class="optionClass(oi)"
            >
              <el-radio :label="opt.key" :value="opt.key" size="large">
                <span class="option-text">{{ opt.label }}</span>
              </el-radio>
            </div>
          </el-radio-group>

          <!-- 反馈 -->
          <div v-if="answered" class="feedback-area">
            <el-alert
              :title="isCorrect ? '回答正确！' : '回答错误'"
              :type="isCorrect ? 'success' : 'error'"
              :closable="false"
              show-icon
            >
              <template v-if="!isCorrect">
                <p class="correct-answer">正确答案：{{ currentQuestion.answer || currentQuestion.correctAnswer }}</p>
                <p v-if="explainText" class="explanation">{{ explainText }}</p>
              </template>
            </el-alert>
          </div>
        </div>

        <div class="question-actions">
          <el-button v-if="!answered" type="primary" size="large" @click="submitAnswer" :disabled="!selectedAnswer">
            提交答案
          </el-button>
          <el-button v-else size="large" @click="nextQuestion">
            {{ currentIndex < questions.length - 1 ? '下一题' : '查看结果' }}
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPracticeQuestions, addWrongQuestion } from '../../api/student'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const questions = ref([])
const currentIndex = ref(0)
const selectedAnswer = ref('')
const answered = ref(false)
const isCorrect = ref(false)
const correctCount = ref(0)
const finished = ref(false)
const userAnswers = ref([])

const currentQuestion = computed(() => {
  return questions.value[currentIndex.value] || {}
})
const explainText = computed(() => {
  return currentQuestion.value.explanation || currentQuestion.value.analysis || ''
})

const parsedOptions = computed(() => {
  const q = currentQuestion.value
  let opts = q.options
  if (typeof opts === 'string') {
    try {
      opts = JSON.parse(opts)
    } catch {
      opts = []
    }
  }
  if (Array.isArray(opts)) {
    return opts.map((o, i) => {
      if (typeof o === 'string') {
        return { key: String(i), label: o }
      }
      return { key: o.key || o.value || String(i), label: o.label || o.text || o.value || String(o) }
    })
  }
  // 对象格式 { A: '...', B: '...' }
  if (typeof opts === 'object' && opts !== null) {
    return Object.entries(opts).map(([key, label]) => ({ key, label: String(label) }))
  }
  return []
})

const scorePercent = computed(() => {
  if (!questions.value.length) return 0
  return Math.round((correctCount.value / questions.value.length) * 100)
})

const diffLabel = (level) => {
  const map = { 1: '入门', 2: '基础', 3: '进阶', 4: '困难', 5: '挑战' }
  return map[level] || '未知'
}

const diffTagType = (level) => {
  const map = { 1: 'success', 2: 'info', 3: 'warning', 4: 'danger', 5: 'danger' }
  return map[level] || 'info'
}

// 将答案统一转为索引（A→0, B→1, C→2, D→3），兼容数字索引
const normalizeAnswer = (ans) => {
  if (!ans && ans !== 0) return ''
  const s = String(ans).trim().toUpperCase()
  const letterMap = { A: '0', B: '1', C: '2', D: '3', E: '4', F: '5' }
  return letterMap[s] || s
}

const optionClass = (oi) => {
  if (!answered.value) return {}
  const key = parsedOptions.value[oi]?.key
  const correctKey = normalizeAnswer(currentQuestion.value.answer || currentQuestion.value.correctAnswer)
  return {
    'option-correct': String(key) === correctKey,
    'option-wrong': !isCorrect.value && key === selectedAnswer.value
  }
}

const submitAnswer = () => {
  if (!selectedAnswer.value) return
  answered.value = true
  const correctKey = normalizeAnswer(currentQuestion.value.answer || currentQuestion.value.correctAnswer)
  isCorrect.value = String(selectedAnswer.value) === correctKey
  if (isCorrect.value) {
    correctCount.value++
  } else {
    addWrongQuestion({
      questionId: currentQuestion.value.id,
      wrongAnswer: selectedAnswer.value
    }).catch(() => {})
  }
  userAnswers.value.push({
    questionId: currentQuestion.value.id,
    answer: selectedAnswer.value,
    correct: isCorrect.value
  })
}

const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
    selectedAnswer.value = ''
    answered.value = false
    isCorrect.value = false
  } else {
    finished.value = true
  }
}

const restart = () => {
  currentIndex.value = 0
  selectedAnswer.value = ''
  answered.value = false
  isCorrect.value = false
  correctCount.value = 0
  finished.value = false
  userAnswers.value = []
}

const goBack = () => {
  const nodeId = route.query.nodeId
  if (nodeId) {
    router.push('/student/study/' + nodeId)
  } else {
    router.back()
  }
}

const fetchQuestions = async () => {
  const nodeId = route.query.nodeId
  const questionId = route.query.questionId
  if (!nodeId && !questionId) {
    ElMessage.warning('缺少知识点或题目参数')
    return
  }

  loading.value = true
  try {
    const res = await getPracticeQuestions({ nodeId, questionId })
    questions.value = Array.isArray(res) ? res : (res.records || res.questions || [])
    if (questions.value.length === 0) {
      ElMessage.info('暂无练习题')
    }
  } catch {
    ElMessage.error('加载练习题失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchQuestions)
</script>

<style scoped>
.practice-page {
  min-height: 100vh;
  background: #faf7f2;
  padding: 24px 32px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #2d2a26;
}

.loading-area {
  min-height: 300px;
}

/* 成绩总结 */
.result-area {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}

.result-card {
  width: 500px;
  text-align: center;
  border-radius: 16px;
  padding: 40px 20px;
}

.result-circle {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.score-text {
  font-size: 20px;
  font-weight: 700;
  color: #2d2a26;
}

.result-title {
  margin: 0 0 8px;
  font-size: 22px;
  color: #2d2a26;
}

.result-summary {
  font-size: 15px;
  color: #6b655e;
  margin: 0 0 24px;
}

.result-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* 答题区 */
.quiz-area {
  max-width: 750px;
  margin: 0 auto;
}

.quiz-progress {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.progress-label {
  font-size: 14px;
  color: #6b655e;
  white-space: nowrap;
}

.question-card {
  border-radius: 14px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-no {
  font-weight: 600;
  font-size: 15px;
  color: #2d2a26;
}

.question-body {
  padding: 8px 0;
}

.question-text {
  font-size: 16px;
  color: #2d2a26;
  line-height: 1.8;
  margin: 0 0 24px;
}

.options-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  padding: 12px 16px;
  border: 1px solid #e8e3db;
  border-radius: 10px;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.option-item:hover:not(.option-correct):not(.option-wrong) {
  border-color: #bbb6ad;
}

.option-item :deep(.el-radio) {
  width: 100%;
}

.option-correct {
  border-color: #67c23a;
  background: #edf2ec;
}

.option-wrong {
  border-color: #f56c6c;
  background: #fef0f0;
}

.option-text {
  font-size: 15px;
}

.feedback-area {
  margin-top: 20px;
}

.explanation {
  margin: 8px 0 0;
  font-size: 14px;
  color: #6b655e;
  line-height: 1.6;
}

.question-actions {
  margin-top: 24px;
  text-align: center;
}
</style>
