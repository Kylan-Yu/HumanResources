import {
  APPROVAL_MODE_OPTIONS,
  ASSIGNEE_TYPE_OPTIONS,
  BRANCH_OFFSET_Y,
  DEFAULT_TEMPLATE_CATEGORY,
  DEFAULT_TEMPLATE_CODE,
  DEFAULT_TEMPLATE_ID,
  DEFAULT_TEMPLATE_NAME,
  DEFAULT_TEMPLATE_STATUS,
  MERGE_OFFSET_Y,
  NODE_LABEL_MAP,
  PARALLEL_FAIL_STRATEGY_OPTIONS,
  PARALLEL_JOIN_STRATEGY_OPTIONS,
  PARALLEL_TIMEOUT_STRATEGY_OPTIONS,
  TIMEOUT_ACTION_OPTIONS,
  createDefaultNodeConfig
} from '../constants'
import type {
  ApprovalWorkflowNode,
  BranchChildNode,
  CcWorkflowNode,
  ConditionBranch,
  ConditionBranchWorkflowNode,
  ConditionJoinWorkflowNode,
  ConditionWorkflowNode,
  EndWorkflowNode,
  InsertableNodeType,
  ParallelBranch,
  ParallelBranchWorkflowNode,
  ParallelForkWorkflowNode,
  ParallelJoinWorkflowNode,
  PositionMode,
  StarterWorkflowNode,
  WorkflowEdgeModel,
  WorkflowFlowEdge,
  WorkflowFlowNode,
  WorkflowNodeConfig,
  WorkflowNodeModel,
  WorkflowNodeRenderData,
  WorkflowNodeType,
  WorkflowTemplateModel,
  WorkflowTemplateStatus
} from '../types'
import { createWorkflowEdge } from './edgeFactory'
import { getSymmetricOffsets, layoutMainChain } from './layout'
import { createWorkflowNode } from './nodeFactory'

interface GraphSnapshot {
  nodes: WorkflowNodeModel[]
  edges: WorkflowEdgeModel[]
}

interface SyncOptions {
  forceAutoLayout?: boolean
}

const cloneDeep = <T>(data: T): T => JSON.parse(JSON.stringify(data)) as T

const getOptionLabel = <T extends string>(
  options: Array<{ value: T; label: string }>,
  value: T
): string => {
  return options.find((item) => item.value === value)?.label ?? value
}

const getAssigneeSummary = (node: ApprovalWorkflowNode): string => {
  const { assigneeType, leaderLevel, roleIds, positionIds, userIds } = node.config
  const assigneeTypeLabel = getOptionLabel(ASSIGNEE_TYPE_OPTIONS, assigneeType)

  switch (assigneeType) {
    case 'direct_leader':
      return '直属上级审批'
    case 'level_leader':
      return `第 ${leaderLevel || 1} 级上级审批`
    case 'department_manager':
      return '部门负责人审批'
    case 'self_select':
      return '发起人自选审批人'
    case 'role':
      return roleIds.length ? `指定角色：${roleIds.join('、')}` : '指定角色（未配置）'
    case 'position':
      return positionIds.length ? `指定岗位：${positionIds.join('、')}` : '指定岗位（未配置）'
    case 'user':
      return userIds.length ? `指定用户：${userIds.join('、')}` : '指定用户（未配置）'
    default:
      return `${assigneeTypeLabel}（待配置）`
  }
}

const getCcSummary = (node: CcWorkflowNode): string => {
  const { targetType, roleIds, positionIds, userIds } = node.config

  if (targetType === 'direct_leader') {
    return '抄送给：发起人直属上级'
  }
  if (targetType === 'role') {
    return roleIds.length ? `抄送给：${roleIds.join('、')}` : '抄送角色未配置'
  }
  if (targetType === 'position') {
    return positionIds.length ? `抄送给：${positionIds.join('、')}` : '抄送岗位未配置'
  }
  return userIds.length ? `抄送给：${userIds.join('、')}` : '抄送用户未配置'
}

const getDefaultBranchName = (branches: ConditionBranch[], defaultBranchId?: string): string => {
  if (!defaultBranchId) {
    return '未设置'
  }
  return branches.find((branch) => branch.id === defaultBranchId)?.name ?? '未设置'
}

const toPositionMode = (value: unknown): PositionMode => (value === 'manual' ? 'manual' : 'auto')

const normalizeBranchChildren = (
  childNodeIds: string[],
  childNodes?: BranchChildNode[]
): { childNodeIds: string[]; childNodes: BranchChildNode[] } => {
  const safeNodes = Array.isArray(childNodes)
    ? childNodes.filter((item) => item?.id && item?.name)
    : []
  const safeIds = Array.isArray(childNodeIds)
    ? childNodeIds.filter((item) => typeof item === 'string' && item.trim())
    : []
  const merged = Array.from(new Set([...safeNodes.map((item) => item.id), ...safeIds]))

  return {
    childNodeIds: merged,
    childNodes: merged.map((id) => {
      const existed = safeNodes.find((item) => item.id === id)
      return existed || ({ id, type: 'approval', name: `鑺傜偣 ${id}` } as BranchChildNode)
    })
  }
}

