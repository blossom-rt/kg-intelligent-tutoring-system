<template>
  <div class="oper-log">
    <StudentHeader title="操作日志查询" />

    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" @submit.prevent>
        <el-form-item label="操作模块">
          <el-select v-model="filterForm.module" placeholder="全部模块" clearable style="width: 160px">
            <el-option label="用户管理" value="用户管理" />
            <el-option label="角色管理" value="角色管理" />
            <el-option label="课程管理" value="课程管理" />
            <el-option label="认证管理" value="认证管理" />
            <el-option label="公告管理" value="公告管理" />
            <el-option label="测评管理" value="测评管理" />
            <el-option label="题库管理" value="题库管理" />
            <el-option label="知识点管理" value="知识点管理" />
            <el-option label="知识图谱" value="知识图谱" />
            <el-option label="主题管理" value="主题管理" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card">
      <el-table :data="paginatedLogList" v-loading="loading" border stripe>
        <el-table-column prop="userName" label="操作人" min-width="100" /><el-table-column prop="userId" label="ID" min-width="60" />
        <el-table-column prop="module" label="模块" min-width="120" />
        <el-table-column prop="operation" label="操作内容" min-width="280" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" min-width="140" />
        <el-table-column prop="createTime" label="操作时间" min-width="160" />
      </el-table>
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, prev, pager, next"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import StudentHeader from '../../components/StudentHeader.vue'
import { ref, reactive, onMounted, computed } from 'vue'
import { getOperLogs } from '../../api/admin'

const loading = ref(false)
const logList = ref([])
const paginatedLogList = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return logList.value.slice(start, start + pagination.pageSize)
})

const filterForm = reactive({
  module: '',
  dateRange: []
})

const pagination = reactive({
  page: 1,
  pageSize: 15,
  total: 0
})

onMounted(() => {
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    if (filterForm.module) params.module = filterForm.module
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    const res = await getOperLogs(params)
    if (Array.isArray(res)) { logList.value = res; pagination.total = res.length }
    else if (res && res.records) { logList.value = res.records; pagination.total = res.total || 0 }
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchList()
}

function handleReset() {
  filterForm.module = ''
  filterForm.dateRange = []
  pagination.page = 1
  fetchList()
}
</script>

<style scoped>
.oper-log { padding: 24px 36px; background: var(--bg-root); min-height: 100vh; }
.page-header h2 { margin: 0 0 16px; font-size: 20px; color: var(--text-primary); }
.filter-card { margin-bottom: 16px; }
.table-card { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
