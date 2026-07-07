import { ref } from 'vue'

const isCollapsed = ref(false)

export function useSidebar() {
  const toggle = () => { isCollapsed.value = !isCollapsed.value }
  return { isCollapsed, toggle }
}
