<template>
  <div class="course-manage">
    <StudentHeader title="课程管理" />

    <el-card class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" @click="openAddDialog">新增课程</el-button>
      </div>
      <el-table :data="paginatedCourseList" v-loading="loading" border stripe>
        <el-table-column prop="courseName" label="课程名称" min-width="180" />
        <el-table-column prop="subject" label="所属学科" min-width="140" />
        <el-table-column prop="teacherId" label="教师ID" min-width="120" />
        <el-table-column label="状态" min-width="80">
          <template #default>
            <el-tag type="success" size="small">启用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, prev, pager, next"
        class="pagination"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="480px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="所属学科" prop="subject">
          <el-input v-model="form.subject" placeholder="如：数学、物理、信息技术" />
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-input v-model="form.teacherId" placeholder="请输入教师ID" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
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
import StudentHeader from '../../components/StudentHeader.vue'
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourseList, createCourse, updateCourse, deleteCourse } from '../../api/admin'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const courseList = ref([])
const paginatedCourseList = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return courseList.value.slice(start, start + pagination.pageSize)
})

const dialogTitle = computed(() => isEdit.value ? '编辑课程' : '新增课程')

const form = reactive({
  courseName: '',
  subject: '',
  teacherId: '',
  description: ''
})

const rules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  courseCode: [{ required: true, message: '请输入课程编码', trigger: 'blur' }]
}

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

onMounted(() => {
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    const res = await getCourseList(params)
    if (Array.isArray(res)) { courseList.value = res; pagination.total = res.length }
    else if (res && res.records) { courseList.value = res.records; pagination.total = res.total || 0 }
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

function openAddDialog() {
  isEdit.value = false
  editingId.value = null
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  editingId.value = row.id
  form.courseName = row.courseName
  form.subject = row.subject || ''
  form.courseCode = row.courseCode
  form.teacherId = row.teacherId || ''
  form.description = row.description || ''
  dialogVisible.value = true
}

function resetForm() {
  form.courseName = ''
  form.subject = ''
  form.teacherId = ''
  form.description = ''
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      courseName: form.courseName,
      subject: form.subject,
      teacherId: form.teacherId,
      description: form.description
    }
    if (!isEdit.value) {
      await createCourse(data)
      ElMessage.success('新增课程成功')
    } else {
      await updateCourse(editingId.value, data)
      ElMessage.success('更新课程成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch { /* ignore */ } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除课程「${row.courseName}」吗？此操作不可恢复。`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCourse(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch { /* ignore */ }
  }).catch(() => { /* 取消 */ })
}
</script>

<style scoped>
.course-manage { padding: 24px 36px; background: var(--bg-root); min-height: 100vh; }
.page-header h2 { margin: 0 0 16px; font-size: 20px; color: var(--text-primary); }
.table-card { margin-bottom: 16px; }
.table-toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
