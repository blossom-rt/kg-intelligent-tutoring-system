<template>
  <div class="forgot-container">
    <div class="forgot-card">
      <h1 class="page-title">找回密码</h1>
      <p class="page-subtitle">通过注册邮箱验证后重置密码</p>

      <el-form ref="formRef" :model="form" :rules="rules" class="forgot-form" @keyup.enter="submit">
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="请输入注册时绑定的邮箱"
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

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="新密码（至少6位）"
            :prefix-icon="Lock" size="large" show-password />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入新密码"
            :prefix-icon="Lock" size="large" show-password />
        </el-form-item>

        <el-button type="primary" size="large" class="forgot-btn" :loading="loading" @click="submit">
          重置密码
        </el-button>
      </el-form>

      <p class="back-link">
        想起密码了？<span @click="$router.push('/login')">返回登录</span>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, Message, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { sendEmailCode, resetPassword } from '../api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

const form = reactive({ email: '', code: '', password: '', confirmPassword: '' })

const validateConfirm = (_rule, value, callback) => {
  callback(value !== form.password ? new Error('两次密码输入不一致') : undefined)
}

const rules = {
  email: [
    { required: true, message: '请输入注册邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const sendCode = async () => {
  if (!form.email) { ElMessage.warning('请先填写邮箱'); return }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) { ElMessage.warning('邮箱格式不正确'); return }
  try {
    await sendEmailCode(form.email, 'reset')
    ElMessage.success('验证码已发送')
    codeCountdown.value = 60
    countdownTimer = setInterval(() => { if (--codeCountdown.value <= 0) clearInterval(countdownTimer) }, 1000)
  } catch { ElMessage.error('发送失败') }
}

const submit = async () => {
  if (!(await formRef.value.validate().catch(() => false))) return
  loading.value = true
  try {
    await resetPassword({ email: form.email, code: form.code, password: form.password })
    ElMessage.success('密码重置成功，请使用新密码登录')
    router.push('/login')
  } catch { }
  finally { loading.value = false }
}
</script>

<style scoped>
.forgot-container {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #eef0f5 0%, #e4e7ef 50%, #d8dce8 100%);
}
.forgot-card {
  width: 440px; padding: 44px 44px 32px; border-radius: 20px; background: #fff;
  box-shadow: 0 20px 60px rgba(0,0,0,0.06), 0 4px 16px rgba(0,0,0,0.03);
}
.page-title { text-align: center; font-size: 24px; font-weight: 700; color: #4a5a78; margin: 0 0 8px; letter-spacing: 2px; }
.page-subtitle { text-align: center; font-size: 13px; color: #9aa4b8; margin: 0 0 30px; }
.forgot-form :deep(.el-form-item) { margin-bottom: 18px; }
.forgot-form :deep(.el-input__wrapper) { border-radius: 10px; transition: box-shadow 0.3s ease; }
.forgot-form :deep(.el-input__wrapper:focus-within) { box-shadow: 0 0 0 2px rgba(89,109,148,0.18) inset, 0 0 10px rgba(89,109,148,0.05); }
.code-row { display: flex; gap: 10px; }
.code-row .el-input { flex: 1; }
.code-btn { flex-shrink: 0; min-width: 120px; border-radius: 10px; border: 1px solid #5a6a90; color: #5a6a90; background: #fff; }
.code-btn:hover:not(:disabled) { background: #eff1f6; }
.code-btn:disabled { border-color: #d0d8e4; color: #bbb; }
.forgot-btn {
  width: 100%; height: 46px; font-size: 16px; font-weight: 600; letter-spacing: 3px;
  border-radius: 10px; margin-top: 6px; border: none;
  background: linear-gradient(135deg, #5a6a90, #4a5a78) !important;
  box-shadow: 0 4px 16px rgba(74,90,120,0.25);
  transition: transform 0.2s ease, box-shadow 0.3s ease;
}
.forgot-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(74,90,120,0.38); }
.forgot-btn:active { transform: scale(0.96); }
.back-link { text-align: center; margin: 18px 0 0; font-size: 14px; color: #aaa; }
.back-link span { color: #5a6a90; cursor: pointer; }
.back-link span:hover { color: #4a5a78; text-decoration: underline; }
</style>
