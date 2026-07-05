<template>
  <div class="page-container">
    <StudentHeader title="学生学习路径督导" subtitle="教师查看学生的学习路径与进度">
      <template #actions>
        <el-button type="primary" @click="$router.push('/teacher/analysis')" plain>
          <el-icon :size="14"><DataAnalysis /></el-icon>
          返回学情分析
        </el-button>
      </template>
    </StudentHeader>

    <!-- 学生筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="选择学生">
          <el-select
            v-model="selectedStudentId"
            placeholder="请选择学生"
            filterable
            clearable
            @change="loadStudentPaths"
            style="width: 280px"
          >
            <el-option
              v-for="s in studentList"
              :key="s.id"
              :label="s.realName || s.username || '学生' + s.id"
              :value="s.id"
            >
              <span>{{ s.realName || s.username || '学生' + s.id }}</span>
              <span style="float: right; color: #909399; font-size: 12px;">ID: {{ s.id }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStudentPaths" :disabled="!selectedStudentId">查询路径</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 路径汇总 -->
    <el-card v-if="studentInfo" class="summary-card">
      <div class="summary-row">
        <div class="summary-item">
          <span class="summary-label">学生</span>
          <span class="summary-value">{{ studentInfo.studentName }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">学习路径数</span>
          <span class="summary-value highlight">{{ pathList.length }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">已完成路径</span>
          <span class="summary-value highlight green">{{ completedCount }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">进行中路径</span>
          <span class="summary-value highlight orange">{{ activeCount }}</span>
        </div>
      </div>
    </el-card>

    <!-- 路径列表 -->
    <el-card class="table-card">
      <template #header>
        <span class="panel-title">学习路径列表</span>
      </template>

      <el-table v-loading="loading" :data="pathList" stripe empty-text="请先选择学生查询学习路径">
        <el-table-column prop="pathName" label="路径名称" min-width="180">
          <template #default="{ row }">
            <span class="path-name">{{ row.pathName || '未命名路径' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalNodes" label="总节点数" width="100" align="center" />
        <el-table-column label="进度" width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="row.progress || 0"
              :stroke-width="10"
              :status="row.progress === 100 ? 'success' : undefined"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'completed' ? 'success' : 'primary'" size="small">
              {{ row.status === 'completed' ? '已完成' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="viewPathDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 路径详情对话框 -->
    <el-dialog
      v-model="detailDialog"
      :title="'学习路径详情 - ' + (currentPath?.pathName || '')"
      width="700px"
      destroy-on-close
    >
      <div v-loading="detailLoading">
        <template v-if="pathDetail">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="路径名称">{{ pathDetail.pathName }}</el-descriptions-item>
            <el-descriptions-item label="总节点数">{{ pathDetail.totalNodes }}</el-descriptions-item>
            <el-descriptions-item label="总预计时长">{{ pathDetail.totalMinutes || '-' }} 分钟</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="pathDetail.status === 1 ? 'success' : 'primary'" size="small">
                {{ pathDetail.status === 1 ? '已完成' : '进行中' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="AI总览" :span="2">{{ pathDetail.aiSummary || '无' }}</el-descriptions-item>
          </el-descriptions>

          <h4 style="margin: 16px 0 8px; color: #303133;">学习节点列表</h4>
          <el-table :data="pathDetail.nodes || []" size="small" stripe>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="name" label="知识点名称" min-width="140" />
            <el-table-column prop="difficulty" label="难度" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="difficultyTagType(row.difficulty)" size="small">
                  {{ difficultyLabel(row.difficulty) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="完成状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'completed' ? 'success' : 'info'" size="small">
                  {{ row.status === 'completed' ? '已掌握' : '学习中' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </template>
        <el-empty v-else description="暂无数据" :image-size="60" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DataAnalysis } from '@element-plus/icons-vue'
import StudentHeader from '../../components/StudentHeader.vue'
import request from '../../utils/request'
import { getStudentPaths } from '../../api/teacher'

const loading = ref(false)
const detailLoading = ref(false)
const selectedStudentId = ref(null)
const studentList = ref([])
const pathList = ref([])
const studentInfo = ref(null)
const detailDialog = ref(false)
const currentPath = ref(null)
const pathDetail = ref(null)

const completedCount = computed(() => pathList.value.filter(p => p.status === 'completed').length)
const activeCount = computed(() => pathList.value.filter(p => p.status === 'active').length)

const difficultyLabel = (level) => {
  const map = { 1: '入门', 2: '基础', 3: '进阶', 4: '困难', 5: '挑战' }
  return map[level] || '未知'
}

const difficultyTagType = (level) => {
  const map = { 1: 'success', 2: 'info', 3: 'warning', 4: 'danger', 5: 'danger' }
  return map[level] || 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const loadStudents = async () => {
  try {
    // 获取所有学生列表（教师端可以看到学生）
    const res = await request.get('/teacher/analysis/students')
    studentList.value = Array.isArray(res) ? res : (res.records || res.data || [])
  } catch {
    // 如果带 roleId 过滤不支持，尝试获取全部用户再过滤
    try {
      const res = await request.get('/teacher/analysis/students')
      const all = Array.isArray(res) ? res : (res.records || res.data || [])
      studentList.value = all.filter(u => u.roleId === 1 || u.role?.id === 1)
    } catch {
      studentList.value = []
    }
  }
}

const loadStudentPaths = async () => {
  if (!selectedStudentId.value) {
    ElMessage.warning('请先选择学生')
    return
  }
  loading.value = true
  pathList.value = []
  studentInfo.value = null
  try {
    const res = await getStudentPaths(selectedStudentId.value)
    if (res) {
      const data = res.data || res
      studentInfo.value = {
        studentId: data.studentId || res.studentId,
        studentName: data.studentName || res.studentName
      }
      pathList.value = Array.isArray(data.paths || res.paths) ? (data.paths || res.paths) : []
    }
  } catch {
    ElMessage.error('查询学生学习路径失败')
  } finally {
    loading.value = false
  }
}

const viewPathDetail = async (row) => {
  currentPath.value = row
  detailDialog.value = true
  detailLoading.value = true
  pathDetail.value = null
  try {
    // 从学生路径详情接口获取详情
    const { getPathDetail } = await import('../../api/student')
    const res = await getPathDetail(row.id)
    pathDetail.value = res
  } catch {
    ElMessage.error('加载路径详情失败')
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadStudents()
})
</script>

<style scoped>
.page-container { padding: 20px 24px; background: #f5f7fa; min-height: 100vh; }
.filter-card { margin-bottom: 16px; border-radius: 12px; }
.summary-card { margin-bottom: 16px; border-radius: 12px; }
.summary-row { display: flex; gap: 24px; }
.summary-item { flex: 1; text-align: center; padding: 8px 0; }
.summary-label { display: block; font-size: 13px; color: #909399; margin-bottom: 4px; }
.summary-value { display: block; font-size: 20px; font-weight: 700; color: #303133; }
.summary-value.highlight { color: #409eff; font-size: 24px; }
.summary-value.highlight.green { color: #67c23a; }
.summary-value.highlight.orange { color: #e6a23c; }
.table-card { border-radius: 12px; }
.panel-title { font-weight: 600; color: #303133; }
.path-name { font-weight: 500; color: #303133; }
</style>
