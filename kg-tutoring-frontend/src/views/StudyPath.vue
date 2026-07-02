<template>
  <div class="page">
    <header class="top-bar">
      <h2 @click="$router.push('/student')" style="cursor:pointer">学习路径</h2>
    </header>
    <div class="content">
      <el-card v-if="!paths.length"><el-empty description="暂无学习路径，去知识图谱生成一个吧" :image-size="80" /></el-card>
      <div v-else class="path-list">
        <el-card v-for="p in paths" :key="p.id" class="path-card" @click="viewDetail(p.id)">
          <div class="path-header">
            <span class="path-title">{{ p.pathName || '学习路径' }}</span>
            <el-tag :type="p.status === 1 ? 'success' : 'warning'" size="small">
              {{ p.status === 1 ? '已完成' : '进行中' }}
            </el-tag>
          </div>
          <el-progress :percentage="p.progress || 0" :stroke-width="8" style="margin:12px 0" />
          <div class="path-meta">
            <span>{{ p.finishedNodes || 0 }}/{{ p.totalNodes || 0 }} 节点</span>
            <span>预计 {{ p.totalMinutes || 0 }} 分钟</span>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStudyPaths } from '../api/student'

const paths = ref([])
onMounted(async () => {
  try { paths.value = (await getStudyPaths()) || [] } catch { }
})

const viewDetail = (id) => {}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fa; }
.top-bar { display: flex; align-items: center; padding: 16px 32px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.top-bar h2 { margin: 0; font-size: 18px; color: #2c5eb5; }
.content { padding: 24px 32px; max-width: 800px; }
.path-list { display: flex; flex-direction: column; gap: 16px; }
.path-card { border-radius: 12px; cursor: pointer; transition: box-shadow 0.2s; }
.path-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
.path-header { display: flex; justify-content: space-between; align-items: center; }
.path-title { font-weight: 600; color: #333; }
.path-meta { display: flex; gap: 24px; color: #aaa; font-size: 13px; }
</style>
