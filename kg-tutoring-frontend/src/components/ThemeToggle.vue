<template>
  <button
    class="theme-toggle"
    @click="toggle"
    :aria-label="resolved === 'dark' ? '切换到浅色模式' : '切换到暗色模式'"
    :title="resolved === 'dark' ? '浅色模式' : '暗色模式'"
  >
    <el-icon :size="18">
      <component :is="resolved === 'dark' ? Sunny : Moon" />
    </el-icon>
  </button>
</template>

<script setup>
import { Sunny, Moon } from '@element-plus/icons-vue'
import { useTheme } from '../composables/useTheme'

const { resolved, toggle } = useTheme()
</script>

<style scoped>
.theme-toggle {
  width: 36px; height: 36px; border-radius: 10px;
  border: 1px solid var(--border-subtle); background: var(--bg-surface);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--text-secondary); flex-shrink: 0;
  transition: background 0.2s ease, color 0.2s ease, transform 0.15s ease;
}
.theme-toggle:hover { background: var(--bg-hover); color: var(--accent); transform: scale(1.05); }
.theme-toggle:active { transform: scale(0.95); }
</style>
