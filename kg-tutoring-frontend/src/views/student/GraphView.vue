<template>
  <div class="graph-view">
    <StudentHeader title="知识图谱" subtitle="点击节点查看详情，拖拽可自由探索">
      <template #actions>
        <el-select v-model="selectedCourseId" placeholder="选择课程" clearable size="default" style="width:200px" @change="onCourseChange">
          <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
        </el-select>
        <el-button @click="resetZoom" round>重置视图</el-button>
      </template>
    </StudentHeader>

    <el-card class="graph-card" v-loading="loading">
      <div ref="chartRef" class="chart-container"></div>
      <el-empty v-if="!loading && nodes.length === 0" description="暂无知识点数据" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="currentNode?.name || '知识点详情'" width="560px" destroy-on-close>
      <template v-if="currentNode">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="知识点名称">{{ currentNode.name }}</el-descriptions-item>
          <el-descriptions-item label="难度等级">
            <el-tag :type="diffTag(currentNode.difficulty)" size="small">{{ diffLabel(currentNode.difficulty) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属章节">{{ currentNode.chapter || '无' }}</el-descriptions-item>
          <el-descriptions-item label="预计时长">{{ currentNode.expectedMinutes || '-' }} 分钟</el-descriptions-item>
        </el-descriptions>
        <div class="detail-desc">
          <h4>详细描述</h4>
          <p>{{ currentNode.description || '暂无详细描述' }}</p>
        </div>
      </template>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button @click="goStudy(currentNode)">直接学习</el-button>
        <el-button type="primary" :loading="genLoading" @click="handleGeneratePath(currentNode)">生成学习路径</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as d3 from 'd3'
import { ElMessage } from 'element-plus'
import StudentHeader from '../../components/StudentHeader.vue'
import { getStudentGraph, generatePath } from '../../api/student'
import { getCourseList } from '../../api/knowledge'

const router = useRouter()
const loading = ref(false)
const nodes = ref([])
const edges = ref([])
const courses = ref([])
const selectedCourseId = ref('')
const dialogVisible = ref(false)
const currentNode = ref(null)
const genLoading = ref(false)
const chartRef = ref(null)

let svg = null
let zoomG = null
let zoom = null

const diffLabel = (v) => ({ 1: '入门', 2: '基础', 3: '进阶', 4: '困难', 5: '挑战' }[v] || '未知')
const diffTag = (v) => ({ 1: 'success', 2: 'info', 3: 'warning', 4: 'danger', 5: 'danger' }[v] || 'info')

// 边线端点计算（从节点边缘到边缘）
const edgeX1 = (e, m) => { const s = m[e.fromNodeId], t = m[e.toNodeId]; if (!s||!t) return 0; const r=radiusByDifficulty(s), dx=t.x-s.x, dy=t.y-s.y, d=Math.sqrt(dx*dx+dy*dy)||1; return s.x+dx/d*r }
const edgeY1 = (e, m) => { const s = m[e.fromNodeId], t = m[e.toNodeId]; if (!s||!t) return 0; const r=radiusByDifficulty(s), dx=t.x-s.x, dy=t.y-s.y, d=Math.sqrt(dx*dx+dy*dy)||1; return s.y+dy/d*r }
const edgeX2 = (e, m) => { const s = m[e.fromNodeId], t = m[e.toNodeId]; if (!s||!t) return 0; const r=radiusByDifficulty(t), dx=s.x-t.x, dy=s.y-t.y, d=Math.sqrt(dx*dx+dy*dy)||1; return t.x+dx/d*r }
const edgeY2 = (e, m) => { const s = m[e.fromNodeId], t = m[e.toNodeId]; if (!s||!t) return 0; const r=radiusByDifficulty(t), dx=s.x-t.x, dy=s.y-t.y, d=Math.sqrt(dx*dx+dy*dy)||1; return t.y+dy/d*r }

const colorByDifficulty = (d) => {
  if (d.difficulty === 3) return '#e74c3c'
  if (d.difficulty === 2) return '#f39c12'
  return '#409eff'
}

const radiusByDifficulty = (d) => {
  if (d.difficulty === 3) return 22
  if (d.difficulty === 2) return 18
  return 14
}

function getFilteredData() {
  let list = nodes.value
  if (selectedCourseId.value) {
    list = nodes.value.filter(n => n.courseId === selectedCourseId.value)
  }
  const nodeIds = new Set(list.map(n => n.id))
  const edgeList = edges.value.filter(e => nodeIds.has(e.fromNodeId) && nodeIds.has(e.toNodeId))
  return { nodeList: list, edgeList }
}

function renderChart() {
  if (!chartRef.value) return

  // 清理旧画布
  d3.select(chartRef.value).selectAll('svg').remove()

  const { nodeList, edgeList } = getFilteredData()
  const W = 1200, H = 800

  chartRef.value.style.position = 'relative'
  chartRef.value.style.width = '100%'
  chartRef.value.style.height = H + 'px'

  svg = d3.select(chartRef.value)
    .append('svg')
    .attr('viewBox', `0 0 ${W} ${H}`)
    .attr('preserveAspectRatio', 'xMidYMid meet')
    .style('width', '100%')
    .style('height', '100%')
    .style('background', '#fafafa')
    .style('border-radius', '8px')
    .style('display', 'block')

  zoom = d3.zoom().scaleExtent([0.2, 5]).on('zoom', (event) => {
    zoomG.attr('transform', event.transform)
  })

  svg.call(zoom)
  zoomG = svg.append('g')

  // 箭头定义
  svg.append('defs').selectAll('marker')
    .data(['arrow'])
    .join('marker')
    .attr('id', 'arrow')
    .attr('viewBox', '0 -5 10 10')
    .attr('refX', 10)
    .attr('refY', 0)
    .attr('markerWidth', 8)
    .attr('markerHeight', 8)
    .attr('orient', 'auto')
    .append('path')
    .attr('d', 'M0,-5L10,0L0,5')
    .attr('fill', '#bbb')

  if (!nodeList.length) return

  // 松散布局（自适应间距 + 随机偏移）
  const cols = Math.ceil(Math.sqrt(nodeList.length))
  const rows = Math.ceil(nodeList.length / cols)
  const cw = Math.min((W - 100) / cols, 220)
  const rh = Math.min((H - 80) / rows, 160)
  const ox = (W - cols * cw) / 2
  const oy = (H - rows * rh) / 2
  nodeList.forEach((d, i) => {
    d.x = ox + (i % cols) * cw + (Math.random() - 0.5) * 30
    d.y = oy + Math.floor(i / cols) * rh + (Math.random() - 0.5) * 25
  })

  // 连线坐标映射
  const nodeMap = {}
  nodeList.forEach(d => { nodeMap[d.id] = d })

  // 不再画课程背景色块

  // 边（从节点边缘到边缘，避免箭头被挡住）
  const link = zoomG.append('g').selectAll('line').data(edgeList).join('line')
    .attr('stroke', '#999').attr('stroke-width', 1.5)
    .attr('stroke-opacity', 0.5).attr('marker-end', 'url(#arrow)')
    .attr('x1', d => edgeX1(d, nodeMap))
    .attr('y1', d => edgeY1(d, nodeMap))
    .attr('x2', d => edgeX2(d, nodeMap))
    .attr('y2', d => edgeY2(d, nodeMap))

  // 节点组（可拖拽）
  const nodeGroup = zoomG.append('g').selectAll('g').data(nodeList).join('g')
    .attr('transform', d => `translate(${d.x},${d.y})`)
    .style('cursor', 'grab')
    .call(d3.drag()
      .on('start', function(event) { event.sourceEvent.stopPropagation(); d3.select(this).style('cursor', 'grabbing') })
      .on('drag', function(event, d) {
        d.x += event.dx; d.y += event.dy
        d3.select(this).attr('transform', `translate(${d.x},${d.y})`)
        link.attr('x1', d2 => edgeX1(d2, nodeMap)).attr('y1', d2 => edgeY1(d2, nodeMap))
            .attr('x2', d2 => edgeX2(d2, nodeMap)).attr('y2', d2 => edgeY2(d2, nodeMap))
      })
      .on('end', function(event, d) {
        d3.select(this).style('cursor', 'grab')
      })
    )

  // 圆形节点
  nodeGroup.append('circle')
    .attr('r', d => radiusByDifficulty(d))
    .attr('fill', d => colorByDifficulty(d))
    .attr('stroke', '#fff').attr('stroke-width', 2.5)
    .style('filter', 'drop-shadow(0 2px 3px rgba(0,0,0,0.15))')

  // 标签
  nodeGroup.append('text')
    .text(d => d.name.length > 8 ? d.name.slice(0, 8) + '..' : d.name)
    .attr('text-anchor', 'middle')
    .attr('dy', d => radiusByDifficulty(d) + 14)
    .attr('font-size', 11).attr('fill', '#444').attr('font-weight', '500')

  // 悬停高亮（无关连线隐藏）
  nodeGroup.on('mouseenter', function(ev, d) {
    const ids = new Set(edgeList.flatMap(e => e.fromNodeId === d.id || e.toNodeId === d.id ? [e.fromNodeId, e.toNodeId] : []))
    nodeGroup.style('opacity', n => ids.has(n.id) ? 1 : 0.2)
    link.style('opacity', e => e.fromNodeId === d.id || e.toNodeId === d.id ? 1 : 0)
  })
  nodeGroup.on('mouseleave', () => { nodeGroup.style('opacity', 1); link.style('opacity', 1) })

  // 点击
  nodeGroup.on('click', (ev, d) => { currentNode.value = d; dialogVisible.value = true })

  // 不再使用呼吸抖动，避免拖拽时箭头跳动
}

function resetZoom() {
  if (svg && zoom) {
    svg.transition().duration(500).call(zoom.transform, d3.zoomIdentity)
  }
}

const onCourseChange = () => { renderChart() }

const handleGeneratePath = async (node) => {
  if (!node) return
  genLoading.value = true
  try {
    const pathId = await generatePath({ targetNodeId: node.id })
    ElMessage.success('学习路径已生成')
    dialogVisible.value = false
    router.push('/student/path/' + pathId)
  } catch { ElMessage.error('生成失败，请重试') }
  finally { genLoading.value = false }
}

const goStudy = (node) => {
  dialogVisible.value = false
  if (node) router.push('/student/study/' + node.id)
}

const fetchGraph = async () => {
  loading.value = true
  try {
    const res = await getStudentGraph()
    if (res && res.nodes) { nodes.value = res.nodes; edges.value = res.edges || [] }
  } catch {
    ElMessage.error('知识图谱加载失败')
    nodes.value = []
    edges.value = []
  }
  finally { loading.value = false }
}

const fetchCourses = async () => {
  try {
    const res = await getCourseList()
    courses.value = Array.isArray(res) ? res : (res.records || [])
  } catch { /* ignore */ }
}

onMounted(async () => {
  await fetchGraph()
  await fetchCourses()
  await nextTick()
  renderChart()
})

onBeforeUnmount(() => {
  svg = null; zoom = null; zoomG = null
})
</script>

<style scoped>
.graph-view { min-height: 100vh; background: var(--bg-root); }
.graph-card { border-radius: 12px; min-height: 500px; position: relative; margin: 0 32px 24px; }
.chart-container { width: 100%; min-height: 600px; overflow: hidden; }
.chart-container :deep(svg) { display: block; }
.detail-desc { margin-top: 20px; }
.detail-desc h4 { margin: 0 0 8px; font-size: 15px; color: var(--text-primary); }
.detail-desc p { font-size: 14px; color: var(--text-secondary); line-height: 1.8; white-space: pre-wrap; }
</style>
