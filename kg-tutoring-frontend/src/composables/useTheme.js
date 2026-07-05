import { ref } from 'vue'

const STORAGE_KEY = 'theme-mode'

// 'light' | 'dark' | 'auto'（auto 跟随系统）
const mode = ref(localStorage.getItem(STORAGE_KEY) || 'auto')
const resolved = ref('light')
let mq = null

function systemDark() {
  return typeof window !== 'undefined' && window.matchMedia
    ? window.matchMedia('(prefers-color-scheme: dark)').matches
    : false
}

function resolve() {
  return mode.value === 'auto' ? (systemDark() ? 'dark' : 'light') : mode.value
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
  apply()
  if (typeof window !== 'undefined' && window.matchMedia) {
    mq = window.matchMedia('(prefers-color-scheme: dark)')
    mq.addEventListener('change', () => { if (mode.value === 'auto') apply() })
  }
}

export function useTheme() {
  return { mode, resolved, setMode, toggle, init }
}
