<template>
  <div class="register-container">
    <div class="register-card">
      <h1 class="page-title">创建学生账号</h1>
      <p class="page-subtitle">注册后即可开始个性化学习</p>

      <el-form ref="formRef" :model="form" :rules="rules" class="register-form" @keyup.enter="submit">
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
            <el-button class="code-btn" size="large" :disabled="codeCountdown > 0" @click="sendCode">
              {{ codeCountdown > 0 ? codeCountdown + 's 后重发' : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="submit">
          注册
        </el-button>
      </el-form>

      <p class="back-link">
        已有账号？<span @click="$router.push('/login')">返回登录</span>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, UserFilled, Message, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register, sendEmailCode } from '../api/user'

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
    await sendEmailCode(form.email, 'register')
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
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f4fd 0%, #d4e8fb 50%, #c5ddf8 100%);
}
.register-card {
  width: 460px;
  padding: 44px 44px 32px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 20px 60px rgba(47, 102, 207, 0.08), 0 4px 16px rgba(0,0,0,0.03);
}
.page-title { text-align: center; font-size: 24px; font-weight: 700; color: #2c5eb5; margin: 0 0 8px; letter-spacing: 2px; }
.page-subtitle { text-align: center; font-size: 13px; color: #a0b5d4; margin: 0 0 30px; }
.register-form :deep(.el-form-item) { margin-bottom: 18px; }
.register-form :deep(.el-input__wrapper) { border-radius: 10px; transition: box-shadow 0.3s ease; }
.register-form :deep(.el-input__wrapper:focus-within) { box-shadow: 0 0 0 2px rgba(79,140,247,0.20) inset, 0 0 10px rgba(79,140,247,0.06); }
.code-row { display: flex; gap: 10px; }
.code-row .el-input { flex: 1; }
.code-btn { flex-shrink: 0; min-width: 120px; border-radius: 10px; border: 1px solid #4f8cf7; color: #4f8cf7; background: #fff; }
.code-btn:hover:not(:disabled) { background: #eef4ff; }
.code-btn:disabled { border-color: #d0d8e4; color: #bbb; }
.register-btn {
  width: 100%; height: 46px; font-size: 16px; font-weight: 600; letter-spacing: 3px;
  border-radius: 10px; margin-top: 6px; border: none;
  background: linear-gradient(135deg, #4f8cf7, #3670e8) !important;
  box-shadow: 0 4px 16px rgba(54,112,232,0.28);
  transition: transform 0.2s ease, box-shadow 0.3s ease;
}
.register-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(54,112,232,0.40); }
.register-btn:active { transform: scale(0.96); }
.back-link { text-align: center; margin: 18px 0 0; font-size: 14px; color: #aaa; }
.back-link span { color: #4f8cf7; cursor: pointer; }
.back-link span:hover { color: #3670e8; text-decoration: underline; }
</style>
