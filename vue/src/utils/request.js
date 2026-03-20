import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 如果是 blob 响应（文件下载），直接返回 response.data
    if (response.config.responseType === 'blob') {
      // 检查是否是错误响应（错误响应通常是 JSON 格式的 blob）
      if (response.data.type && response.data.type.includes('application/json')) {
        return new Promise((resolve, reject) => {
          const reader = new FileReader()
          reader.onload = () => {
            try {
              const errorData = JSON.parse(reader.result)
              ElMessage.error(errorData.message || '下载失败')
              reject(new Error(errorData.message || '下载失败'))
            } catch (e) {
              reject(new Error('下载失败'))
            }
          }
          reader.readAsText(response.data)
        })
      }
      return response.data
    }
    
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    if (error.response?.status === 401) {
      ElMessage.error('未登录或登录已过期')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default request