const normalizeConditionBranches = (
  branches: ConditionBranch[],
  defaultBranchId?: string
): ConditionBranch[] => {
  const ordered = [...branches]
    .sort((a, b) => a.priority - b.priority)
    .map((branch, index) => ({
      ...branch,
      ...normalizeBranchChildren(branch.childNodeIds || [], branch.childNodes),
      priority: index + 1
    }))

  const safeDefaultId =
    defaultBranchId && ordered.some((item) => item.id === defaultBranchId)
      ? defaultBranchId
      : ordered[0]?.id

  return ordered.map((branch) => ({
    ...branch,
    isDefault: branch.id === safeDefaultId
  }))
}

const normalizeParallelBranches = (branches: ParallelBranch[]): ParallelBranch[] => {
  return [...branches]
    .sort((a, b) => a.order - b.order)
    .map((branch, index) => ({
      ...branch,
      ...normalizeBranchChildren(branch.childNodeIds || [], branch.childNodes),
      order: index + 1
    }))
}

const createLockedEdge = (
  source: string,
  target: string,
  group: 'condition' | 'parallel',
  groupId: string
): WorkflowEdgeModel => {
  return {
    id: `edge_locked_${group}_${source}_${target}`,
    source,
    target,
    metadata: {
      locked: true,
      group,
      groupId
    }
  }
}

const deduplicateEdges = (edges: WorkflowEdgeModel[]): WorkflowEdgeModel[] => {
  const seen = new Set<string>()
  return edges.filter((edge) => {
    const key = `${edge.source}->${edge.target}->${edge.metadata?.locked ? 'locked' : 'normal'}`
    if (seen.has(key)) {
      return false
    }
    seen.add(key)
    return true
  })
}

const updateNodeMap = (nodes: WorkflowNodeModel[]): Map<string, WorkflowNodeModel> =>
  new Map(nodes.map((node) => [node.id, node]))

const applyGroupAutoLayout = (
  nodes: WorkflowNodeModel[],
  edges: WorkflowEdgeModel[],
  forceAutoLayout: boolean
): WorkflowNodeModel[] => {
  const chainNodes = nodes.filter((node) => !['condition_branch', 'parallel_branch'].includes(node.type))
  const chainEdges = edges.filter((edge) => !edge.metadata?.locked)
  const layouted = layoutMainChain(chainNodes, chainEdges)
  const layoutMap = new Map(layouted.map((node) => [node.id, node.position]))

  const nextNodes = nodes.map((node) => {
    const shouldAuto = forceAutoLayout || node.positionMode !== 'manual'
    if (shouldAuto && layoutMap.has(node.id)) {
      return {
        ...node,
        position: layoutMap.get(node.id)!
      }
    }
    return node
  })

  const nextMap = updateNodeMap(nextNodes)

  nextNodes.forEach((node) => {
    if (node.type === 'condition') {
      const conditionNode = node as ConditionWorkflowNode
      const joinNode = nextMap.get(conditionNode.config.joinNodeId || '')
      if (joinNode && joinNode.type === 'condition_join') {
        if (forceAutoLayout || joinNode.positionMode !== 'manual') {
          joinNode.position = {
            x: conditionNode.position.x,
            y: conditionNode.position.y + MERGE_OFFSET_Y
          }
        }
      }

      const offsets = getSymmetricOffsets(conditionNode.config.branches.length)
      conditionNode.config.branches.forEach((branch, index) => {
        const branchNode = nextMap.get(branch.nodeId || '')
        if (!branchNode || branchNode.type !== 'condition_branch') {
          return
        }
        if (forceAutoLayout || branchNode.positionMode !== 'manual') {
          branchNode.position = {
            x: conditionNode.position.x + offsets[index],
            y: conditionNode.position.y + BRANCH_OFFSET_Y
          }
        }
      })
    }

    if (node.type === 'parallel_fork') {
      const forkNode = node as ParallelForkWorkflowNode
      const joinNode = nextMap.get(forkNode.config.joinNodeId || '')
      if (joinNode && joinNode.type === 'parallel_join') {
        if (forceAutoLayout || joinNode.positionMode !== 'manual') {
          joinNode.position = {
            x: forkNode.position.x,
            y: forkNode.position.y + MERGE_OFFSET_Y
          }
        }
      }

      const offsets = getSymmetricOffsets(forkNode.config.branches.length)
      forkNode.config.branches.forEach((branch, index) => {
        const branchNode = nextMap.get(branch.nodeId || '')
        if (!branchNode || branchNode.type !== 'parallel_branch') {
          return
        }
        if (forceAutoLayout || branchNode.positionMode !== 'manual') {
          branchNode.position = {
            x: forkNode.position.x + offsets[index],
            y: forkNode.position.y + BRANCH_OFFSET_Y
          }
        }
      })
    }
  })

  return nextNodes
}

