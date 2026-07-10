<template>
  <div class="page-container">
    <StudentHeader title="测评管理">
      <template #actions>
        <el-button type="primary" @click="openCreate">创建测评</el-button>
      </template>
    </StudentHeader>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" @submit.prevent>
        <el-form-item label="关联课程">
          <el-select v-model="filterForm.courseId" placeholder="全部课程" clearable @change="loadData" style="width:200px">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="paginatedTableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="测评ID" min-width="80" />
        <el-table-column prop="examName" label="测评名称" min-width="180" />
        <el-table-column prop="totalScore" label="总分" width="80" align="center" />
        <el-table-column prop="courseId" label="课程ID" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pagination.page"
        :page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 创建测评弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="displayDialogTitle"
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="测评名称" prop="examName">
          <el-input v-model="form.examName" placeholder="请输入测评名称" />
        </el-form-item>
        <el-form-item label="关联课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%" @change="onCourseChange">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="智能组卷" v-if="form.courseId">
          <div style="display:flex; gap:12px; align-items:center; width:100%;">
            <el-input-number v-model="autoQuestionCount" :min="1" :max="100" :disabled="!form.courseId" style="width:140px;" />
            <span style="font-size:13px;color:#a09a92;">道题目</span>
            <el-button type="success" plain :loading="autoLoading" :disabled="!form.courseId" @click="autoPickQuestions">
              智能选题
            </el-button>
            <el-button size="small" @click="form.questionIds = []">清空已选</el-button>
          </div>
        </el-form-item>
        <el-form-item label="按章节筛选" v-if="form.courseId">
          <el-select v-model="examChapterFilter" placeholder="全部章节" clearable style="width:200px" @change="onExamChapterChange">
            <el-option v-for="ch in examChapterList" :key="ch.id" :label="ch.chapterName" :value="ch.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择题目" prop="questionIds">
          <el-select
            v-model="form.questionIds"
            placeholder="请选择题目（可多选）"
            style="width: 100%"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            :max-collapse-tags="3"
          >
            <el-option
              v-for="q in filteredQuestionBank"
              :key="q.id"
              :label="`[${typeLabel(q.questionType)}] ${truncate(q.content, 30)}`"
              :value="q.id"
            />
          </el-select>
          <div style="font-size: 12px; color: #a09a92; margin-top: 4px;">
            题库共 {{ questionBank.length }} 道，筛选 {{ filteredQuestionBank.length }} 道，已选 {{ form.questionIds.length }} 道
          </div>
        </el-form-item>
        <el-form-item label="总分" prop="totalScore">
          <el-input-number v-model="form.totalScore" :min="1" :max="1000" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import StudentHeader from '../../components/StudentHeader.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExamList, createExam, updateExam, deleteExam, getQuestionList } from '../../api/teacher'
import { getCourseList, getChapterList, getNodeList } from '../../api/knowledge'

const loading = ref(false)
const submitLoading = ref(false)
const autoLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const tableData = ref([])
const courseList = ref([])
const questionBank = ref([])
const examChapterFilter = ref(null)
const examChapterList = ref([])
const nodeChapterMap = ref({})
const filteredQuestionBank = computed(() => {
  if (!examChapterFilter.value) return questionBank.value
  const map = nodeChapterMap.value
  return questionBank.value.filter(q => {
    return map[q.nodeId] === examChapterFilter.value
  })
})
const autoQuestionCount = ref(5)

const filterForm = reactive({
  courseId: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const paginatedTableData = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return tableData.value.slice(start, start + pagination.size)
})

const form = reactive({
  examName: '',
  courseId: null,
  questionIds: [],
  totalScore: 100
})

