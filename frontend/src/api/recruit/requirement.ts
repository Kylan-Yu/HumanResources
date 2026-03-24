import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types'
import type { RecruitRequirement } from '@/types/recruit'

// 招聘需求分页查询
export const getRecruitRequirementPage = (params: {
  pageNum?: number
  pageSize?: number
  title?: string
  orgId?: number
  deptId?: number
  positionId?: number
  requirementStatus?: string
  urgencyLevel?: string
  industryType?: string
  expectedEntryDateBegin?: string
  expectedEntryDateEnd?: string
}) => {
  return get<PageResult<RecruitRequirement>>('/recruit-requirements/page', params)
}

// 根据ID查询招聘需求详情
export const getRecruitRequirementById = (id: number) => {
  return get<RecruitRequirement>(`/recruit-requirements/${id}`)
}

// 创建招聘需求
export const createRecruitRequirement = (data: any) => {
  return post<number>('/recruit-requirements', data)
}

// 更新招聘需求
export const updateRecruitRequirement = (id: number, data: any) => {
  return put<boolean>(`/recruit-requirements/${id}`, data)
}

// 删除招聘需求
export const deleteRecruitRequirement = (id: number) => {
  return del<boolean>(`/recruit-requirements/${id}`)
}

// 更新招聘需求状态
export const updateRecruitRequirementStatus = (id: number, status: string) => {
  return put<boolean>(`/recruit-requirements/${id}/status`, { status })
}