const ensureConditionGroup = (
  gateway: ConditionWorkflowNode,
  nodes: WorkflowNodeModel[],
  edges: WorkflowEdgeModel[]
): { nodes: WorkflowNodeModel[]; edges: WorkflowEdgeModel[] } => {
  let nextNodes = [...nodes]
  let nextEdges = [...edges]
  let nodeMap = updateNodeMap(nextNodes)

  gateway.config.branches = normalizeConditionBranches(
    gateway.config.branches,
    gateway.config.defaultBranchId
  )
  gateway.config.defaultBranchId = gateway.config.branches.find((item) => item.isDefault)?.id

  let joinNode: ConditionJoinWorkflowNode | null = null
  if (gateway.config.joinNodeId) {
    const existed = nodeMap.get(gateway.config.joinNodeId)
    if (existed?.type === 'condition_join') {
      joinNode = existed as ConditionJoinWorkflowNode
    }
  }

  if (!joinNode) {
    joinNode = createWorkflowNode({
      type: 'condition_join',
      name: '条件汇合',
      position: {
        x: gateway.position.x,
        y: gateway.position.y + MERGE_OFFSET_Y
      },
      config: {
        gatewayId: gateway.id,
        displayText: '条件汇合'
      }
    }) as ConditionJoinWorkflowNode
    nextNodes.push(joinNode)
    nodeMap = updateNodeMap(nextNodes)
  }

  joinNode.config.gatewayId = gateway.id
  gateway.config.joinNodeId = joinNode.id

  const activeBranchNodeIds = new Set<string>()

  gateway.config.branches.forEach((branch) => {
    let branchNode: ConditionBranchWorkflowNode | null = null
    if (branch.nodeId) {
      const existed = nodeMap.get(branch.nodeId)
      if (existed?.type === 'condition_branch') {
        branchNode = existed as ConditionBranchWorkflowNode
      }
    }

    if (!branchNode) {
      const existedByBranchId = nextNodes.find(
        (item) =>
          item.type === 'condition_branch' &&
          (item as ConditionBranchWorkflowNode).config.gatewayId === gateway.id &&
          (item as ConditionBranchWorkflowNode).config.branchId === branch.id
      ) as ConditionBranchWorkflowNode | undefined
      if (existedByBranchId) {
        branchNode = existedByBranchId
      }
    }

    if (!branchNode) {
      branchNode = createWorkflowNode({
        type: 'condition_branch',
        name: branch.name,
        position: {
          x: gateway.position.x,
          y: gateway.position.y + BRANCH_OFFSET_Y
        },
        config: {
          gatewayId: gateway.id,
          joinNodeId: joinNode.id,
          branchId: branch.id,
          expression: branch.expression,
          priority: branch.priority,
          isDefault: Boolean(branch.isDefault),
          remark: branch.remark || '',
          childNodeIds: branch.childNodeIds || [],
          collapsed: branch.collapsed ?? true
        }
      }) as ConditionBranchWorkflowNode
      nextNodes.push(branchNode)
      nodeMap = updateNodeMap(nextNodes)
    }

    const normalizedChildren = normalizeBranchChildren(branch.childNodeIds || [], branch.childNodes)
    branch.nodeId = branchNode.id
    branchNode.name = branch.name
    branchNode.config = {
      ...branchNode.config,
      gatewayId: gateway.id,
      joinNodeId: joinNode.id,
      branchId: branch.id,
      expression: branch.expression,
      priority: branch.priority,
      isDefault: Boolean(branch.isDefault),
      remark: branch.remark || '',
      childNodeIds: normalizedChildren.childNodeIds,
      collapsed: branch.collapsed ?? branchNode.config.collapsed ?? true
    }
    branch.collapsed = branchNode.config.collapsed
    branch.childNodeIds = branchNode.config.childNodeIds
    branch.childNodes = normalizedChildren.childNodes
    activeBranchNodeIds.add(branchNode.id)
  })

  const staleBranchNodes = nextNodes
    .filter(
      (node) =>
        node.type === 'condition_branch' &&
        (node as ConditionBranchWorkflowNode).config.gatewayId === gateway.id &&
        !activeBranchNodeIds.has(node.id)
    )
    .map((node) => node.id)

  if (staleBranchNodes.length) {
    const staleSet = new Set(staleBranchNodes)
    nextNodes = nextNodes.filter((node) => !staleSet.has(node.id))
    nextEdges = nextEdges.filter((edge) => !staleSet.has(edge.source) && !staleSet.has(edge.target))
  }

  const branchNodeIdSet = new Set(gateway.config.branches.map((branch) => branch.nodeId).filter(Boolean) as string[])
  nextEdges = nextEdges.map((edge) => {
    if (edge.metadata?.locked) {
      return edge
    }
    if (edge.source === gateway.id && !branchNodeIdSet.has(edge.target)) {
      return {
        ...edge,
        source: joinNode!.id
      }
    }
    return edge
  })

  nextEdges = nextEdges.filter((edge) => {
    if (!edge.metadata?.locked) {
      return true
    }
    return edge.metadata.groupId !== gateway.id
  })

  gateway.config.branches.forEach((branch) => {
    if (!branch.nodeId) {
      return
    }
    nextEdges.push(createLockedEdge(gateway.id, branch.nodeId, 'condition', gateway.id))
    nextEdges.push(createLockedEdge(branch.nodeId, joinNode!.id, 'condition', gateway.id))
  })

  return {
    nodes: nextNodes,
    edges: deduplicateEdges(nextEdges)
  }
}

