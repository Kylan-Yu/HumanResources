import type { Edge, Node, XYPosition } from '@xyflow/react'

export type PositionMode = 'auto' | 'manual'

export type WorkflowNodeType =
  | 'starter'
  | 'approval'
  | 'cc'
  | 'condition'
  | 'condition_branch'
  | 'condition_join'
  | 'parallel_fork'
  | 'parallel_branch'
  | 'parallel_join'
  | 'end'

export type InsertableNodeType = 'approval' | 'cc' | 'condition' | 'parallel'

export type InternalFlowNodeType = WorkflowNodeType

export type ApprovalMode = 'any_one' | 'all' | 'sequence'

export type AssigneeType =
  | 'direct_leader'
  | 'level_leader'
  | 'department_manager'
  | 'self_select'
  | 'role'
  | 'position'
  | 'user'

export type InitiatorScopeType = 'all' | 'role' | 'department' | 'user'

export type TimeoutActionType = 'auto_pass' | 'auto_reject' | 'transfer_admin'

export type CcTargetType = 'role' | 'position' | 'user' | 'direct_leader'

export type WorkflowTemplateStatus = 'draft' | 'published' | 'disabled'

export type ConditionBranchSortStrategy = 'priority_asc' | 'custom'

export type ParallelJoinStrategy = 'all' | 'any' | 'n_of_m'

export type ParallelFailStrategy = 'reject_all' | 'ignore_failed' | 'manual'

export type ParallelTimeoutStrategy = 'auto_pass' | 'auto_reject' | 'notify_admin'

export type BranchChildNodeType = 'approval' | 'cc' | 'condition' | 'parallel_fork' | 'end'

export interface BranchChildNode {
  id: string
  type: BranchChildNodeType
  name: string
}

export interface BaseWorkflowNodeData {
  id: string
  type: WorkflowNodeType
  name: string
  description?: string
  required?: boolean
  positionMode?: PositionMode
}

export interface StarterNodeConfig {
  initiatorScopeType: InitiatorScopeType
  roleIds: string[]
  departmentIds: string[]
  userIds: string[]
  remark?: string
}

export interface ApprovalNodeConfig {
  approvalMode: ApprovalMode
  assigneeType: AssigneeType
  leaderLevel: number
  roleIds: string[]
  positionIds: string[]
  userIds: string[]
  required: boolean
  timeoutHours?: number
  timeoutAction: TimeoutActionType
  remark?: string
}

export interface CcNodeConfig {
  targetType: CcTargetType
  roleIds: string[]
  positionIds: string[]
  userIds: string[]
  canViewAllComments: boolean
  remark?: string
}

export interface ConditionBranch {
  id: string
  nodeId?: string
  name: string
  expression: string
  priority: number
  isDefault?: boolean
  remark?: string
  childNodeIds: string[]
  childNodes?: BranchChildNode[]
  collapsed?: boolean
}

export interface ConditionNodeConfig {
  branches: ConditionBranch[]
  defaultBranchId?: string
  sortStrategy: ConditionBranchSortStrategy
  expressionRemark?: string
  conditionField?: string
  joinNodeId?: string
}

export interface ConditionBranchNodeConfig {
  gatewayId: string
  joinNodeId: string
  branchId: string
  expression: string
  priority: number
  isDefault: boolean
  remark?: string
  childNodeIds: string[]
  collapsed?: boolean
}

export interface ConditionJoinNodeConfig {
  gatewayId?: string
  displayText?: string
}

export interface ParallelBranch {
  id: string
  nodeId?: string
  name: string
  order: number
  remark?: string
  childNodeIds: string[]
  childNodes?: BranchChildNode[]
  collapsed?: boolean
}

export interface ParallelForkNodeConfig {
  groupName?: string
  joinNodeId?: string
  branches: ParallelBranch[]
}

export interface ParallelBranchNodeConfig {
  gatewayId: string
  joinNodeId: string
  branchId: string
  order: number
  remark?: string
  childNodeIds: string[]
  collapsed?: boolean
}

export interface ParallelJoinNodeConfig {
  gatewayId?: string
  strategy: ParallelJoinStrategy
  requiredCount: number
  failStrategy: ParallelFailStrategy
  timeoutStrategy: ParallelTimeoutStrategy
}

export interface EndNodeConfig {
  displayText?: string
}

