<template>
  <div class="graph-view">
    <StudentHeader title="知识图谱" subtitle="拖拽旋转视角，滚轮缩放，点击节点查看详情">
      <template #actions>
        <el-select v-model="selectedCourseId" placeholder="选择课程" clearable size="default" style="width:200px" @change="onCourseChange">
          <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
        </el-select>
        <el-button @click="resetView" round>重置视角</el-button>
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
          <el-descriptions-item label="节点类型">{{ nodeTypeLabel(currentNode.nodeType) }}</el-descriptions-item>
          <el-descriptions-item label="难度等级">
            <el-tag :type="diffTag(currentNode.difficulty)" size="small">{{ diffLabel(currentNode.difficulty) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属章节">{{ currentNode.chapter || '无' }}</el-descriptions-item>
          <el-descriptions-item label="预计时长">{{ currentNode.expectedMinutes || '-' }} 分钟</el-descriptions-item>
          <el-descriptions-item v-if="currentNode.keywords" label="关键词">{{ currentNode.keywords }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="currentNode.learningGoal" class="detail-desc">
          <h4>学习目标</h4>
          <p>{{ currentNode.learningGoal }}</p>
        </div>
        <div class="detail-desc">
          <h4>详细描述</h4>
          <p>{{ currentNode.description || '暂无详细描述' }}</p>
        </div>
        <div v-if="currentNode.exampleHint" class="detail-desc">
          <h4>例题提示</h4>
          <p>{{ currentNode.exampleHint }}</p>
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
import { useRoute, useRouter } from 'vue-router'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { ElMessage } from 'element-plus'
import StudentHeader from '../../components/StudentHeader.vue'
import { usePet } from '../../composables/usePet'
import { getStudentGraph, generatePath } from '../../api/student'
import { getCourseList } from '../../api/knowledge'

const router = useRouter()
const route = useRoute()
const pet = usePet()
const loading = ref(false)
const nodes = ref([])
const edges = ref([])
const learnedNodeIds = ref(new Set())
const pathNodeIds = ref(new Set())
const courses = ref([])
const selectedCourseId = ref('')
const dialogVisible = ref(false)
const currentNode = ref(null)
const genLoading = ref(false)
const chartRef = ref(null)
const lastSpokenCourseId = ref(null)

let scene = null
let camera = null
let renderer = null
let controls = null
let raycaster = null
let resizeObserver = null
let animationId = null
let hoveredNodeId = null
let nodeObjects = []
let lineObjects = []
let labelObjects = []

const diffLabel = (v) => ({ 1: '入门', 2: '基础', 3: '进阶', 4: '困难', 5: '挑战' }[v] || '未知')
const diffTag = (v) => ({ 1: 'success', 2: 'info', 3: 'warning', 4: 'danger', 5: 'danger' }[v] || 'info')
const nodeTypeLabel = (v) => ({ concept: '概念理解', skill: '方法技能', application: '应用实践' }[v] || '概念理解')

const getCssVar = (name, fallback) => {
  const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim()
  return value || fallback
}

const colorByDifficulty = (node) => {
  if (node.difficulty === 3) return 0xe74c3c
  if (node.difficulty === 2) return 0xf5a623
  return 0x4aa3ff
}

const radiusByDifficulty = (node) => {
  if (node.difficulty === 3) return 2.9
  if (node.difficulty === 2) return 2.4
  return 2
}

const getNodeState = (node) => {
  const nodeId = Number(node.id)
  if (learnedNodeIds.value.has(nodeId)) return 'learned'
  if (pathNodeIds.value.has(nodeId)) return 'path'
  return 'unseen'
}

const nodeOpacity = (node) => {
  const state = getNodeState(node)
  if (state === 'learned') return 1
  if (state === 'path') return 0.78
  return 0.22
}

const getFilteredData = () => {
  let list = nodes.value
  if (selectedCourseId.value) {
    list = nodes.value.filter(n => Number(n.courseId) === Number(selectedCourseId.value))
  }
  const nodeIds = new Set(list.map(n => Number(n.id)))
  const edgeList = edges.value.filter(e => nodeIds.has(Number(e.fromNodeId)) && nodeIds.has(Number(e.toNodeId)))
  return { nodeList: list, edgeList }
}

const getRelatedChainNodeIds = (nodeId) => {
  const { nodeList, edgeList } = getFilteredData()
  const visibleNodeIds = new Set(nodeList.map(node => Number(node.id)))
  const startId = Number(nodeId)
  if (!visibleNodeIds.has(startId)) return new Set()

  const forward = new Map()
  const backward = new Map()
  edgeList.forEach(edge => {
    const from = Number(edge.fromNodeId)
    const to = Number(edge.toNodeId)
    if (!forward.has(from)) forward.set(from, [])
    if (!backward.has(to)) backward.set(to, [])
    forward.get(from).push(to)
    backward.get(to).push(from)
  })

  const relatedIds = new Set([startId])
  const walk = (graph) => {
    const queue = [startId]
    const visited = new Set([startId])
    while (queue.length) {
      const current = queue.shift()
      ;(graph.get(current) || []).forEach(next => {
        if (visited.has(next)) return
        visited.add(next)
        relatedIds.add(next)
        queue.push(next)
      })
    }
  }

  walk(forward)
  walk(backward)
  return relatedIds
}

const placeNodes = (nodeList) => {
  const count = Math.max(nodeList.length, 1)
  const radius = Math.max(22, Math.min(58, count * 1.8))
  const goldenAngle = Math.PI * (3 - Math.sqrt(5))
  return nodeList.map((node, index) => {
    const y = 1 - (index / Math.max(count - 1, 1)) * 2
    const r = Math.sqrt(1 - y * y)
    const theta = index * goldenAngle
    const stable = ((Number(node.id) * 17) % 11 - 5) * 0.28
    return {
      ...node,
      x: Math.cos(theta) * r * radius + stable,
      y: y * radius * 0.62,
      z: Math.sin(theta) * r * radius - stable
    }
  })
}

const createTextSprite = (text, opacity) => {
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  const ratio = window.devicePixelRatio || 1
  const label = text.length > 8 ? text.slice(0, 8) + '..' : text
  canvas.width = 220 * ratio
  canvas.height = 64 * ratio
  ctx.scale(ratio, ratio)
  ctx.font = '600 24px "PingFang SC", system-ui, sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillStyle = getCssVar('--text-primary', '#2d2a26')
  ctx.fillText(label, 110, 32)
  const texture = new THREE.CanvasTexture(canvas)
  texture.colorSpace = THREE.SRGBColorSpace
  const material = new THREE.SpriteMaterial({ map: texture, transparent: true, opacity, depthWrite: false })
  const sprite = new THREE.Sprite(material)
  sprite.scale.set(10.5, 3.1, 1)
  return sprite
}

const clearScene = () => {
  nodeObjects.forEach(item => {
    item.mesh.geometry.dispose()
    item.mesh.material.dispose()
  })
  lineObjects.forEach(item => {
    item.line.geometry.dispose()
    item.line.material.dispose()
  })
  labelObjects.forEach(item => {
    item.sprite.material.map?.dispose()
    item.sprite.material.dispose()
  })
  nodeObjects = []
  lineObjects = []
  labelObjects = []
  if (scene) {
    while (scene.children.length) {
      scene.remove(scene.children[0])
    }
  }
}

const applyDefaultStyles = () => {
  nodeObjects.forEach(item => {
    const state = getNodeState(item.node)
    item.mesh.material.opacity = nodeOpacity(item.node)
    item.mesh.scale.setScalar(state === 'learned' ? 1.18 : 1)
    item.mesh.material.emissiveIntensity = state === 'learned' ? 0.35 : (state === 'path' ? 0.16 : 0.04)
  })
  labelObjects.forEach(item => {
    item.sprite.material.opacity = Math.max(nodeOpacity(item.node), 0.42)
  })
  lineObjects.forEach(item => {
    const from = item.fromNode
    const to = item.toNode
    if (item.isCone) {
      item.line.material.opacity = Math.max(0.06, Math.min(nodeOpacity(from), nodeOpacity(to)) * 0.4)
      const state = getNodeState(to)
      const s = state === 'learned' ? 1.18 : 1
      item.line.scale.setScalar(s)
    } else {
      item.line.material.opacity = Math.max(0.06, Math.min(nodeOpacity(from), nodeOpacity(to)) * 0.4)
      item.line.material.linewidth = 1
    }
  })
}

const applyHoverStyles = (nodeId) => {
  const highlighted = getRelatedChainNodeIds(nodeId)
  nodeObjects.forEach(item => {
    const isActive = highlighted.has(Number(item.node.id))
    item.mesh.material.opacity = isActive ? Math.max(nodeOpacity(item.node), 0.96) : Math.min(nodeOpacity(item.node), 0.14)
    item.mesh.scale.setScalar(isActive ? 1.32 : 0.92)
    item.mesh.material.emissiveIntensity = isActive ? 0.58 : 0.04
  })
  labelObjects.forEach(item => {
    item.sprite.material.opacity = highlighted.has(Number(item.node.id)) ? 1 : 0.25
  })
  lineObjects.forEach(item => {
    const active = highlighted.has(Number(item.edge.fromNodeId)) && highlighted.has(Number(item.edge.toNodeId))
    if (item.isCone) {
      item.line.material.opacity = active ? 0.86 : 0.06
      item.line.scale.setScalar(active ? 1.32 : 0.92)
    } else {
      item.line.material.opacity = active ? 0.86 : 0.06
    }
  })
}

const renderChart = () => {
  if (!chartRef.value) return
  clearScene()

  const { nodeList, edgeList } = getFilteredData()
  if (!nodeList.length) return

  const placedNodes = placeNodes(nodeList)
  const nodeMap = {}
  placedNodes.forEach(node => { nodeMap[Number(node.id)] = node })

  const bgColor = new THREE.Color(getCssVar('--bg-input', '#f8f5f0'))
  scene.background = bgColor
  scene.add(new THREE.AmbientLight(0xffffff, 1.9))
  const keyLight = new THREE.DirectionalLight(0xffffff, 2.2)
  keyLight.position.set(30, 40, 55)
  scene.add(keyLight)
  const fillLight = new THREE.PointLight(0xffb277, 1.6, 160)
  fillLight.position.set(-35, -20, 35)
  scene.add(fillLight)

  edgeList.forEach(edge => {
    const from = nodeMap[Number(edge.fromNodeId)]
    const to = nodeMap[Number(edge.toNodeId)]
    if (!from || !to) return
    const points = [
      new THREE.Vector3(from.x, from.y, from.z),
      new THREE.Vector3(to.x, to.y, to.z)
    ]
    const geometry = new THREE.BufferGeometry().setFromPoints(points)
    const material = new THREE.LineBasicMaterial({
      color: getCssVar('--text-muted', '#a09a92'),
      transparent: true,
      opacity: Math.max(0.06, Math.min(nodeOpacity(from), nodeOpacity(to)) * 0.4)
    })
    const line = new THREE.Line(geometry, material)
    scene.add(line)
    lineObjects.push({ line, edge, fromNode: from, toNode: to })

    // 箭头锥体
    const dir = new THREE.Vector3(to.x - from.x, to.y - from.y, to.z - from.z)
    const len = dir.length()
    if (len < 0.01) return
    dir.normalize()
    const r = radiusByDifficulty(to)
    const arrowPos = new THREE.Vector3(to.x - dir.x * (r + 0.9), to.y - dir.y * (r + 0.9), to.z - dir.z * (r + 0.9))
    const cone = new THREE.Mesh(
      new THREE.ConeGeometry(0.6, 1.2, 8),
      new THREE.MeshBasicMaterial({ color: getCssVar('--text-muted', '#a09a92'), transparent: true, opacity: 0.5 })
    )
    cone.position.copy(arrowPos)
    cone.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), dir)
    scene.add(cone)
    lineObjects.push({ line: cone, isCone: true, edge, fromNode: from, toNode: to })
  })

  placedNodes.forEach(node => {
    const state = getNodeState(node)
    const material = new THREE.MeshStandardMaterial({
      color: colorByDifficulty(node),
      roughness: 0.38,
      metalness: 0.1,
      transparent: true,
      opacity: nodeOpacity(node),
      emissive: colorByDifficulty(node),
      emissiveIntensity: state === 'learned' ? 0.35 : (state === 'path' ? 0.16 : 0.04)
    })
    const mesh = new THREE.Mesh(new THREE.SphereGeometry(radiusByDifficulty(node), 32, 24), material)
    mesh.position.set(node.x, node.y, node.z)
    mesh.scale.setScalar(state === 'learned' ? 1.18 : 1)
    mesh.userData.node = node
    scene.add(mesh)
    nodeObjects.push({ mesh, node })


    const label = createTextSprite(node.name || '未命名', Math.max(nodeOpacity(node), 0.42))
    label.position.set(node.x, node.y - radiusByDifficulty(node) - 3.2, node.z)
    scene.add(label)
    labelObjects.push({ sprite: label, node })
  })
}

const initScene = () => {
  if (!chartRef.value || renderer) return
  scene = new THREE.Scene()
  camera = new THREE.PerspectiveCamera(50, 1, 0.1, 1000)
  camera.position.set(0, 16, 118)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  renderer.outputColorSpace = THREE.SRGBColorSpace
  chartRef.value.appendChild(renderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  controls.minDistance = 38
  controls.maxDistance = 210
  controls.autoRotate = true
  controls.autoRotateSpeed = 0.22

  raycaster = new THREE.Raycaster()
  renderer.domElement.addEventListener('pointermove', handlePointerMove)
  renderer.domElement.addEventListener('pointerleave', handlePointerLeave)
  renderer.domElement.addEventListener('click', handleCanvasClick)

  resizeObserver = new ResizeObserver(resizeScene)
  resizeObserver.observe(chartRef.value)
  resizeScene()
  animate()
}

const resizeScene = () => {
  if (!chartRef.value || !renderer || !camera) return
  const width = chartRef.value.clientWidth || 800
  const height = chartRef.value.clientHeight || 620
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height, false)
}

const getIntersectedNode = (event) => {
  if (!renderer || !camera) return null
  const rect = renderer.domElement.getBoundingClientRect()
  const pointer = new THREE.Vector2(
    ((event.clientX - rect.left) / rect.width) * 2 - 1,
    -((event.clientY - rect.top) / rect.height) * 2 + 1
  )
  raycaster.setFromCamera(pointer, camera)
  const meshes = nodeObjects.map(item => item.mesh).filter(mesh => mesh.userData.node)
  const hit = raycaster.intersectObjects(meshes, false)[0]
  return hit?.object?.userData?.node || null
}

const handlePointerMove = (event) => {
  const node = getIntersectedNode(event)
  const nextId = node ? Number(node.id) : null
  renderer.domElement.style.cursor = node ? 'pointer' : 'grab'
  if (hoveredNodeId === nextId) return
  hoveredNodeId = nextId
  if (nextId) {
    controls.autoRotate = false
    applyHoverStyles(nextId)
  } else {
    applyDefaultStyles()
  }
}

const handlePointerLeave = () => {
  hoveredNodeId = null
  renderer.domElement.style.cursor = 'grab'
  applyDefaultStyles()
}

const handleCanvasClick = (event) => {
  const node = getIntersectedNode(event)
  if (!node) return
  currentNode.value = node
  dialogVisible.value = true
  speakForNode(node)
}

const animate = () => {
  animationId = requestAnimationFrame(animate)
  controls?.update()
  renderer?.render(scene, camera)
}

const resetView = () => {
  if (!camera || !controls) return
  camera.position.set(0, 16, 118)
  controls.target.set(0, 0, 0)
  controls.autoRotate = true
  controls.update()
}

const onCourseChange = () => {
  hoveredNodeId = null
  renderChart()
  resetView()
  speakForCourse(selectedCourseId.value)
}

const getCourseMessage = (course) => {
  const name = course.courseName || '这门课'
  const subject = course.subject || ''
  if (subject.includes('数学')) return `打开《${name}》啦，先抓住核心概念，再一步步推理。`
  if (subject.includes('物理')) return `《${name}》很适合联系生活现象，一边观察一边理解规律。`
  if (subject.includes('信息') || subject.includes('计算机') || name.toLowerCase().includes('python')) return `学《${name}》可以多动手试试，小程序会帮你把思路跑通。`
  if (subject.includes('跨学科')) return `《${name}》要把不同学科串起来，先找一个真实问题切入。`
  return `开始看《${name}》吧，先扫一遍知识点，再挑重点突破。`
}

const speakForCourse = (courseId) => {
  if (!courseId || lastSpokenCourseId.value === courseId) return
  const course = courses.value.find(c => Number(c.id) === Number(courseId))
  if (!course) return
  lastSpokenCourseId.value = courseId
  pet.say(getCourseMessage(course))
}

const speakForNode = (node) => {
  if (!node) return
  const name = node.name || '这个知识点'
  if (node.difficulty >= 3) {
    pet.say(`《${name}》有点挑战，先拆成概念、例题、练习三步来。`)
    return
  }
  if (node.difficulty === 2) {
    pet.say(`点到《${name}》啦，先看懂例子，再做两道题巩固。`)
    return
  }
  pet.say(`《${name}》是打基础的好节点，稳稳拿下它。`)
}

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
    if (res && res.nodes) {
      nodes.value = res.nodes
      edges.value = res.edges || []
      learnedNodeIds.value = new Set((res.learnedNodeIds || []).map(id => Number(id)))
      pathNodeIds.value = new Set((res.pathNodeIds || []).map(id => Number(id)))
    }
  } catch {
    ElMessage.error('知识图谱加载失败')
    nodes.value = []
    edges.value = []
  } finally {
    loading.value = false
  }
}

