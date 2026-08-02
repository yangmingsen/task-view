import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器：自动带 Token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：401 跳登录
request.interceptors.response.use(
  (res) => {
    if (res.data?.code !== 200) {
      return Promise.reject(new Error(res.data?.msg || '请求失败'))
    }
    return res.data.data // 直接返回 data 字段
  },
  (err) => {
    if (err.response?.status === 401) {
      localStorage.clear()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  },
)

export default request
