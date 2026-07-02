<template>
  <div class="page">
    <header class="top-bar">
      <h2 @click="$router.push('/student')" style="cursor:pointer">知识图谱</h2>
      <el-select v-model="courseId" placeholder="按课程筛选" clearable @change="fetchData" style="width:200px">
        <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
      </el-select>
    </header>
    <div class="content">
      <el-card v-if="!nodes.length"><el-empty description="暂无知识点数据" :image-size="80" /></el-card>
      <div v-else class="node-grid">
        <el-card v-for="n in nodes" :key="n.id" class="node-card" :class="'diff-' + n.difficulty">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span>{{ n.name }}</span>
              <el-tag size="small" :type="n.difficulty === 3 ? 'danger' : n.difficulty === 2 ? 'warning' : 'success'">
                {{ ['', '基础', '进阶', '困难'][n.difficulty] }}
              </el-tag>
            </div>
          </template>
          <p class="node-desc">{{ n.description || '暂无简介' }}</p>
          <p class="node-extra">预计 {{ n.expectedMinutes || 30 }} 分钟 · 第{{ n.chapter || '?' }}章</p>
          <el-button type="primary" size="small" @click="$router.push('/student/path/' + n.id)">生成学习路径</el-button>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getKnowledgeNodes } from '../api/student'
import request from '../utils/request'

const nodes = ref([])
const courses = ref([])
const courseId = ref(null)

onMounted(async () => {
  try { courses.value = (await request.get('/student/courses')) || [] } catch { }
  fetchData()
})

const fetchData = async () => {
  try { nodes.value = (await getKnowledgeNodes({ courseId: courseId.value })) || [] } catch { }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fa; }
.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 16px 32px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.top-bar h2 { margin: 0; font-size: 18px; color: #2c5eb5; }
.content { padding: 24px 32px; }
.node-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }
.node-card { border-radius: 12px; }
.node-desc { color: #666; font-size: 14px; margin-bottom: 8px; }
.node-extra { color: #aaa; font-size: 12px; margin-bottom: 12px; }
</style>
