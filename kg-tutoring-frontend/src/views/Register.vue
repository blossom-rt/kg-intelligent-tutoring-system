<template>
  <div class="register-root">
    <ThemeToggle class="corner-toggle" />
    <section class="brand-side">
      <div class="brand-inner">
        <p class="brand-eyebrow">CUPK · 计算机系</p>
        <h1 class="brand-title">加入我们</h1>
        <p class="brand-desc">
          注册学生账号<br />开启个性化学习旅程
        </p>
      </div>
    </section>

    <section class="form-side">
      <div class="form-card">
        <h2 class="form-heading">创建学生账号</h2>

        <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="submit">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="登录账号（4-20位字母或数字）"
              :prefix-icon="User" size="large" clearable maxlength="20" />
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="登录密码（至少6位）"
              :prefix-icon="Lock" size="large" show-password />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码"
              :prefix-icon="Lock" size="large" show-password />
          </el-form-item>

          <el-form-item prop="realName">
            <el-input v-model="form.realName" placeholder="真实姓名"
              :prefix-icon="UserFilled" size="large" clearable />
          </el-form-item>

          <el-form-item prop="email">
            <el-input v-model="form.email" placeholder="注册邮箱"
              :prefix-icon="Message" size="large" clearable />
          </el-form-item>

          <el-form-item prop="code">
            <div class="code-row">
              <el-input v-model="form.code" placeholder="邮箱验证码"
                :prefix-icon="Key" size="large" maxlength="6" />
              <button type="button" class="code-btn" :disabled="codeCountdown > 0" @click="sendCode">
                {{ codeCountdown > 0 ? codeCountdown + 's 后重发' : '获取验证码' }}
              </button>
            </div>
          </el-form-item>

          <button type="button" class="submit-btn" :disabled="loading" @click="submit">
            <span v-if="loading" class="btn-loading"></span>
            <span v-else>注册</span>
          </button>
        </el-form>

        <p class="form-footer">
          已有账号？<a @click="$router.push('/login')">返回登录</a>
        </p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, UserFilled, Message, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register, sendEmailCode } from '../api/user'
import ThemeToggle from '../components/ThemeToggle.vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

const form = reactive({
  username: '', password: '', confirmPassword: '',
  realName: '', email: '', code: ''
})

const validateConfirm = (_rule, value, callback) => {
  callback(value !== form.password ? new Error('两次密码输入不一致') : undefined)
}

const rules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]{4,20}$/, message: '4-20位字母或数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入注册邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位', trigger: 'blur' }
  ]
}

const sendCode = async () => {
  if (!form.email) { ElMessage.warning('请先填写邮箱'); return }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) { ElMessage.warning('邮箱格式不正确'); return }
  try {
    await sendEmailCode({ email: form.email, type: 'register' })
    ElMessage.success('验证码已发送')
    codeCountdown.value = 60
    countdownTimer = setInterval(() => { if (--codeCountdown.value <= 0) clearInterval(countdownTimer) }, 1000)
  } catch { ElMessage.error('发送失败') }
}

const submit = async () => {
  if (!(await formRef.value.validate().catch(() => false))) return
  loading.value = true
  try {
    await register({ username: form.username, password: form.password, realName: form.realName, email: form.email, code: form.code })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch { }
  finally { loading.value = false }
}
</script>

<style scoped>
.register-root {
  display: flex;
  min-height: 100vh;
  background: var(--bg-root);
  position: relative;
}
.corner-toggle {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
}

.brand-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 48px 64px 48px 48px;
  background: var(--bg-grad);
}
.brand-inner {
  max-width: 400px;
  text-align: right;
}
.brand-eyebrow {
  font-size: 12px; font-weight: 600; letter-spacing: 3px;
  text-transform: uppercase; color: var(--accent); margin: 0 0 16px;
}
.brand-title {
  font-size: 48px; font-weight: 800; letter-spacing: 2px;
  line-height: 1.15; color: var(--text-primary); margin: 0 0 16px;
}
.brand-desc {
  font-size: 15px; line-height: 1.7; color: var(--text-secondary); margin: 0;
}

.form-side {
  flex: 0.9;
  display: flex;
  align-items: center;
  padding: 48px;
}
.form-card {
  width: 100%;
  max-width: 420px;
  padding: 40px 36px;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: 12px;
}
.form-heading {
  font-size: 22px; font-weight: 700; color: var(--text-primary); margin: 0 0 24px;
}

.form-card :deep(.el-input__wrapper) {
  background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: 8px; box-shadow: none;
}
.form-card :deep(.el-input__wrapper:focus-within),
.form-card :deep(.el-input__wrapper:hover) {
  border-color: var(--accent); box-shadow: none;
}
.form-card :deep(.el-input__inner) { color: var(--text-primary); }
.form-card :deep(.el-input__inner::placeholder) { color: var(--text-muted); }
.form-card :deep(.el-form-item) { margin-bottom: 16px; }
.form-card :deep(.el-input__prefix) { color: var(--text-muted); }

.code-row { display: flex; gap: 10px; }
.code-row :deep(.el-input) { flex: 1; }

.code-btn {
  flex-shrink: 0; min-width: 110px;
  padding: 0 12px; font-size: 13px; font-weight: 600;
  border: 1px solid var(--accent); border-radius: 8px;
  background: transparent; color: var(--accent); cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}
.code-btn:hover:not(:disabled) { background: rgba(255,123,61,0.1); }
.code-btn:disabled { border-color: var(--border-subtle); color: var(--text-muted); cursor: not-allowed; }

.submit-btn {
  width: 100%; padding: 12px 0; font-size: 15px; font-weight: 700;
  letter-spacing: 1px; border: none; border-radius: 8px; cursor: pointer;
  color: #fff; background: var(--accent);
  transition: background 0.2s ease, transform 0.15s ease;
  margin-top: 6px;
}
.submit-btn:hover:not(:disabled) { background: var(--accent-hover); }
.submit-btn:active:not(:disabled) { transform: scale(0.97); }
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-loading {
  display: inline-block; width: 20px; height: 20px;
  border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff;
  border-radius: 50%; animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.form-footer {
  text-align: center; margin-top: 20px; font-size: 13px; color: var(--text-muted);
}
.form-footer a { color: var(--accent); cursor: pointer; font-weight: 600; }
.form-footer a:hover { color: var(--accent-hover); }
</style>
