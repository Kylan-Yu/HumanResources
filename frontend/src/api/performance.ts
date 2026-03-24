import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types'

export interface PerformancePlan {
  id: number
  planName: string
  planYear: number
  planPeriod: string
  orgId?: number
  orgName?: string
  deptId?: number
  deptName?: string
  status: string
  description?: string
  createdTime?: string
}

export interface PerformanceRecord {
  id: number
  planId: number
  planName?: string
  employeeId: number
  employeeNo?: string
  employeeName?: string
  score?: number
  grade?: string
  resultStatus?: string
  managerComment?: string
}

export const getPerformancePlanPage = (params: any) =>
  get<PageResult<PerformancePlan>>('/performance/plans/page', params)

export const createPerformancePlan = (data: any) =>
  post<number>('/performance/plans', data)

export const updatePerformancePlan = (id: number, data: any) =>
  put<boolean>(`/performance/plans/${id}`, data)

export const deletePerformancePlan = (id: number) =>
  del<boolean>(`/performance/plans/${id}`)

export const updatePerformancePlanStatus = (id: number, status: string) =>
  put<boolean>(`/performance/plans/${id}/status`, { status })

export const getPerformanceRecordPage = (params: any) =>
  get<PageResult<PerformanceRecord>>('/performance/records/page', params)

export const createPerformanceRecord = (data: any) =>
  post<number>('/performance/records', data)

export const updatePerformanceRecord = (id: number, data: any) =>
  put<boolean>(`/performance/records/${id}`, data)

export const deletePerformanceRecord = (id: number) =>
  del<boolean>(`/performance/records/${id}`)

