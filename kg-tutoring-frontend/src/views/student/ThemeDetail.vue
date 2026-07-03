<template>
  <div class="theme-detail-page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="router.push('/student/themes')" :icon="ArrowLeft" circle size="small" />
        <h2 class="page-title">{{ theme?.themeName || theme?.name || '主题详情' }}</h2>
      </div>
      <el-button
        type="primary"
        size="large"
        :loading="genLoading"
        @click="handleGeneratePath"
      >
        按主题生成学习路径
      </el-button>
    </div>

    <div v-loading="loading">
      <template v-if="theme">
        <!-- 主题信息 -->
        <el-card shadow="never" class="info-card">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="主题名称">
              {{ theme.themeName || theme.name }}
            </el-descriptions-item>
            <el-descriptions-item label="难度等级">
              <el-tag :type="diffTagType(theme.difficulty)" size="small">
                {{ diffLabel(theme.difficulty) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="预计学习时长">
              {{ theme.estimatedTime || theme.duration || '-' }} 分钟
            </el-descriptions-item>
            <el-descriptions-item label="涉及学科">
              <template v-if="theme.subjects && theme.subjects.length">
                <el-tag
                  v-for="(sub, si) in theme.subjects"
                  :key="si"
                  size="small"
                  style="margin-right: 6px"
                >
                  {{ typeof sub === 'string' ? sub : sub.name || sub.subjectName || sub }}
                </el-tag>
              </template>
              <span v-else>-</span>
            </el-descriptions-item>
          </el-descriptions>
          <div class="desc-section" v-if="theme.description || theme.desc">
            <h4>主题描述</h4>
            <p>{{ theme.description || theme.desc }}</p>
          </div>
        </el-card>

        <!-- 关联知识点 -->
        <el-card shadow="never" class="nodes-card" v-if="associatedNodes.length">
          <template #header>
            <span class="card-title">关联知识点（{{ associatedNodes.length }}）</span>
          </template>
          <div class="node-list">
            <div
              v-for="node in associatedNodes"
              :key="node.id"
              class="node-item"
              @click="goStudy(node)"
            >
              <div class="node-info">
                <span class="node-name">{{ node.nodeName || node.name }}</span>
                <span class="node-course">{{ node.courseName || '' }}</span>
              </div>
              <div class="node-actions">
                <el-tag :type="diffTagType(node.difficulty)" size="small">
                  {{ diffLabel(node.difficulty) }}
                </el-tag>
                <el-icon :size="16" color="#c0c4cc"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </el-card>

        <el-empty
          v-if="!associatedNodes.length"
          description="暂无关联知识点"
          :image-size="60"
        />
      </template>
      <el-empty v-else description="主题不存在或已删除" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { generatePathByTheme } from '../../api/student'
import request from '../../utils/request'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const genLoading = ref(false)
const theme = ref(null)

const associatedNodes = computed(() => {
  if (!theme.value) return []
  return theme.value.nodes || theme.value.nodeList || theme.value.knowledgeNodes || []
})

const diffLabel = (level) => {
  const map = { 1: '入门', 2: '基础', 3: '进阶', 4: '困难', 5: '挑战' }
  return map[level] || '未知'
}

const diffTagType = (level) => {
  const map = { 1: 'success', 2: '', 3: 'warning', 4: 'danger', 5: 'danger' }
  return map[level] || 'info'
}

const goStudy = (node) => {
  router.push('/student/study/' + (node.nodeId || node.id))
}

const handleGeneratePath = async () => {
  genLoading.value = true
  try {
    const res = await generatePathByTheme({ themeId: theme.value.id })
    ElMessage.success('学习路径生成成功')
    if (res && res.id) {
      router.push('/student/path/' + res.id)
    } else {
      router.push('/student/path')
    }
  } catch {
    ElMessage.error('生成学习路径失败')
  } finally {
    genLoading.value = false
  }
}

const fetchTheme = async () => {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    const res = await request.get('/themes/' + id)
    theme.value = res
  } catch {
    ElMessage.error('加载主题详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchTheme)
</script>

<style scoped>
.theme-detail-page {
  min-height: 100vh;
  background: #faf7f2;
  padding: 24px 32px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #2d2a26;
}

.info-card {
  margin-bottom: 20px;
  border-radius: 14px;
}

.desc-section {
  margin-top: 20px;
}

.desc-section h4 {
  margin: 0 0 8px;
  font-size: 15px;
  color: #2d2a26;
}

.desc-section p {
  margin: 0;
  font-size: 14px;
  color: #6b655e;
  line-height: 1.8;
}

.nodes-card {
  border-radius: 14px;
}

.card-title {
  font-weight: 600;
  color: #2d2a26;
}

.node-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.node-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  background: #f8f5f0;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}

.node-item:hover {
  background: #f3efe8;
  transform: translateX(4px);
}

.node-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.node-name {
  font-size: 15px;
  font-weight: 600;
  color: #2d2a26;
}

.node-course {
  font-size: 12px;
  color: #bbb6ad;
}

.node-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
