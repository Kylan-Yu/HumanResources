import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types'

export interface Rank {
  id: number
  rankCode: string
  rankName: string
  rankSeries?: string
  rankLevel?: number
  description?: string
  status: number
  sortOrder?: number
  industryType?: string
}

export const getRankPage = (params: any) => get<PageResult<Rank>>('/rank/page', params)
export const getRankList = (params?: any) => get<Rank[]>('/rank/list', params)
export const getRankById = (id: number) => get<Rank>(`/rank/${id}`)
export const createRank = (data: any) => post<number>('/rank', data)
export const updateRank = (id: number, data: any) => put<boolean>(`/rank/${id}`, data)
export const deleteRank = (id: number) => del<boolean>(`/rank/${id}`)
export const updateRankStatus = (id: number, status: number) => put<boolean>(`/rank/${id}/status?status=${status}`)