const fetchCourses = async () => {
  try {
    const res = await getCourseList()
    courses.value = Array.isArray(res) ? res : (res.records || [])
  } catch { /* ignore */ }
}

const destroyScene = () => {
  if (animationId) cancelAnimationFrame(animationId)
  renderer?.domElement?.removeEventListener('pointermove', handlePointerMove)
  renderer?.domElement?.removeEventListener('pointerleave', handlePointerLeave)
  renderer?.domElement?.removeEventListener('click', handleCanvasClick)
  resizeObserver?.disconnect()
  clearScene()
  controls?.dispose()
  renderer?.dispose()
  if (renderer?.domElement?.parentNode) renderer.domElement.parentNode.removeChild(renderer.domElement)
  scene = null
  camera = null
  renderer = null
  controls = null
  raycaster = null
}

onMounted(async () => {
  await fetchGraph()
  await fetchCourses()
  if (route.query.courseId) selectedCourseId.value = Number(route.query.courseId)
  await nextTick()
  initScene()
  renderChart()
  resetView()
  speakForCourse(selectedCourseId.value)
})

watch(() => route.query.courseId, (courseId) => {
  selectedCourseId.value = courseId ? Number(courseId) : ''
  renderChart()
  resetView()
  speakForCourse(selectedCourseId.value)
})

onBeforeUnmount(destroyScene)
</script>

<style scoped>
.graph-view { min-height: 100vh; background: var(--bg-root); }
.graph-card { border-radius: 12px; min-height: 620px; position: relative; margin: 0 32px 24px; overflow: hidden; }
.chart-container { width: 100%; height: min(72vh, 780px); min-height: 560px; overflow: hidden; border-radius: 8px; background: var(--bg-input); }
.chart-container :deep(canvas) { display: block; width: 100%; height: 100%; cursor: grab; }
.detail-desc { margin-top: 20px; }
.detail-desc h4 { margin: 0 0 8px; font-size: 15px; color: var(--text-primary); }
.detail-desc p { font-size: 14px; color: var(--text-secondary); line-height: 1.8; white-space: pre-wrap; }

@media (max-width: 700px) {
  .graph-card { margin: 0 12px 16px; min-height: 520px; }
  .chart-container { height: 66vh; min-height: 480px; }
}
</style>
