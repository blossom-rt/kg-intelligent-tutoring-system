<template>
  <div class="page-container">
    <StudentHeader title="图谱编辑">
      <template #actions>
        <el-button type="primary" @click="openAddEdge">添加依赖</el-button>
      </template>
    </StudentHeader>

    <!-- 筛选 + 编辑模式切换 -->
    <el-card class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="所属课程">
          <el-select v-model="filterForm.courseId" placeholder="全部课程" clearable @change="onCourseChange" style="width:200px">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </el-form-item>
        <el-form-item style="margin-left:20px">
          <el-switch v-model="isEditMode" active-text="编辑模式" inactive-text="浏览模式" @change="onEditModeChange" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 编辑模式提示条 -->
    <div v-if="isEditMode" class="edit-status-bar" :class="{ 'has-source': !!sourceNode, 'edge-created': edgeCreated }">
      <template v-if="edgeCreated">
        <el-icon color="#67c23a"><CircleCheck /></el-icon> 依赖关系已添加！
      </template>
      <template v-else-if="!sourceNode">
        <el-icon color="#ff7b3d"><Warning /></el-icon> 请点击图谱中的一个节点作为「前置知识点」
      </template>
      <template v-else>
        <el-icon color="#409eff"><Connection /></el-icon>
        已选前置：<strong>{{ sourceNode.name }}</strong>，请点击第二个节点作为「后置知识点」
        <el-button size="small" text @click="cancelEdit" style="margin-left:12px">取消</el-button>
      </template>
    </div>

    <!-- 知识图谱 SVG -->
    <el-card class="graph-card" v-loading="loading">
      <div ref="chartRef" class="chart-container"></div>
      <el-empty v-if="!loading && nodeList.length === 0" description="暂无知识点数据" />
    </el-card>

    <!-- 下方：知识点列表 + 依赖边表格 -->
    <div class="split-panels">
      <el-card class="panel-left">
        <template #header><span class="panel-title">知识点列表（{{ nodeList.length }}）</span></template>
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
    <el-dialog v-model="dialogVisible" title="添加依赖边" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="edgeRules" label-width="100px">
        <el-form-item label="前置节点" prop="fromNodeId">
          <el-select v-model="form.fromNodeId" placeholder="请选择前置节点" style="width:100%" filterable>
            <el-option v-for="n in nodeList" :key="n.id" :label="n.name" :value="n.id"
              :disabled="n.id === form.toNodeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="后继节点" prop="toNodeId">
          <el-select v-model="form.toNodeId" placeholder="请选择后继节点" style="width:100%" filterable>
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
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import * as d3 from 'd3'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning, CircleCheck, Connection } from '@element-plus/icons-vue'
import StudentHeader from '../../components/StudentHeader.vue'
import { getEdgeList, createEdge, deleteEdge, getNodeList, getCourseList } from '../../api/knowledge'

// ─── 状态 ───
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const edgeList = ref([])
const nodeList = ref([])
const courseList = ref([])
const selectedNode = ref(null)
const chartRef = ref(null)

const filterForm = reactive({ courseId: null })
const form = reactive({ fromNodeId: null, toNodeId: null })

const edgeRules = {
  fromNodeId: [{ required: true, message: '请选择前置节点', trigger: 'change' }],
  toNodeId: [{ required: true, message: '请选择后继节点', trigger: 'change' }]
}

const pagination = reactive({ page: 1, size: 10, total: 0 })
const paginatedEdgeList = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return edgeList.value.slice(start, start + pagination.size)
})

// ─── 编辑模式 ───
const isEditMode = ref(false)
const sourceNode = ref(null)
const targetNode = ref(null)
const edgeCreated = ref(false)

// ─── d3 引用 ───
let svg = null
let zoomG = null
let zoom = null
let nodeGroup = null
let linkGroup = null
let currentLinkSelection = null
let currentNodeSelection = null

// ─── 样式工具 ───
const diffLabel = (v) => ({ 1: '简', 2: '中', 3: '难' }[v] || '-')
const diffTag = (v) => ({ 1: 'success', 2: 'warning', 3: 'danger' }[v] || 'info')
const colorByDifficulty = (d) => d?.difficulty === 3 ? '#e74c3c' : d?.difficulty === 2 ? '#f39c12' : '#409eff'
const radiusByDifficulty = (d) => d?.difficulty === 3 ? 22 : d?.difficulty === 2 ? 18 : 14

const nodeName = (id) => {
  const n = nodeList.value.find(n => n.id === id)
  return n ? n.name : ('节点-' + id)
}

// ─── 数据加载 ───
const loadCourses = async () => {
  try {
    const res = await getCourseList({ page: 1, size: 999 })
    courseList.value = Array.isArray(res) ? res : (res.records || res.data || [])
  } catch { courseList.value = [] }
}