const ensureParallelGroup = (
  fork: ParallelForkWorkflowNode,
  nodes: WorkflowNodeModel[],
  edges: WorkflowEdgeModel[]
): { nodes: WorkflowNodeModel[]; edges: WorkflowEdgeModel[] } => {
  let nextNodes = [...nodes]
  let nextEdges = [...edges]
  let nodeMap = updateNodeMap(nextNodes)

  const mergedBranches = normalizeParallelBranches(fork.config.branches)
  fork.config.branches =
    mergedBranches.length >= 2
      ? mergedBranches
      : normalizeParallelBranches([
          ...mergedBranches,
          {
            id: `parallel_branch_${Date.now()}_seed_1`,
            nodeId: '',
            name: '并行分支1',
            order: 1,
            remark: '',
            childNodeIds: [],
            childNodes: [],
            collapsed: true
          },
          {
            id: `parallel_branch_${Date.now()}_seed_2`,
            nodeId: '',
            name: '并行分支2',
            order: 2,
            remark: '',
            childNodeIds: [],
            childNodes: [],
            collapsed: true
          }
        ].slice(0, 2))

  let joinNode: ParallelJoinWorkflowNode | null = null
  if (fork.config.joinNodeId) {
    const existed = nodeMap.get(fork.config.joinNodeId)
    if (existed?.type === 'parallel_join') {
      joinNode = existed as ParallelJoinWorkflowNode
    }
  }

  if (!joinNode) {
    joinNode = createWorkflowNode({
      type: 'parallel_join',
      name: '并行汇合',
      position: {
        x: fork.position.x,
        y: fork.position.y + MERGE_OFFSET_Y
      },
      config: {
        gatewayId: fork.id,
        strategy: 'all',
        requiredCount: Math.max(2, fork.config.branches.length),
        failStrategy: 'reject_all',
        timeoutStrategy: 'notify_admin'
      }
    }) as ParallelJoinWorkflowNode
    nextNodes.push(joinNode)
    nodeMap = updateNodeMap(nextNodes)
  }

  joinNode.config.gatewayId = fork.id
  joinNode.config.requiredCount = Math.min(
    Math.max(1, joinNode.config.requiredCount || 1),
    Math.max(1, fork.config.branches.length)
  )
  fork.config.joinNodeId = joinNode.id

  const activeBranchNodeIds = new Set<string>()

  fork.config.branches.forEach((branch) => {
    let branchNode: ParallelBranchWorkflowNode | null = null
    if (branch.nodeId) {
      const existed = nodeMap.get(branch.nodeId)
      if (existed?.type === 'parallel_branch') {
        branchNode = existed as ParallelBranchWorkflowNode
      }
    }

    if (!branchNode) {
      const existedByBranchId = nextNodes.find(
        (item) =>
          item.type === 'parallel_branch' &&
          (item as ParallelBranchWorkflowNode).config.gatewayId === fork.id &&
          (item as ParallelBranchWorkflowNode).config.branchId === branch.id
      ) as ParallelBranchWorkflowNode | undefined
      if (existedByBranchId) {
        branchNode = existedByBranchId
      }
    }

    if (!branchNode) {
      branchNode = createWorkflowNode({
        type: 'parallel_branch',
        name: branch.name,
        position: {
          x: fork.position.x,
          y: fork.position.y + BRANCH_OFFSET_Y
        },
        config: {
          gatewayId: fork.id,
          joinNodeId: joinNode.id,
          branchId: branch.id,
          order: branch.order,
          remark: branch.remark || '',
          childNodeIds: branch.childNodeIds || [],
          collapsed: branch.collapsed ?? true
        }
      }) as ParallelBranchWorkflowNode
      nextNodes.push(branchNode)
      nodeMap = updateNodeMap(nextNodes)
    }

    const normalizedChildren = normalizeBranchChildren(branch.childNodeIds || [], branch.childNodes)
    branch.nodeId = branchNode.id
    branchNode.name = branch.name
    branchNode.config = {
      ...branchNode.config,
      gatewayId: fork.id,
      joinNodeId: joinNode.id,
      branchId: branch.id,
      order: branch.order,
      remark: branch.remark || '',
      childNodeIds: normalizedChildren.childNodeIds,
      collapsed: branch.collapsed ?? branchNode.config.collapsed ?? true
    }
    branch.collapsed = branchNode.config.collapsed
    branch.childNodeIds = branchNode.config.childNodeIds
    branch.childNodes = normalizedChildren.childNodes
    activeBranchNodeIds.add(branchNode.id)
  })

  const staleBranchNodes = nextNodes
    .filter(
      (node) =>
        node.type === 'parallel_branch' &&
        (node as ParallelBranchWorkflowNode).config.gatewayId === fork.id &&
        !activeBranchNodeIds.has(node.id)
    )
    .map((node) => node.id)

  if (staleBranchNodes.length) {
    const staleSet = new Set(staleBranchNodes)
    nextNodes = nextNodes.filter((node) => !staleSet.has(node.id))
    nextEdges = nextEdges.filter((edge) => !staleSet.has(edge.source) && !staleSet.has(edge.target))
  }

  const branchNodeIdSet = new Set(fork.config.branches.map((branch) => branch.nodeId).filter(Boolean) as string[])
  nextEdges = nextEdges.map((edge) => {
    if (edge.metadata?.locked) {
      return edge
    }
    if (edge.source === fork.id && !branchNodeIdSet.has(edge.target)) {
      return {
        ...edge,
        source: joinNode!.id
      }
    }
    return edge
  })

  nextEdges = nextEdges.filter((edge) => {
    if (!edge.metadata?.locked) {
      return true
    }
    return edge.metadata.groupId !== fork.id
  })

  fork.config.branches.forEach((branch) => {
    if (!branch.nodeId) {
      return
    }
    nextEdges.push(createLockedEdge(fork.id, branch.nodeId, 'parallel', fork.id))
    nextEdges.push(createLockedEdge(branch.nodeId, joinNode!.id, 'parallel', fork.id))
  })

  return {
    nodes: nextNodes,
    edges: deduplicateEdges(nextEdges)
  }
}

