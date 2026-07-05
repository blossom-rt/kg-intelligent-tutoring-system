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
        <template #header><span class="panel-title">知识点平均正确率 TOP8</span></template>
        <div ref="barChartRef" class="chart-container"></div>
      </el-card>
    </div>

    <!-- 薄弱知识点排行 -->
    <el-card v-if="weakRank.length" class="section-card">
      <template #header><span class="panel-title">薄弱知识点排行 TOP8</span></template>
      <div class="weak-list">
        <div v-for="(item, idx) in weakRank" :key="idx" class="weak-item">
          <span class="weak-rank" :class="'rank-' + Math.min(idx + 1, 5)" style="width:24px;height:24px;border-radius:50%;display:inline-flex;align-items:center;justify-content:center;color:#fff;font-size:12px;">{{ idx + 1 }}</span>
          <span class="weak-name" style="flex:1;font-size:13px;">{{ item.nodeName }}</span>
          <el-progress :percentage="item.correctRate || 0" :stroke-width="6" style="flex:2" :color="weakColor(item.correctRate || 0)" />
          <span style="width:40px;text-align:right;font-size:12px;color:#909399;">{{ item.studentCount }}人</span>
        </div>
      </div>
    </el-card>

    <div class="data-panels">
      <!-- 学生列表 -->
      <el-card class="panel-main">
        <template #header><span class="panel-title">学生学情明细</span></template>
        <el-table :data="paginatedTableData" v-loading="loading" stripe border>
          <el-table-column prop="userId" label="学生ID" min-width="80" />
          <el-table-column prop="studentName" label="学生姓名" min-width="100" />
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
          style="margin-top: 16px; justify-content: flex-end"
        />
      </el-card>

    </div>

    <el-dialog v-model="detailDialog" :title="'学生详情 - ' + (studentDetail?.studentName || '')" width="600px">
      <template v-if="studentDetail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="学生ID">{{ studentDetail.userId }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ studentDetail.studentName }}</el-descriptions-item>
          <el-descriptions-item label="掌握度">{{ studentDetail.masteryLevel || 0 }}</el-descriptions-item>
          <el-descriptions-item label="正确率">{{ studentDetail.correctRate || 0 }}%</el-descriptions-item>
          <el-descriptions-item label="学习时长">{{ studentDetail.studyMinutes || 0 }} 分钟</el-descriptions-item>
          <el-descriptions-item label="趋势">
            <el-button type="primary" size="small" :loading="trendLoading" @click="loadTrend(studentDetail.userId)">加载趋势图</el-button>
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="trendVisible" ref="trendChartRef" style="width:100%;height:250px;margin-top:16px;"></div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch, computed } from 'vue'
import StudentHeader from '../../components/StudentHeader.vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getClassAnalysis, getStudentTrend } from '../../api/teacher'
import { getCourseList } from '../../api/knowledge'

const loading = ref(false)
const tableData = ref([])
const rawStudentList = ref([])
const courseList = ref([])
const weakNodes = ref([])
const masteryDistribution = ref([])
const nodeCorrectRates = ref([])
const studyTrend = ref([])
const weakRank = ref([])

const filterForm = reactive({
  courseId: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const paginatedTableData = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return rawStudentList.value.slice(start, start + pagination.size)
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
  const list = (rates || []).slice(0, 8)
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
      rawStudentList.value = allStudents
      pagination.total = allStudents.length
      weakNodes.value = (res.weakNodes || []).slice(0, 5)

      // 图表数据
      masteryDistribution.value = res.masteryDistribution || []
      nodeCorrectRates.value = res.nodeCorrectRates || []
      studyTrend.value = res.studyTrend || []
      weakRank.value = res.weakRank || []

      // 渲染图表
      await nextTick()
      renderPieChart(masteryDistribution.value)
      renderBarChart(nodeCorrectRates.value)
    }
  } catch {
    tableData.value = []
    rawStudentList.value = []
    weakNodes.value = []
    masteryDistribution.value = []
    nodeCorrectRates.value = []
    disposeCharts()
  } finally {
    loading.value = false
  }
}

const studentDetail = ref(null)
const detailDialog = ref(false)
const trendLoading = ref(false)
const trendVisible = ref(false)
const trendChartRef = ref(null)

const loadTrend = async (userId) => {
  trendLoading.value = true
  trendVisible.value = true
  try {
    const res = await getStudentTrend(userId)
    await nextTick()
    if (!trendChartRef.value) return
    const chart = echarts.init(trendChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '5%', right: '5%', bottom: '15%', top: '10%', containLabel: true },
      xAxis: { type: 'category', data: (res || []).map(d => d.date.slice(5)), axisLabel: { fontSize: 11 } },
      yAxis: { type: 'value', minInterval: 1, name: '学习记录数' },
      series: [{
        type: 'line', smooth: true, data: (res || []).map(d => d.studyCount || 0),
        areaStyle: { color: 'rgba(64,158,255,0.15)' },
        lineStyle: { color: '#409eff', width: 2 },
        itemStyle: { color: '#409eff' }
      }]
    })
  } catch { /* ignore */ }
  finally { trendLoading.value = false }
}

const viewDetail = (row) => {
  studentDetail.value = row
  detailDialog.value = true
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