const loadData = async () => {
  loading.value = true
  try {
    const [edgeRes, nodeRes] = await Promise.all([
      getEdgeList(),
      getNodeList({ page: 1, size: 999, ...(filterForm.courseId ? { courseId: filterForm.courseId } : {}) })
    ])
    nodeList.value = Array.isArray(nodeRes) ? nodeRes : (nodeRes.records || nodeRes.data || [])
    const allEdges = Array.isArray(edgeRes) ? edgeRes : (edgeRes.records || edgeRes.data || [])
    const nodeIds = new Set(nodeList.value.map(n => n.id))
    edgeList.value = allEdges.filter(e => nodeIds.has(e.fromNodeId) && nodeIds.has(e.toNodeId))
    pagination.total = edgeList.value.length
    await nextTick()
    renderGraph()
  } catch {
    edgeList.value = []
    nodeList.value = []
  } finally { loading.value = false }
}

const onCourseChange = () => {
  selectedNode.value = null
  cancelEdit()
  loadData()
}

// ─── 编辑模式 ───
const onEditModeChange = (val) => {
  if (!val) cancelEdit()
  if (val && nodeList.value.length < 2) {
    ElMessage.warning('当前课程下知识点不足 2 个，无法连线')
    isEditMode.value = false
  }
}

const cancelEdit = () => {
  sourceNode.value = null
  targetNode.value = null
  edgeCreated.value = false
  // 清除所有节点高亮
  if (currentNodeSelection) {
    currentNodeSelection.select('circle').attr('stroke', '#fff').attr('stroke-width', 2.5)
  }
}

