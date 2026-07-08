import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api/kg',
  timeout: 60000
})

// 请求拦截器，自动带上 token
service.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：解包统一响应 Result<T>，取出 data
service.interceptors.response.use(
  res => {
    const body = res.data
    // 如果后端返回的是统一 Result 格式，自动解包
    if (body && typeof body.code === 'number') {
      if (body.code === 200) {
        return body.data
      }
      if (!res.config?.silent) {
        ElMessage.error(body.message || '请求失败')
      }
      return Promise.reject(new Error(body.message))
    }
    // 非 Result 格式直接返回
    return body
  },
  err => {
    const status = err.response?.status
    if (err.config?.silent) {
      return Promise.reject(err)
    }
    if (status === 401) {
      ElMessage.error('账号或密码错误')
    } else if (status === 403) {
      ElMessage.error('无权限')
    } else if (status !== 404) {
      ElMessage.error('请求异常')
    }
    return Promise.reject(err)
  }
)

export default service
