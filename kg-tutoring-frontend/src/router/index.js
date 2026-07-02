import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ForgotPassword from '../views/ForgotPassword.vue'
import StudentHome from '../views/StudentHome.vue'
import TeacherHome from '../views/TeacherHome.vue'
import AdminHome from '../views/AdminHome.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/forgot-password', component: ForgotPassword },
  { path: '/student', component: StudentHome },
  { path: '/teacher', component: TeacherHome },
  { path: '/admin', component: AdminHome }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 无需登录即可访问的页面
const publicPaths = ['/login', '/register', '/forgot-password']

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!publicPaths.includes(to.path) && !token) {
    next('/login')
  } else {
    next()
  }
})
export default router