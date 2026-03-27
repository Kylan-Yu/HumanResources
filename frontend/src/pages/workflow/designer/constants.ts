import type {
  ApprovalMode,
  AssigneeType,
  CcTargetType,
  ConditionBranchSortStrategy,
  InitiatorScopeType,
  ParallelFailStrategy,
  ParallelJoinStrategy,
  ParallelTimeoutStrategy,
  TimeoutActionType,
  WorkflowNodeConfigMap,
  WorkflowTemplateStatus,
  WorkflowNodeType,
  InsertableNodeType
} from './types'

export const DESIGNER_TEMPLATE_COLLECTION_KEY = 'hrms_workflow_templates'
export const DESIGNER_TEMPLATE_HISTORY_MAP_KEY = 'hrms_workflow_template_histories'
export const LEGACY_DESIGNER_TEMPLATE_COLLECTION_KEY = 'hrms.workflow.designer.templates'
export const LEGACY_DESIGNER_TEMPLATE_HISTORY_MAP_KEY = 'hrms.workflow.designer.history.map'

export const DEFAULT_TEMPLATE_ID = 'leave_process_001'
export const DEFAULT_TEMPLATE_NAME = '请假审批流程'
export const DEFAULT_TEMPLATE_CODE = 'LEAVE_PROCESS_001'
export const DEFAULT_TEMPLATE_CATEGORY = '请假'
export const DEFAULT_TEMPLATE_STATUS: WorkflowTemplateStatus = 'draft'

export const MAIN_NODE_X = 0
export const MAIN_NODE_START_Y = 40
export const MAIN_NODE_GAP_Y = 170

export const BRANCH_GAP_X = 280
export const BRANCH_OFFSET_Y = 132
export const MERGE_OFFSET_Y = 280

export const NODE_COLOR_MAP: Record<WorkflowNodeType, string> = {
  starter: '#5B8FF9',
  approval: '#FA8C16',
  cc: '#13C2C2',
  condition: '#52C41A',
  condition_branch: '#5fb25f',
  condition_join: '#4a9a43',
  parallel_fork: '#722ED1',
  parallel_branch: '#8d63d8',
  parallel_join: '#9f6af2',
  end: '#8C8C8C'
}

export const NODE_LABEL_MAP: Record<WorkflowNodeType, string> = {
  starter: '发起人节点',
  approval: '审批节点',
  cc: '抄送节点',
  condition: '条件网关',
  condition_branch: '条件分支',
  condition_join: '条件汇合',
  parallel_fork: '并行开始',
  parallel_branch: '并行分支',
  parallel_join: '并行汇合',
  end: '结束节点'
}

export const NODE_DEFAULT_NAME_MAP: Record<WorkflowNodeType, string> = {
  starter: '发起人',
  approval: '审批节点',
  cc: '抄送节点',
  condition: '条件分支',
  condition_branch: '条件分支',
  condition_join: '条件汇合',
  parallel_fork: '并行分支',
  parallel_branch: '并行分支',
  parallel_join: '并行汇合',
  end: '流程结束'
}

export const WORKFLOW_TEMPLATE_STATUS_OPTIONS: Array<{ label: string; value: WorkflowTemplateStatus }> = [
  { label: '草稿', value: 'draft' },
  { label: '已发布', value: 'published' },
  { label: '停用', value: 'disabled' }
]

export const INITIATOR_SCOPE_OPTIONS: Array<{ label: string; value: InitiatorScopeType }> = [
  { label: '所有人', value: 'all' },
  { label: '指定角色', value: 'role' },
  { label: '指定部门', value: 'department' },
  { label: '指定用户', value: 'user' }
]

export const APPROVAL_MODE_OPTIONS: Array<{ label: string; value: ApprovalMode }> = [
  { label: '任一通过', value: 'any_one' },
  { label: '会签（全部通过）', value: 'all' },
  { label: '依次审批', value: 'sequence' }
]

export const ASSIGNEE_TYPE_OPTIONS: Array<{ label: string; value: AssigneeType }> = [
  { label: '直属上级', value: 'direct_leader' },
  { label: '第 N 级上级', value: 'level_leader' },
  { label: '部门负责人', value: 'department_manager' },
  { label: '发起人自选', value: 'self_select' },
  { label: '指定角色', value: 'role' },
  { label: '指定岗位', value: 'position' },
  { label: '指定用户', value: 'user' }
]

export const TIMEOUT_ACTION_OPTIONS: Array<{ label: string; value: TimeoutActionType }> = [
  { label: '自动通过', value: 'auto_pass' },
  { label: '自动拒绝', value: 'auto_reject' },
  { label: '转交管理员', value: 'transfer_admin' }
]

export const CC_TARGET_OPTIONS: Array<{ label: string; value: CcTargetType }> = [
  { label: '指定角色', value: 'role' },
  { label: '指定岗位', value: 'position' },
  { label: '指定用户', value: 'user' },
  { label: '发起人直属上级', value: 'direct_leader' }
]

export const CONDITION_SORT_STRATEGY_OPTIONS: Array<{ label: string; value: ConditionBranchSortStrategy }> = [
  { label: '按优先级升序', value: 'priority_asc' },
  { label: '自定义排序', value: 'custom' }
]

