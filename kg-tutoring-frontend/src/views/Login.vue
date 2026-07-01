<template>
  <el-card style="width:400px;margin:100px auto;">
    <el-form ref="formRef" :model="form">
      <el-form-item label="账号">
        <el-input v-model="form.username"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password"></el-input>
      </el-form-item>
      <el-button type="primary" @click="submit">登录</el-button>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/user'

const router = useRouter()
const formRef = ref(null)
const form = ref({
  username: '',
  password: ''
})

const submit = async () => {
  const res = await login(form.value)
  localStorage.setItem('token', res.token)
  // 根据角色跳转页面
  if (res.role === 'student') {
    router.push('/student')
  } else {
    router.push('/teacher')
  }
}
</script>