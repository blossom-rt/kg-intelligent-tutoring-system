<template>
  <div class="page-container">
    <StudentHeader title="题库管理">
      <template #actions>
        <el-button type="primary" @click="openAdd">新增题目</el-button>
      </template>
    </StudentHeader>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" @submit.prevent>
        <el-form-item label="关联知识点">
          <el-select v-model="filterForm.nodeId" placeholder="全部知识点" clearable @change="loadData" filterable style="width:200px">
            <el-option v-for="n in nodeList" :key="n.id" :label="n.name" :value="n.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="filterForm.difficulty" placeholder="全部难度" clearable @change="loadData" style="width:200px">
            <el-option label="简单 (1)" :value="1" />
            <el-option label="中等 (2)" :value="2" />
            <el-option label="困难 (3)" :value="3" />
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
        <el-table-column prop="content" label="题干" min-width="240" show-overflow-tooltip />
        <el-table-column prop="questionType" label="题型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTag(row?.questionType)" size="small">{{ typeLabel(row?.questionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="diffTag(row.difficulty)" size="small">{{ diffLabel(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nodeId" label="知识点ID" width="140" />
        <el-table-column label="操作" width="160" fixed="right">
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
        @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑题目' : '新增题目'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="关联知识点" prop="nodeId">
          <el-select v-model="form.nodeId" placeholder="请选择知识点" style="width: 100%" filterable>
            <el-option v-for="n in nodeList" :key="n.id" :label="n.name" :value="n.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型" prop="questionType">
          <el-select v-model="form.questionType" placeholder="请选择题型" style="width: 100%">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multi" />
            <el-option label="判断题" value="judge" />
          </el-select>
        </el-form-item>
        <el-form-item label="题干" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题干内容" />
        </el-form-item>
        <el-form-item label="选项(JSON)" prop="options">
          <el-input v-model="form.options" type="textarea" :rows="3" placeholder='例: [{"key":"A","value":"选项A"}]' />
        </el-form-item>
        <el-form-item label="答案" prop="answer">
          <el-input v-model="form.answer" placeholder="请输入正确答案" />
        </el-form-item>
        <el-form-item label="解析" prop="analysis">
          <el-input v-model="form.analysis" type="textarea" :rows="2" placeholder="请输入题目解析" />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 100%">
            <el-option label="简单 (1)" :value="1" />
            <el-option label="中等 (2)" :value="2" />
            <el-option label="困难 (3)" :value="3" />
          </el-select>
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
import { getQuestionList, createQuestion, updateQuestion, deleteQuestion } from '../../api/teacher'
import { getNodeList } from '../../api/knowledge'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const tableData = ref([])
const nodeList = ref([])

const filterForm = reactive({
  nodeId: null,
  difficulty: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  nodeId: null,
  questionType: 'single',
  content: '',
  options: '',
  answer: '',
  analysis: '',
  difficulty: 1
})

const rules = {
  nodeId: [{ required: true, message: '请选择知识点', trigger: 'change' }],
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入答案', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

const typeLabel = (v) => {
  if (v === 'single') return '单选'
  if (v === 'multi') return '多选'
  if (v === 'judge') return '判断'
  return v
}
const typeTag = (v) => {
  if (v === 'single') return 'success'
  if (v === 'multi') return 'primary'
  if (v === 'judge') return 'warning'
  return 'info'
}

const diffLabel = (val) => {
  if (val === 1) return '简单'
  if (val === 2) return '中等'
  if (val === 3) return '困难'
  return '-'
}
const diffTag = (val) => {
  if (val === 1) return 'success'
  if (val === 2) return 'warning'
  if (val === 3) return 'danger'
  return 'info'
}

const loadNodes = async () => {
  try {
    const res = await getNodeList({ page: 1, size: 999 })
    nodeList.value = Array.isArray(res) ? res : (res.records || res.data || [])
  } catch {
    nodeList.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (filterForm.nodeId) params.nodeId = filterForm.nodeId
    if (filterForm.difficulty) params.difficulty = filterForm.difficulty
    const res = await getQuestionList(params)
    if (Array.isArray(res)) { tableData.value = res; pagination.total = res.length }
    else if (res && res.records) { tableData.value = res.records; pagination.total = res.total || 0 }
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  filterForm.nodeId = null
  filterForm.difficulty = null
  pagination.page = 1
  loadData()
}

const openAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null, nodeId: null, questionType: 'single',
    content: '', options: '', answer: '', analysis: '', difficulty: 1
  })
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    nodeId: row.nodeId || null,
    questionType: row.questionType || 'single',
    content: row.content || '',
    options: row.options || '',
    answer: row.answer || '',
    analysis: row.analysis || '',
    difficulty: row.difficulty || 1
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const data = {
      nodeId: form.nodeId,
      questionType: form.questionType,
      content: form.content,
      options: form.options,
      answer: form.answer,
      analysis: form.analysis,
      difficulty: form.difficulty
    }
    if (isEdit.value) {
      await updateQuestion(form.id, data)
      ElMessage.success('编辑成功')
    } else {
      await createQuestion(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch { } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除该题目吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteQuestion(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch { }
  }).catch(() => { })
}

onMounted(() => {
  loadNodes()
  loadData()
})
</script>

<style scoped>
.page-container { padding: 20px 24px; background: #faf7f2; min-height: 100vh; }
</style>
