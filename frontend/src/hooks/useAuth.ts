import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth'

export const useAuth = () => {
  const navigate = useNavigate()
  const { user, token, isAuthenticated, login, logout, updateUser } = useAuthStore()

  const checkAuth = () => {
    if (!isAuthenticated) {
      navigate('/login')
      return false
    }
    return true
  }

  const hasPermission = (permission: string) => {
    if (!user || !user.permissions) return false
    return user.permissions.includes('*:*:*') || user.permissions.includes(permission)
  }

  const hasRole = (role: string) => {
    if (!user || !user.roles) return false
    return user.roles.includes(role)
  }

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return {
    user,
    token,
    isAuthenticated,
    login,
    logout,
    updateUser,
    checkAuth,
    hasPermission,
    hasRole,
    handleLogout
  }
}
