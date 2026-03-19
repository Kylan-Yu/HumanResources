import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface User {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  avatar?: string
  roles: string[]
  permissions: string[]
}

interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  updateUser: (user: Partial<User>) => void
}

// 模拟登录API
const loginAPI = async (username: string, password: string) => {
  // 模拟API调用延迟
  await new Promise(resolve => setTimeout(resolve, 1000))
  
  if (username === 'admin' && password === '123456') {
    return {
      accessToken: 'mock-token-' + Date.now(),
      refreshToken: 'mock-refresh-token-' + Date.now(),
      expiresIn: 7200,
      user: {
        id: 1,
        username: 'admin',
        realName: '系统管理员',
        email: 'admin@hrms.com',
        phone: '13800138000',
        avatar: '',
        roles: ['SUPER_ADMIN'],
        permissions: ['*:*:*']
      }
    }
  }
  
  throw new Error('用户名或密码错误')
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,

      login: async (username: string, password: string) => {
        try {
          const response = await loginAPI(username, password)
          
          set({
            user: response.user,
            token: response.accessToken,
            isAuthenticated: true
          })
          
          // 存储token到localStorage
          localStorage.setItem('token', response.accessToken)
        } catch (error) {
          throw error
        }
      },

      logout: () => {
        set({
          user: null,
          token: null,
          isAuthenticated: false
        })
        
        // 清除localStorage中的token
        localStorage.removeItem('token')
      },

      updateUser: (userData: Partial<User>) => {
        const currentUser = get().user
        if (currentUser) {
          set({
            user: { ...currentUser, ...userData }
          })
        }
      }
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated
      })
    }
  )
)
