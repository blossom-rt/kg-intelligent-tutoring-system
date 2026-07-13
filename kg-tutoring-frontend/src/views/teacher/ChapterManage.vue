<template>
  <div class="page-container">
    <StudentHeader title="章节管理">
      <template #actions>
        <el-button type="primary" @click="openAdd">新增章节</el-button>
      </template>
    </StudentHeader>

    <el-card class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="所属课程">
          <el-select v-model="filterForm.courseId" placeholder="请选择课程" clearable @change="loadData" style="width:240px">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="tableData" v-loading="loading" stripe border row-key="id">
        <el-table-column label="排序" width="80" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" :disabled="row.sort <= 1" @click="moveUp(row)">↑</el-button>
              <el-button size="small" @click="moveDown(row)">↓</el-button>
            </el-button-group>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="序号" width="60" align="center" />
        <el-table-column prop="chapterName" label="章节名称" min-width="160" />
        <el-table-column prop="description" label="教学目标" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑章节' : '新增章节'" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width:100%">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="章节名称" prop="chapterName">
          <el-input v-model="form.chapterName" placeholder="例如：第一章 有理数" />
        </el-form-item>
        <el-form-item label="排序号" prop="sort">
          <el-input-number v-model="form.sort" :min="1" :max="99" style="width:100%" />
        </el-form-item>
        <el-form-item label="教学目标" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="本章节的教学目标" />
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
import StudentHeader from '../../components/StudentHeader.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getChapterList, createChapter, updateChapter, updateChapterSort, deleteChapter, getCourseList } from '../../api/knowledge'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const tableData = ref([])
const courseList = ref([])

const filterForm = reactive({ courseId: null })
const form = reactive({ id: null, courseId: null, chapterName: '', sort: 1, description: '' })
const rules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  chapterName: [{ required: true, message: '请输入章节名称', trigger: 'blur' }]
}

const loadCourses = async () => {
  try {
    const res = await getCourseList({ page: 1, size: 999 })
    courseList.value = Array.isArray(res) ? res : (res.records || res.data || [])
  } catch { courseList.value = [] }
}

const loadData = async () => {
  if (!filterForm.courseId) { tableData.value = []; return }
  loading.value = true
  try {
    const res = await getChapterList({ courseId: filterForm.courseId })
    let list = Array.isArray(res) ? res : []
    // 如果所有章节序号都相同（迁移默认都是1），自动重排
    const sorts = new Set(list.map(r => r.sort))
    if (sorts.size <= 1 && list.length > 1) {
      list = list.map((r, i) => ({ ...r, sort: i + 1 }))
    }
    tableData.value = list
  } catch { tableData.value = [] }
  finally { loading.value = false }
}

const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, courseId: filterForm.courseId || null, chapterName: '', sort: tableData.value.length + 1, description: '' })
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { id: row.id, courseId: row.courseId, chapterName: row.chapterName, sort: row.sort, description: row.description || '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const data = { courseId: form.courseId, chapterName: form.chapterName, sort: form.sort, description: form.description }
    if (isEdit.value) { await updateChapter(form.id, data); ElMessage.success('编辑成功') }
    else { await createChapter(data); ElMessage.success('新增成功') }
    dialogVisible.value = false
    loadData()
  } catch { } finally { submitLoading.value = false }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除章节「${row.chapterName}」吗？`, '删除确认', { type: 'warning' })
  try { await deleteChapter(row.id); ElMessage.success('删除成功'); loadData() } catch { }
}

const moveUp = (row) => {
  const idx = tableData.value.findIndex(r => r.id === row.id)
  if (idx <= 0) return;
  [tableData.value[idx - 1], tableData.value[idx]] = [tableData.value[idx], tableData.value[idx - 1]]
  tableData.value.forEach((r, i) => { r.sort = i + 1 })
  updateChapterSort(row.id, { sort: row.sort - 1 }).catch(() => {})
}

const moveDown = (row) => {
  const idx = tableData.value.findIndex(r => r.id === row.id)
  if (idx < 0 || idx >= tableData.value.length - 1) return;
  [tableData.value[idx], tableData.value[idx + 1]] = [tableData.value[idx + 1], tableData.value[idx]]
  tableData.value.forEach((r, i) => { r.sort = i + 1 })
  updateChapterSort(row.id, { sort: row.sort + 1 }).catch(() => {})
}

onMounted(() => { loadCourses() })
</script>

<style scoped>
.page-container { padding: 20px 24px; background: var(--bg-root); min-height: 100vh; }
.filter-card { margin-bottom: 16px; }
</style>