const rules = {
  examName: [{ required: true, message: '请输入测评名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  questionIds: [{ required: true, message: '请选择题目', trigger: 'change' }],
  totalScore: [{ required: true, message: '请输入总分', trigger: 'blur' }]
}

const typeLabel = (v) => {
  if (v === 'single') return '单选'
  if (v === 'multi') return '多选'
  if (v === 'judge') return '判断'
  return v
}

const truncate = (str, len) => {
  if (!str) return ''
  return str.length > len ? str.slice(0, len) + '...' : str
}

const loadCourses = async () => {
  try {
    const res = await getCourseList({ page: 1, size: 999 })
    courseList.value = Array.isArray(res) ? res : (res.records || res.data || [])
  } catch {
    courseList.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (filterForm.courseId) params.courseId = filterForm.courseId
    const res = await getExamList(params)
    tableData.value = Array.isArray(res) ? res : (res.records || res.data || [])
    pagination.total = Array.isArray(res) ? res.length : (res.total || 0)
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  filterForm.courseId = null
  pagination.page = 1
  loadData()
}

const onCourseChange = (courseId) => {
  form.questionIds = []
  examChapterFilter.value = null
  loadQuestions(courseId)
  loadExamChapters(courseId)
}

const loadExamChapters = async (courseId) => {
  if (!courseId) { examChapterList.value = []; nodeChapterMap.value = {}; return }
  try {
    const res = await getChapterList({ courseId })
    examChapterList.value = Array.isArray(res) ? res : []
    // 同时获取节点→章节映射
    const nodeRes = await getNodeList({ courseId, page: 1, size: 999 })
    const nodes = Array.isArray(nodeRes) ? nodeRes : (nodeRes?.records || [])
    const map = {}
    nodes.forEach(n => { if (n.chapterId != null) map[n.id] = n.chapterId })
    nodeChapterMap.value = map
  } catch { examChapterList.value = [] }
}

const onExamChapterChange = () => {
  form.questionIds = []
}

const loadQuestions = async (courseId) => {
  if (!courseId) return
  try {
    const res = await getQuestionList({ page: 1, size: 999, courseId })
    questionBank.value = Array.isArray(res) ? res : (res.records || res.data || [])
  } catch {
    questionBank.value = []
  }
}

const autoPickQuestions = async () => {
  if (!form.courseId) {
    ElMessage.warning('请先选择关联课程')
    return
  }
  if (!questionBank.value.length) {
    await loadQuestions(form.courseId)
  }
  const bank = questionBank.value
  if (!bank.length) {
    ElMessage.warning('该课程暂无可用题目')
    return
  }
  const count = Math.min(autoQuestionCount.value, bank.length)

  // 按知识点分组，保证覆盖度
  const groups = {}
  bank.forEach(q => {
    const key = q.nodeId || 'unknown'
    if (!groups[key]) groups[key] = []
    groups[key].push(q)
  })

  // 每个知识点内按难度排序（穿插简单/中等/困难）
  Object.values(groups).forEach(arr => {
    arr.sort((a, b) => (a.difficulty || 1) - (b.difficulty || 1))
    const easy = arr.filter(q => (q.difficulty || 1) <= 1)
    const medium = arr.filter(q => q.difficulty === 2)
    const hard = arr.filter(q => (q.difficulty || 3) >= 3)
    const interleaved = []
    const maxLen = Math.max(easy.length, medium.length, hard.length)
    for (let i = 0; i < maxLen; i++) {
      if (i < easy.length) interleaved.push(easy[i])
      if (i < medium.length) interleaved.push(medium[i])
      if (i < hard.length) interleaved.push(hard[i])
    }
    arr.length = 0
    arr.push(...interleaved)
  })

  const keys = Object.keys(groups)
  // 轮询每个知识点取题，直到取够 count 个
  const picked = []
  let idx = 0
  while (picked.length < count) {
    const key = keys[idx % keys.length]
    if (groups[key].length > 0) {
      picked.push(groups[key].shift())
    }
    idx++
    // 安全终止：所有知识点都取完了
    if (idx > keys.length * 50) break
  }

  form.questionIds = picked.map(q => q.id)
  ElMessage.success(
    `智能选题完成：${count} 道题，覆盖 ${keys.length} 个知识点`
  )
}

const displayDialogTitle = computed(() => editingExamId.value ? '编辑测评' : '创建测评')
const editingExamId = ref(null)

const openCreate = () => {
  editingExamId.value = null
  Object.assign(form, { examName: '', courseId: null, questionIds: [], totalScore: 100 })
  autoQuestionCount.value = 5
  questionBank.value = []
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingExamId.value = row.id
  Object.assign(form, {
    examName: row.examName || '',
    courseId: row.courseId || null,
    questionIds: row.questionIds || [],
    totalScore: row.totalScore || 100
  })
  autoQuestionCount.value = 5
  if (row.courseId) loadQuestions(row.courseId)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const data = {
      examName: form.examName,
      courseId: form.courseId,
      questionIds: form.questionIds,
      totalScore: form.totalScore
    }
    if (editingExamId.value) {
      await updateExam(editingExamId.value, data)
      ElMessage.success('测评修改成功')
    } else {
      await createExam(data)
      ElMessage.success('测评创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch { } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除测评「${row.examName}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteExam(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch { }
  }).catch(() => { })
}

onMounted(() => {
  loadCourses()
  loadData()
})
</script>

<style scoped>
.page-container { padding: 20px 24px; background: var(--bg-root); min-height: 100vh; }
</style>
