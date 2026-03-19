import { post, get } from '@/utils/request'

export interface LoginRequest {
  username: string
  password: string
  remember?: boolean
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: {
    id: number
    username: string
    realName: string
    email: string
    phone: string
    avatar?: string
    roles: string[]
    permissions: string[]
  }
}

export interface UserInfo {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  avatar?: string
  roles: string[]
  permissions: string[]
}

// 登录
export const login = (data: LoginRequest) => {
  return post<LoginResponse>('/auth/login', data)
}

// 登出
export const logout = () => {
  return post('/auth/logout')
}

// 获取用户信息
export const getUserInfo = () => {
  return get<UserInfo>('/auth/user-info')
}

// 刷新Token
export const refreshToken = () => {
  return post<LoginResponse>('/auth/refresh')
}
