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
          <el-select v-model="filterForm.courseId" placeholder="全部课程" clearable style="width:200px" @change="onFilterCourseChange">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属章节">
          <el-select v-model="filterForm.chapterId" placeholder="全部章节" clearable style="width:200px" @change="loadData" :disabled="!filterForm.courseId">
            <el-option v-for="ch in filterChapterList" :key="ch.id" :label="ch.chapterName" :value="ch.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点名称">
          <el-input v-model="filterForm.name" placeholder="输入关键词搜索" clearable @clear="loadData" @keyup.enter="loadData" style="width:180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="paginatedTableData" v-loading="loading" stripe border>
        <el-table-column prop="name" label="知识点名称" min-width="160" />
        <el-table-column prop="courseName" label="课程" width="120" />
        <el-table-column prop="chapterName" label="章节" width="140" />
        <el-table-column prop="difficulty" label="难度" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="diffTag(row.difficulty)" size="small">{{ diffLabel(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nodeType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ nodeTypeLabel(row.nodeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expectedMinutes" label="预计时长(分)" width="120" align="center" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button type="success" size="small" link @click="openResourceDialog(row)">资源</el-button>
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
      :title="isEdit ? '编辑知识点' : '新增知识点'"
      width="620px"
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
        <el-form-item label="节点类型" prop="nodeType">
          <el-select v-model="form.nodeType" placeholder="请选择节点类型" style="width: 100%">
            <el-option label="概念理解" value="concept" />
            <el-option label="方法技能" value="skill" />
            <el-option label="应用实践" value="application" />
          </el-select>
        </el-form-item>
        <el-form-item label="学习目标" prop="learningGoal">
          <el-input v-model="form.learningGoal" maxlength="200" show-word-limit placeholder="学生学完后应能做到什么" />
        </el-form-item>
        <el-form-item label="关键词" prop="keywords">
          <el-input v-model="form.keywords" maxlength="200" placeholder="例如：数轴、绝对值、有理数运算" />
        </el-form-item>
        <el-form-item label="例题提示" prop="exampleHint">
          <el-input v-model="form.exampleHint" type="textarea" :rows="2" maxlength="300" show-word-limit placeholder="可填写一个典型题型或学习提醒" />
        </el-form-item>
        <el-form-item label="所属章节" prop="chapterId">
          <el-select v-model="form.chapterId" placeholder="请先选择课程" style="width:100%" :disabled="!form.courseId" filterable>
            <el-option v-for="ch in formChapterList" :key="ch.id" :label="ch.chapterName" :value="ch.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 100%">
            <el-option label="简单 (1)" :value="1" />
            <el-option label="中等 (2)" :value="2" />
            <el-option label="困难 (3)" :value="3" />
          </el-select>
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

    <!-- 学习资源维护弹窗 -->
    <el-dialog
      v-model="resourceDialogVisible"
      :title="`学习资源 - ${currentNode?.name || ''}`"
      width="760px"
      :close-on-click-modal="false"
    >
      <el-form ref="resourceFormRef" :model="resourceForm" :rules="resourceRules" label-width="92px" class="resource-form">
        <el-form-item label="资源标题" prop="title">
          <el-input v-model="resourceForm.title" placeholder="例如：有理数运算微课" />
        </el-form-item>
        <el-form-item label="资源链接" prop="url">
          <el-input v-model="resourceForm.url" placeholder="可填写 mp4 直链、B站/慕课/学校资源站链接" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="类型" prop="resourceType">
              <el-select v-model="resourceForm.resourceType" style="width: 100%">
                <el-option label="视频" value="video" />
                <el-option label="文章" value="article" />
                <el-option label="文档" value="pdf" />
                <el-option label="链接" value="link" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源">
              <el-select v-model="resourceForm.source" style="width: 100%">
                <el-option label="自定义" value="custom" />
                <el-option label="Bilibili" value="bilibili" />
                <el-option label="慕课" value="mooc" />
                <el-option label="本地/校内" value="local" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch v-model="resourceForm.enabled" active-text="启用" inactive-text="停用" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="排序">
              <el-input-number v-model="resourceForm.sortOrder" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="时长(秒)">
              <el-input-number v-model="resourceForm.durationSeconds" :min="0" :max="86400" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="封面">
              <el-input v-model="resourceForm.coverUrl" placeholder="可选" />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="resource-actions">
          <el-button @click="resetResourceForm">清空</el-button>
          <el-button type="primary" :loading="resourceSubmitLoading" @click="submitResource">
            {{ resourceForm.id ? '保存修改' : '新增资源' }}
          </el-button>
        </div>
      </el-form>

      <el-divider />

      <el-table :data="resourceList" v-loading="resourceLoading" border stripe>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="resourceType" label="类型" width="80" align="center">
          <template #default="{ row }">{{ resourceTypeLabel(row.resourceType) }}</template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="链接" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="editResource(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="deleteResource(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import StudentHeader from '../../components/StudentHeader.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getNodeList,
  createNode,
  updateNode,
  deleteNode,
  getCourseList,
  getChapterList,
  getNodeResources,
  createNodeResource,
  updateNodeResource,
  deleteNodeResource
} from '../../api/knowledge'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const resourceFormRef = ref(null)
const tableData = ref([])
const courseList = ref([])
const resourceList = ref([])
const currentNode = ref(null)
const resourceDialogVisible = ref(false)
const resourceLoading = ref(false)
const resourceSubmitLoading = ref(false)

const filterChapterList = ref([])
const formChapterList = ref([])

const filterForm = reactive({
  courseId: null,
  chapterId: null,
  name: ''
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
  courseId: null,
  chapterId: null,
  name: '',
  description: '',
  nodeType: 'concept',
  learningGoal: '',
  keywords: '',
  exampleHint: '',
  difficulty: 1,
  expectedMinutes: 30
})

const resourceForm = reactive({
  id: null,
  title: '',
  resourceType: 'video',
  url: '',
  coverUrl: '',
  durationSeconds: 0,
  source: 'custom',
  sortOrder: 0,
  enabled: true
})

const rules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  name: [{ required: true, message: '请输入知识点名称', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  expectedMinutes: [{ required: true, message: '请输入预计时长', trigger: 'blur' }]
}

const resourceRules = {
  title: [{ required: true, message: '请输入资源标题', trigger: 'blur' }],
  url: [{ required: true, message: '请输入资源链接', trigger: 'blur' }],
  resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }]
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

const nodeTypeLabel = (val) => {
  const map = { concept: '概念', skill: '技能', application: '应用' }
  return map[val] || '概念'
}

const resourceTypeLabel = (val) => {
  const map = { video: '视频', article: '文章', pdf: '文档', link: '链接' }
  return map[val] || '资源'
}

const loadCourses = async () => {
  try {
    const res = await getCourseList({ page: 1, size: 999 })
    courseList.value = Array.isArray(res) ? res : (res.records || res.data || [])
  } catch {
    courseList.value = []
  }
}

const loadChapterList = async (courseId) => {
  if (!courseId) { filterChapterList.value = []; formChapterList.value = []; return }
  try {
    const res = await getChapterList({ courseId })
    filterChapterList.value = Array.isArray(res) ? res : []
    formChapterList.value = Array.isArray(res) ? res : []
  } catch { filterChapterList.value = []; formChapterList.value = [] }
}

const onFilterCourseChange = () => {
  filterForm.chapterId = null
  loadChapterList(filterForm.courseId)
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterForm.courseId) params.courseId = filterForm.courseId
    if (filterForm.chapterId) params.chapterId = filterForm.chapterId
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

const resetFilter = () => {
  filterForm.courseId = null
  filterForm.chapterId = null
  filterForm.name = ''
  filterChapterList.value = []
  pagination.page = 1
  loadData()
}

const openAdd = () => {
  isEdit.value = false
  formChapterList.value = []
  Object.assign(form, {
    id: null,
    courseId: null,
    chapterId: null,
    name: '',
    description: '',
    nodeType: 'concept',
    learningGoal: '',
    keywords: '',
    exampleHint: '',
    difficulty: 1,
    expectedMinutes: 30
  })
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  if (row.courseId) loadChapterList(row.courseId)
  Object.assign(form, {
    id: row.id,
    courseId: row.courseId || null,
    chapterId: row.chapterId || null,
    name: row.name || '',
    description: row.description || '',
    nodeType: row.nodeType || 'concept',
    learningGoal: row.learningGoal || '',
    keywords: row.keywords || '',
    exampleHint: row.exampleHint || '',
    difficulty: row.difficulty || 1,
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
      chapterId: form.chapterId,
      name: form.name,
      description: form.description,
      nodeType: form.nodeType,
      learningGoal: form.learningGoal,
      keywords: form.keywords,
      exampleHint: form.exampleHint,
      difficulty: form.difficulty,
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

const resetResourceForm = () => {
  Object.assign(resourceForm, {
    id: null,
    title: '',
    resourceType: 'video',
    url: '',
    coverUrl: '',
    durationSeconds: 0,
    source: 'custom',
    sortOrder: 0,
    enabled: true
  })
  resourceFormRef.value?.clearValidate()
}

const loadResources = async () => {
  if (!currentNode.value?.id) return
  resourceLoading.value = true
  try {
    const res = await getNodeResources(currentNode.value.id, { onlyEnabled: false })
    resourceList.value = Array.isArray(res) ? res : []
  } catch {
    resourceList.value = []
  } finally {
    resourceLoading.value = false
  }
}

const openResourceDialog = async (row) => {
  currentNode.value = row
  resourceDialogVisible.value = true
  resetResourceForm()
  await loadResources()
}

const editResource = (row) => {
  Object.assign(resourceForm, {
    id: row.id,
    title: row.title || '',
    resourceType: row.resourceType || 'video',
    url: row.url || '',
    coverUrl: row.coverUrl || '',
    durationSeconds: row.durationSeconds || 0,
    source: row.source || 'custom',
    sortOrder: row.sortOrder || 0,
    enabled: row.status !== 0
  })
}

const submitResource = async () => {
  if (!currentNode.value?.id) return
  const valid = await resourceFormRef.value.validate().catch(() => false)
  if (!valid) return
  resourceSubmitLoading.value = true
  const data = {
    title: resourceForm.title,
    resourceType: resourceForm.resourceType,
    url: resourceForm.url,
    coverUrl: resourceForm.coverUrl,
    durationSeconds: resourceForm.durationSeconds || null,
    source: resourceForm.source,
    sortOrder: resourceForm.sortOrder,
    status: resourceForm.enabled ? 1 : 0
  }
  try {
    if (resourceForm.id) {
      await updateNodeResource(resourceForm.id, data)
      ElMessage.success('资源已更新')
    } else {
      await createNodeResource(currentNode.value.id, data)
      ElMessage.success('资源已新增')
    }
    resetResourceForm()
    loadResources()
  } catch { } finally {
    resourceSubmitLoading.value = false
  }
}

const deleteResource = (row) => {
  ElMessageBox.confirm(`确定要删除资源「${row.title}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteNodeResource(row.id)
      ElMessage.success('删除成功')
      loadResources()
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
.resource-form {
  padding-top: 4px;
}
.resource-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
