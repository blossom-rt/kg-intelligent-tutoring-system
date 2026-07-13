import request from '../utils/request'

// 账号密码登录
export function login(data) {
  return request.post('/auth/login', data)
}

// 学生自主注册
export function register(data) {
  return request.post('/auth/register', data)
}

// 发送邮箱验证码（type: register | reset）
export function sendEmailCode(data) {
  return request.post('/auth/send-code', data)
}

// 邮箱找回密码
export function resetPassword(data) {
  return request.post('/auth/reset-password', data)
}

// 修改登录密码
export function updatePassword(data) {
  return request.put('/user/update-password', data)
}

// 获取 / 修改个人信息
export function getProfile() {
  return request.get('/user/profile')
}
export function updateProfile(data) {
  return request.put('/user/profile', data)
}