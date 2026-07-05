<template>
  <div class="wrong-book-page">
    <StudentHeader title="我的错题本" subtitle="温故知新，每次回顾都是进步" />

    <!-- 选项卡 -->
    <div class="tab-bar">
      <button
        v-for="t in tabs" :key="t.key"
        class="tab-btn" :class="{ on: activeTab === t.key }"
        @click="activeTab = t.key"
      >
        {{ t.label }}
        <span class="tab-count">{{ t.key === 'active' ? activeList.length : reviewedList.length }}</span>
      </button>
    </div>

    <!-- 当前错题 -->
    <el-table v-if="activeTab === 'active'"
      v-loading="loading"

      :data="activeList"
      style="width: 100%" stripe
      empty-text="暂无错题，继续保持！"

      :data="paginatedWrongList"
      style="width: 100%"
      stripe
      empty-text="暂无错题记录"

    >
      <el-table-column label="题目内容" min-width="220">
        <template #default="{ row }">
          <span class="question-snippet">{{ truncate(row.content || row.question || row.title || '', 50) }}</span>
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
          {{ formatTime(row.lastWrongTime || row.updateTime || row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="goRedo(row)">重做</el-button>
          <el-button type="warning" link size="small" @click="handleAiExplain(row)" :loading="row._aiLoading">AI讲解</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 曾经错题 -->
    <el-table v-else
      :data="reviewedList"
      style="width: 100%" stripe
      empty-text="还没有复习过的错题"
    >
      <el-table-column label="题目内容" min-width="220">
        <template #default="{ row }">
          <span class="question-snippet reviewed">{{ truncate(row.content || row.question || row.title || '', 50) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="所属知识点" width="160">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.nodeName || row.knowledgeName || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="曾错次数" width="100" align="center">
        <template #default="{ row }">
          <span class="reviewed-count">{{ row.wrongCount || row.count || 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="复习时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row._reviewedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="goRedo(row)">重做</el-button>
          <el-button type="warning" link size="small" @click="handleAiExplain(row)" :loading="row._aiLoading">AI讲解</el-button>
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

    <!-- AI 讲解弹窗 -->
    <el-dialog v-model="aiDialogVisible" title="AI 错题讲解" width="600px" destroy-on-close>
      <div v-loading="aiLoading">
        <div v-if="aiContent" class="ai-content markdown-body" v-html="formattedAiContent"></div>
        <el-empty v-if="!aiLoading && !aiContent" description="暂无讲解内容" :image-size="60" />
      </div>
      <template #footer>
        <el-button @click="aiDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StudentHeader from '../../components/StudentHeader.vue'
import { getWrongQuestions, aiWrongExplain } from '../../api/student'
import { renderMarkdown } from '../../utils/markdown'

const router = useRouter()
const loading = ref(false)
const wrongList = ref([])
const activeTab = ref('active')

const tabs = [
  { key: 'active', label: '当前错题' },
  { key: 'reviewed', label: '曾经错题' },
]

const pagination = reactive({ page: 1, size: 10, total: 0 })
const paginatedWrongList = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  const list = activeTab.value === 'active' ? activeList.value : reviewedList.value
  return list.slice(start, start + pagination.size)
})

const aiDialogVisible = ref(false)
const aiLoading = ref(false)
const aiContent = ref('')

// localStorage 记录已复习的错题: { questionId: timestamp }
const getReviewed = () => {
  try { return JSON.parse(localStorage.getItem('reviewedWrongQuestions') || '{}') } catch { return {} }
}
const saveReviewed = (map) => {
  localStorage.setItem('reviewedWrongQuestions', JSON.stringify(map))
}

const reviewedMap = ref(getReviewed())

const activeList = computed(() => {
  return wrongList.value.filter(r => !reviewedMap.value[r.questionId])
})
const reviewedList = computed(() => {
  return wrongList.value
    .filter(r => reviewedMap.value[r.questionId])
    .map(r => ({ ...r, _reviewedAt: reviewedMap.value[r.questionId] }))
})

const formattedAiContent = computed(() => renderMarkdown(aiContent.value))

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
  router.push('/student/practice?questionId=' + (row.questionId || row.id) + '&fromWrong=true')
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

const fetchWrongList = async () => {
  loading.value = true
  try {
    const res = await getWrongQuestions()
    wrongList.value = Array.isArray(res) ? res : (res.records || res.list || [])
    pagination.total = wrongList.value.length
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

/* 选项卡 */
.tab-bar {
  display: flex; gap: 0;
  padding: 16px 32px 0;
  border-bottom: 1px solid #e8e3db;
  background: #faf7f2;
}
.tab-btn {
  padding: 10px 20px;
  font-size: 14px; font-weight: 500;
  border: none; background: none;
  color: #6b655e; cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s ease;
  display: flex; align-items: center; gap: 6px;
}
.tab-btn:hover { color: #2d2a26; }
.tab-btn.on {
  color: #ff7b3d;
  border-bottom-color: #ff7b3d;
}
.tab-count {
  font-size: 12px; background: #f3efe8; color: #6b655e;
  padding: 1px 8px; border-radius: 10px; min-width: 20px; text-align: center;
}
.tab-btn.on .tab-count { background: #fff3e8; color: #ff7b3d; }

/* 表格 */
.question-snippet { font-size: 14px; color: #6b655e; }
.question-snippet.reviewed { color: #a09a92; }
.wrong-count { font-weight: 700; color: #f56c6c; }
.reviewed-count { font-weight: 600; color: #5eaf83; }

.ai-content {
  font-size: 15px; color: #2d2a26; line-height: 1.9; min-height: 120px;
}
.ai-content :deep(br) { display: block; content: ''; margin-bottom: 6px; }
</style>