export const synchronizeWorkflowTopology = (
  snapshot: GraphSnapshot,
  options?: SyncOptions
): GraphSnapshot => {
  const forceAutoLayout = Boolean(options?.forceAutoLayout)
  let nodes: WorkflowNodeModel[] = cloneDeep(snapshot.nodes).map(
    (node) =>
      ({
        ...node,
        positionMode: toPositionMode(node.positionMode)
      }) as WorkflowNodeModel
  )
  let edges: WorkflowEdgeModel[] = cloneDeep(snapshot.edges)

  nodes.forEach((node) => {
    if (node.type === 'condition') {
      const synced = ensureConditionGroup(node as ConditionWorkflowNode, nodes, edges)
      nodes = synced.nodes
      edges = synced.edges
    }
  })

  nodes.forEach((node) => {
    if (node.type === 'parallel_fork') {
      const synced = ensureParallelGroup(node as ParallelForkWorkflowNode, nodes, edges)
      nodes = synced.nodes
      edges = synced.edges
    }
  })

  const nodeMap = updateNodeMap(nodes)
  const orphanNodeIds = new Set<string>()

  nodes.forEach((node) => {
    if (node.type === 'condition_branch') {
      const owner = nodeMap.get(node.config.gatewayId)
      if (!owner || owner.type !== 'condition') {
        orphanNodeIds.add(node.id)
      }
    }
    if (node.type === 'parallel_branch') {
      const owner = nodeMap.get(node.config.gatewayId)
      if (!owner || owner.type !== 'parallel_fork') {
        orphanNodeIds.add(node.id)
      }
    }
    if (node.type === 'condition_join') {
      const owner = node.config.gatewayId ? nodeMap.get(node.config.gatewayId) : null
      if (!owner || owner.type !== 'condition') {
        orphanNodeIds.add(node.id)
      }
    }
    if (node.type === 'parallel_join') {
      const owner = node.config.gatewayId ? nodeMap.get(node.config.gatewayId) : null
      if (!owner || owner.type !== 'parallel_fork') {
        orphanNodeIds.add(node.id)
      }
    }
  })

  if (orphanNodeIds.size) {
    nodes = nodes.filter((node) => !orphanNodeIds.has(node.id))
    edges = edges.filter((edge) => !orphanNodeIds.has(edge.source) && !orphanNodeIds.has(edge.target))
  }

  const currentNodeIds = new Set(nodes.map((node) => node.id))
  edges = edges.filter((edge) => currentNodeIds.has(edge.source) && currentNodeIds.has(edge.target))
  edges = deduplicateEdges(edges)

  nodes = applyGroupAutoLayout(nodes, edges, forceAutoLayout)

  return {
    nodes,
    edges
  }
}

const getConditionBranchSummary = (node: ConditionBranchWorkflowNode): string[] => {
  return [
    node.config.expression || '未配置表达式',
    `浼樺厛绾э細${node.config.priority}`,
    node.config.isDefault ? '默认分支' : '普通分支'
  ]
}

const getParallelBranchSummary = (node: ParallelBranchWorkflowNode): string[] => {
  return [`顺序：${node.config.order}`, `节点数：${node.config.childNodeIds.length}`]
}

export const buildNodeSummary = (node: WorkflowNodeModel): string[] => {
  switch (node.type) {
    case 'starter': {
      const starterNode = node as StarterWorkflowNode
      const { initiatorScopeType, roleIds, departmentIds, userIds } = starterNode.config
      const scopeSummaryMap: Record<string, string> = {
        all: '所有人发起',
        role: roleIds.length ? `指定角色：${roleIds.join('、')}` : '指定角色（未配置）',
        department: departmentIds.length ? `指定部门：${departmentIds.join('、')}` : '指定部门（未配置）',
        user: userIds.length ? `指定用户：${userIds.join('、')}` : '指定用户（未配置）'
      }
      return [scopeSummaryMap[initiatorScopeType] || '发起范围未配置']
    }
    case 'approval': {
      const approvalNode = node as ApprovalWorkflowNode
      const modeText = getOptionLabel(APPROVAL_MODE_OPTIONS, approvalNode.config.approvalMode)
      const timeoutText = approvalNode.config.timeoutHours
        ? `审批时限：${approvalNode.config.timeoutHours} 小时`
        : '审批时限：未设置'

      return [modeText, getAssigneeSummary(approvalNode), approvalNode.config.required ? '必经' : '非必经', timeoutText]
    }
    case 'cc': {
      const ccNode = node as CcWorkflowNode
      return [getCcSummary(ccNode), ccNode.config.canViewAllComments ? '可查看全部审批意见' : '仅查看当前节点意见']
    }
    case 'condition': {
      const conditionNode = node as ConditionWorkflowNode
      const defaultBranch = getDefaultBranchName(
        conditionNode.config.branches,
        conditionNode.config.defaultBranchId
      )
      return [
        `${conditionNode.config.branches.length} 个条件分支`,
        `默认分支：${defaultBranch}`,
        conditionNode.config.conditionField ? `字段：${conditionNode.config.conditionField}` : '字段：未设置'
      ]
    }
    case 'condition_branch':
      return getConditionBranchSummary(node as ConditionBranchWorkflowNode)
    case 'condition_join':
      return [(node as ConditionJoinWorkflowNode).config.displayText || '条件汇合后继续']
    case 'parallel_fork': {
      const forkNode = node as ParallelForkWorkflowNode
      return [`${forkNode.config.branches.length} 条并行分支`, forkNode.config.groupName || '并行分支组']
    }
    case 'parallel_branch':
      return getParallelBranchSummary(node as ParallelBranchWorkflowNode)
    case 'parallel_join': {
      const joinNode = node as ParallelJoinWorkflowNode
      const strategyText = getOptionLabel(PARALLEL_JOIN_STRATEGY_OPTIONS, joinNode.config.strategy)
      const failText = getOptionLabel(PARALLEL_FAIL_STRATEGY_OPTIONS, joinNode.config.failStrategy)
      const timeoutText = getOptionLabel(PARALLEL_TIMEOUT_STRATEGY_OPTIONS, joinNode.config.timeoutStrategy)
      return [
        `汇合：${strategyText}${joinNode.config.strategy === 'n_of_m' ? `（N=${joinNode.config.requiredCount}）` : ''}`,
        `失败：${failText}`,
        `超时：${timeoutText}`
      ]
    }
    case 'end':
      return [(node as EndWorkflowNode).config.displayText || '流程结束']
    default:
      return []
  }
}

