import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types'

export const getLeaveMyPage = (params: any) => get<PageResult<any>>('/leave-applications/my/page', params)

export const getLeavePage = (params: any) => get<PageResult<any>>('/leave-applications/page', params)

export const createLeaveApplication = (data: any) => post<number>('/leave-applications', data)

export const getLeaveProgress = (id: number) => get<any>(`/leave-applications/${id}/progress`)

export const getTodoTaskPage = (params: any) => get<PageResult<any>>('/workflow/tasks/todo/page', params)

export const workflowTaskAction = (taskId: number, data: any) => post<boolean>(`/workflow/tasks/${taskId}/action`, data)

export const getWorkflowTemplatePage = (params: any) => get<PageResult<any>>('/workflow/templates/page', params)

export const getWorkflowTemplateDetail = (id: string) => get<any>(`/workflow/templates/${id}`)

export const createWorkflowTemplate = (data: any) => post<any>('/workflow/templates', data)

export const updateWorkflowTemplate = (id: string, data: any) => put<any>(`/workflow/templates/${id}`, data)

export const deleteWorkflowTemplate = (id: string) => del<boolean>(`/workflow/templates/${id}`)

export const getWorkflowTemplateNodes = (id: number) => {
  console.log('🔍 [前端] 请求工作流模板节点:', { id, url: `/workflow/templates/${id}/nodes` })
  return get<any[]>(`/workflow/templates/${id}/nodes`)
}

export const saveWorkflowTemplateNodes = (id: number, data: any[]) => {
  console.log('🔍 [前端] 保存工作流模板节点:', { id, dataLength: data?.length || 0 })
  return post<boolean>(`/workflow/templates/${id}/nodes`, data)
}

export const getWorkflowRoleList = () => get<any[]>('/roles/list')

export const getWorkflowUserPage = (params: any) => get<PageResult<any>>('/users/page', params)

export const createPatchApplication = (data: any) => post<number>('/patch-applications', data)

export const getPatchMyPage = (params: any) => get<PageResult<any>>('/patch-applications/my/page', params)

export const withdrawPatchApplication = (id: number) => post<boolean>(`/patch-applications/${id}/withdraw`)

export const createOvertimeApplication = (data: any) => post<number>('/overtime-applications', data)

export const getOvertimeMyPage = (params: any) => get<PageResult<any>>('/overtime-applications/my/page', params)

export const withdrawOvertimeApplication = (id: number) => post<boolean>(`/overtime-applications/${id}/withdraw`)

export const getMyApplicationPage = (params: any) => get<PageResult<any>>('/applications/my/page', params)

export const getApplicationProgress = (businessType: string, businessId: number) =>
  get<any>(`/applications/${businessType}/${businessId}/progress`)
