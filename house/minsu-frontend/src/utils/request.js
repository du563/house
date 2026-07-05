import axios from 'axios'
import store from '@/store'
import router from '@/router'
import { Message } from 'element-ui'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = store.state.token
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
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
    const res = response.data
    if (res.code === 200) {
      return res
    } else if (res.code === 401) {
      Message.error(res.message || '请先登录')
      store.dispatch('logout')
      router.push('/login')
      return Promise.reject(new Error(res.message))
    } else {
      Message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
  },
  error => {
    Message.error('网络错误，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
