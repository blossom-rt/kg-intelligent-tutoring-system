<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>个人中心</h2>
    </div>

    <!-- 基本信息 -->
    <el-card class="section-card">
      <template #header><span class="section-title">基本信息</span></template>
      <el-descriptions :column="2" border size="large">
        <el-descriptions-item label="用户名">{{ profile.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ profile.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ profile.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleLabel }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 修改个人信息 -->
    <el-card class="section-card">
      <template #header><span class="section-title">修改个人信息</span></template>
      <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="80px" style="max-width: 480px">
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="profileLoading" @click="handleUpdateProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 修改密码 -->
    <el-card class="section-card">
      <template #header><span class="section-title">修改密码</span></template>
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" style="max-width: 480px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="passwordLoading" @click="handleUpdatePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, updatePassword } from '../api/user'

const router = useRouter()

const profile = reactive({
  username: '',
  realName: '',
  email: '',
  role: ''
})

const roleLabel = computed(() => {
  const map = { student: '学生', teacher: '教师', admin: '管理员' }
  return map[profile.role] || profile.role || '-'
})

// ---- 个人信息表单 ----
const profileFormRef = ref(null)
const profileLoading = ref(false)
const profileForm = reactive({
  realName: '',
  email: ''
})

const profileRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

// ---- 密码表单 ----
const passwordFormRef = ref(null)
const passwordLoading = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

onMounted(async () => {
  try {
    const res = await getProfile()
    if (res) {
      profile.username = res.username || ''
      profile.realName = res.realName || ''
      profile.email = res.email || ''
      profile.role = localStorage.getItem('role') || ''
      profileForm.realName = res.realName || ''
      profileForm.email = res.email || ''
    }
  } catch { /* ignore */ }
})

async function handleUpdateProfile() {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  profileLoading.value = true
  try {
    await updateProfile({
      realName: profileForm.realName,
      email: profileForm.email
    })
    ElMessage.success('个人信息修改成功')
    profile.realName = profileForm.realName
    profile.email = profileForm.email
  } catch { /* ignore */ } finally {
    profileLoading.value = false
  }
}

async function handleUpdatePassword() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  passwordLoading.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordFormRef.value.resetFields()
    setTimeout(() => {
      localStorage.clear()
      router.push('/login')
    }, 1500)
  } catch { /* ignore */ } finally {
    passwordLoading.value = false
  }
}
</script>

<style scoped>
.profile-page { padding: 20px; background: #faf7f2; min-height: 100vh; }
.page-header h2 { margin: 0 0 16px; font-size: 20px; color: #2d2a26; }
.section-card { margin-bottom: 20px; }
.section-title { font-weight: 600; color: #2d2a26; }
</style>