// ─── 图谱渲染 ───
function renderGraph() {
  if (!chartRef.value) return
  d3.select(chartRef.value).selectAll('svg').remove()

  const data = nodeList.value
  const links = edgeList.value
  if (!data.length) return

  const W = 1200, H = 700

  chartRef.value.style.position = 'relative'
  chartRef.value.style.width = '100%'
  chartRef.value.style.height = H + 'px'

  svg = d3.select(chartRef.value)
    .append('svg')
    .attr('viewBox', `0 0 ${W} ${H}`)
    .attr('preserveAspectRatio', 'xMidYMid meet')
    .style('width', '100%').style('height', '100%')
    .style('background', '#fafafa').style('border-radius', '8px').style('display', 'block')

  zoom = d3.zoom().scaleExtent([0.2, 5]).on('zoom', (event) => {
    zoomG.attr('transform', event.transform)
  })
  svg.call(zoom)
  zoomG = svg.append('g')

  // 箭头定义
  svg.append('defs').selectAll('marker')
    .data(['arrow']).join('marker')
    .attr('id', 'arrow').attr('viewBox', '0 -5 10 10')
    .attr('refX', 10).attr('refY', 0)
    .attr('markerWidth', 8).attr('markerHeight', 8)
    .attr('orient', 'auto')
    .append('path').attr('d', 'M0,-5L10,0L0,5').attr('fill', '#bbb')

  // 布局
  const nodeMap = {}
  data.forEach(d => { nodeMap[d.id] = d })
  const cols = Math.ceil(Math.sqrt(data.length))
  const rows = Math.ceil(data.length / cols)
  const cw = Math.min((W - 100) / cols, 220)
  const rh = Math.min((H - 80) / rows, 160)
  const ox = (W - cols * cw) / 2
  const oy = (H - rows * rh) / 2
  data.forEach((d, i) => {
    d.x = ox + (i % cols) * cw + (Math.random() - 0.5) * 30
    d.y = oy + Math.floor(i / cols) * rh + (Math.random() - 0.5) * 25
  })

  // 边线端点计算
  const edgeX1 = (e, m) => { const s = m[e.fromNodeId], t = m[e.toNodeId]; if (!s || !t) return 0; const r = radiusByDifficulty(s), dx = t.x - s.x, dy = t.y - s.y, d = Math.sqrt(dx * dx + dy * dy) || 1; return s.x + dx / d * r }
  const edgeY1 = (e, m) => { const s = m[e.fromNodeId], t = m[e.toNodeId]; if (!s || !t) return 0; const r = radiusByDifficulty(s), dx = t.x - s.x, dy = t.y - s.y, d = Math.sqrt(dx * dx + dy * dy) || 1; return s.y + dy / d * r }
  const edgeX2 = (e, m) => { const s = m[e.fromNodeId], t = m[e.toNodeId]; if (!s || !t) return 0; const r = radiusByDifficulty(t), dx = s.x - t.x, dy = s.y - t.y, d = Math.sqrt(dx * dx + dy * dy) || 1; return t.x + dx / d * r }
  const edgeY2 = (e, m) => { const s = m[e.fromNodeId], t = m[e.toNodeId]; if (!s || !t) return 0; const r = radiusByDifficulty(t), dx = s.x - t.x, dy = s.y - t.y, d = Math.sqrt(dx * dx + dy * dy) || 1; return t.y + dy / d * r }

  // 边
  linkGroup = zoomG.append('g')
  currentLinkSelection = linkGroup.selectAll('line').data(links).join('line')
    .attr('stroke', '#999').attr('stroke-width', 1.5).attr('stroke-opacity', 0.5)
    .attr('marker-end', 'url(#arrow)')
    .attr('x1', d => edgeX1(d, nodeMap)).attr('y1', d => edgeY1(d, nodeMap))
    .attr('x2', d => edgeX2(d, nodeMap)).attr('y2', d => edgeY2(d, nodeMap))

  // 节点组（可拖拽 + 点击）
  nodeGroup = zoomG.append('g')
  currentNodeSelection = nodeGroup.selectAll('g').data(data).join('g')
    .attr('transform', d => `translate(${d.x},${d.y})`)
    .style('cursor', isEditMode.value ? 'pointer' : 'grab')
    .call(d3.drag()
      .on('start', function (event) {
        event.sourceEvent.stopPropagation()
        if (!isEditMode.value) d3.select(this).style('cursor', 'grabbing')
      })
      .on('drag', function (event, d) {
        if (isEditMode.value) return // 编辑模式下禁止拖拽
        d.x += event.dx; d.y += event.dy
        d3.select(this).attr('transform', `translate(${d.x},${d.y})`)
        currentLinkSelection
          .attr('x1', d2 => edgeX1(d2, nodeMap)).attr('y1', d2 => edgeY1(d2, nodeMap))
          .attr('x2', d2 => edgeX2(d2, nodeMap)).attr('y2', d2 => edgeY2(d2, nodeMap))
      })
      .on('end', function (event, d) {
        if (!isEditMode.value) d3.select(this).style('cursor', 'grab')
      })
    )

  // 圆形
  currentNodeSelection.append('circle')
    .attr('r', d => radiusByDifficulty(d))
    .attr('fill', d => colorByDifficulty(d))
    .attr('stroke', '#fff').attr('stroke-width', 2.5)
    .style('filter', 'drop-shadow(0 2px 3px rgba(0,0,0,0.15))')

  // 标签
  currentNodeSelection.append('text')
    .text(d => d.name.length > 8 ? d.name.slice(0, 8) + '..' : d.name)
    .attr('text-anchor', 'middle')
    .attr('dy', d => radiusByDifficulty(d) + 14)
    .attr('font-size', 11).attr('fill', '#444').attr('font-weight', '500')

  // 悬停高亮
  currentNodeSelection.on('mouseenter', function (ev, d) {
    const ids = new Set(links.flatMap(e => e.fromNodeId === d.id || e.toNodeId === d.id ? [e.fromNodeId, e.toNodeId] : []))
    nodeGroup.selectAll('g').style('opacity', n => ids.has(n.id) ? 1 : 0.2)
    linkGroup.selectAll('line').style('opacity', e => e.fromNodeId === d.id || e.toNodeId === d.id ? 1 : 0)
  })
  currentNodeSelection.on('mouseleave', () => {
    nodeGroup.selectAll('g').style('opacity', 1)
    linkGroup.selectAll('line').style('opacity', 1)
  })

  // ─── 点击：编辑模式连线 / 浏览模式选中 ───
  currentNodeSelection.on('click', (ev, d) => {
    if (!isEditMode.value) {
      selectedNode.value = d.id
      return
    }
    ev.stopPropagation()
    handleGraphNodeClick(d)
  })

  // 点击空白取消高亮
  svg.on('click', () => {
    if (isEditMode.value && edgeCreated.value) {
      // 连线完成，重置状态
      cancelEdit()
    }
  })

  updateNodeStyles()
}

