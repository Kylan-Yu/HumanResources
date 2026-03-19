import { get, post, put, del } from '@/utils/request'
import type { PageResult, PayrollStandard } from '@/types'

// 薪资标准分页查询
export const getPayrollStandardPage = (params: {
  pageNum?: number
  pageSize?: number
  standardName?: string
  orgId?: number
  deptId?: number
  positionId?: number
  gradeLevel?: string
  status?: string
  industryType?: string
}) => {
  return get<PageResult<PayrollStandard>>('/payroll-standards/page', params)
}

// 根据ID查询薪资标准详情
export const getPayrollStandardById = (id: number) => {
  return get<PayrollStandard>(`/payroll-standards/${id}`)
}

// 创建薪资标准
export const createPayrollStandard = (data: any) => {
  return post<number>('/payroll-standards', data)
}

// 更新薪资标准
export const updatePayrollStandard = (id: number, data: any) => {
  return put<boolean>(`/payroll-standards/${id}`, data)
}

// 删除薪资标准
export const deletePayrollStandard = (id: number) => {
  return del<boolean>(`/payroll-standards/${id}`)
}

// 更新薪资标准状态
export const updatePayrollStandardStatus = (id: number, status: string) => {
  return put<boolean>(`/payroll-standards/${id}/status`, { status })
}

// 根据员工ID查询适用的薪资标准
export const getPayrollStandardByEmployeeId = (employeeId: number) => {
  return get<PayrollStandard>(`/payroll-standards/employee/${employeeId}`)
}
