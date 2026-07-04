<template>
  <div class="wrong-book-page">
    <StudentHeader title="我的错题本" subtitle="温故知新，每次回顾都是进步" />

    <el-table
      v-loading="loading"
      :data="wrongList"
      style="width: 100%"
      stripe
      empty-text="暂无错题记录"
    >
      <el-table-column label="题目内容" min-width="220">
        <template #default="{ row }">
          <span class="question-snippet">
            {{ truncate(row.content || row.question || row.title || '', 50) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="所属知识点" width="160">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.nodeName || row.knowledgeName || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="错误次数" width="100" align="center">
        <template #default="{ row }">
          <span class="wrong-count">{{ row.wrongCount || row.count || 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最近错误时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.lastWrongTime || row.updateTime || row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="goRedo(row)">
            重做
          </el-button>
          <el-button type="warning" link size="small" @click="handleAiExplain(row)" :loading="row._aiLoading">
            AI讲解
          </el-button>
          <el-button type="danger" link size="small" @click="handleRemove(row)">
            移除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- AI 讲解弹窗 -->
    <el-dialog
      v-model="aiDialogVisible"
      title="AI 错题讲解"
      width="600px"
      destroy-on-close
    >
      <div v-loading="aiLoading">
        <div v-if="aiContent" class="ai-content" v-html="formattedAiContent"></div>
        <el-empty v-if="!aiLoading && !aiContent" description="暂无讲解内容" :image-size="60" />
      </div>
      <template #footer>
        <el-button @click="aiDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import StudentHeader from '../../components/StudentHeader.vue'
import { getWrongQuestions, deleteWrongQuestion, aiWrongExplain } from '../../api/student'

const router = useRouter()
const loading = ref(false)
const wrongList = ref([])

const aiDialogVisible = ref(false)
const aiLoading = ref(false)
const aiContent = ref('')

const formattedAiContent = computed(() => {
  const text = typeof aiContent.value === 'string' ? aiContent.value : ''
  if (!text) return ''
  if (/<[^>]+>/.test(text)) return text
  return text.replace(/\n/g, '<br>')
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

const goRedo = (row) => {
  router.push('/student/practice?questionId=' + row.questionId + '&nodeId=' + (row.nodeId || ''))
}

const handleAiExplain = async (row) => {
  row._aiLoading = true
  aiDialogVisible.value = true
  aiLoading.value = true
  aiContent.value = ''

  try {
    const res = await aiWrongExplain({ questionId: row.questionId || row.id })
    aiContent.value = res?.aiExplain || res?.explanation || res || '暂无讲解'
  } catch {
    ElMessage.error('AI讲解请求失败')
  } finally {
    aiLoading.value = false
    row._aiLoading = false
  }
}

const handleRemove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要移除该错题吗？', '确认移除', {
      confirmButtonText: '移除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteWrongQuestion(row.id)
    ElMessage.success('已移除')
    fetchWrongList()
  } catch {
    // 取消移除
  }
}

const fetchWrongList = async () => {
  loading.value = true
  try {
    const res = await getWrongQuestions()
    wrongList.value = Array.isArray(res) ? res : (res.records || res.list || [])
  } catch {
    ElMessage.error('加载错题本失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchWrongList)
</script>

<style scoped>
.wrong-book-page {
  min-height: 100vh;
  background: #faf7f2;
}

.question-snippet {
  font-size: 14px;
  color: #6b655e;
}

.wrong-count {
  font-weight: 700;
  color: #f56c6c;
}

.ai-content {
  font-size: 15px;
  color: #2d2a26;
  line-height: 1.9;
  min-height: 120px;
}

.ai-content :deep(br) {
  display: block;
  content: '';
  margin-bottom: 6px;
}
</style>
