<template>
  <div class="theme-list-page">
    <StudentHeader title="跨学科主题" subtitle="多学科融合，拓展思维边界" />

    <div v-loading="loading" class="theme-grid">
      <el-empty v-if="!loading && themes.length === 0" description="暂无跨学科主题" :image-size="60" />

      <el-card
        v-for="theme in themes"
        :key="theme.id"
        class="theme-card"
        shadow="hover"
        @click="goDetail(theme)"
      >
        <div class="theme-header">
          <h3 class="theme-name">{{ theme.themeName || theme.name || '未命名主题' }}</h3>
          <el-tag :type="difficultyTagType(theme.difficulty)" size="small">
            {{ difficultyLabel(theme.difficulty) }}
          </el-tag>
        </div>
        <p class="theme-desc">{{ truncate(theme.description || theme.desc || '暂无描述', 100) }}</p>
        <div class="theme-footer">
          <div class="theme-subjects" v-if="theme.subjects && theme.subjects.length">
            <el-tag
              v-for="(sub, si) in theme.subjects"
              :key="si"
              size="small"
              type="info"
              class="subject-tag"
            >
              {{ typeof sub === 'string' ? sub : sub.name || sub.subjectName || sub }}
            </el-tag>
          </div>
          <span class="theme-time" v-if="theme.estimatedTime || theme.duration">
            <el-icon :size="14"><Clock /></el-icon>
            {{ theme.estimatedTime || theme.duration }} 分钟
          </span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'
import StudentHeader from '../../components/StudentHeader.vue'
import request from '../../utils/request'

const router = useRouter()
const loading = ref(false)
const themes = ref([])

const difficultyLabel = (level) => {
  const map = { 1: '入门', 2: '基础', 3: '进阶', 4: '困难', 5: '挑战' }
  return map[level] || '未知'
}

const difficultyTagType = (level) => {
  const map = { 1: 'success', 2: 'info', 3: 'warning', 4: 'danger', 5: 'danger' }
  return map[level] || 'info'
}

const truncate = (str, max) => {
  if (!str) return ''
  return str.length > max ? str.slice(0, max) + '...' : str
}

const goDetail = (theme) => {
  router.push('/student/theme/' + theme.id)
}

const fetchThemes = async () => {
  loading.value = true
  try {
    const res = await request.get('/themes')
    themes.value = Array.isArray(res) ? res : (res.records || res.list || [])
  } catch {
    ElMessage.error('加载跨学科主题失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchThemes)
</script>

<style scoped>
.theme-list-page {
  min-height: 100vh;
  background: #faf7f2;
  
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.theme-card {
  cursor: pointer;
  border-radius: 14px;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.theme-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.theme-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.theme-name {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #2d2a26;
}

.theme-desc {
  font-size: 14px;
  color: #a09a92;
  line-height: 1.7;
  margin: 0 0 16px;
}

.theme-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.theme-subjects {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.subject-tag {
  font-size: 12px;
}

.theme-time {
  font-size: 13px;
  color: #bbb6ad;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
</style>
