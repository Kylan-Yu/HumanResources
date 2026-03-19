import { get, post, put, del } from '@/utils/request'
import type { PageResult, Employee } from '@/types'

// 员工分页查询
export const getEmployeePage = (params: {
  pageNum?: number
  pageSize?: number
  employeeNo?: string
  name?: string
  mobile?: string
  employeeStatus?: number
  deptId?: number
  orgId?: number
  industryType?: string
}) => {
  return get<PageResult<Employee>>('/employees/page', params)
}

// 创建员工
export const createEmployee = (data: any) => {
  return post<number>('/employees', data)
}

// 更新员工
export const updateEmployee = (id: number, data: any) => {
  return put<boolean>(`/employees/${id}`, data)
}

// 删除员工
export const deleteEmployee = (id: number) => {
  return del<boolean>(`/employees/${id}`)
}

// 获取员工详情
export const getEmployeeDetail = (id: number) => {
  return get<Employee>(`/employees/${id}`)
}

// 更新员工状态
export const updateEmployeeStatus = (id: number, status: number) => {
  return put<boolean>(`/employees/${id}/status`, { status })
}
