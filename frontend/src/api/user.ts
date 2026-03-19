import { get, post, put, del } from '@/utils/request'
import type { PageResult, User } from '@/types'

// 用户分页查询
export const getUserPage = (params: {
  pageNum?: number
  pageSize?: number
  username?: string
  realName?: string
  mobile?: string
  status?: number
  orgId?: number
  deptId?: number
  industryType?: string
}) => {
  return get<PageResult<User>>('/users/page', params)
}

// 创建用户
export const createUser = (data: any) => {
  return post<number>('/users', data)
}

// 更新用户
export const updateUser = (id: number, data: any) => {
  return put<boolean>(`/users/${id}`, data)
}

// 删除用户
export const deleteUser = (id: number) => {
  return del<boolean>(`/users/${id}`)
}

// 获取用户详情
export const getUserDetail = (id: number) => {
  return get<User>(`/users/${id}`)
}

// 分配角色
export const assignUserRoles = (id: number, roleIds: number[]) => {
  return post<boolean>(`/users/${id}/roles`, { roleIds })
}

// 更新用户状态
export const updateUserStatus = (id: number, status: number) => {
  return put<boolean>(`/users/${id}/status`, { status })
}

// 重置密码
export const resetUserPassword = (id: number, newPassword: string) => {
  return put<boolean>(`/users/${id}/password`, { newPassword })
}
