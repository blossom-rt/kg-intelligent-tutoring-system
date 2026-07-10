<template>
  <div class="page-container">
    <StudentHeader title="跨学科主题管理">
      <template #actions>
        <el-button type="primary" @click="openAdd">新增主题</el-button>
      </template>
    </StudentHeader>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" @submit.prevent>
        <el-form-item label="难度">
          <el-select v-model="filterForm.difficulty" placeholder="全部难度" clearable style="width:180px" @change="loadData">
            <el-option label="简单 (1)" :value="1" />
            <el-option label="中等 (2)" :value="2" />
            <el-option label="困难 (3)" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width:180px" @change="loadData">
            <el-option label="已发布" value="1" />
            <el-option label="已下架" value="0" />
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
        <el-table-column prop="themeName" label="主题名称" min-width="180" />
        <el-table-column prop="difficulty" label="难度" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="diffTag(row.difficulty)" size="small">{{ diffLabel(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布教师" min-width="120" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '发布' }}
            </el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑主题' : '新增主题'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="主题名称" prop="themeName">
          <el-input v-model="form.themeName" placeholder="请输入主题名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入主题描述" />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 100%">
            <el-option label="简单 (1)" :value="1" />
            <el-option label="中等 (2)" :value="2" />
            <el-option label="困难 (3)" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联知识点" prop="nodeIds">
          <el-select
            v-model="form.nodeIds"
            placeholder="请选择关联知识点（可多选）"
            style="width: 100%"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            :max-collapse-tags="3"
          >
            <el-option v-for="n in nodeList" :key="n.id" :label="n.name" :value="n.id" />
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
import StudentHeader from '../../components/StudentHeader.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getThemeList, createTheme, updateTheme, deleteTheme, toggleThemeStatus } from '../../api/admin'
import { getNodeList } from '../../api/knowledge'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const tableData = ref([])
const nodeList = ref([])

const filterForm = reactive({
  difficulty: null,
  status: null
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
  id: null,
  themeName: '',
  description: '',
  difficulty: 1,
  nodeIds: []
})

const rules = {
  themeName: [{ required: true, message: '请输入主题名称', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
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
    if (filterForm.difficulty) params.difficulty = filterForm.difficulty
    if (filterForm.status !== null && filterForm.status !== '') params.status = filterForm.status
    const res = await getThemeList(params)
    if (Array.isArray(res)) { tableData.value = res; pagination.total = res.length }
    else if (res && res.records) { tableData.value = res.records; pagination.total = res.total || 0 }
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  filterForm.difficulty = null
  filterForm.status = null
  pagination.page = 1
  loadData()
}

const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, themeName: '', description: '', difficulty: 1, nodeIds: [] })
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    themeName: row.themeName || '',
    description: row.description || '',
    difficulty: row.difficulty || 1,
    nodeIds: row.nodeIds || row.nodes || []
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const data = {
      themeName: form.themeName,
      description: form.description,
      difficulty: form.difficulty,
      nodeIds: form.nodeIds
    }
    if (isEdit.value) {
      await updateTheme(form.id, data)
      ElMessage.success('编辑成功')
    } else {
      await createTheme(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch { } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '下架' : '发布'
  ElMessageBox.confirm(`确定要${action}该主题吗？`, `${action}确认`, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await toggleThemeStatus(row.id, { status: newStatus })
      ElMessage.success(`${action}成功`)
      loadData()
    } catch { }
  }).catch(() => { })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除主题「${row.themeName}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteTheme(row.id)
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
.page-container { padding: 20px 24px; background: var(--bg-root); min-height: 100vh; }
</style>
