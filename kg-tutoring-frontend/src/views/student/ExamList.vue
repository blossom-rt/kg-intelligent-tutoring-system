<template>
  <div class="exam-list-page">
    <div class="page-header">
      <h2 class="page-title">我的测评</h2>
    </div>

    <el-table
      v-loading="loading"
      :data="examList"
      style="width: 100%"
      stripe
      highlight-current-row
      empty-text="暂无测评记录"
    >
      <el-table-column prop="examName" label="测评名称" min-width="180">
        <template #default="{ row }">
          <span class="exam-name">{{ row.examName || row.name || row.title || '未命名测评' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="成绩" width="120" align="center">
        <template #default="{ row }">
          <span class="score-value" :class="scoreClass(row.userScore ?? row.score)">
            {{ row.userScore ?? row.score ?? '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="满分" width="100" align="center">
        <template #default="{ row }">
          {{ row.totalScore || row.maxScore || 100 }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.passed ? 'success' : row.score != null ? 'danger' : 'info'" size="small">
            {{ row.finished ? (row.passed ? '已通过' : '未通过') : '未参加' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="测评日期" width="180">
        <template #default="{ row }">
          {{ formatTime(row.finishTime || row.createTime || row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.finished" type="primary" link @click="goDetail(row)">查看结果</el-button>
          <el-button v-else type="primary" link @click="goTake(row)">参加测评</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getStudentExams } from '../../api/student'

const router = useRouter()
const loading = ref(false)
const examList = ref([])

const scoreClass = (score) => {
  if (score == null) return ''
  return score >= 60 ? 'score-pass' : 'score-fail'
}

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const goDetail = (row) => {
  router.push('/student/exam/' + row.recordId)
}

const goTake = (row) => {
  router.push('/student/exam/take/' + row.id)
}

const fetchExams = async () => {
  loading.value = true
  try {
    const res = await getStudentExams()
    if (Array.isArray(res)) {
      examList.value = res
    } else if (res && res.records) {
      examList.value = res.records
    } else if (res && res.list) {
      examList.value = res.list
    }
  } catch {
    ElMessage.error('加载测评列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchExams)
</script>

<style scoped>
.exam-list-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 24px 32px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #2c5eb5;
}

.exam-name {
  font-weight: 500;
  color: #303133;
}

.score-value {
  font-weight: 700;
  font-size: 16px;
}

.score-pass {
  color: #67c23a;
}

.score-fail {
  color: #f56c6c;
}
</style>
