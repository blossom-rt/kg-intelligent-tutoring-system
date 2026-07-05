<template>
  <div class="notice-manage">
    <StudentHeader title="系统公告管理" />

    <el-card class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" @click="openAddDialog">新增公告</el-button>
      </div>
      <el-table :data="paginatedNoticeList" v-loading="loading" border stripe>
        <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip />
        <el-table-column label="推送对象" min-width="120">
          <template #default="{ row }">
            <el-tag :type="tagType(row.targetRole)" size="small">
              {{ targetLabel(row.targetRole) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" min-width="160" />
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
      width="560px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="推送对象" prop="targetRole">
          <el-select v-model="form.targetRole" placeholder="请选择推送对象" style="width: 100%">
            <el-option label="全部" value="all" />
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入公告内容" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoticeList, createNotice, updateNotice, deleteNotice } from '../../api/admin'
import StudentHeader from '../../components/StudentHeader.vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const noticeList = ref([])
const paginatedNoticeList = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return noticeList.value.slice(start, start + pagination.pageSize)
})

const dialogTitle = computed(() => isEdit.value ? '编辑公告' : '新增公告')

const form = reactive({
  title: '',
  content: '',
  targetRole: 'all'
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
  targetRole: [{ required: true, message: '请选择推送对象', trigger: 'change' }]
}

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

function targetLabel(role) {
  const map = { all: '全部', student: '学生', teacher: '教师' }
  return map[role] || role
}

function tagType(role) {
  const map = { all: 'info', student: 'success', teacher: 'warning' }
  return map[role] || 'info'
}

onMounted(() => {
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getNoticeList()
    if (Array.isArray(res)) {
      noticeList.value = res
      pagination.total = res.length
    } else if (res && res.records) {
      noticeList.value = res.records
      pagination.total = res.total || 0
    }
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
  form.title = row.title
  form.content = row.content
  form.targetRole = row.targetRole || 'all'
  dialogVisible.value = true
}

function resetForm() {
  form.title = ''
  form.content = ''
  form.targetRole = 'all'
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      title: form.title,
      content: form.content,
      targetRole: form.targetRole
    }
    if (!isEdit.value) {
      await createNotice(data)
      ElMessage.success('新增公告成功')
    } else {
      await updateNotice(editingId.value, data)
      ElMessage.success('更新公告成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch { /* ignore */ } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除公告「${row.title}」吗？此操作不可恢复。`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteNotice(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch { /* ignore */ }
  }).catch(() => { /* 取消 */ })
}
</script>

<style scoped>
.notice-manage { padding: 20px; background: #f5f7fa; min-height: 100vh; }
.page-header h2 { margin: 0 0 16px; font-size: 20px; color: #2d2a26; }
.table-card { margin-bottom: 16px; }
.table-toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
