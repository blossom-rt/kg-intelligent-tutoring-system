import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', component: () => import('../views/Register.vue') },
  { path: '/forgot-password', component: () => import('../views/ForgotPassword.vue') },

  // 学生端
  {
    path: '/student',
    component: () => import('../views/StudentHome.vue'),
    meta: { roles: ['student'] }
  },
  { path: '/student/knowledge', component: () => import('../views/student/GraphView.vue'), meta: { roles: ['student'] } },
  { path: '/student/path', component: () => import('../views/student/PathList.vue'), meta: { roles: ['student'] } },
  { path: '/student/path/:id', component: () => import('../views/student/PathDetail.vue'), meta: { roles: ['student'] } },
  { path: '/student/study/:nodeId', component: () => import('../views/student/NodeStudy.vue'), meta: { roles: ['student'] } },
  { path: '/student/practice', component: () => import('../views/student/Practice.vue'), meta: { roles: ['student'] } },
  { path: '/student/exams', component: () => import('../views/student/ExamList.vue'), meta: { roles: ['student'] } },
  { path: '/student/exam/:id', component: () => import('../views/student/ExamResult.vue'), meta: { roles: ['student'] } },
  { path: '/student/wrong', component: () => import('../views/student/WrongBook.vue'), meta: { roles: ['student'] } },
  { path: '/student/themes', component: () => import('../views/student/ThemeList.vue'), meta: { roles: ['student'] } },
  { path: '/student/theme/:id', component: () => import('../views/student/ThemeDetail.vue'), meta: { roles: ['student'] } },
  { path: '/student/profile', component: () => import('../views/Profile.vue'), meta: { roles: ['student'] } },

  // 教师端
  { path: '/teacher', component: () => import('../views/TeacherHome.vue'), meta: { roles: ['teacher'] } },
  { path: '/teacher/courses', component: () => import('../views/teacher/CourseManage.vue'), meta: { roles: ['teacher'] } },
  { path: '/teacher/nodes', component: () => import('../views/teacher/NodeManage.vue'), meta: { roles: ['teacher'] } },
  { path: '/teacher/edges', component: () => import('../views/teacher/GraphEdit.vue'), meta: { roles: ['teacher'] } },
  { path: '/teacher/questions', component: () => import('../views/teacher/QuestionManage.vue'), meta: { roles: ['teacher'] } },
  { path: '/teacher/exams', component: () => import('../views/teacher/ExamManage.vue'), meta: { roles: ['teacher'] } },
  { path: '/teacher/themes', component: () => import('../views/teacher/ThemeManage.vue'), meta: { roles: ['teacher'] } },
  { path: '/teacher/analysis', component: () => import('../views/teacher/ClassAnalysis.vue'), meta: { roles: ['teacher'] } },
  { path: '/teacher/profile', component: () => import('../views/Profile.vue'), meta: { roles: ['teacher'] } },

  // 管理员端
  { path: '/admin', component: () => import('../views/AdminHome.vue'), meta: { roles: ['admin'] } },
  { path: '/admin/users', component: () => import('../views/admin/UserManage.vue'), meta: { roles: ['admin'] } },
  { path: '/admin/roles', component: () => import('../views/admin/RoleManage.vue'), meta: { roles: ['admin'] } },
  { path: '/admin/courses', component: () => import('../views/admin/CourseManage.vue'), meta: { roles: ['admin'] } },
  { path: '/admin/notices', component: () => import('../views/admin/NoticeManage.vue'), meta: { roles: ['admin'] } },
  { path: '/admin/logs', component: () => import('../views/admin/OperLog.vue'), meta: { roles: ['admin'] } },
  { path: '/admin/ai-logs', component: () => import('../views/admin/AiLog.vue'), meta: { roles: ['admin'] } },

  // 404
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const publicPaths = ['/login', '/register', '/forgot-password']

router.beforeEach((to, from) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  // 公开页面放行
  if (publicPaths.includes(to.path)) {
    return true
  }

  // 未登录跳转
  if (!token) {
    return '/login'
  }

  // 角色校验
  if (to.meta.roles && !to.meta.roles.includes(role)) {
    return `/${role}`
  }

  return true
})

export default router
