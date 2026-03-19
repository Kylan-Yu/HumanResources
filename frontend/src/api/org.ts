import { get, post, put, del } from '@/utils/request'
import type { Organization } from '@/types'

// 获取组织树
export const getOrganizationTree = () => {
  return get<Organization[]>('/org/tree')
}

// 分页查询组织
export const getOrganizationPage = (params: {
  orgName?: string
  orgType?: string
  status?: number
  industryType?: string
}) => {
  return get<Organization[]>('/org/page', params)
}

// 创建组织
export const createOrganization = (data: any) => {
  return post<number>('/org', data)
}

// 更新组织
export const updateOrganization = (id: number, data: any) => {
  return put<boolean>(`/org/${id}`, data)
}

// 删除组织
export const deleteOrganization = (id: number) => {
  return del<boolean>(`/org/${id}`)
}

// 获取组织详情
export const getOrganizationDetail = (id: number) => {
  return get<Organization>(`/org/${id}`)
}

// 更新组织状态
export const updateOrganizationStatus = (id: number, status: number) => {
  return put<boolean>(`/org/${id}/status`, { status })
}
