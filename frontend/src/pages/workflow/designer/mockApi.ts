import dayjs from 'dayjs'
import { del, get, post, put } from '@/utils/request'
import { DEFAULT_TEMPLATE_CATEGORY, DEFAULT_TEMPLATE_NAME } from './constants'
import { normalizeTemplateModel } from './utils/flowData'
import type { WorkflowTemplateModel, WorkflowTemplateStatus, WorkflowTemplateVersionSnapshot } from './types'

interface WorkflowTemplateQuery {
  keyword?: string
  category?: string
  status?: WorkflowTemplateStatus | 'all'
  pageNum?: number
  pageSize?: number
}

interface WorkflowTemplatePageResult {
  list: WorkflowTemplateModel[]
  total: number
  pageNum: number
  pageSize: number
}

interface CreateWorkflowTemplateInput {
  templateName: string
  templateCode: string
  category: string
}

const nowText = (): string => dayjs().format('YYYY-MM-DD HH:mm:ss')

const normalizeStatus = (status: unknown): WorkflowTemplateStatus => {
  const raw = String(status || '').toLowerCase()
  if (raw === 'published' || raw === 'enabled') {
    return 'published'
  }
  if (raw === 'disabled') {
    return 'disabled'
  }
  return 'draft'
}

const mapApiTemplate = (raw: any, fallbackTemplateId?: string): WorkflowTemplateModel => {
  const definition =
    raw?.definition ||
    raw?.snapshot?.definition ||
    (Array.isArray(raw?.nodes) || Array.isArray(raw?.edges)
      ? {
          nodes: Array.isArray(raw?.nodes) ? raw.nodes : [],
          edges: Array.isArray(raw?.edges) ? raw.edges : []
        }
      : undefined)

  return normalizeTemplateModel({
    templateId: String(raw?.templateId || fallbackTemplateId || ''),
    templateName: String(raw?.templateName || DEFAULT_TEMPLATE_NAME),
    templateCode: String(raw?.templateCode || (fallbackTemplateId || 'TEMPLATE')),
    category: String(raw?.category || DEFAULT_TEMPLATE_CATEGORY),
    status: normalizeStatus(raw?.status),
    version: Number(raw?.version || raw?.currentVersion || 1),
    updatedAt: String(raw?.updatedAt || nowText()),
    viewport: raw?.viewport,
    layout: raw?.layout,
    definition,
    nodes: definition?.nodes || [],
    edges: definition?.edges || []
  })
}

const buildSnapshotPayload = (payload: WorkflowTemplateModel): Record<string, unknown> => {
  const definition = {
    nodes: payload.definition?.nodes || payload.nodes || [],
    edges: payload.definition?.edges || payload.edges || []
  }

  const snapshot = {
    templateId: payload.templateId,
    templateName: payload.templateName,
    templateCode: payload.templateCode,
    category: payload.category,
    status: payload.status,
    version: payload.version,
    viewport: payload.viewport || { x: 0, y: 0, zoom: 1 },
    layout: payload.layout || { manualPositions: true },
    definition,
    meta: {
      updatedAt: payload.updatedAt || nowText()
    }
  }

  return {
    templateId: payload.templateId,
    templateName: payload.templateName,
    templateCode: payload.templateCode,
    category: payload.category,
    status: payload.status,
    version: payload.version,
    updatedAt: payload.updatedAt,
    viewport: snapshot.viewport,
    layout: snapshot.layout,
    definition,
    snapshot
  }
}

export const getWorkflowTemplatePage = async (
  query: WorkflowTemplateQuery = {}
): Promise<WorkflowTemplatePageResult> => {
  const response = await get<any>('/workflow/templates/page', query)
  const data = response.data || {}
  const list = Array.isArray(data.list) ? data.list.map((item: any) => mapApiTemplate(item)) : []

  return {
    list,
    total: Number(data.total || 0),
    pageNum: Number(data.pageNum || query.pageNum || 1),
    pageSize: Number(data.pageSize || query.pageSize || 10)
  }
}

export const getWorkflowTemplateCategories = async (): Promise<string[]> => {
  const response = await get<any[]>('/workflow/templates/categories')
  return Array.isArray(response.data) ? response.data.filter(Boolean) : []
}