const resolveGroupSelection = (modelNodes: WorkflowNodeModel[], selectedNodeId: string | null): Set<string> => {
  const selected = selectedNodeId ? modelNodes.find((item) => item.id === selectedNodeId) : null
  if (!selected) {
    return new Set<string>()
  }

  if (selected.type === 'condition') {
    const ids = [selected.id, selected.config.joinNodeId, ...selected.config.branches.map((item) => item.nodeId)]
    return new Set(ids.filter(Boolean) as string[])
  }
  if (selected.type === 'condition_branch') {
    const owner = modelNodes.find((item) => item.id === selected.config.gatewayId)
    const ids = [
      selected.id,
      selected.config.joinNodeId,
      selected.config.gatewayId,
      ...(owner?.type === 'condition' ? owner.config.branches.map((item) => item.nodeId) : [])
    ]
    return new Set(ids.filter(Boolean) as string[])
  }
  if (selected.type === 'condition_join') {
    const owner = selected.config.gatewayId
      ? modelNodes.find((item) => item.id === selected.config.gatewayId)
      : null
    const ids = [
      selected.id,
      selected.config.gatewayId,
      ...(owner?.type === 'condition' ? owner.config.branches.map((item) => item.nodeId) : [])
    ]
    return new Set(ids.filter(Boolean) as string[])
  }

  if (selected.type === 'parallel_fork') {
    const ids = [selected.id, selected.config.joinNodeId, ...selected.config.branches.map((item) => item.nodeId)]
    return new Set(ids.filter(Boolean) as string[])
  }
  if (selected.type === 'parallel_branch') {
    const owner = modelNodes.find((item) => item.id === selected.config.gatewayId)
    const ids = [
      selected.id,
      selected.config.joinNodeId,
      selected.config.gatewayId,
      ...(owner?.type === 'parallel_fork' ? owner.config.branches.map((item) => item.nodeId) : [])
    ]
    return new Set(ids.filter(Boolean) as string[])
  }
  if (selected.type === 'parallel_join') {
    const owner = selected.config.gatewayId
      ? modelNodes.find((item) => item.id === selected.config.gatewayId)
      : null
    const ids = [
      selected.id,
      selected.config.gatewayId,
      ...(owner?.type === 'parallel_fork' ? owner.config.branches.map((item) => item.nodeId) : [])
    ]
    return new Set(ids.filter(Boolean) as string[])
  }

  return new Set<string>()
}

const createNodeRenderData = (
  node: WorkflowNodeModel,
  selectedNodeId: string | null,
  groupSelectedIds: Set<string>,
  onDeleteNode?: (nodeId: string) => void,
  onAppendNode?: (nodeId: string, nodeType: InsertableNodeType) => void,
  onToggleCollapse?: (nodeId: string) => void
): WorkflowNodeRenderData => {
  const appendableNodeTypes: WorkflowNodeType[] = [
    'starter',
    'approval',
    'cc',
    'condition_branch',
    'parallel_branch',
    'condition_join',
    'parallel_join'
  ]
  const deletableNodeTypes: WorkflowNodeType[] = ['approval', 'cc', 'condition', 'parallel_fork']

  const baseData: WorkflowNodeRenderData = {
    label: NODE_LABEL_MAP[node.type],
    title: node.name,
    summary: buildNodeSummary(node),
    config: node.config,
    selected: selectedNodeId === node.id,
    groupSelected: groupSelectedIds.has(node.id),
    addable: true,
    editable: true,
    nodeType: node.type,
    deletable: deletableNodeTypes.includes(node.type),
    appendable: appendableNodeTypes.includes(node.type),
    onDelete: onDeleteNode,
    onAppendNode,
    onToggleCollapse
  }

  if (node.type === 'condition_branch') {
    baseData.branchExpression = node.config.expression
    baseData.branchPriority = node.config.priority
    baseData.branchIsDefault = node.config.isDefault
    baseData.branchChildren = node.config.childNodeIds
    baseData.collapsed = node.config.collapsed
  }

  if (node.type === 'parallel_branch') {
    baseData.branchPriority = node.config.order
    baseData.branchChildren = node.config.childNodeIds
    baseData.collapsed = node.config.collapsed
  }

  return baseData
}

