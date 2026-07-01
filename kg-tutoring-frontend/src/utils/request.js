import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api/kg',
  timeout: 10000
})

// 请求拦截器，自动带上token
service.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器，统一处理报错
service.interceptors.response.use(
  res => res.data,
  err => {
    ElMessage.error('请求异常')
    return Promise.reject(err)
  }
)

export default service