import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { getUserInfo, login as loginApi, logout as logoutApi } from '@/api/auth'
import { getCurrentMenuTree, type MenuItem } from '@/api/menu'

interface User {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  avatar?: string
  roles: string[]
  permissions: string[]
}

interface AuthState {
  user: User | null
  token: string | null
  refreshToken: string | null
  menuTree: MenuItem[]
  isAuthenticated: boolean
  initialized: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => Promise<void>
  updateUser: (user: Partial<User>) => void
  initialize: () => Promise<void>
  loadMenuTree: () => Promise<void>
}

const mapLoginPayloadToUser = (payload: any): User => ({
  id: payload.userId,
  username: payload.username,
  realName: payload.realName,
  email: payload.email,
  phone: payload.mobile,
  avatar: payload.avatar,
  roles: payload.roles || [],
  permissions: payload.permissions || []
})

const clearAuthStorage = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      refreshToken: null,
      menuTree: [],
      isAuthenticated: false,
      initialized: false,

      login: async (username: string, password: string) => {
        const response = await loginApi({ username, password })
        const payload = response.data

        const user = mapLoginPayloadToUser(payload)
        const token = payload.accessToken
        const refreshToken = payload.refreshToken

        localStorage.setItem('token', token)
        localStorage.setItem('refreshToken', refreshToken)

        set({
          user,
          token,
          refreshToken,
          isAuthenticated: true
        })

        await get().loadMenuTree()
      },

      logout: async () => {
        try {
          await logoutApi()
        } catch {
          // ignore logout API failure and clear local auth anyway
        }

        clearAuthStorage()
        set({
          user: null,
          token: null,
          refreshToken: null,
          menuTree: [],
          isAuthenticated: false,
          initialized: true
        })
      },

      updateUser: (userData: Partial<User>) => {
        const currentUser = get().user
        if (!currentUser) {
          return
        }
        set({
          user: { ...currentUser, ...userData }
        })
      },

      initialize: async () => {
        if (get().initialized) {
          return
        }

        const cachedToken = localStorage.getItem('token')
        const cachedRefreshToken = localStorage.getItem('refreshToken')
        if (!cachedToken) {
          set({ initialized: true, isAuthenticated: false })
          return
        }

        set({ token: cachedToken, refreshToken: cachedRefreshToken })

        try {
          const userInfoRes = await getUserInfo()
          const user = mapLoginPayloadToUser(userInfoRes.data)
          set({ user, isAuthenticated: true })
          await get().loadMenuTree()
        } catch {
          clearAuthStorage()
          set({
            user: null,
            token: null,
            refreshToken: null,
            menuTree: [],
            isAuthenticated: false
          })
        } finally {
          set({ initialized: true })
        }
      },

      loadMenuTree: async () => {
        try {
          const response = await getCurrentMenuTree()
          set({ menuTree: response.data || [] })
        } catch {
          set({ menuTree: [] })
        }
      }
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        refreshToken: state.refreshToken,
        menuTree: state.menuTree,
        isAuthenticated: state.isAuthenticated
      })
    }
  )
)
