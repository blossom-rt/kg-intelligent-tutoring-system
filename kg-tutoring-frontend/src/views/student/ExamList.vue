<template>
  <div class="exam-list-page">
    <StudentHeader title="测评中心" subtitle="检验学习成果，发现薄弱环节" />

    <el-table
      v-loading="loading"
      :data="paginatedExamList"
      style="width: 100%"
      stripe
      highlight-current-row
    >
      <template #empty>
        <el-empty description="暂无测评记录" :image-size="60" />
      </template>
      <el-table-column prop="examName" label="测评名称" min-width="180">
        <template #default="{ row }">
          <span class="exam-name">{{ row.examName || '课程' + (row.courseId || '-') + '测评' }}</span>
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
          {{ row.totalScore || 100 }}
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
          {{ formatTime(row.finishTime || row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.finished" type="primary" link @click="goDetail(row)">查看结果</el-button>
          <el-button v-else type="primary" link @click="goTake(row)">参加测评</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      layout="total, prev, pager, next, sizes"
      :page-sizes="[5, 10, 20, 50]"
      style="margin-top: 16px; justify-content: flex-end"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StudentHeader from '../../components/StudentHeader.vue'
import { getStudentExams } from '../../api/student'

const router = useRouter()
const loading = ref(false)
const examList = ref([])

const pagination = reactive({ page: 1, size: 10, total: 0 })
const paginatedExamList = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return examList.value.slice(start, start + pagination.size)
})

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
    examList.value = Array.isArray(res) ? res : (res.records || res.list || [])
    pagination.total = examList.value.length
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
  background: var(--bg-root);
  
}

.exam-name {
  font-weight: 500;
  color: var(--text-primary);
}

.score-value {
  font-weight: 700;
  font-size: 16px;
}

.score-pass {
  color: var(--success);
}

.score-fail {
  color: var(--danger);
}
</style>
