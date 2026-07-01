<template>
  <div class="login-container" :class="themeClass">
    <div class="login-card">

      <!-- 系统标题 -->
      <h1 class="system-title">智能导学系统</h1>

      <!-- 角色滑动选择器：学生 / 教师 / 管理员 -->
      <div class="role-switcher">
        <div class="role-track">
          <div
            v-for="item in roles"
            :key="item.key"
            class="role-option"
            :class="{ active: role === item.key }"
            @click="switchRole(item.key)"
          >
            {{ item.label }}
          </div>
          <div class="role-slider" :class="sliderClass"></div>
        </div>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @keyup.enter="submit"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            :placeholder="placeholderText"
            :prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <!-- 记住账号 + 忘记密码 -->
        <div class="form-extra">
          <el-checkbox v-model="rememberAccount" class="remember-check">
            记住账号
          </el-checkbox>
          <span class="forgot-link" @click="goForgot">忘记密码</span>
        </div>

        <!-- 登录按钮 -->
        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="loading"
          @click="submit"
        >
          {{ btnText }}
        </el-button>
      </el-form>

      <!-- 注册入口（仅学生角色显示） -->
      <div class="bottom-links">
        <span v-if="role === 'student'" class="register-link" @click="goRegister">
          注册新账号
        </span>
        <span v-if="role !== 'student'" class="register-tip">
          账号由管理员统一创建分配
        </span>
      </div>

      <!-- 测试账号提示 -->
      <p class="test-tip">{{ testTip }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const role = ref('student')
const rememberAccount = ref(false)

const roles = [
  { key: 'student', label: '学生' },
  { key: 'teacher', label: '教师' },
  { key: 'admin', label: '管理员' }
]

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, message: '密码不能少于4位', trigger: 'blur' }
  ]
}

// ---- 计算属性 ----

const themeClass = computed(() => {
  if (role.value === 'teacher') return 'theme-teacher'
  if (role.value === 'admin') return 'theme-admin'
  return 'theme-student'
})

const sliderClass = computed(() => {
  if (role.value === 'teacher') return 'slide-teacher'
  if (role.value === 'admin') return 'slide-admin'
  return 'slide-student'
})

const placeholderText = computed(() => {
  if (role.value === 'student') return '请输入学号'
  if (role.value === 'teacher') return '请输入工号'
  return '请输入管理员账号'
})

const btnText = computed(() => {
  if (role.value === 'student') return '学生登录'
  if (role.value === 'teacher') return '教师登录'
  return '管理员登录'
})

const testTip = computed(() => {
  if (role.value === 'student') return '测试账号：student / 123456'
  if (role.value === 'teacher') return '测试账号：teacher / 123456'
  return '测试账号：admin / admin123'
})

// ---- 方法 ----

const switchRole = (r) => {
  role.value = r
  form.username = ''
  form.password = ''
  formRef.value?.resetFields()
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 记住账号
    if (rememberAccount.value) {
      localStorage.setItem('rememberedAccount', form.username)
    } else {
      localStorage.removeItem('rememberedAccount')
    }

    const res = await login({ ...form, role: role.value })
    localStorage.setItem('token', res.token)
    localStorage.setItem('role', res.role)

    if (res.role === 'student') router.push('/student')
    else if (res.role === 'teacher') router.push('/teacher')
    else router.push('/admin')
  } catch {
    // 登录失败由 request 拦截器统一处理
  } finally {
    loading.value = false
  }
}

const goRegister = () => {
  // 跳转注册页（后续实现）
  router.push('/register')
}

const goForgot = () => {
  // 跳转忘记密码页（后续实现）
  router.push('/forgot-password')
}

// 初始化：回填记住的账号
const remembered = localStorage.getItem('rememberedAccount')
if (remembered) {
  form.username = remembered
  rememberAccount.value = true
}
</script>

