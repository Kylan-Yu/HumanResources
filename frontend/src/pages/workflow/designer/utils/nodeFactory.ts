import { MAIN_NODE_START_Y, MAIN_NODE_X, NODE_DEFAULT_NAME_MAP, createDefaultNodeConfig } from '../constants'
import type { WorkflowNodeConfigMap, WorkflowNodeModel, WorkflowNodeType } from '../types'

const nodeCounter: Record<string, number> = {}

const getNextNodeSeq = (type: WorkflowNodeType): number => {
  const current = nodeCounter[type] ?? 0
  const next = current + 1
  nodeCounter[type] = next
  return next
}

const cloneConfig = <T extends WorkflowNodeType>(type: T): WorkflowNodeConfigMap[T] => {
  return JSON.parse(JSON.stringify(createDefaultNodeConfig(type))) as WorkflowNodeConfigMap[T]
}

export const createNodeId = (type: WorkflowNodeType): string => {
  const seq = getNextNodeSeq(type)
  return `node_${type}_${seq}`
}

export const createWorkflowNode = <T extends WorkflowNodeType>(params: {
  type: T
  id?: string
  name?: string
  position?: { x: number; y: number }
  positionMode?: 'auto' | 'manual'
  config?: Partial<WorkflowNodeConfigMap[T]>
}): WorkflowNodeModel => {
  const { type, id, name, position, positionMode, config } = params
  const defaultConfig = cloneConfig(type)

  return {
    id: id ?? createNodeId(type),
    type,
    name: name ?? NODE_DEFAULT_NAME_MAP[type],
    description: '',
    required: type === 'approval' ? true : undefined,
    positionMode: positionMode ?? 'auto',
    position: position ?? { x: MAIN_NODE_X, y: MAIN_NODE_START_Y },
    config: {
      ...defaultConfig,
      ...(config || {})
    }
  } as WorkflowNodeModel
}
