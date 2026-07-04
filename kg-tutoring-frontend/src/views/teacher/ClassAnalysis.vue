<template>
  <div class="page-container">
    <StudentHeader title="班级学情统计" />

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="选择课程">
          <el-select v-model="filterForm.courseId" placeholder="请选择课程" clearable @change="loadData" style="width: 240px">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">刷新数据</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card">
        <div class="stat-value blue">{{ stats.totalStudents }}</div>
        <div class="stat-label">学生总数</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value green">{{ stats.avgMastery }}%</div>
        <div class="stat-label">平均掌握度</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value orange">{{ stats.avgCorrectRate }}%</div>
        <div class="stat-label">平均正确率</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value purple">{{ stats.activeStudents }}</div>
        <div class="stat-label">活跃学生数</div>
      </el-card>
    </div>

    <div class="data-panels">
      <!-- 学生列表 -->
      <el-card class="panel-main">
        <template #header><span class="panel-title">学生学情明细</span></template>
        <el-table :data="tableData" v-loading="loading" stripe border>
          <el-table-column prop="userId" label="学生ID" min-width="120" />
          <el-table-column prop="masteryLevel" label="掌握度" width="130" align="center" />
          <el-table-column label="正确率" width="100" align="center">
            <template #default="{ row }">
              <el-progress :percentage="row.correctRate || 0" :stroke-width="8" :show-text="false" />
              <span style="font-size: 12px; color: #666">{{ row.correctRate || 0 }}%</span>
            </template>
          </el-table-column>
          <el-table-column prop="studyMinutes" label="学习时长(分)" width="120" align="center" />
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" link @click="viewDetail(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="loadData"
          style="margin-top: 16px; justify-content: flex-end"
        />
      </el-card>

      <!-- 薄弱知识点 -->
      <el-card class="panel-side">
        <template #header><span class="panel-title">薄弱知识点 TOP5</span></template>
        <div v-if="weakNodes.length" class="weak-list">
          <div v-for="(item, idx) in weakNodes" :key="idx" class="weak-item">
            <div class="weak-rank" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</div>
            <div class="weak-info">
              <div class="weak-name">{{ item.nodeName }}</div>
              <el-progress
                :percentage="item.masteryRate || 0"
                :stroke-width="6"
                :color="weakColor(item.masteryRate || 0)"
              />
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无数据" :image-size="60" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getClassAnalysis } from '../../api/teacher'
import { getCourseList } from '../../api/knowledge'

const loading = ref(false)
const tableData = ref([])
const courseList = ref([])
const weakNodes = ref([])

const filterForm = reactive({
  courseId: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const stats = reactive({
  totalStudents: 0,
  avgMastery: 0,
  avgCorrectRate: 0,
  activeStudents: 0
})

const weakColor = (rate) => {
  if (rate < 30) return '#f56c6c'
  if (rate < 60) return '#e6a23c'
  return '#67c23a'
}

const loadCourses = async () => {
  try {
    const res = await getCourseList({ page: 1, size: 999 })
    courseList.value = Array.isArray(res) ? res : (res.records || res.data || [])
  } catch {
    courseList.value = []
  }
}

const loadData = async () => {
  if (!filterForm.courseId) {
    tableData.value = []
    weakNodes.value = []
    Object.assign(stats, { totalStudents: 0, avgMastery: 0, avgCorrectRate: 0, activeStudents: 0 })
    pagination.total = 0
    return
  }
  loading.value = true
  try {
    const params = {
      courseId: filterForm.courseId,
      page: pagination.page,
      size: pagination.size
    }
    const res = await getClassAnalysis(params)
    if (res) {
      stats.totalStudents = res.totalStudents || 0
      stats.avgMastery = res.avgMastery || 0
      stats.avgCorrectRate = res.avgCorrectRate || 0
      stats.activeStudents = res.activeStudents || 0
      tableData.value = Array.isArray(res) ? res : (res.studentList || res.records || res.data || [])
      pagination.total = res.studentTotal || res.total || 0
      weakNodes.value = (res.weakNodes || []).slice(0, 5)
    }
  } catch {
    tableData.value = []
    weakNodes.value = []
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  ElMessage.info(`查看学生「${row.studentName}」的详细分析（功能待扩展）`)
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.page-container { padding: 20px 24px; background: #faf7f2; min-height: 100vh; }
.rank-1 { background: #f56c6c; }
.rank-2 { background: #e6943b; }
.rank-3 { background: #e6a23c; }
.rank-4, .rank-5 { background: #909399; }
.weak-info { flex: 1; min-width: 0; }
.weak-name { font-size: 13px; color: #6b655e; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