export const PARALLEL_JOIN_STRATEGY_OPTIONS: Array<{ label: string; value: ParallelJoinStrategy }> = [
  { label: '全部分支完成后继续', value: 'all' },
  { label: '任一分支完成后继续', value: 'any' },
  { label: '至少 N 个分支完成后继续', value: 'n_of_m' }
]

export const PARALLEL_FAIL_STRATEGY_OPTIONS: Array<{ label: string; value: ParallelFailStrategy }> = [
  { label: '整体驳回', value: 'reject_all' },
  { label: '忽略失败继续', value: 'ignore_failed' },
  { label: '转人工处理', value: 'manual' }
]

export const PARALLEL_TIMEOUT_STRATEGY_OPTIONS: Array<{ label: string; value: ParallelTimeoutStrategy }> = [
  { label: '自动通过', value: 'auto_pass' },
  { label: '自动驳回', value: 'auto_reject' },
  { label: '通知管理员', value: 'notify_admin' }
]

export const NODE_PALETTE_ITEMS: Array<{
  type: InsertableNodeType
  title: string
  description: string
}> = [
  {
    type: 'approval',
    title: '审批节点',
    description: '用于配置审批人和审批方式'
  },
  {
    type: 'cc',
    title: '抄送节点',
    description: '用于配置抄送范围和权限'
  },
  {
    type: 'condition',
    title: '条件分支',
    description: '按表达式匹配不同审批路径'
  },
  {
    type: 'parallel',
    title: '并行分支',
    description: '多个分支同时执行并在汇合点合并'
  }
]

export const WORKFLOW_CATEGORY_OPTIONS = [
  { label: '请假', value: '请假' },
  { label: '加班', value: '加班' },
  { label: '报销', value: '报销' },
  { label: '通用', value: '通用' }
]

export const createDefaultNodeConfig = <T extends WorkflowNodeType>(type: T): WorkflowNodeConfigMap[T] => {
  switch (type) {
    case 'starter':
      return {
        initiatorScopeType: 'all',
        roleIds: [],
        departmentIds: [],
        userIds: [],
        remark: ''
      } as WorkflowNodeConfigMap[T]
    case 'approval':
      return {
        approvalMode: 'any_one',
        assigneeType: 'direct_leader',
        leaderLevel: 1,
        roleIds: [],
        positionIds: [],
        userIds: [],
        required: true,
        timeoutHours: undefined,
        timeoutAction: 'auto_pass',
        remark: ''
      } as WorkflowNodeConfigMap[T]
    case 'cc':
      return {
        targetType: 'role',
        roleIds: [],
        positionIds: [],
        userIds: [],
        canViewAllComments: false,
        remark: ''
      } as WorkflowNodeConfigMap[T]
    case 'condition':
      return {
        branches: [
          {
            id: 'branch_1',
            nodeId: '',
            name: '分支 1',
            expression: 'days <= 3',
            priority: 1,
            isDefault: true,
            remark: '',
            childNodeIds: [],
            childNodes: [],
            collapsed: true
          },
          {
            id: 'branch_2',
            nodeId: '',
            name: '分支 2',
            expression: 'days > 3',
            priority: 2,
            isDefault: false,
            remark: '',
            childNodeIds: [],
            childNodes: [],
            collapsed: true
          }
        ],
        defaultBranchId: 'branch_1',
        sortStrategy: 'priority_asc',
        expressionRemark: '',
        conditionField: 'days'
      } as WorkflowNodeConfigMap[T]
    case 'condition_branch':
      return {
        gatewayId: '',
        joinNodeId: '',
        branchId: '',
        expression: '',
        priority: 1,
        isDefault: false,
        remark: '',
        childNodeIds: [],
        collapsed: true
      } as WorkflowNodeConfigMap[T]
    case 'condition_join':
      return {
        displayText: '条件汇合',
        gatewayId: ''
      } as WorkflowNodeConfigMap[T]
    case 'parallel_fork':
      return {
        groupName: '并行分支',
        joinNodeId: '',
        branches: [
          {
            id: 'parallel_branch_1',
            nodeId: '',
            name: '并行分支1',
            order: 1,
            remark: '',
            childNodeIds: [],
            childNodes: [],
            collapsed: true
          },
          {
            id: 'parallel_branch_2',
            nodeId: '',
            name: '并行分支2',
            order: 2,
            remark: '',
            childNodeIds: [],
            childNodes: [],
            collapsed: true
          }
        ]
      } as WorkflowNodeConfigMap[T]
    case 'parallel_branch':
      return {
        gatewayId: '',
        joinNodeId: '',
        branchId: '',
        order: 1,
        remark: '',
        childNodeIds: [],
        collapsed: true
      } as WorkflowNodeConfigMap[T]
    case 'parallel_join':
      return {
        gatewayId: '',
        strategy: 'all',
        requiredCount: 2,
        failStrategy: 'reject_all',
        timeoutStrategy: 'notify_admin'
      } as WorkflowNodeConfigMap[T]
    case 'end':
      return {
        displayText: '流程已结束'
      } as WorkflowNodeConfigMap[T]
    default:
      return {} as WorkflowNodeConfigMap[T]
  }
}
