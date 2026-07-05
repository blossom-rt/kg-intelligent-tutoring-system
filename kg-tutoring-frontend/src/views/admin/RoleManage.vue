<template>
  <div class="role-manage">
    <StudentHeader title="角色管理" />

    <el-card class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" @click="openAddDialog">新增角色</el-button>
      </div>
      <el-table :data="roleList" v-loading="loading" border stripe>
        <el-table-column prop="roleCode" label="角色标识" min-width="120" />
        <el-table-column prop="roleName" label="角色名称" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
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
        <el-form-item label="角色标识" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色标识" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
import { getRoleList, createRole, updateRole, deleteRole } from '../../api/admin'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const roleList = ref([])

const dialogTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')

const form = reactive({
  roleCode: '',
  roleName: '',
  description: ''
})

const rules = {
  roleCode: [{ required: true, message: '请输入角色标识', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

onMounted(() => {
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getRoleList()
    roleList.value = res || []
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
  form.roleCode = row.roleCode
  form.roleName = row.roleName
  form.description = row.description || ''
  dialogVisible.value = true
}

function resetForm() {
  form.roleCode = ''
  form.roleName = ''
  form.description = ''
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      roleCode: form.roleCode,
      roleName: form.roleName,
      description: form.description
    }
    if (!isEdit.value) {
      await createRole(data)
      ElMessage.success('新增角色成功')
    } else {
      await updateRole(editingId.value, data)
      ElMessage.success('更新角色成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch { /* ignore */ } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除角色「${row.roleName}」吗？此操作不可恢复。`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch { /* ignore */ }
  }).catch(() => { /* 取消 */ })
}
</script>

<style scoped>
.role-manage { padding: 24px 36px; background: var(--bg-root); min-height: 100vh; }
.page-header h2 { margin: 0 0 16px; font-size: 20px; color: var(--text-primary); }
.table-card { margin-bottom: 16px; }
.table-toolbar { margin-bottom: 16px; }
</style>
