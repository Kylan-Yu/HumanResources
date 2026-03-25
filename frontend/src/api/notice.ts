import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types'

export const getNoticePage = (params: any) => get<PageResult<any>>('/notices/page', params)

export const getCurrentNoticePage = (params: any) => get<PageResult<any>>('/notices/current/page', params)

export const getNoticeDetail = (id: number) => get<any>(`/notices/${id}`)

export const createNotice = (data: any) => post<number>('/notices', data)

export const updateNotice = (id: number, data: any) => put<boolean>(`/notices/${id}`, data)

export const deleteNotice = (id: number) => del<boolean>(`/notices/${id}`)

export const markNoticeRead = (id: number) => post<boolean>(`/notices/${id}/read`)

export const getDeptNoticePage = (params: any) => get<PageResult<any>>('/notices/dept/page', params)

export const getDeptNoticeDetail = (id: number) => get<any>(`/notices/dept/${id}`)
