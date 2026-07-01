import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import StudentHome from '../views/StudentHome.vue'
import TeacherHome from '../views/TeacherHome.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/student', component: StudentHome },
  { path: '/teacher', component: TeacherHome }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})
export default router