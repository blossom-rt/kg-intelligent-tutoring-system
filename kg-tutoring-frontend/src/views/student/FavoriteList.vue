<template>
  <div class="fav-page">
    <StudentHeader title="我的收藏" subtitle="收藏的重点、难点知识点" />

    <div class="fav-content" v-loading="loading">
      <el-empty v-if="!loading && list.length === 0" description="暂无收藏，去知识点页面收藏吧" :image-size="80" />

      <div v-else class="fav-grid">
        <div v-for="item in list" :key="item.id" class="fav-card" @click="goNode(item)">
          <div class="fav-top">
            <el-tag :type="diffTag(item.difficulty)" size="small">{{ diffLabel(item.difficulty) }}</el-tag>
            <el-button text type="danger" size="small" @click.stop="remove(item)">取消收藏</el-button>
          </div>
          <h4 class="fav-name">{{ item.nodeName }}</h4>
          <p class="fav-chapter">{{ item.chapter || '未分章' }}</p>
          <div class="fav-time">收藏于 {{ formatTime(item.createTime) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import StudentHeader from '../../components/StudentHeader.vue'
import { getFavoriteList, deleteFavorite } from '../../api/student'

const router = useRouter()
const loading = ref(false)
const list = ref([])

const diffLabel = (v) => ({ 1: '入门', 2: '基础', 3: '进阶', 4: '困难', 5: '挑战' }[v] || '未知')
const diffTag = (v) => ({ 1: 'success', 2: 'info', 3: 'warning', 4: 'danger', 5: 'danger' }[v] || 'info')

const formatTime = (t) => {
  if (!t) return '-'
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getFavoriteList()
    list.value = Array.isArray(res) ? res : []
  } catch { list.value = [] }
  finally { loading.value = false }
}

const goNode = (item) => {
  router.push('/student/study/' + item.nodeId)
}

const remove = async (item) => {
  try {
    await ElMessageBox.confirm('取消收藏「' + item.nodeName + '」？', '确认')
    await deleteFavorite(item.id)
    ElMessage.success('已取消收藏')
    fetchList()
  } catch { }
}

onMounted(fetchList)
</script>

<style scoped>
.fav-page { min-height: 100vh; background: var(--bg-root); }
.fav-content { padding: 20px 32px; }
.fav-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
.fav-card {
  background: var(--bg-surface); border: 1px solid var(--border-subtle);
  border-radius: 12px; padding: 18px; cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.fav-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.fav-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.fav-name { margin: 0 0 6px; font-size: 16px; color: var(--text-primary); }
.fav-chapter { margin: 0 0 8px; font-size: 13px; color: var(--text-muted); }
.fav-time { font-size: 12px; color: var(--text-muted); }
</style>
