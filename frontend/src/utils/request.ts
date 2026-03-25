import axios, { AxiosInstance } from 'axios'
import { message } from 'antd'

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: number
}

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config: any) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

;(request.interceptors.response as any).use(
  (response: any) => {
    const payload: ApiResponse = response.data
    if (payload?.code === 200) {
      return payload
    }
    message.error(payload?.message || '请求失败')
    return Promise.reject(new Error(payload?.message || '请求失败'))
  },
  (error: any) => {
    const status = error?.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      message.error('未登录或登录已过期')
      window.location.href = '/login'
    } else if (status === 403) {
      message.error('没有权限访问')
    } else if (status === 404) {
      message.error('请求资源不存在')
    } else if (status === 500) {
      message.error('服务器内部错误')
    } else {
      message.error(error?.response?.data?.message || '网络连接失败')
    }
    return Promise.reject(error)
  }
)

export const get = <T = any>(url: string, params?: any): Promise<ApiResponse<T>> => {
  return request.get(url, { params }) as unknown as Promise<ApiResponse<T>>
}

export const post = <T = any>(url: string, data?: any): Promise<ApiResponse<T>> => {
  return request.post(url, data) as unknown as Promise<ApiResponse<T>>
}

export const put = <T = any>(url: string, data?: any): Promise<ApiResponse<T>> => {
  return request.put(url, data) as unknown as Promise<ApiResponse<T>>
}

export const del = <T = any>(url: string, params?: any): Promise<ApiResponse<T>> => {
  return request.delete(url, { params }) as unknown as Promise<ApiResponse<T>>
}

export const upload = <T = any>(url: string, formData: FormData): Promise<ApiResponse<T>> => {
  return request.post(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }) as unknown as Promise<ApiResponse<T>>
}

export default request
