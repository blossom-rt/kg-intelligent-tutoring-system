<template>
  <div class="page-container">
    <StudentHeader title="依赖边管理">
      <template #actions>
        <el-button type="primary" @click="openAddEdge">添加依赖</el-button>
      </template>
    </StudentHeader>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" @submit.prevent>
        <el-form-item label="所属课程">
          <el-select v-model="filterForm.courseId" placeholder="全部课程" clearable @change="onCourseChange" style="width:200px">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="split-panels">
      <!-- 左：知识点列表 -->
      <el-card class="panel-left">
        <template #header><span class="panel-title">知识点列表</span></template>
        <div class="node-list">
          <div
            v-for="node in nodeList"
            :key="node.id"
            class="node-chip"
            :class="{ 'node-selected': selectedNode === node.id }"
            @click="selectedNode = node.id"
          >
            <el-tag :type="diffTag(node.difficulty)" size="small" effect="dark">{{ diffLabel(node.difficulty) }}</el-tag>
            <span class="node-name">{{ node.name }}</span>
          </div>
          <el-empty v-if="nodeList.length === 0" description="暂无知识点" :image-size="60" />
        </div>
      </el-card>

      <!-- 右：依赖边列表 -->
      <el-card class="panel-right">
        <template #header><span class="panel-title">依赖边列表（{{ edgeList.length }} 条）</span></template>
        <el-table :data="paginatedEdgeList" v-loading="loading" size="small" max-height="500" stripe border>
          <el-table-column label="前置节点" min-width="140">
            <template #default="{ row }">
              <span>{{ nodeName(row.fromNodeId) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60" align="center">
            <template #default>
              <el-tag type="info" size="small">→</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="后继节点" min-width="140">
            <template #default="{ row }">
              <span>{{ nodeName(row.toNodeId) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next, sizes"
          :page-sizes="[5, 10, 20, 50]"
          style="margin-top: 16px; justify-content: flex-end"
        />
      </el-card>
    </div>

    <!-- 添加依赖弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="添加依赖边"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="edgeRules" label-width="100px">
        <el-form-item label="前置节点" prop="fromNodeId">
          <el-select v-model="form.fromNodeId" placeholder="请选择前置节点" style="width: 100%" filterable>
            <el-option v-for="n in nodeList" :key="n.id" :label="n.name" :value="n.id"
              :disabled="n.id === form.toNodeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="后继节点" prop="toNodeId">
          <el-select v-model="form.toNodeId" placeholder="请选择后继节点" style="width: 100%" filterable>
            <el-option v-for="n in nodeList" :key="n.id" :label="n.name" :value="n.id"
              :disabled="n.id === form.fromNodeId" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import StudentHeader from '../../components/StudentHeader.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEdgeList, createEdge, deleteEdge, getNodeList, getCourseList } from '../../api/knowledge'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const edgeList = ref([])

const pagination = reactive({ page: 1, size: 10, total: 0 })
const paginatedEdgeList = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return edgeList.value.slice(start, start + pagination.size)
})

const nodeList = ref([])
const courseList = ref([])
const selectedNode = ref(null)

const filterForm = reactive({
  courseId: null
})

const form = reactive({
  fromNodeId: null,
  toNodeId: null
})

const edgeRules = {
  fromNodeId: [{ required: true, message: '请选择前置节点', trigger: 'change' }],
  toNodeId: [{ required: true, message: '请选择后继节点', trigger: 'change' }]
}

const diffLabel = (val) => {
  if (val === 1) return '简'
  if (val === 2) return '中'
  if (val === 3) return '难'
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

const nodeName = (id) => {
  const n = nodeList.value.find(n => n.id === id)
  return n ? n.name : ('节点-' + id)
}

const loadData = async () => {
  loading.value = true
  try {
    const [edgeRes, nodeRes] = await Promise.all([
      getEdgeList(),
      getNodeList({ page: 1, size: 999, ...(filterForm.courseId ? { courseId: filterForm.courseId } : {}) })
    ])
    nodeList.value = Array.isArray(nodeRes) ? nodeRes : (nodeRes.records || nodeRes.data || [])
    // 过滤只保留当前课程节点间的边
    const allEdges = Array.isArray(edgeRes) ? edgeRes : (edgeRes.records || edgeRes.data || [])
    const nodeIds = new Set(nodeList.value.map(n => n.id))
    edgeList.value = allEdges.filter(e => nodeIds.has(e.fromNodeId) && nodeIds.has(e.toNodeId))
    pagination.total = edgeList.value.length
  } catch {
    edgeList.value = []
    nodeList.value = []
  } finally {
    loading.value = false
  }
}

const onCourseChange = () => {
  selectedNode.value = null
  loadData()
}

const openAddEdge = () => {
  if (nodeList.value.length < 2) {
    ElMessage.warning('当前课程下知识点不足，请至少拥有2个知识点')
    return
  }
  Object.assign(form, { fromNodeId: null, toNodeId: null })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await createEdge({
      fromNodeId: form.fromNodeId,
      toNodeId: form.toNodeId
    })
    ElMessage.success('依赖边创建成功')
    dialogVisible.value = false
    loadData()
  } catch { } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除该依赖边吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteEdge(row.id)
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
.page-container { padding: 20px 24px; background: var(--bg-root); min-height: 100vh; }
.node-chip:hover { background: #dce4f0; }
.node-chip.node-selected { background: rgba(255,123,61,0.1); box-shadow: 0 0 0 2px var(--accent) inset; }
.node-name { font-size: 13px; color: var(--text-secondary); }
</style>