// ─── 编辑模式：节点点击逻辑 ───
function handleGraphNodeClick(d) {
  if (!sourceNode.value) {
    // 第一步：选中前置节点
    sourceNode.value = d
    edgeCreated.value = false
    // 高亮该节点
    currentNodeSelection.select('circle')
      .attr('stroke', n => n.id === d.id ? '#00D2FF' : '#fff')
      .attr('stroke-width', n => n.id === d.id ? 4 : 2.5)
    ElMessage.info(`已选前置节点：${d.name}，请点击后置节点`)
  } else if (sourceNode.value && !targetNode.value) {
    // 防止自环
    if (d.id === sourceNode.value.id) {
      ElMessage.warning('前置和后置节点不能相同')
      return
    }
    targetNode.value = d

    // 检查是否已存在
    const exists = edgeList.value.some(e => e.fromNodeId === sourceNode.value.id && e.toNodeId === targetNode.value.id)
    if (exists) {
      ElMessage.warning('该依赖关系已存在')
      cancelEdit()
      return
    }

    // 立即更新 d3 视图
    const newEdge = { fromNodeId: sourceNode.value.id, toNodeId: targetNode.value.id }
    edgeList.value.push(newEdge)
    pagination.total = edgeList.value.length
    edgeCreated.value = true

    // 重新渲染图谱（保留布局位置）
    const oldPositions = {}
    nodeList.value.forEach(n => { oldPositions[n.id] = { x: n.x, y: n.y } })
    renderGraph()
    // 恢复位置
    nodeList.value.forEach(n => {
      if (oldPositions[n.id]) { n.x = oldPositions[n.id].x; n.y = oldPositions[n.id].y }
    })
    updateNodeStyles()
    if (sourceNode.value) {
      currentNodeSelection.select('circle')
        .attr('stroke', n => n.id === sourceNode.value.id ? '#00D2FF' : '#fff')
        .attr('stroke-width', n => n.id === sourceNode.value.id ? 4 : 2.5)
    }

    // 异步调用后端
    createEdge({ fromNodeId: sourceNode.value.id, toNodeId: targetNode.value.id })
      .then(() => {
        ElMessage.success('依赖边创建成功')
      })
      .catch((err) => {
        ElMessage.error(err?.response?.data?.message || '创建失败')
        // 后端失败则回滚前端
        const idx = edgeList.value.findIndex(e => e.fromNodeId === sourceNode.value.id && e.toNodeId === targetNode.value.id)
        if (idx >= 0) edgeList.value.splice(idx, 1)
        pagination.total = edgeList.value.length
        renderGraph()
        nodeList.value.forEach(n => {
          if (oldPositions[n.id]) { n.x = oldPositions[n.id].x; n.y = oldPositions[n.id].y }
        })
        updateNodeStyles()
      })

    // 连线后重置，允许继续连下一条
    sourceNode.value = null
    targetNode.value = null
    setTimeout(() => { edgeCreated.value = false }, 800)
  }
}

// ─── 更新节点样式（高亮、光标） ───
function updateNodeStyles() {
  if (!currentNodeSelection) return
  currentNodeSelection.style('cursor', isEditMode.value ? 'pointer' : 'grab')
}

// ─── 对话框添加 ───
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
    await createEdge({ fromNodeId: form.fromNodeId, toNodeId: form.toNodeId })
    ElMessage.success('依赖边创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '创建失败')
  } finally { submitLoading.value = false }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除该依赖边吗？`, '删除确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      await deleteEdge(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch { }
  }).catch(() => { })
}

// ─── 生命周期 ───
onMounted(() => {
  loadCourses()
  loadData()
})

onBeforeUnmount(() => {
  svg = null; zoom = null; zoomG = null
})

// 窗口大小变化自适应
let resizeTimer = null
window.addEventListener('resize', () => {
  clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    if (svg) { /* 简单重绘由外部触发刷新 */ }
  }, 200)
})
</script>

<style scoped>
.page-container { padding: 20px 24px; background: var(--bg-root); min-height: 100vh; }

.filter-card { margin-bottom: 16px; border-radius: 12px; }

.edit-status-bar {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 20px; border-radius: 10px;
  background: var(--accent-soft); color: var(--text-secondary);
  font-size: 14px; margin-bottom: 16px; transition: background 0.2s;
}
.edit-status-bar.has-source { background: rgba(64,158,255,0.08); }
.edit-status-bar.edge-created { background: rgba(103,194,58,0.1); }
.edit-status-bar strong { color: var(--text-primary); }

.graph-card { border-radius: 12px; margin-bottom: 16px; min-height: 400px; }
.chart-container { width: 100%; min-height: 600px; overflow: hidden; }
.chart-container :deep(svg) { display: block; }

.split-panels { display: flex; gap: 16px; }
.panel-left { flex: 1; border-radius: 12px; }
.panel-right { flex: 1; border-radius: 12px; }
.panel-title { font-weight: 600; color: var(--text-primary); }

.node-list { max-height: 500px; overflow-y: auto; display: flex; flex-direction: column; gap: 6px; }
.node-chip {
  display: flex; align-items: center; gap: 8px; padding: 8px 12px;
  border-radius: 8px; cursor: pointer; transition: background 0.2s, box-shadow 0.2s;
}
.node-chip:hover { background: var(--bg-hover); }
.node-chip.node-selected { background: var(--accent-soft); box-shadow: 0 0 0 2px var(--accent) inset; }
.node-name { font-size: 13px; color: var(--text-secondary); }

@media (max-width: 900px) { .split-panels { flex-direction: column; } }
</style>
