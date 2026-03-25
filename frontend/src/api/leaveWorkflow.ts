import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types'

export const getLeaveMyPage = (params: any) => get<PageResult<any>>('/leave-applications/my/page', params)

export const getLeavePage = (params: any) => get<PageResult<any>>('/leave-applications/page', params)

export const createLeaveApplication = (data: any) => post<number>('/leave-applications', data)

export const getLeaveProgress = (id: number) => get<any>(`/leave-applications/${id}/progress`)

export const getTodoTaskPage = (params: any) => get<PageResult<any>>('/workflow/tasks/todo/page', params)

export const workflowTaskAction = (taskId: number, data: any) => post<boolean>(`/workflow/tasks/${taskId}/action`, data)

export const getWorkflowTemplatePage = (params: any) => get<PageResult<any>>('/workflow/templates/page', params)

export const getWorkflowTemplateDetail = (id: number) => get<any>(`/workflow/templates/${id}`)

export const createWorkflowTemplate = (data: any) => post<number>('/workflow/templates', data)

export const updateWorkflowTemplate = (id: number, data: any) => put<boolean>(`/workflow/templates/${id}`, data)

export const deleteWorkflowTemplate = (id: number) => del<boolean>(`/workflow/templates/${id}`)

export const getWorkflowTemplateNodes = (id: number) => get<any[]>(`/workflow/templates/${id}/nodes`)

export const saveWorkflowTemplateNodes = (id: number, data: any[]) => post<boolean>(`/workflow/templates/${id}/nodes`, data)