<style scoped>
/* ================================================================
   容器 — 背景渐变缓慢呼吸
   ================================================================ */
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.theme-student { background: linear-gradient(135deg, #e8f4fd 0%, #d4e8fb 50%, #c5ddf8 100%); }
.theme-teacher { background: linear-gradient(135deg, #e8f8f0 0%, #d4f0e2 50%, #c0e8d4 100%); }
.theme-admin   { background: linear-gradient(135deg, #f4eefc 0%, #e8ddf5 50%, #dcd0f0 100%); }

/* ================================================================
   卡片 — 阴影随主题流动
   ================================================================ */
.login-card {
  width: 440px;
  padding: 48px 44px 36px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.06), 0 4px 16px rgba(0, 0, 0, 0.03);
  transition:
    box-shadow 0.6s cubic-bezier(0.4, 0, 0.2, 1),
    transform 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.theme-student .login-card { box-shadow: 0 24px 64px rgba(47, 102, 207, 0.10), 0 4px 16px rgba(47, 102, 207, 0.04); }
.theme-teacher .login-card { box-shadow: 0 24px 64px rgba(45, 138, 78, 0.10),  0 4px 16px rgba(45, 138, 78, 0.04); }
.theme-admin   .login-card { box-shadow: 0 24px 64px rgba(91, 62, 158, 0.10),  0 4px 16px rgba(91, 62, 158, 0.04); }

/* ================================================================
   标题 — 渐变色 + 字间距呼吸
   ================================================================ */
.system-title {
  text-align: center;
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 36px;
  letter-spacing: 3px;
  transition:
    color 0.6s cubic-bezier(0.4, 0, 0.2, 1),
    letter-spacing 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.theme-student .system-title { color: #2c5eb5; }
.theme-teacher .system-title { color: #2d8a4e; }
.theme-admin   .system-title { color: #5b3e9e; }

/* ================================================================
   角色滑动选择器
   ================================================================ */
.role-switcher {
  margin-bottom: 32px;
}

.role-track {
  position: relative;
  display: flex;
  background: #f0f3f7;
  border-radius: 12px;
  padding: 5px;
  transition: background 0.5s ease;
}

.role-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 11px 0;
  font-size: 15px;
  font-weight: 500;
  color: #8c99a8;
  border-radius: 9px;
  cursor: pointer;
  user-select: none;
  position: relative;
  z-index: 1;
  transition:
    color 0.5s cubic-bezier(0.4, 0, 0.2, 1),
    font-weight 0.3s ease;
}

.role-option:hover:not(.active) {
  color: #5a6b7d;
}

.role-option.active {
  color: #fff;
  font-weight: 600;
}

/* 滑动块 — 弹簧缓动 + 阴影光晕 */
.role-slider {
  position: absolute;
  top: 5px;
  left: 5px;
  width: calc(33.333% - 5px);
  height: calc(100% - 10px);
  border-radius: 9px;
  transition:
    transform 0.55s cubic-bezier(0.22, 0.97, 0.38, 1.02),
    background 0.6s cubic-bezier(0.4, 0, 0.2, 1),
    box-shadow 0.55s cubic-bezier(0.22, 0.97, 0.38, 1.02);
  z-index: 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.slide-student {
  transform: translateX(0);
  background: linear-gradient(135deg, #4f8cf7, #3670e8);
  box-shadow: 0 3px 14px rgba(54, 112, 232, 0.30);
}
.slide-teacher {
  transform: translateX(100%);
  background: linear-gradient(135deg, #43b878, #2d8a4e);
  box-shadow: 0 3px 14px rgba(45, 138, 78, 0.30);
}
.slide-admin {
  transform: translateX(200%);
  background: linear-gradient(135deg, #8b5cf6, #6d3fd6);
  box-shadow: 0 3px 14px rgba(109, 63, 214, 0.30);
}

/* ================================================================
   表单
   ================================================================ */
.login-form {
  margin-top: 4px;
}

/* 输入框 — 焦点光晕扩散 */
.login-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  transition:
    box-shadow 0.35s cubic-bezier(0.4, 0, 0.2, 1),
    border-color 0.35s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d0d8e4 inset;
}

.theme-student .login-form :deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 2px rgba(79, 140, 247, 0.25) inset, 0 0 12px rgba(79, 140, 247, 0.08);
}
.theme-teacher .login-form :deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 2px rgba(67, 184, 120, 0.25) inset, 0 0 12px rgba(67, 184, 120, 0.08);
}
.theme-admin   .login-form :deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 2px rgba(139, 92, 246, 0.25) inset, 0 0 12px rgba(139, 92, 246, 0.08);
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

/* 表单校验错误时轻微抖动 */
.login-form :deep(.el-form-item.is-error .el-input__wrapper) {
  animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97);
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%      { transform: translateX(-6px); }
  40%      { transform: translateX(6px); }
  60%      { transform: translateX(-4px); }
  80%      { transform: translateX(3px); }
}

/* ================================================================
   记住账号 & 忘记密码
   ================================================================ */
.form-extra {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.remember-check :deep(.el-checkbox__label) {
  font-size: 13px;
  color: #999;
}

.forgot-link {
  font-size: 13px;
  color: #8c99a8;
  cursor: pointer;
  user-select: none;
  transition: color 0.25s ease;
  position: relative;
}

.forgot-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background: currentColor;
  transition: width 0.3s ease;
}

.forgot-link:hover {
  color: #4f8cf7;
}

.forgot-link:hover::after {
  width: 100%;
}

/* ================================================================
   登录按钮 — 渐变流光 + 悬浮上浮 + 按压回弹
   ================================================================ */
.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 3px;
  border-radius: 10px;
  margin-top: 12px;
  border: none;
  position: relative;
  overflow: hidden;
  transition:
    transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1),
    box-shadow 0.35s ease,
    background 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.theme-student .login-btn {
  background: linear-gradient(135deg, #4f8cf7, #3670e8) !important;
  box-shadow: 0 4px 16px rgba(54, 112, 232, 0.30);
}
.theme-teacher .login-btn {
  background: linear-gradient(135deg, #43b878, #2d8a4e) !important;
  box-shadow: 0 4px 16px rgba(45, 138, 78, 0.30);
}
.theme-admin   .login-btn {
  background: linear-gradient(135deg, #8b5cf6, #6d3fd6) !important;
  box-shadow: 0 4px 16px rgba(109, 63, 214, 0.30);
}

/* 按钮悬浮：上浮 + 光晕扩散 */
.login-btn:hover {
  transform: translateY(-2px);
}

.theme-student .login-btn:hover { box-shadow: 0 8px 24px rgba(54, 112, 232, 0.42); }
.theme-teacher .login-btn:hover { box-shadow: 0 8px 24px rgba(45, 138, 78, 0.42); }
.theme-admin   .login-btn:hover { box-shadow: 0 8px 24px rgba(109, 63, 214, 0.42); }

/* 按钮按下：回弹 */
.login-btn:active {
  transform: scale(0.96);
  transition: transform 0.1s ease;
}

/* 按钮流光扫过 */
.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.18), transparent);
  transition: left 0.55s ease;
}

.login-btn:hover::before {
  left: 100%;
}

/* ================================================================
   底部链接
   ================================================================ */
.bottom-links {
  text-align: center;
  margin-top: 22px;
}

.register-link {
  font-size: 14px;
  color: #4f8cf7;
  cursor: pointer;
  user-select: none;
  transition:
    color 0.25s ease,
    letter-spacing 0.3s ease;
  position: relative;
}

.register-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background: #3670e8;
  transition: width 0.3s ease;
}

.register-link:hover {
  color: #3670e8;
  letter-spacing: 1px;
}

.register-link:hover::after {
  width: 100%;
}

.register-tip {
  font-size: 13px;
  color: #bbb;
  user-select: none;
  transition: color 0.4s ease;
}

/* ================================================================
   测试账号提示
   ================================================================ */
.test-tip {
  text-align: center;
  margin: 14px 0 0;
  font-size: 13px;
  transition:
    color 0.6s cubic-bezier(0.4, 0, 0.2, 1),
    opacity 0.5s ease;
  user-select: none;
  opacity: 0.7;
}

.test-tip:hover {
  opacity: 1;
}

.theme-student .test-tip { color: #8aabd4; }
.theme-teacher .test-tip { color: #87b89a; }
.theme-admin   .test-tip { color: #b09fd4; }
</style>
