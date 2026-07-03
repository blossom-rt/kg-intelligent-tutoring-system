<template>
  <div class="page">
    <header class="top-bar">
      <div class="top-left">
        <span class="back-btn" @click="$router.push('/student')">&#8592; 返回</span>
        <h2>错题本</h2>
      </div>
      <el-select v-model="nodeFilter" placeholder="全部知识点" clearable @change="fetchData" style="width:200px" size="large">
        <el-option v-for="n in nodeList" :key="n.id" :label="n.name" :value="n.id" />
      </el-select>
    </header>
    <div class="content">
      <div v-if="!wrongList.length" class="empty-full">
        <div class="empty-circle">W</div>
        <p>暂无错题</p>
        <span>继续保持，你很棒</span>
      </div>
      <div v-else class="wrong-list">
        <div v-for="w in wrongList" :key="w.id" class="wrong-card">
          <div class="wrong-head">
            <el-tag type="danger" size="small" round>错误 {{ w.wrongCount }} 次</el-tag>
            <span class="wrong-time">{{ w.createTime }}</span>
          </div>
          <p class="wrong-content">{{ w.questionContent || '题目加载中...' }}</p>
          <div class="wrong-bottom">
            <span class="your-answer">你的答案：<em>{{ w.wrongAnswer || '未作答' }}</em></span>
            <el-button type="primary" size="small" round>重新作答</el-button>
          </div>
        </div>
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
.page { min-height: 100vh; background: #faf7f2; }
.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 16px 36px; background: #fffdf9; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.top-left { display: flex; align-items: center; gap: 16px; }
.back-btn { color: #ff7b3d; cursor: pointer; font-size: 14px; }
.top-bar h2 { margin: 0; font-size: 18px; color: #2d2a26; }
.content { padding: 28px 36px; max-width: 800px; }

.empty-full { text-align: center; padding: 80px 0; }
.empty-circle { width: 72px; height: 72px; border-radius: 50%; background: #f3efe8; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; font-size: 28px; color: #ff7b3d; font-weight: 700; }
.empty-full p { font-size: 16px; color: #6b655e; margin: 0 0 6px; }
.empty-full span { font-size: 13px; color: #a09a92; }

.wrong-list { display: flex; flex-direction: column; gap: 16px; }
.wrong-card { background: #fffdf9; border-radius: 16px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); transition: box-shadow 0.2s; }
.wrong-card:hover { box-shadow: 0 6px 24px rgba(0,0,0,0.08); }
.wrong-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.wrong-time { color: #ccc; font-size: 13px; }
.wrong-content { color: #2d2a26; font-size: 15px; line-height: 1.7; margin-bottom: 16px; }
.wrong-bottom { display: flex; justify-content: space-between; align-items: center; padding-top: 14px; border-top: 1px solid #f0f3f7; }
.your-answer { font-size: 13px; color: #a09a92; }
.your-answer em { color: #e06060; font-style: normal; }
</style>
