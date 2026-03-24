import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types'
import type { Contract } from '@/types/contract'

// 合同分页查询
export const getContractPage = (params: {
  pageNum?: number
  pageSize?: number
  employeeId?: number
  employeeName?: string
  contractNo?: string
  contractType?: string
  contractStatus?: string
  startDateBegin?: string
  startDateEnd?: string
  endDateBegin?: string
  endDateEnd?: string
  industryType?: string
}) => {
  return get<PageResult<Contract>>('/contracts/page', params)
}

// 根据ID查询合同详情
export const getContractById = (id: number) => {
  return get<Contract>(`/contracts/${id}`)
}

// 创建合同
export const createContract = (data: any) => {
  return post<number>('/contracts', data)
}

// 更新合同
export const updateContract = (id: number, data: any) => {
  return put<boolean>(`/contracts/${id}`, data)
}

// 删除合同
export const deleteContract = (id: number) => {
  return del<boolean>(`/contracts/${id}`)
}

// 更新合同状态
export const updateContractStatus = (id: number, status: string) => {
  return put<boolean>(`/contracts/${id}/status`, { status })
}

// 续签合同
export const renewContract = (id: number, data: any) => {
  return post<boolean>(`/contracts/${id}/renew`, data)
}

// 查询即将到期的合同
export const getExpireWarningContracts = (params: {
  pageNum?: number
  pageSize?: number
  warningDays?: number
}) => {
  return get<PageResult<Contract>>('/contracts/expire-warning/page', params)
}
