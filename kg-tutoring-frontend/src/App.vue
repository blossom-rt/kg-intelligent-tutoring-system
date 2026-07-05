<template>
  <div class="app-shell" :class="{ 'has-sidebar': showSidebar }">
    <div class="grain-global"></div>
    <Sidebar v-if="showSidebar" />
    <main class="main-content" :style="{ marginLeft: contentMargin }">
      <router-view></router-view>
    </main>
    <StudyPet ref="petRef" v-if="showPet" />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './components/Sidebar.vue'
import StudyPet from './components/StudyPet.vue'
import { usePet } from './composables/usePet'
import { useSidebar } from './composables/useSidebar'

const route = useRoute()
const { register } = usePet()
const { isCollapsed } = useSidebar()
const petRef = ref(null)

const showSidebar = computed(() => {
  return route.path.startsWith('/student') || route.path.startsWith('/teacher')
})
const showPet = computed(() => route.path.startsWith('/student'))
const contentMargin = computed(() => showSidebar.value ? (isCollapsed.value ? '60px' : '200px') : '0')

watch(petRef, (el) => { if (el) register(el) })
</script>

<style>
.app-shell {
  position: relative;
  min-height: 100vh;
}
.main-content { min-height: 100vh; transition: margin-left 0.25s ease; }
.grain-global {
  pointer-events: none;
  position: fixed;
  inset: 0;
  z-index: 9999;
  opacity: var(--grain-opacity);
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  background-repeat: repeat;
  background-size: 256px 256px;
}
</style>