const createBaseFlowNodes = (
  modelNodes: WorkflowNodeModel[],
  selectedNodeId: string | null,
  onDeleteNode?: (nodeId: string) => void,
  onAppendNode?: (nodeId: string, nodeType: InsertableNodeType) => void,
  onToggleCollapse?: (nodeId: string) => void
): WorkflowFlowNode[] => {
  const groupSelectedIds = resolveGroupSelection(modelNodes, selectedNodeId)

  return modelNodes.map((node) => ({
    id: node.id,
    type: node.type,
    position: node.position,
    draggable: true,
    selectable: true,
    data: createNodeRenderData(
      node,
      selectedNodeId,
      groupSelectedIds,
      onDeleteNode,
      onAppendNode,
      onToggleCollapse
    )
  }))
}

const createBaseFlowEdges = (
  _modelNodes: WorkflowNodeModel[],
  modelEdges: WorkflowEdgeModel[],
  onAddNode?: (edgeId: string, nodeType: InsertableNodeType) => void
): WorkflowFlowEdge[] => {
  return modelEdges.map((edge) => {
    return {
      id: edge.id,
      type: 'workflowEdge',
      source: edge.source,
      target: edge.target,
      animated: false,
      data: {
        addable: edge.metadata?.locked ? false : true,
        onAddNode
      },
      style: {
        stroke:
          edge.metadata?.group === 'parallel'
            ? '#9f7aea'
            : edge.metadata?.group === 'condition'
            ? '#6abf69'
            : '#5e7092',
        strokeWidth: edge.metadata?.locked ? 1.8 : 2,
        strokeDasharray: edge.metadata?.locked ? '5 4' : undefined
      }
    }
  })
}

export const toReactFlowElements = (params: {
  modelNodes: WorkflowNodeModel[]
  modelEdges: WorkflowEdgeModel[]
  selectedNodeId: string | null
  onDeleteNode?: (nodeId: string) => void
  onAppendNode?: (nodeId: string, nodeType: InsertableNodeType) => void
  onAddNode?: (edgeId: string, nodeType: InsertableNodeType) => void
  onToggleCollapse?: (nodeId: string) => void
}): { nodes: WorkflowFlowNode[]; edges: WorkflowFlowEdge[] } => {
  const { modelNodes, modelEdges, selectedNodeId, onDeleteNode, onAppendNode, onAddNode, onToggleCollapse } = params

  return {
    nodes: createBaseFlowNodes(modelNodes, selectedNodeId, onDeleteNode, onAppendNode, onToggleCollapse),
    edges: createBaseFlowEdges(modelNodes, modelEdges, onAddNode)
  }
}

const isWorkflowNodeType = (value: string): value is WorkflowNodeType => {
  return [
    'starter',
    'approval',
    'cc',
    'condition',
    'condition_branch',
    'condition_join',
    'parallel_fork',
    'parallel_branch',
    'parallel_join',
    'end'
  ].includes(value)
}

const isWorkflowTemplateStatus = (value: string): value is WorkflowTemplateStatus => {
  return ['draft', 'published', 'disabled'].includes(value)
}

const mergeNodeConfig = (type: WorkflowNodeType, config: unknown): WorkflowNodeConfig => {
  const base = createDefaultNodeConfig(type)
  if (!config || typeof config !== 'object') {
    return base
  }

  return {
    ...base,
    ...(config as Record<string, unknown>)
  } as WorkflowNodeConfig
}

const getRawDefinitionNodes = (template: Partial<WorkflowTemplateModel>): Partial<WorkflowNodeModel>[] => {
  if (Array.isArray(template.definition?.nodes)) {
    return template.definition.nodes
  }
  if (Array.isArray(template.nodes)) {
    return template.nodes
  }
  return []
}

const getRawDefinitionEdges = (template: Partial<WorkflowTemplateModel>): Partial<WorkflowEdgeModel>[] => {
  if (Array.isArray(template.definition?.edges)) {
    return template.definition.edges
  }
  if (Array.isArray(template.edges)) {
    return template.edges
  }
  return []
}

const isValidPosition = (position: { x?: unknown; y?: unknown } | undefined): boolean => {
  return (
    typeof position?.x === 'number' &&
    Number.isFinite(position.x) &&
    typeof position?.y === 'number' &&
    Number.isFinite(position.y)
  )
}

