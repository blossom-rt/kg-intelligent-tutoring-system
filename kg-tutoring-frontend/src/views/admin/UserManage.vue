<template>
  <div class="user-manage">
    <div class="page-header">
      <h2>用户账号管理</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名/姓名">
          <el-input v-model="searchForm.keyword" placeholder="请输入用户名或姓名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.roleId" placeholder="全部角色" clearable style="width: 150px">
            <el-option v-for="r in roleList" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" @click="openAddDialog">新增用户</el-button>
      </div>
      <el-table :data="userList" v-loading="loading" border stripe>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="角色" min-width="100">
          <template #default="{ row }">
            <span>{{ roleMap[row.roleId] || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" min-width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="fetchList"
        @size-change="fetchList"
        class="pagination"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roleList" :key="r.id" :label="r.roleName" :value="r.id" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, toggleUserStatus, getRoleList } from '../../api/admin'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const userList = ref([])
const roleList = ref([])

const roleMap = computed(() => {
  const map = {}
  roleList.value.forEach(r => { map[r.id] = r.roleName })
  return map
})

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

const searchForm = reactive({
  keyword: '',
  roleId: ''
})

const form = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  roleId: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于6位', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

onMounted(() => {
  fetchRoles()
  fetchList()
})

async function fetchRoles() {
  try {
    const res = await getRoleList()
    roleList.value = res || []
  } catch { /* ignore */ }
}

async function fetchList() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.roleId) params.roleId = searchForm.roleId
    const res = await getUserList(params)
    if (Array.isArray(res)) {
      userList.value = res
      pagination.total = res.length
    } else if (res && res.records) {
      userList.value = res.records
      pagination.total = res.total || 0
    }
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchList()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.roleId = ''
  pagination.page = 1
  fetchList()
}

function openAddDialog() {
  isEdit.value = false
  editingId.value = null
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  editingId.value = row.id
  form.username = row.username
  form.password = ''
  form.realName = row.realName
  form.email = row.email
  form.roleId = row.roleId
  dialogVisible.value = true
}

function resetForm() {
  form.username = ''
  form.password = ''
  form.realName = ''
  form.email = ''
  form.roleId = ''
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      username: form.username,
      realName: form.realName,
      email: form.email,
      roleId: form.roleId
    }
    if (!isEdit.value) {
      data.password = form.password
      await createUser(data)
      ElMessage.success('新增用户成功')
    } else {
      if (form.password) data.password = form.password
      await updateUser(editingId.value, data)
      ElMessage.success('更新用户成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch { /* ignore */ } finally {
    submitLoading.value = false
  }
}

function handleToggleStatus(row) {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}用户「${row.username}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await toggleUserStatus(row.id, { status: row.status === 1 ? 0 : 1 })
      ElMessage.success(`${action}成功`)
      fetchList()
    } catch { /* ignore */ }
  }).catch(() => { /* 取消 */ })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除用户「${row.username}」吗？此操作不可恢复。`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch { /* ignore */ }
  }).catch(() => { /* 取消 */ })
}
</script>

<style scoped>
.user-manage { padding: 20px; background: #faf7f2; min-height: 100vh; }
.page-header h2 { margin: 0 0 16px; font-size: 20px; color: #2d2a26; }
.search-card { margin-bottom: 16px; }
.table-card { margin-bottom: 16px; }
.table-toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
