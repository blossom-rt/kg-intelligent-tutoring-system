<template>
  <div class="ai-log">
    <StudentHeader title="AI 调用日志" />

    <el-card class="table-card">
      <el-table :data="logList" v-loading="loading" border stripe>
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
        <el-table-column prop="userId" label="用户ID" min-width="120" />
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
        @current-change="fetchList"
        @size-change="fetchList"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAiLogs } from '../../api/admin'

const loading = ref(false)
const logList = ref([])

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
.ai-log { padding: 20px; background: #faf7f2; min-height: 100vh; }
.page-header h2 { margin: 0 0 16px; font-size: 20px; color: #2d2a26; }
.table-card { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }

.expand-content { padding: 16px 24px; }
.expand-section { margin-bottom: 16px; }
.expand-section h4 { margin: 0 0 8px; font-size: 14px; color: #2d2a26; }
.code-block {
  background: #faf7f2;
  padding: 12px 16px;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 300px;
  overflow-y: auto;
  color: #2d2a26;
  margin: 0;
}
</style>
