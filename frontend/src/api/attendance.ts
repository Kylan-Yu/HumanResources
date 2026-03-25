import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types'

export const getAttendanceShiftPage = (params: any) =>
  get<PageResult<any>>('/attendance/shifts/page', params)

export const getAttendanceShiftOptions = (params?: any) =>
  get<any[]>('/attendance/shifts/options', params)

export const createAttendanceShift = (data: any) =>
  post<number>('/attendance/shifts', data)

export const updateAttendanceShift = (id: number, data: any) =>
  put<boolean>(`/attendance/shifts/${id}`, data)

export const deleteAttendanceShift = (id: number) =>
  del<boolean>(`/attendance/shifts/${id}`)

export const getAttendanceRecordPage = (params: any) =>
  get<PageResult<any>>('/attendance/records/page', params)

export const getAttendanceEmployeeOptions = (params?: any) =>
  get<any[]>('/attendance/employees/options', params)

export const createAttendanceRecord = (data: any) =>
  post<number>('/attendance/records', data)

export const updateAttendanceRecord = (id: number, data: any) =>
  put<boolean>(`/attendance/records/${id}`, data)

export const deleteAttendanceRecord = (id: number) =>
  del<boolean>(`/attendance/records/${id}`)

export const getAttendanceAppealPage = (params: any) =>
  get<PageResult<any>>('/attendance/appeals/page', params)

export const createAttendanceAppeal = (data: any) =>
  post<number>('/attendance/appeals', data)

export const updateAttendanceAppealStatus = (id: number, data: any) =>
  put<boolean>(`/attendance/appeals/${id}/status`, data)

export const deleteAttendanceAppeal = (id: number) =>
  del<boolean>(`/attendance/appeals/${id}`)

export const getAttendanceMonthlyStats = (params?: any) =>
  get<any[]>('/attendance/statistics/monthly', params)

export const getMyAttendancePage = (params: any) =>
  get<PageResult<any>>('/attendance/my/page', params)

export const getTeamAttendanceSummaryPage = (params: any) =>
  get<PageResult<any>>('/attendance/team/summary', params)
