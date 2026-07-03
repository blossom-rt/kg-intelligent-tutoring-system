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
          // StudentController 和 TeacherController 没有 /api 前缀
          if (path.startsWith('/api/kg/student') || path.startsWith('/api/kg/teacher')) {
            return path.replace(/^\/api\/kg/, '')
          }
          return path.replace(/^\/api\/kg/, '/api')
        }
      }
    }
  }
})