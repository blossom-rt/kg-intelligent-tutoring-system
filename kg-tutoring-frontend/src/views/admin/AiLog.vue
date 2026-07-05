<template>
  <div class="ai-log">
    <StudentHeader title="AI 调用日志" />

    <el-card class="table-card">
      <el-table :data="paginatedLogList" v-loading="loading" border stripe>
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-content">
              <div class="expand-section">
                <h4>提示词 (Prompt)</h4>
                <pre class="code-block">{{ row.prompt || '无' }}</pre>
              </div>
              <div class="expand-section">
                <h4>结果 (Result)</h4>
                <pre class="code-block">{{ row.result || '无' }}</pre>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="调用用户" min-width="100" /><el-table-column prop="userId" label="ID" min-width="60" />
        <el-table-column prop="scene" label="场景" min-width="140">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ sceneLabel(row.scene) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="callDuration" label="耗时(ms)" min-width="100" />
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" min-width="160" />
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
import { getAiLogs } from '../../api/admin'

const loading = ref(false)
const logList = ref([])
const paginatedLogList = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return logList.value.slice(start, start + pagination.pageSize)
})

const pagination = reactive({
  page: 1,
  pageSize: 15,
  total: 0
})

function sceneLabel(scene) {
  const map = {
    'node_summary': '知识点总结',
    'wrong_explain': '错题讲解',
    'exam_report': '测评报告',
    'question_generate': '题目生成'
  }
  return map[scene] || scene || '-'
}

onMounted(() => {
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    const res = await getAiLogs(params)
    if (Array.isArray(res)) { logList.value = res; pagination.total = res.length }
    else if (res && res.records) { logList.value = res.records; pagination.total = res.total || 0 }
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-log { padding: 24px 36px; background: var(--bg-root); min-height: 100vh; }
.page-header h2 { margin: 0 0 16px; font-size: 20px; color: var(--text-primary); }
.table-card { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }

.expand-content { padding: 16px 24px; }
.expand-section { margin-bottom: 16px; }
.expand-section h4 { margin: 0 0 8px; font-size: 14px; color: var(--text-primary); }
.code-block {
  background: var(--bg-root);
  padding: 12px 16px;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 300px;
  overflow-y: auto;
  color: var(--text-primary);
  margin: 0;
}
</style>
