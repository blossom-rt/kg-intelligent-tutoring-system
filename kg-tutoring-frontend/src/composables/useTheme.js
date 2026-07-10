import { ref } from 'vue'

const STORAGE_KEY = 'theme-mode'

// 界面设计默认使用浅色主题，避免操作系统的深色外观将页面变成黑底。
// 旧版保存过的 auto 也迁移为 light；用户主动选择的 dark 仍然保留。
const savedMode = localStorage.getItem(STORAGE_KEY)
const initialMode = savedMode === 'dark' ? 'dark' : 'light'
const mode = ref(initialMode)
const resolved = ref('light')

function resolve() {
  return mode.value === 'dark' ? 'dark' : 'light'
}

function apply() {
  resolved.value = resolve()
  document.documentElement.setAttribute('data-theme', resolved.value)
}

function setMode(m) {
  mode.value = m
  localStorage.setItem(STORAGE_KEY, m)
  apply()
}

function toggle() {
  setMode(resolved.value === 'dark' ? 'light' : 'dark')
}

function init() {
  if (savedMode === 'auto') {
    localStorage.setItem(STORAGE_KEY, 'light')
  }
  apply()
}

export function useTheme() {
  return { mode, resolved, setMode, toggle, init }
}
