<template>
  <div class="app-shell">
    <div class="grain-global"></div>
    <router-view></router-view>
  </div>
</template>

<style>
.app-shell {
  position: relative;
  min-height: 100vh;
}
.grain-global {
  pointer-events: none;
  position: fixed;
  inset: 0;
  z-index: 9999;
  opacity: 0.025;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  background-repeat: repeat;
  background-size: 256px 256px;
}
</style>