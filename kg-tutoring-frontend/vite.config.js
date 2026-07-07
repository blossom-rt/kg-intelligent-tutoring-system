import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api/kg': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        rewrite: path => {
          // 统一将 /api/kg 替换为 /api (StudentController/TeacherController 也已改为 /api 前缀)
          return path.replace(/^\/api\/kg/, '/api')
        }
      }
    }
  }
})