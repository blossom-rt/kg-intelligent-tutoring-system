<template>
  <div class="graph-view">
    <div class="page-header">
      <h2 class="page-title">知识图谱浏览</h2>
      <div class="header-actions">
        <el-select
          v-model="selectedCourseId"
          placeholder="选择课程"
          clearable
          style="width: 220px"
          @change="filterNodes"
        >
          <el-option
            v-for="c in courses"
            :key="c.id"
            :label="c.courseName || c.name"
            :value="c.id"
          />
        </el-select>
      </div>
    </div>

    <div v-loading="loading" class="node-grid">
      <el-empty v-if="!loading && filteredNodes.length === 0" description="暂无知识点数据" />
      <el-card
        v-for="node in filteredNodes"
        :key="node.id"
        class="node-card"
        shadow="hover"
        @click="openDetail(node)"
      >
        <div class="node-header">
          <span class="node-name">{{ node.nodeName || node.name }}</span>
          <el-tag :type="difficultyTagType(node.difficulty)" size="small">
            {{ difficultyLabel(node.difficulty) }}
          </el-tag>
        </div>
        <p class="node-desc">{{ truncate(node.description || node.desc || '暂无描述', 80) }}</p>
        <div class="node-meta">
          <span v-if="node.courseName" class="meta-course">{{ node.courseName }}</span>
        </div>
      </el-card>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="currentNode?.nodeName || currentNode?.name || '知识点详情'"
      width="560px"
      destroy-on-close
    >
      <template v-if="currentNode">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="知识点名称">
            {{ currentNode.nodeName || currentNode.name }}
          </el-descriptions-item>
          <el-descriptions-item label="所属课程">
            {{ currentNode.courseName || '未分类' }}
          </el-descriptions-item>
          <el-descriptions-item label="难度等级">
            <el-tag :type="difficultyTagType(currentNode.difficulty)" size="small">
              {{ difficultyLabel(currentNode.difficulty) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="前置知识点" v-if="currentNode.prerequisites">
            {{ currentNode.prerequisites }}
          </el-descriptions-item>
        </el-descriptions>
        <div class="detail-desc">
          <h4>详细描述</h4>
          <p>{{ currentNode.description || currentNode.desc || '暂无详细描述' }}</p>
        </div>
      </template>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goStudy(currentNode)">
          开始学习
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getStudentGraph } from '../../api/student'
import { getCourseList } from '../../api/knowledge'

const router = useRouter()
const loading = ref(false)
const nodes = ref([])
const courses = ref([])
const selectedCourseId = ref('')
const dialogVisible = ref(false)
const currentNode = ref(null)

const filteredNodes = computed(() => {
  if (!selectedCourseId.value) return nodes.value
  return nodes.value.filter(
    (n) => n.courseId === selectedCourseId.value || n.course_id === selectedCourseId.value
  )
})

const difficultyLabel = (level) => {
  const map = { 1: '入门', 2: '基础', 3: '进阶', 4: '困难', 5: '挑战' }
  return map[level] || '未知'
}

const difficultyTagType = (level) => {
  const map = { 1: 'success', 2: '', 3: 'warning', 4: 'danger', 5: 'danger' }
  return map[level] || 'info'
}

const truncate = (str, max) => {
  if (!str) return ''
  return str.length > max ? str.slice(0, max) + '...' : str
}

const filterNodes = () => {}

const openDetail = (node) => {
  currentNode.value = node
  dialogVisible.value = true
}

const goStudy = (node) => {
  dialogVisible.value = false
  router.push('/student/study/' + (node.nodeId || node.id))
}

const fetchGraph = async () => {
  loading.value = true
  try {
    const res = await getStudentGraph()
    if (res && res.nodes) {
      nodes.value = res.nodes
    } else if (Array.isArray(res)) {
      nodes.value = res
    }
  } catch {
    ElMessage.error('加载知识图谱失败')
  } finally {
    loading.value = false
  }
}

const fetchCourses = async () => {
  try {
    const res = await getCourseList()
    if (res && res.records) {
      courses.value = res.records
    } else if (Array.isArray(res)) {
      courses.value = res
    }
  } catch {
    // 课程列表加载失败不影响主体功能
  }
}

onMounted(() => {
  fetchGraph()
  fetchCourses()
})
</script>

<style scoped>
.graph-view {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 24px 32px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #2c5eb5;
}

.node-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  min-height: 200px;
}

.node-card {
  cursor: pointer;
  border-radius: 12px;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.node-card:hover {
  transform: translateY(-4px);
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.node-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.node-desc {
  font-size: 13px;
  color: #909399;
  margin: 0 0 10px;
  line-height: 1.6;
}

.node-meta {
  font-size: 12px;
  color: #c0c4cc;
}

.detail-desc {
  margin-top: 20px;
}

.detail-desc h4 {
  margin: 0 0 8px;
  font-size: 15px;
  color: #303133;
}

.detail-desc p {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
