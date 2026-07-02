import request from '../utils/request'

// 登录
export function login(data) {
  return request.post('/user/login', data)
}

// 注册
export function register(data) {
  return request.post('/user/register', data)
}

// 发送邮箱验证码（type: register | reset）
export function sendEmailCode(email, type) {
  return request.get('/user/send-code', { params: { email, type } })
}

// 忘记密码-重置
export function resetPassword(data) {
  return request.post('/user/reset-password', data)
}