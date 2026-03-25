import { get, post } from '@/utils/request'

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
  userId: number
  username: string
  realName: string
  email?: string
  mobile?: string
  avatar?: string
  roles: string[]
  permissions: string[]
}

export const login = (data: LoginRequest) => post<LoginResponse>('/auth/login', data)

export const logout = () => post('/auth/logout')

export const getUserInfo = () => get<LoginResponse>('/auth/user-info')

export const refreshToken = () => post<LoginResponse>('/auth/refresh')
