<template>
  <div class="exam-take-page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="router.push('/student/exams')" :icon="ArrowLeft" circle size="small" />
        <div>
          <h2 class="page-title">{{ paper?.examName || '测评答题' }}</h2>
          <p class="page-meta">{{ paper?.courseName || '-' }} · 满分 {{ paper?.totalScore || 100 }}</p>
        </div>
      </div>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交测评</el-button>
    </div>

    <!-- 提交中遮罩 -->
    <div v-if="submitLoading" class="submit-overlay">
      <div class="submit-loading-box">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p class="submit-tip">{{ submitTip }}</p>
      </div>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!questions.length" description="暂无题目" :image-size="80" />
      <div v-else class="question-list">
        <el-card v-for="(q, index) in questions" :key="q.id" shadow="never" class="question-card">
          <div class="question-title">
            <span class="question-index">{{ index + 1 }}</span>
            <span>{{ q.content }}</span>
          </div>
          <el-radio-group v-model="answers[q.id]" class="option-list">
            <el-radio v-for="option in parseOptions(q.options)" :key="option.key" :label="option.key">
              {{ option.key }}. {{ option.value }}
            </el-radio>
          </el-radio-group>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import { getExamPaper, submitExam } from '../../api/student'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const submitTip = ref('')
let submitTipTimer = null
const paper = ref(null)
const answers = reactive({})

const questions = computed(() => paper.value?.questions || [])

const parseOptions = (raw) => {
  if (!raw) return []
  try {
    const obj = typeof raw === 'string' ? JSON.parse(raw) : raw
    return Object.entries(obj).map(([key, value]) => ({ key, value }))
  } catch {
    return []
  }
}

const loadPaper = async () => {
  loading.value = true
  try {
    paper.value = await getExamPaper(route.params.id)
  } catch {
    ElMessage.error('加载测评失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  const unanswered = questions.value.filter(q => !answers[q.id])
  if (unanswered.length) {
    ElMessage.warning(`还有 ${unanswered.length} 道题未作答`)
    return
  }

  const confirmed = await ElMessageBox.confirm('提交后将生成本次测评成绩，确定提交吗？', '提交确认', {
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    type: 'warning'
  }).catch(() => false)
  if (!confirmed) return

  submitLoading.value = true
  const examTips = ['正在批阅答卷...', '正在生成AI诊断报告...', '分析薄弱环节中...', '整理错题数据...']
  submitTip.value = examTips[0]
  let tipIdx = 1
  submitTipTimer = setInterval(() => {
    submitTip.value = examTips[tipIdx % examTips.length]
    tipIdx++
  }, 2500)

  try {
    await submitExam({
      examId: paper.value.id,
      courseId: paper.value.courseId,
      issuedAt: paper.value.issuedAt,
      answers: questions.value.map(q => ({
        questionId: q.id,
        answer: answers[q.id]
      }))
    })
    ElMessage.success('测评提交成功')
    router.push('/student/exams')
  } catch {
  } finally {
    if (submitTipTimer) { clearInterval(submitTipTimer); submitTipTimer = null }
    submitLoading.value = false
  }
}

onMounted(loadPaper)
</script>

<style scoped>
.exam-take-page {
  min-height: 100vh;
  background: var(--bg-root);
  padding: 24px 32px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
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

.page-meta {
  margin: 4px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.question-list {
  display: grid;
  gap: 14px;
}

.question-card {
  border-radius: 8px;
}

.question-title {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  color: var(--text-primary);
  font-weight: 600;
  line-height: 1.7;
}

.question-index {
  display: inline-flex;
  width: 24px;
  height: 24px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--accent);
  color: #fff;
  font-size: 13px;
  flex: 0 0 auto;
  margin-top: 2px;
}

.option-list {
  display: grid;
  gap: 8px;
  margin-top: 14px;
  padding-left: 34px;
}

/* 提交遮罩 */
.submit-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(0,0,0,0.45); display: flex;
  align-items: center; justify-content: center;
}
.submit-loading-box {
  background: var(--bg-surface); border-radius: 16px;
  padding: 48px 56px; text-align: center;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
}
.submit-tip {
  margin-top: 20px; font-size: 16px; color: var(--text-secondary);
}
</style>
