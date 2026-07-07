import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/global.css'
import { useTheme } from './composables/useTheme'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 主题初始化（mount 前应用 data-theme，防闪烁）
useTheme().init()

app.mount('#app')