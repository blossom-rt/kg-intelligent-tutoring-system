<template>
  <div class="page">
    <header class="top-bar">
      <div class="top-left">
        <span class="back-btn" @click="$router.push('/student')">&#8592; 返回</span>
        <h2>知识图谱</h2>
      </div>
      <el-select v-model="courseId" placeholder="全部课程" clearable @change="fetchData" style="width:200px" size="large">
        <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
      </el-select>
    </header>
    <div class="content">
      <div v-if="!nodes.length" class="empty-full">
        <div class="empty-circle">N</div>
        <p>暂无知识点</p>
        <span>请教师先在后台添加知识点数据</span>
      </div>
      <div v-else class="node-grid">
        <div v-for="n in nodes" :key="n.id" class="node-card" @click="$router.push('/student/path/' + n.id)">
          <div class="node-top">
            <h3>{{ n.name }}</h3>
            <el-tag size="small" :type="n.difficulty===3?'danger':n.difficulty===2?'warning':''" round>
              {{ ['','基础','进阶','困难'][n.difficulty] || '基础' }}
            </el-tag>
          </div>
          <p class="node-desc">{{ n.description || '暂无内容简介' }}</p>
          <div class="node-meta">
            <span>&#9711; {{ n.chapter || '未分类' }}</span>
            <span>&#9202; {{ n.expectedMinutes || 30 }}分钟</span>
          </div>
          <div class="node-action">
            <span class="action-text">生成学习路径</span>
            <span class="action-arrow">&#8594;</span>
          </div>
        </div>
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
.page { min-height: 100vh; background: #faf7f2; }
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 36px; background: #fffdf9; box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.top-left { display: flex; align-items: center; gap: 16px; }
.back-btn { color: #ff7b3d; cursor: pointer; font-size: 14px; }
.top-bar h2 { margin: 0; font-size: 18px; color: #2d2a26; }
.content { padding: 28px 36px; }

.empty-full { text-align: center; padding: 80px 0; }
.empty-circle {
  width: 72px; height: 72px; border-radius: 50%; background: #f3efe8;
  display: flex; align-items: center; justify-content: center; margin: 0 auto 16px;
  font-size: 28px; color: #ff7b3d; font-weight: 700;
}
.empty-full p { font-size: 16px; color: #6b655e; margin: 0 0 6px; }
.empty-full span { font-size: 13px; color: #a09a92; }

.node-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 20px; }
.node-card {
  background: #fffdf9; border-radius: 16px; padding: 24px; cursor: pointer;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: all 0.25s;
}
.node-card:hover { transform: translateY(-4px); box-shadow: 0 8px 28px rgba(0,0,0,0.08); }
.node-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.node-top h3 { margin: 0; font-size: 17px; color: #2d2a26; }
.node-desc { color: #777; font-size: 14px; line-height: 1.6; min-height: 40px; margin-bottom: 14px; }
.node-meta { display: flex; gap: 20px; color: #bbb; font-size: 13px; margin-bottom: 16px; }
.node-action {
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 14px; border-top: 1px solid #f0f3f7;
}
.action-text { color: #ff7b3d; font-size: 14px; font-weight: 500; }
.action-arrow { color: #ff7b3d; font-size: 18px; transition: transform 0.2s; }
.node-card:hover .action-arrow { transform: translateX(4px); }
</style>