export const normalizeTemplateModel = (
  template: Partial<WorkflowTemplateModel> | null | undefined
): WorkflowTemplateModel => {
  const fallback = createDefaultTemplateModel()
  if (!template) {
    return fallback
  }

  const rawNodes = getRawDefinitionNodes(template)
  const rawEdges = getRawDefinitionEdges(template)
  if (!rawNodes.length) {
    return fallback
  }

  let hasInvalidPosition = false
  const nodes: WorkflowNodeModel[] = rawNodes
    .filter((node): node is Partial<WorkflowNodeModel> => Boolean(node?.id && node?.type))
    .map((node) => {
      const rawType = String(node.type || '')
      const remapLegacyType = rawType === 'parallel' ? 'parallel_fork' : rawType
      const type = isWorkflowNodeType(remapLegacyType) ? remapLegacyType : 'approval'
      const validPosition = isValidPosition(node.position)
      if (!validPosition) {
        hasInvalidPosition = true
      }

      return {
        ...node,
        type,
        name: String(node.name || NODE_LABEL_MAP[type]),
        positionMode: toPositionMode(node.positionMode),
        position: {
          x: validPosition ? Number(node.position?.x) : 0,
          y: validPosition ? Number(node.position?.y) : 0
        },
        config: mergeNodeConfig(type, node.config)
      } as WorkflowNodeModel
    })

  const nodeIdSet = new Set(nodes.map((node) => node.id))
  const edges: WorkflowEdgeModel[] = rawEdges
    .filter((edge): edge is WorkflowEdgeModel => Boolean(edge?.id && edge?.source && edge?.target))
    .filter((edge) => nodeIdSet.has(edge.source) && nodeIdSet.has(edge.target))

  if (!nodes.length) {
    return fallback
  }

  const synced = synchronizeWorkflowTopology(
    {
      nodes,
      edges
    },
    {
      forceAutoLayout: hasInvalidPosition
    }
  )

  const templateStatus = isWorkflowTemplateStatus(String(template.status || ''))
    ? (template.status as WorkflowTemplateStatus)
    : DEFAULT_TEMPLATE_STATUS

  return {
    templateId: template.templateId || DEFAULT_TEMPLATE_ID,
    templateName: template.templateName || DEFAULT_TEMPLATE_NAME,
    templateCode: template.templateCode || DEFAULT_TEMPLATE_CODE,
    category: template.category || DEFAULT_TEMPLATE_CATEGORY,
    status: templateStatus,
    version: Number(template.version || 1),
    updatedAt: template.updatedAt || '',
    viewport: template.viewport || { x: 0, y: 0, zoom: 1 },
    layout: template.layout || { manualPositions: true },
    definition: {
      nodes: synced.nodes,
      edges: synced.edges
    },
    nodes: synced.nodes,
    edges: synced.edges
  }
}

interface DefaultTemplateOptions {
  templateId?: string
  templateName?: string
  templateCode?: string
  category?: string
  status?: WorkflowTemplateStatus
  version?: number
  updatedAt?: string
}

export const createDefaultTemplateModel = (options?: DefaultTemplateOptions): WorkflowTemplateModel => {
  const starter = createWorkflowNode({
    type: 'starter',
    id: 'node_start',
    name: '发起人',
    config: {
      initiatorScopeType: 'all'
    }
  })

  const approval = createWorkflowNode({
    type: 'approval',
    id: 'node_approval_1',
    name: '鐩村睘涓荤瀹℃壒',
    config: {
      approvalMode: 'any_one',
      assigneeType: 'direct_leader',
      required: true
    }
  })

  const end = createWorkflowNode({
    type: 'end',
    id: 'node_end',
    name: '流程结束'
  })

  const rawEdges: WorkflowEdgeModel[] = [
    {
      ...createWorkflowEdge(starter.id, approval.id),
      id: `edge_${starter.id}_${approval.id}`
    },
    {
      ...createWorkflowEdge(approval.id, end.id),
      id: `edge_${approval.id}_${end.id}`
    }
  ]

  const synced = synchronizeWorkflowTopology({
    nodes: [starter, approval, end],
    edges: rawEdges
  })

  return {
    templateId: options?.templateId || DEFAULT_TEMPLATE_ID,
    templateName: options?.templateName || DEFAULT_TEMPLATE_NAME,
    templateCode: options?.templateCode || DEFAULT_TEMPLATE_CODE,
    category: options?.category || DEFAULT_TEMPLATE_CATEGORY,
    status: options?.status || DEFAULT_TEMPLATE_STATUS,
    version: options?.version || 1,
    updatedAt: options?.updatedAt || '',
    viewport: { x: 0, y: 0, zoom: 1 },
    layout: { manualPositions: true },
    definition: {
      nodes: synced.nodes,
      edges: synced.edges
    },
    nodes: synced.nodes,
    edges: synced.edges
  }
}

export const buildTemplatePayload = (params: {
  templateId: string
  templateName: string
  templateCode: string
  category: string
  status: WorkflowTemplateStatus
  version: number
  updatedAt: string
  viewport?: { x: number; y: number; zoom: number }
  layout?: Record<string, unknown>
  nodes: WorkflowNodeModel[]
  edges: WorkflowEdgeModel[]
}): WorkflowTemplateModel => {
  const synced = synchronizeWorkflowTopology({
    nodes: params.nodes,
    edges: params.edges
  })

  return {
    templateId: params.templateId,
    templateName: params.templateName,
    templateCode: params.templateCode,
    category: params.category,
    status: params.status,
    version: params.version,
    updatedAt: params.updatedAt,
    viewport: params.viewport || { x: 0, y: 0, zoom: 1 },
    layout: params.layout || { manualPositions: true },
    definition: {
      nodes: synced.nodes,
      edges: synced.edges
    },
    nodes: synced.nodes,
    edges: synced.edges
  }
}

export const cloneTemplateModel = (template: WorkflowTemplateModel): WorkflowTemplateModel => {
  return cloneDeep(template)
}

export const getNodeNextId = (edges: WorkflowEdgeModel[], nodeId: string): string | null => {
  return edges.find((edge) => edge.source === nodeId)?.target || null
}

export const getNodePrevId = (edges: WorkflowEdgeModel[], nodeId: string): string | null => {
  return edges.find((edge) => edge.target === nodeId)?.source || null
}

export const getTimeoutActionLabel = (value: ApprovalWorkflowNode['config']['timeoutAction']): string => {
  return getOptionLabel(TIMEOUT_ACTION_OPTIONS, value)
}
