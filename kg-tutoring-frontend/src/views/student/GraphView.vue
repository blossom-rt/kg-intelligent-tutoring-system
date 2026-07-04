<template>
  <div class="graph-view">
    <StudentHeader title="知识图谱" subtitle="点击节点查看详情，拖拽可自由探索">
      <template #actions>
        <el-select v-model="selectedCourseId" placeholder="选择课程" clearable size="default" style="width:200px" @change="onCourseChange">
          <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
        </el-select>
        <el-button @click="fitGraph" :disabled="!chart" round>重置视图</el-button>
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
import * as echarts from 'echarts'
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
const chart = ref(null)

const diffLabel = (v) => ({ 1: '入门', 2: '基础', 3: '进阶', 4: '困难', 5: '挑战' }[v] || '未知')
const diffTag = (v) => ({ 1: 'success', 2: 'info', 3: 'warning', 4: 'danger', 5: 'danger' }[v] || 'info')

function buildChartData() {
  let list = nodes.value
  if (selectedCourseId.value) {
    list = nodes.value.filter(n => n.courseId === selectedCourseId.value)
  }
  // 只保留跟当前节点列表有关的边
  const nodeIds = new Set(list.map(n => n.id))
  const edgeList = edges.value.filter(e => nodeIds.has(e.fromNodeId) && nodeIds.has(e.toNodeId))

  const chartNodes = list.map(n => ({
    id: String(n.id),
    name: n.name,
    value: n.name,
    symbolSize: n.difficulty === 3 ? 60 : n.difficulty === 2 ? 50 : 40,
    itemStyle: {
      color: n.difficulty === 3 ? '#e74c3c' : n.difficulty === 2 ? '#f39c12' : '#ff7b3d'
    },
    category: n.courseId,
    raw: n
  }))

  const chartEdges = edgeList.map(e => ({
    source: String(e.fromNodeId),
    target: String(e.toNodeId),
    lineStyle: { color: '#ccc', width: 2, curveness: 0.2, type: 'solid' }
  }))

  return { chartNodes, chartEdges }
}

function renderChart() {
  if (!chartRef.value) return
  const { chartNodes, chartEdges } = buildChartData()
  if (!chart.value) chart.value = echarts.init(chartRef.value)

  chart.value.setOption({
    title: { show: false },
    tooltip: {
      formatter: (p) => {
        if (p.dataType === 'node') return `<b>${p.name}</b><br/>难度: ${diffLabel(p.data.raw?.difficulty)}`
        return ''
      }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      force: { repulsion: 500, edgeLength: [120, 250], gravity: 0.05, friction: 0.15 },
      roam: true,               // 可拖拽缩放
      draggable: true,
      data: chartNodes,
      edges: chartEdges,
      categories: [],
      label: {
        show: true,
        position: 'bottom',
        fontSize: 12,
        color: '#2d2a26',
        formatter: (p) => p.name.length > 6 ? p.name.slice(0, 6) + '..' : p.name
      },
      edgeLabel: { show: false },
      lineStyle: { color: '#bbb', width: 2, curveness: 0.2 },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 3, color: '#ff7b3d' }
      }
    }]
  })

  chart.value.on('click', (params) => {
    if (params.dataType === 'node') {
      const raw = params.data.raw
      if (raw) { currentNode.value = raw; dialogVisible.value = true }
    }
  })
}

function fitGraph() { chart.value?.resize() }

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
  } catch { /* ignore */ }
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
  chart.value?.dispose()
  chart.value = null
})

// 窗口改变时自适应
watch(() => [dialogVisible.value], () => setTimeout(() => chart.value?.resize(), 300))
</script>

<style scoped>
.graph-view { min-height: 100vh; background: #faf7f2; }
.graph-card { border-radius: 12px; min-height: 500px; position: relative; margin: 0 32px 24px; }
.chart-container { width: 100%; height: 600px; }
.detail-desc { margin-top: 20px; }
.detail-desc h4 { margin: 0 0 8px; font-size: 15px; color: #2d2a26; }
.detail-desc p { font-size: 14px; color: #6b655e; line-height: 1.8; white-space: pre-wrap; }
</style>
