<template>
  <div class="page-container">
    <StudentHeader title="知识点管理">
      <template #actions>
        <el-button type="primary" @click="openAdd">新增知识点</el-button>
      </template>
    </StudentHeader>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" @submit.prevent>
        <el-form-item label="所属课程">
          <el-select v-model="filterForm.courseId" placeholder="全部课程" clearable style="width:220px" @change="loadData">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点名称">
          <el-input v-model="filterForm.name" placeholder="输入关键词搜索" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="name" label="知识点名称" min-width="160" />
        <el-table-column prop="courseId" label="课程ID" width="160" />
        <el-table-column prop="difficulty" label="难度" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="diffTag(row.difficulty)" size="small">{{ diffLabel(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chapter" label="章节" width="140" />
        <el-table-column prop="expectedMinutes" label="预计时长(分)" width="120" align="center" />
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
      :title="isEdit ? '编辑知识点' : '新增知识点'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入知识点名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入知识点描述" />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 100%">
            <el-option label="简单 (1)" :value="1" />
            <el-option label="中等 (2)" :value="2" />
            <el-option label="困难 (3)" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="章节" prop="chapter">
          <el-input v-model="form.chapter" placeholder="请输入章节" />
        </el-form-item>
        <el-form-item label="预计时长(分)" prop="expectedMinutes">
          <el-input-number v-model="form.expectedMinutes" :min="1" :max="240" style="width: 100%" />
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
import { getNodeList, createNode, updateNode, deleteNode, getCourseList } from '../../api/knowledge'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const tableData = ref([])
const courseList = ref([])

const filterForm = reactive({
  courseId: null,
  name: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  courseId: null,
  name: '',
  description: '',
  difficulty: 1,
  chapter: '',
  expectedMinutes: 30
})

const rules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  name: [{ required: true, message: '请输入知识点名称', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  expectedMinutes: [{ required: true, message: '请输入预计时长', trigger: 'blur' }]
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
    const params = {}
    if (filterForm.courseId) params.courseId = filterForm.courseId
    if (filterForm.name) params.name = filterForm.name
    const res = await getNodeList(params)
    if (Array.isArray(res)) {
      tableData.value = res
      pagination.total = res.length
    } else if (res && res.records) {
      tableData.value = res.records
      pagination.total = res.total || 0
    }
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 无

const resetFilter = () => {
  filterForm.courseId = null
  filterForm.name = ''
  pagination.page = 1
  loadData()
}

const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, courseId: null, name: '', description: '', difficulty: 1, chapter: '', expectedMinutes: 30 })
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    courseId: row.courseId || null,
    name: row.name || '',
    description: row.description || '',
    difficulty: row.difficulty || 1,
    chapter: row.chapter || '',
    expectedMinutes: row.expectedMinutes || 30
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const data = {
      courseId: form.courseId,
      name: form.name,
      description: form.description,
      difficulty: form.difficulty,
      chapter: form.chapter,
      expectedMinutes: form.expectedMinutes
    }
    if (isEdit.value) {
      await updateNode(form.id, data)
      ElMessage.success('编辑成功')
    } else {
      await createNode(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch { } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除知识点「${row.name}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteNode(row.id)
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
.page-container { padding: 20px 24px; background: #faf7f2; min-height: 100vh; }
</style>