export interface WorkflowNodeConfigMap {
  starter: StarterNodeConfig
  approval: ApprovalNodeConfig
  cc: CcNodeConfig
  condition: ConditionNodeConfig
  condition_branch: ConditionBranchNodeConfig
  condition_join: ConditionJoinNodeConfig
  parallel_fork: ParallelForkNodeConfig
  parallel_branch: ParallelBranchNodeConfig
  parallel_join: ParallelJoinNodeConfig
  end: EndNodeConfig
}

export type WorkflowNodeConfig = WorkflowNodeConfigMap[WorkflowNodeType]

interface WorkflowNodeModelBase<T extends WorkflowNodeType, C extends WorkflowNodeConfig>
  extends BaseWorkflowNodeData {
  id: string
  type: T
  name: string
  position: XYPosition
  config: C
}

export type StarterWorkflowNode = WorkflowNodeModelBase<'starter', StarterNodeConfig>
export type ApprovalWorkflowNode = WorkflowNodeModelBase<'approval', ApprovalNodeConfig>
export type CcWorkflowNode = WorkflowNodeModelBase<'cc', CcNodeConfig>
export type ConditionWorkflowNode = WorkflowNodeModelBase<'condition', ConditionNodeConfig>
export type ConditionBranchWorkflowNode = WorkflowNodeModelBase<'condition_branch', ConditionBranchNodeConfig>
export type ConditionJoinWorkflowNode = WorkflowNodeModelBase<'condition_join', ConditionJoinNodeConfig>
export type ParallelForkWorkflowNode = WorkflowNodeModelBase<'parallel_fork', ParallelForkNodeConfig>
export type ParallelBranchWorkflowNode = WorkflowNodeModelBase<'parallel_branch', ParallelBranchNodeConfig>
export type ParallelJoinWorkflowNode = WorkflowNodeModelBase<'parallel_join', ParallelJoinNodeConfig>
export type EndWorkflowNode = WorkflowNodeModelBase<'end', EndNodeConfig>

export type WorkflowNodeModel =
  | StarterWorkflowNode
  | ApprovalWorkflowNode
  | CcWorkflowNode
  | ConditionWorkflowNode
  | ConditionBranchWorkflowNode
  | ConditionJoinWorkflowNode
  | ParallelForkWorkflowNode
  | ParallelBranchWorkflowNode
  | ParallelJoinWorkflowNode
  | EndWorkflowNode

export interface WorkflowEdgeModel {
  id: string
  source: string
  target: string
  branchId?: string
  metadata?: Record<string, string | number | boolean>
}

export interface WorkflowTemplateModel {
  templateId: string
  templateName: string
  templateCode: string
  category: string
  businessType?: string
  status: WorkflowTemplateStatus
  version: number
  updatedAt: string
  viewport?: {
    x: number
    y: number
    zoom: number
  }
  layout?: Record<string, unknown>
  definition?: {
    nodes: WorkflowNodeModel[]
    edges: WorkflowEdgeModel[]
  }
  nodes: WorkflowNodeModel[]
  edges: WorkflowEdgeModel[]
}

export interface WorkflowNodeRenderData extends Record<string, unknown> {
  label: string
  title: string
  summary: string[]
  config: WorkflowNodeConfig
  selected: boolean
  groupSelected?: boolean
  addable: boolean
  editable: boolean
  nodeType: WorkflowNodeType
  deletable: boolean
  appendable?: boolean
  collapsed?: boolean
  onDelete?: (nodeId: string) => void
  onAppendNode?: (nodeId: string, nodeType: InsertableNodeType) => void
  onToggleCollapse?: (nodeId: string) => void
  branchExpression?: string
  branchPriority?: number
  branchIsDefault?: boolean
  branchChildren?: string[]
}

export interface WorkflowEdgeRenderData extends Record<string, unknown> {
  addable: boolean
  virtual?: boolean
  proxyEdgeId?: string
  onAddNode?: (edgeId: string, nodeType: InsertableNodeType) => void
}

export type WorkflowFlowNode = Node<WorkflowNodeRenderData, InternalFlowNodeType>

export type WorkflowFlowEdge = Edge<WorkflowEdgeRenderData, 'workflowEdge'>

export interface WorkflowVersionItem {
  version: number
  updatedAt: string
  operator: string
  remark?: string
}

export interface WorkflowTemplateVersionSnapshot {
  version: number
  updatedAt: string
  operator: string
  actionType?: 'save' | 'publish' | 'restore'
  status?: WorkflowTemplateStatus
  payload: WorkflowTemplateModel
  remark?: string
}
