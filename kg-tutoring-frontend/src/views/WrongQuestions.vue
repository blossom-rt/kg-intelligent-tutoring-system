<template>
  <div class="page">
    <header class="top-bar">
      <h2 @click="$router.push('/student')" style="cursor:pointer">错题本</h2>
      <el-select v-model="nodeFilter" placeholder="按知识点筛选" clearable @change="fetchData" style="width:200px">
        <el-option v-for="n in nodeList" :key="n.id" :label="n.name" :value="n.id" />
      </el-select>
    </header>
    <div class="content">
      <el-card v-if="!wrongList.length"><el-empty description="暂无错题，继续保持" :image-size="80" /></el-card>
      <div v-else class="wrong-list">
        <el-card v-for="w in wrongList" :key="w.id" class="wrong-card">
          <div class="wrong-header">
            <el-tag type="danger" size="small">错误 {{ w.wrongCount }} 次</el-tag>
            <span class="wrong-time">{{ w.createTime }}</span>
          </div>
          <p class="wrong-content">{{ w.questionContent || '题目内容加载中...' }}</p>
          <p class="wrong-answer">你的答案：{{ w.wrongAnswer || '无' }}</p>
          <el-button type="primary" size="small">重新作答</el-button>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWrongQuestions, getKnowledgeNodes } from '../api/student'

const wrongList = ref([])
const nodeList = ref([])
const nodeFilter = ref(null)

onMounted(async () => {
  try { nodeList.value = (await getKnowledgeNodes()) || [] } catch { }
  fetchData()
})

const fetchData = async () => {
  try { wrongList.value = (await getWrongQuestions({ nodeId: nodeFilter.value })) || [] } catch { }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fa; }
.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 16px 32px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.top-bar h2 { margin: 0; font-size: 18px; color: #2c5eb5; }
.content { padding: 24px 32px; max-width: 800px; }
.wrong-list { display: flex; flex-direction: column; gap: 16px; }
.wrong-card { border-radius: 12px; }
.wrong-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.wrong-time { color: #bbb; font-size: 13px; }
.wrong-content { color: #333; font-size: 15px; margin-bottom: 8px; }
.wrong-answer { color: #e06060; font-size: 13px; margin-bottom: 12px; }
</style>
