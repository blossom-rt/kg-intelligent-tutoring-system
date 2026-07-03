<template>
  <div class="login-root" :class="themeClass">
    <!-- 左侧：品牌 + 插画 -->
    <section class="brand-side">
      <div class="brand-inner">
        <p class="brand-eyebrow">CUPK · 计算机系</p>
        <h1 class="brand-title">智能导学系统</h1>
        <p class="brand-desc">
          从知识点到知识图谱<br />找到属于你的学习路径
        </p>
        <AnimatedCharacters
          :is-typing="isTyping"
          :show-password="showPassword"
          :password-length="form.password.length"
        />
      </div>
    </section>

    <!-- 右侧：登录表单 -->
    <section class="form-side">
      <div class="form-card">
        <h2 class="form-heading">登录</h2>

        <!-- 角色切换 -->
        <div class="role-tabs">
          <button
            v-for="r in roles"
            :key="r.key"
            class="role-tab"
            :class="{ on: role === r.key }"
            @click="switchRole(r.key)"
          >{{ r.label }}</button>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="submit">
          <el-form-item prop="username">
            <el-input
              v-model="form.username" :placeholder="placeholderText" :prefix-icon="User"
              size="large" clearable
              @focus="isTyping = true" @blur="isTyping = false"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password" :type="showPassword ? 'text' : 'password'"
              placeholder="密码" :prefix-icon="Lock" size="large"
              @focus="isTyping = true" @blur="isTyping = false"
            >
              <template #suffix>
                <span class="pwd-eye" @mousedown.prevent @click.stop="showPassword = !showPassword">
                  <component :is="showPassword ? View : Hide" />
                </span>
              </template>
            </el-input>
          </el-form-item>

          <div class="form-row">
            <el-checkbox v-model="rememberAccount" class="remember">记住账号</el-checkbox>
            <span class="forgot" @click="goForgot">忘记密码</span>
          </div>

          <button type="button" class="submit-btn" :disabled="loading" @click="submit">
            <span v-if="loading" class="btn-loading"></span>
            <span v-else>{{ btnText }}</span>
          </button>
        </el-form>

        <p class="form-footer">
          <template v-if="role === 'student'">
            没有账号？<a @click="goRegister">创建一个</a>
          </template>
          <template v-else>
            <span class="muted">账号由管理员统一创建分配</span>
          </template>
        </p>

        <p class="test-hint">{{ testTip }}</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, View, Hide } from '@element-plus/icons-vue'
import { login } from '../api/user'
import AnimatedCharacters from '../components/AnimatedCharacters.vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const role = ref('student')
const rememberAccount = ref(false)
const isTyping = ref(false)
const showPassword = ref(false)

const roles = [
  { key: 'student', label: '学生' },
  { key: 'teacher', label: '教师' },
  { key: 'admin', label: '管理员' }
]

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, message: '密码不能少于4位', trigger: 'blur' }
  ]
}

const themeClass = computed(() => 'role-' + role.value)

const placeholderText = computed(() => {
  if (role.value === 'student') return '学号'
  if (role.value === 'teacher') return '工号'
  return '管理员账号'
})
const btnText = computed(() => {
  if (role.value === 'student') return '学生登录'
  if (role.value === 'teacher') return '教师登录'
  return '管理员登录'
})
const testTip = computed(() => {
  if (role.value === 'student') return '测试：student / 123456'
  if (role.value === 'teacher') return '测试：teacher / 123456'
  return '测试：admin / admin123'
})

const switchRole = (r) => { role.value = r; form.username = ''; form.password = ''; formRef.value?.resetFields() }

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    if (rememberAccount.value) localStorage.setItem('rememberedAccount', form.username)
    else localStorage.removeItem('rememberedAccount')
    const res = await login({ ...form, role: role.value })
    localStorage.setItem('token', res.token)
    localStorage.setItem('role', res.role)
    if (res.role === 'student') router.push('/student')
    else if (res.role === 'teacher') router.push('/teacher')
    else router.push('/admin')
  } catch { }
  finally { loading.value = false }
}

const goRegister = () => router.push('/register')
const goForgot = () => router.push('/forgot-password')

const remembered = localStorage.getItem('rememberedAccount')
if (remembered) { form.username = remembered; rememberAccount.value = true }
</script>

<style scoped>
/* ============================================
   根容器 — 浅底暖调 + 不对称分栏
   ============================================ */
.login-root {
  display: flex;
  min-height: 100vh;
  background: #faf7f2;
  position: relative;
  overflow: hidden;
}

/* ============================================
   左侧 — 品牌 + 插画
   ============================================ */
.brand-side {
  flex: 1.15;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 48px 64px 48px 48px;
  position: relative;
  z-index: 1;
  background: linear-gradient(
    155deg,
    #f5f1ea 0%,
    #f8f4ed 40%,
    #f0ece4 100%
  );
}

.brand-inner {
  max-width: 480px;
  text-align: right;
}

.brand-eyebrow {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 3px;
  text-transform: uppercase;
  color: #ff7b3d;
  margin: 0 0 16px;
}

.brand-title {
  font-size: 48px;
  font-weight: 800;
  letter-spacing: 2px;
  line-height: 1.15;
  color: #2d2a26;
  margin: 0 0 16px;
}

