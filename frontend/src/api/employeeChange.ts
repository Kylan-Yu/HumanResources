import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types'

export interface EmployeeChangeRecord {
  id: number
  employeeId: number
  employeeNo?: string
  employeeName?: string
  changeType: string
  changeDate: string
  beforeValue?: string
  afterValue?: string
  changeReason?: string
  approverId?: number
  approveTime?: string
  remark?: string
  createdTime?: string
  updatedTime?: string
}

export const getEmployeeChangePage = (params: any) =>
  get<PageResult<EmployeeChangeRecord>>('/employee-changes/page', params)

export const getEmployeeChangeById = (id: number) =>
  get<EmployeeChangeRecord>(`/employee-changes/${id}`)

export const createEmployeeChange = (data: any) =>
  post<number>('/employee-changes', data)

export const updateEmployeeChange = (id: number, data: any) =>
  put<boolean>(`/employee-changes/${id}`, data)

export const deleteEmployeeChange = (id: number) =>
  del<boolean>(`/employee-changes/${id}`)

