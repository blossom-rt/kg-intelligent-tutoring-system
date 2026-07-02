<template>
  <div class="page-container">
    <div class="page-header">
      <h2>测评管理</h2>
      <el-button type="primary" @click="openCreate">创建测评</el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="关联课程">
          <el-select v-model="filterForm.courseId" placeholder="全部课程" clearable @change="loadData">
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
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="examName" label="测评名称" min-width="180" />
        <el-table-column prop="totalScore" label="总分" width="100" align="center" />
        <el-table-column prop="courseName" label="关联课程" width="160" />
        <el-table-column prop="questionCount" label="题目数" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pagination.page"
        :page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 创建测评弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="创建测评"
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
              v-for="q in questionBank"
              :key="q.id"
              :label="`[${typeLabel(q.questionType)}] ${truncate(q.content, 30)}`"
              :value="q.id"
            />
          </el-select>
          <div style="font-size: 12px; color: #999; margin-top: 4px;">
            已选 {{ form.questionIds.length }} 道题目
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExamList, createExam, deleteExam, getQuestionList } from '../../api/teacher'
import { getCourseList } from '../../api/knowledge'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const tableData = ref([])
const courseList = ref([])
const questionBank = ref([])

const filterForm = reactive({
  courseId: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
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
    courseList.value = res.records || res.data || []
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
    tableData.value = res.records || res.data || []
    pagination.total = res.total || 0
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
  loadQuestions(courseId)
}

const loadQuestions = async (courseId) => {
  if (!courseId) return
  try {
    const res = await getQuestionList({ page: 1, size: 999, courseId })
    questionBank.value = res.records || res.data || []
  } catch {
    questionBank.value = []
  }
}

const openCreate = () => {
  Object.assign(form, { examName: '', courseId: null, questionIds: [], totalScore: 100 })
  questionBank.value = []
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await createExam({
      examName: form.examName,
      courseId: form.courseId,
      questionIds: form.questionIds,
      totalScore: form.totalScore
    })
    ElMessage.success('测评创建成功')
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
.page-container { padding: 20px 24px; background: #f5f7fa; min-height: 100vh; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; color: #303133; }
.filter-card { margin-bottom: 16px; }
</style>