.brand-desc {
  font-size: 15px;
  line-height: 1.7;
  color: #6b655e;
  margin: 0 0 40px;
}

/* ============================================
   右侧 — 表单卡片
   ============================================ */
.form-side {
  flex: 0.85;
  display: flex;
  align-items: center;
  padding: 48px;
  position: relative;
  z-index: 1;
}

.form-card {
  width: 100%;
  max-width: 380px;
  padding: 40px 36px;
  background: #fffdf9;
  border: 1px solid #e8e3db;
  border-radius: 12px;
}

.form-heading {
  font-size: 22px;
  font-weight: 700;
  color: #2d2a26;
  margin: 0 0 24px;
}

/* ── 角色切换 ── */
.role-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  background: #f8f5f0;
  border-radius: 8px;
  padding: 3px;
}
.role-tab {
  flex: 1;
  padding: 8px 0;
  font-size: 13px;
  font-weight: 600;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  background: transparent;
  color: #a09a92;
  transition: background 0.25s ease, color 0.25s ease;
}
.role-tab.on {
  background: #ff7b3d;
  color: #fff;
}

/* ── 表单输入 ── */
.form-card :deep(.el-input__wrapper) {
  background: #f8f5f0;
  border: 1px solid #e8e3db;
  border-radius: 8px;
  box-shadow: none;
  transition: border-color 0.25s ease;
}
.form-card :deep(.el-input__wrapper:focus-within),
.form-card :deep(.el-input__wrapper:hover) {
  border-color: #ff7b3d;
  box-shadow: none;
}
.form-card :deep(.el-input__inner) {
  color: #2d2a26;
}
.form-card :deep(.el-input__inner::placeholder) {
  color: #bbb6ad;
}
.form-card :deep(.el-form-item) {
  margin-bottom: 16px;
}
.form-card :deep(.el-input__prefix) {
  color: #a09a92;
}

/* 密码眼睛 */
.pwd-eye {
  cursor: pointer;
  color: #a09a92;
  transition: color 0.2s ease;
  display: flex;
  align-items: center;
}
.pwd-eye:hover { color: #ff7b3d; }

/* ── 记住 & 忘记 ── */
.form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.remember {
  --el-checkbox-checked-bg-color: #ff7b3d;
  --el-checkbox-checked-border-color: #ff7b3d;
}
.remember :deep(.el-checkbox__label) { color: #6b655e; font-size: 13px; }
.forgot {
  font-size: 13px;
  color: #a09a92;
  cursor: pointer;
  transition: color 0.2s ease;
}
.forgot:hover { color: #ff7b3d; }

/* ── 提交按钮 ── */
.submit-btn {
  width: 100%;
  padding: 12px 0;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 1px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  color: #fff;
  background: #ff7b3d;
  transition: background 0.2s ease, transform 0.15s ease;
}
.submit-btn:hover:not(:disabled) {
  background: #ff9060;
}
.submit-btn:active:not(:disabled) {
  transform: scale(0.97);
}
.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-loading {
  display: inline-block;
  width: 20px; height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── 底部链接 ── */
.form-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: #a09a92;
}
.form-footer a {
  color: #ff7b3d;
  cursor: pointer;
  font-weight: 600;
  transition: color 0.2s ease;
}
.form-footer a:hover { color: #ff9060; }
.form-footer .muted { color: #c5bfb5; }

.test-hint {
  text-align: center;
  margin: 10px 0 0;
  font-size: 12px;
  color: #c5bfb5;
}

/* ============================================
   角色主题微调 — 只用暖调变化
   ============================================ */
.role-teacher .brand-eyebrow { color: #5eaf83; }
.role-teacher .role-tab.on { background: #5eaf83; }
.role-teacher .submit-btn { background: #5eaf83; }
.role-teacher .submit-btn:hover:not(:disabled) { background: #73c496; }
.role-teacher .form-footer a { color: #5eaf83; }
.role-teacher .pwd-eye:hover { color: #5eaf83; }
.role-teacher :deep(.el-input__wrapper:focus-within) { border-color: #5eaf83; }
.role-teacher :deep(.el-input__wrapper:hover) { border-color: #5eaf83; }
.role-teacher .forgot:hover { color: #5eaf83; }

.role-admin .brand-eyebrow { color: #d4a853; }
.role-admin .role-tab.on { background: #d4a853; }
.role-admin .submit-btn { background: #d4a853; }
.role-admin .submit-btn:hover:not(:disabled) { background: #ddb968; }
.role-admin .form-footer a { color: #d4a853; }
.role-admin .pwd-eye:hover { color: #d4a853; }
.role-admin :deep(.el-input__wrapper:focus-within) { border-color: #d4a853; }
.role-admin :deep(.el-input__wrapper:hover) { border-color: #d4a853; }
.role-admin .forgot:hover { color: #d4a853; }

/* ============================================
   响应式
   ============================================ */
@media (max-width: 768px) {
  .login-root { flex-direction: column; }
  .brand-side {
    flex: none;
    justify-content: center;
    padding: 40px 24px;
  }
  .brand-inner { text-align: center; }
  .brand-title { font-size: 32px; }
  .form-side { padding: 24px; justify-content: center; }
}
</style>
