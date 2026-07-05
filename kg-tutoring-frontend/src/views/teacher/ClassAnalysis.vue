<template>
  <div class="page-container">
    <StudentHeader title="班级学情统计" />

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" @submit.prevent>
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

    <!-- ECharts 图表区域 -->
    <div v-if="filterForm.courseId" class="charts-row">
      <el-card class="chart-card">
        <template #header><span class="panel-title">掌握度分布</span></template>
        <div ref="pieChartRef" class="chart-container"></div>
      </el-card>
      <el-card class="chart-card">
        <template #header><span class="panel-title">知识点平均正确率 TOP15</span></template>
        <div ref="barChartRef" class="chart-container"></div>
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
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import StudentHeader from '../../components/StudentHeader.vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getClassAnalysis } from '../../api/teacher'
import { getCourseList } from '../../api/knowledge'

const loading = ref(false)
const tableData = ref([])
const courseList = ref([])
const weakNodes = ref([])
const masteryDistribution = ref([])
const nodeCorrectRates = ref([])

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
  activeStudents: 0,
  totalNodes: 0
})

// ECharts
const pieChartRef = ref(null)
const barChartRef = ref(null)
let pieChartInstance = null
let barChartInstance = null

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

/** 渲染掌握度分布饼图 */
const renderPieChart = (distribution) => {
  if (!pieChartRef.value) return
  if (!pieChartInstance) {
    pieChartInstance = echarts.init(pieChartRef.value)
  }
  const colors = ['#f56c6c', '#e6a23c', '#409eff', '#67c23a']
  pieChartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { fontSize: 12, color: '#606266' }
    },
    series: [{
      type: 'pie',
      radius: ['35%', '60%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' }
      },
      data: (distribution || []).map((d, i) => ({
        ...d,
        itemStyle: { color: colors[i % colors.length] }
      }))
    }]
  })
}

/** 渲染知识点平均正确率柱状图 */
const renderBarChart = (rates) => {
  if (!barChartRef.value) return
  if (!barChartInstance) {
    barChartInstance = echarts.init(barChartRef.value)
  }
  const list = (rates || []).slice(0, 15)
  const names = list.map(r => r.nodeName || '未知')
  const values = list.map(r => r.correctRate || 0)

  barChartInstance.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>正确率: {c}%' },
    grid: { left: '3%', right: '8%', bottom: '15%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { rotate: 35, fontSize: 10, interval: 0, color: '#606266' },
      axisLine: { lineStyle: { color: '#dcdfe6' } }
    },
    yAxis: {
      type: 'value',
      name: '正确率(%)',
      min: 0,
      max: 100,
      axisLabel: { formatter: '{value}%', color: '#909399' },
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      barWidth: '55%',
      data: values.map(v => ({
        value: v,
        itemStyle: {
          color: v >= 80 ? '#67c23a' : v >= 60 ? '#409eff' : v >= 40 ? '#e6a23c' : '#f56c6c',
          borderRadius: [4, 4, 0, 0]
        }
      })),
      label: {
        show: true,
        position: 'top',
        formatter: '{c}%',
        fontSize: 10,
        color: '#606266'
      }
    }]
  })
}

/** 销毁图表实例 */
const disposeCharts = () => {
  if (pieChartInstance) { pieChartInstance.dispose(); pieChartInstance = null }
  if (barChartInstance) { barChartInstance.dispose(); barChartInstance = null }
}

const loadData = async () => {
  if (!filterForm.courseId) {
    tableData.value = []
    weakNodes.value = []
    masteryDistribution.value = []
    nodeCorrectRates.value = []
    Object.assign(stats, { totalStudents: 0, avgMastery: 0, avgCorrectRate: 0, activeStudents: 0 })
    pagination.total = 0
    disposeCharts()
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
      const allStudents = Array.isArray(res) ? res : (res.studentList || res.records || res.data || [])
      pagination.total = allStudents.length
      const start = (pagination.page - 1) * pagination.size
      tableData.value = allStudents.slice(start, start + pagination.size)
      weakNodes.value = (res.weakNodes || []).slice(0, 5)

      // 图表数据
      masteryDistribution.value = res.masteryDistribution || []
      nodeCorrectRates.value = res.nodeCorrectRates || []

      // 渲染图表
      await nextTick()
      renderPieChart(masteryDistribution.value)
      renderBarChart(nodeCorrectRates.value)
    }
  } catch {
    tableData.value = []
    weakNodes.value = []
    masteryDistribution.value = []
    nodeCorrectRates.value = []
    disposeCharts()
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  ElMessage.info(`查看学生「${row.studentName}」的详细分析（功能待扩展）`)
}

// 窗口大小变化时自适应
const handleResize = () => {
  if (pieChartInstance) pieChartInstance.resize()
  if (barChartInstance) barChartInstance.resize()
}

onMounted(() => {
  loadCourses()
  window.addEventListener('resize', handleResize)
})

// 组件卸载前清理
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})
</script>

<style scoped>
.page-container { padding: 20px 24px; background: #f5f7fa; min-height: 100vh; }

/* 统计卡片 */
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card { border-radius: 12px; text-align: center; padding: 8px 0; }
.stat-value { font-size: 28px; font-weight: 700; }
.stat-value.blue { color: #409eff; }
.stat-value.green { color: #67c23a; }
.stat-value.orange { color: #e6a23c; }
.stat-value.purple { color: #7b1fa2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }

/* 图表区域 */
.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
.chart-card { border-radius: 12px; }
.chart-container { width: 100%; height: 320px; }

/* 面板 */
.data-panels { display: flex; gap: 16px; }
.panel-main { flex: 1; border-radius: 12px; }
.panel-side { width: 320px; flex-shrink: 0; border-radius: 12px; }
.panel-title { font-weight: 600; color: #303133; }
.weak-list { display: flex; flex-direction: column; gap: 12px; }
.weak-item { display: flex; align-items: center; gap: 10px; }
.weak-rank {
  width: 24px; height: 24px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 12px; font-weight: 600; flex-shrink: 0;
}
.rank-1 { background: #f56c6c; }
.rank-2 { background: #e6943b; }
.rank-3 { background: #e6a23c; }
.rank-4, .rank-5 { background: #909399; }
.weak-info { flex: 1; min-width: 0; }
.weak-name { font-size: 13px; color: #6b655e; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* 筛选卡片 */
.filter-card { margin-bottom: 16px; border-radius: 12px; }

@media (max-width: 1024px) {
  .charts-row { grid-template-columns: 1fr; }
  .data-panels { flex-direction: column; }
  .panel-side { width: 100%; }
}
</style>