export const createWorkflowTemplate = async (
  input: CreateWorkflowTemplateInput
): Promise<WorkflowTemplateModel> => {
  const response = await post<any>('/workflow/templates', {
    templateName: input.templateName,
    templateCode: input.templateCode,
    category: input.category,
    status: 'draft'
  })
  return mapApiTemplate(response.data)
}

export const duplicateWorkflowTemplate = async (templateId: string): Promise<WorkflowTemplateModel> => {
  const response = await post<any>(`/workflow/templates/${templateId}/duplicate`)
  return mapApiTemplate(response.data)
}

export const deleteWorkflowTemplate = async (templateId: string): Promise<boolean> => {
  const response = await del<boolean>(`/workflow/templates/${templateId}`)
  return Boolean(response.data)
}

export const getWorkflowTemplateDetail = async (templateId: string): Promise<WorkflowTemplateModel> => {
  const response = await get<any>(`/workflow/templates/${templateId}`)
  console.log('[workflow-template:frontend] load detail response', {
    templateId,
    templateName: response.data?.templateName,
    status: response.data?.status,
    definition: response.data?.definition
  })
  return mapApiTemplate(response.data, templateId)
}

export const saveWorkflowTemplate = async (
  payload: WorkflowTemplateModel
): Promise<{ success: boolean; data: WorkflowTemplateModel }> => {
  const requestPayload = buildSnapshotPayload(payload)

  console.log('[workflow-template:frontend] save request', {
    templateId: payload.templateId,
    version: payload.version,
    nodeCount: payload.nodes.length,
    edgeCount: payload.edges.length,
    snapshot: requestPayload.snapshot
  })

  const response = await put<any>(`/workflow/templates/${payload.templateId}`, requestPayload)

  console.log('[workflow-template:frontend] save response', {
    templateId: response.data?.templateId,
    version: response.data?.version,
    status: response.data?.status,
    message: '保存完成'
  })

  return {
    success: true,
    data: mapApiTemplate(response.data, payload.templateId)
  }
}

export const publishWorkflowTemplate = async (
  templateId: string,
  payload?: WorkflowTemplateModel
): Promise<{ success: boolean; publishTime: string; templateId: string; data?: WorkflowTemplateModel }> => {
  const requestPayload = payload ? buildSnapshotPayload(payload) : {}

  const response = await post<any>(`/workflow/templates/${templateId}/publish`, requestPayload)

  const detail = response.data?.data ? mapApiTemplate(response.data.data, templateId) : undefined

  return {
    success: true,
    publishTime: String(response.data?.publishTime || nowText()),
    templateId: String(response.data?.templateId || templateId),
    data: detail
  }
}

export const getWorkflowTemplateVersions = async (
  templateId: string
): Promise<WorkflowTemplateVersionSnapshot[]> => {
  const response = await get<any[]>(`/workflow/templates/${templateId}/versions`)
  const list = Array.isArray(response.data) ? response.data : []

  return list.map((item: any) => ({
    version: Number(item.version || item.versionNo || 0),
    updatedAt: String(item.updatedAt || nowText()),
    operator: String(item.operator || 'system'),
    actionType: item.actionType,
    status: normalizeStatus(item.status),
    remark: item.remark,
    payload: mapApiTemplate(item.payload || item.snapshot || {}, templateId)
  }))
}

export const getWorkflowTemplateVersionDetail = async (
  templateId: string,
  versionNo: number
): Promise<WorkflowTemplateVersionSnapshot> => {
  const response = await get<any>(`/workflow/templates/${templateId}/versions/${versionNo}`)
  const item = response.data || {}

  return {
    version: Number(item.version || versionNo),
    updatedAt: String(item.updatedAt || nowText()),
    operator: String(item.operator || 'system'),
    actionType: item.actionType,
    status: normalizeStatus(item.status),
    remark: item.remark,
    payload: mapApiTemplate(item.payload || item.snapshot || {}, templateId)
  }
}

export const restoreWorkflowTemplateVersion = async (
  templateId: string,
  versionNo: number,
  remark?: string
): Promise<WorkflowTemplateModel> => {
  const response = await post<any>(`/workflow/templates/${templateId}/versions/${versionNo}/restore`, {
    remark: remark || ''
  })
  return mapApiTemplate(response.data, templateId)
}
