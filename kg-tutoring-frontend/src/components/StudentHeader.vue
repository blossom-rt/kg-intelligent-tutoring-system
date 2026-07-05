<template>
  <header class="student-header">
    <div class="header-left">
      <button class="back-btn" @click="goBack" :aria-label="'返回'">
        <el-icon :size="18"><ArrowLeft /></el-icon>
      </button>
      <div class="header-titles">
        <h1 class="page-title">{{ title }}</h1>
        <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
      </div>
    </div>
    <div class="header-right">
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' }
})

const router = useRouter()
const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    const role = localStorage.getItem('role')
    if (role === 'teacher') router.push('/teacher')
    else if (role === 'admin') router.push('/admin')
    else router.push('/student')
  }
}
</script>

<style scoped>
.student-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 32px;
  background: var(--bg-grad);
  border-bottom: 1px solid var(--border-subtle);
  gap: 16px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}
.back-btn {
  width: 36px; height: 36px; border-radius: 10px;
  border: 1px solid rgba(0,0,0,0.1); background: var(--bg-surface);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--text-secondary); flex-shrink: 0;
  transition: background 0.2s ease, transform 0.15s ease;
}
.back-btn:hover { background: var(--bg-hover); transform: scale(1.05); }
.back-btn:active { transform: scale(0.95); }

.header-titles { min-width: 0; }
.page-title {
  font-size: 20px; font-weight: 700; color: var(--text-primary);
  margin: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.page-subtitle {
  font-size: 13px; color: var(--text-muted); margin: 2px 0 0;
}

.header-right {
  display: flex; align-items: center; gap: 10px;
  flex-shrink: 0;
}
</style>
