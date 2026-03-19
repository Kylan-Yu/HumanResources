import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { message } from 'antd'

// 响应数据接口
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, message: msg, data } = response.data
    
    // 请求成功
    if (code === 200) {
      return response.data
    }
    
    // 业务错误
    message.error(msg || '请求失败')
    return Promise.reject(new Error(msg || '请求失败'))
  },
  (error) => {
    const { response } = error
    
    if (response) {
      const { status, data } = response
      
      switch (status) {
        case 401:
          message.error('未登录或登录已过期')
          // 跳转到登录页
          window.location.href = '/login'
          break
        case 403:
          message.error('没有权限访问该资源')
          break
        case 404:
          message.error('请求的资源不存在')
          break
        case 500:
          message.error('服务器内部错误')
          break
        default:
          message.error(data?.message || '网络错误')
      }
    } else {
      // 网络错误
      message.error('网络连接失败，请检查网络设置')
    }
    
    return Promise.reject(error)
  }
)

// GET请求
export const get = <T = any>(url: string, params?: any): Promise<ApiResponse<T>> => {
  return request.get(url, { params })
}

// POST请求
export const post = <T = any>(url: string, data?: any): Promise<ApiResponse<T>> => {
  return request.post(url, data)
}

// PUT请求
export const put = <T = any>(url: string, data?: any): Promise<ApiResponse<T>> => {
  return request.put(url, data)
}

// DELETE请求
export const del = <T = any>(url: string, params?: any): Promise<ApiResponse<T>> => {
  return request.delete(url, { params })
}

// 文件上传
export const upload = <T = any>(url: string, formData: FormData): Promise<ApiResponse<T>> => {
  return request.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export default request
