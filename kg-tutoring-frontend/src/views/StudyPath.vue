<template>
  <div class="page">
    <header class="top-bar">
      <div class="top-left">
        <span class="back-btn" @click="$router.push('/student')">&#8592; 返回</span>
        <h2>学习路径</h2>
      </div>
    </header>
    <div class="content">
      <div v-if="!paths.length" class="empty-full">
        <div class="empty-circle">P</div>
        <p>暂无学习路径</p>
        <span>去知识图谱选择一个知识点，生成你的专属学习路径</span>
      </div>
      <div v-else class="path-list">
        <div v-for="p in paths" :key="p.id" class="path-card" @click="viewDetail(p.id)">
          <div class="path-left">
            <h3>{{ p.pathName || '学习路径' }}</h3>
            <el-progress :percentage="p.progress||0" :stroke-width="6" />
          </div>
          <div class="path-right">
            <el-tag :type="p.status===1?'success':'warning'" size="small" round>
              {{ p.status===1?'已完成':'进行中' }}
            </el-tag>
            <div class="path-stats">
              <span>{{ p.finishedNodes||0 }}/{{ p.totalNodes||0 }} 节点</span>
              <span>{{ p.totalMinutes||0 }} 分钟</span>
            </div>
          </div>
        </div>
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
.page { min-height: 100vh; background: #faf7f2; }
.top-bar { display: flex; align-items: center; padding: 16px 36px; background: #fffdf9; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.top-left { display: flex; align-items: center; gap: 16px; }
.back-btn { color: #ff7b3d; cursor: pointer; font-size: 14px; }
.top-bar h2 { margin: 0; font-size: 18px; color: #2d2a26; }
.content { padding: 28px 36px; max-width: 800px; }

.empty-full { text-align: center; padding: 80px 0; }
.empty-circle { width: 72px; height: 72px; border-radius: 50%; background: #f3efe8; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; font-size: 28px; color: #ff7b3d; font-weight: 700; }
.empty-full p { font-size: 16px; color: #6b655e; margin: 0 0 6px; }
.empty-full span { font-size: 13px; color: #a09a92; }

.path-list { display: flex; flex-direction: column; gap: 16px; }
.path-card {
  display: flex; justify-content: space-between; align-items: center;
  background: #fffdf9; border-radius: 16px; padding: 24px; cursor: pointer;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); transition: all 0.25s;
}
.path-card:hover { transform: translateY(-2px); box-shadow: 0 6px 24px rgba(0,0,0,0.08); }
.path-left { flex: 1; margin-right: 24px; }
.path-left h3 { margin: 0 0 10px; font-size: 16px; color: #2d2a26; }
.path-right { text-align: right; }
.path-stats { display: flex; gap: 12px; color: #bbb; font-size: 13px; margin-top: 8px; }
</style>
