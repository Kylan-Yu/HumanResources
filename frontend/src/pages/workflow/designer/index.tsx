import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Button, Drawer, Empty, List, Modal, Space, Tag, Typography, message } from 'antd'
import {
  applyEdgeChanges,
  applyNodeChanges,
  type Edge,
  type EdgeChange,
  type Node,
  type NodeChange,
  type Viewport
} from '@xyflow/react'
import { useNavigate, useParams } from 'react-router-dom'
import DesignerToolbar from './components/DesignerToolbar'
import NodePalette from './components/NodePalette'
import PropertyPanel from './components/PropertyPanel'
import WorkflowCanvas, { type WorkflowCanvasRef } from './components/WorkflowCanvas'
import { DEFAULT_TEMPLATE_NAME, MERGE_OFFSET_Y } from './constants'
import {
  getWorkflowTemplateDetail,
  getWorkflowTemplateVersions,
  publishWorkflowTemplate,
  saveWorkflowTemplate
} from './mockApi'
import {
  buildTemplatePayload,
  createDefaultTemplateModel,
  normalizeTemplateModel,
  synchronizeWorkflowTopology,
  toReactFlowElements
} from './utils/flowData'
import { createWorkflowEdge } from './utils/edgeFactory'
import { createWorkflowNode } from './utils/nodeFactory'
import type {
  ConditionBranch,
  ConditionBranchWorkflowNode,
  ConditionWorkflowNode,
  InsertableNodeType,
  ParallelBranch,
  ParallelBranchWorkflowNode,
  ParallelForkWorkflowNode,
  WorkflowEdgeModel,
  WorkflowNodeModel,
  WorkflowTemplateStatus,
  WorkflowTemplateVersionSnapshot
} from './types'
import './styles.css'

const { Paragraph, Text } = Typography

const statusLabelMap: Record<WorkflowTemplateStatus, { text: string; color: string }> = {
  draft: { text: '草稿', color: 'default' },
  published: { text: '已发布', color: 'success' },
  disabled: { text: '停用', color: 'warning' }
}

interface GraphSnapshot {
  nodes: WorkflowNodeModel[]
  edges: WorkflowEdgeModel[]
}

interface GraphHistoryState {
  past: GraphSnapshot[]
  present: GraphSnapshot
  future: GraphSnapshot[]
}

interface InsertedSegment {
  nodes: WorkflowNodeModel[]
  edges: WorkflowEdgeModel[]
  entryNodeId: string
  exitNodeId: string
  focusNodeId: string
}

const createEmptySnapshot = (): GraphSnapshot => ({ nodes: [], edges: [] })
const cloneSnapshot = (snapshot: GraphSnapshot): GraphSnapshot => JSON.parse(JSON.stringify(snapshot)) as GraphSnapshot
const isSnapshotEqual = (a: GraphSnapshot, b: GraphSnapshot): boolean => JSON.stringify(a) === JSON.stringify(b)

const toRuntimeNodes = (nodes: WorkflowNodeModel[]): Node[] =>
  nodes.map((node) => ({
    id: node.id,
    type: node.type,
    position: { x: node.position.x, y: node.position.y },
    data: {},
    draggable: true,
    selectable: true
  }))

const toRuntimeEdges = (edges: WorkflowEdgeModel[]): Edge[] =>
  edges.map((edge) => ({
    id: edge.id,
    source: edge.source,
    target: edge.target,
    data: {},
    type: 'workflowEdge'
  }))

const createInsertedSegment = (insertType: InsertableNodeType, position: { x: number; y: number }): InsertedSegment => {
  if (insertType === 'condition') {
    const condition = createWorkflowNode({ type: 'condition', name: '条件分支', position }) as ConditionWorkflowNode
    const join = createWorkflowNode({
      type: 'condition_join',
      name: '条件汇合',
      position: { x: position.x, y: position.y + MERGE_OFFSET_Y },
      config: {
        gatewayId: condition.id,
        displayText: '条件汇合'
      }
    })

    condition.config.joinNodeId = join.id

    return {
      nodes: [condition, join],
      edges: [],
      entryNodeId: condition.id,
      exitNodeId: join.id,
      focusNodeId: condition.id
    }
  }

  if (insertType === 'parallel') {
    const fork = createWorkflowNode({ type: 'parallel_fork', name: '并行分支', position }) as ParallelForkWorkflowNode
    const join = createWorkflowNode({
      type: 'parallel_join',
      name: '并行汇合',
      position: { x: position.x, y: position.y + MERGE_OFFSET_Y },
      config: {
        gatewayId: fork.id,
        strategy: 'all',
        requiredCount: 2,
        failStrategy: 'reject_all',
        timeoutStrategy: 'notify_admin'
      }
    })

    fork.config.joinNodeId = join.id

    return {
      nodes: [fork, join],
      edges: [],
      entryNodeId: fork.id,
      exitNodeId: join.id,
      focusNodeId: fork.id
    }
  }

  const node = createWorkflowNode({ type: insertType, position })
  return {
    nodes: [node],
    edges: [],
    entryNodeId: node.id,
    exitNodeId: node.id,
    focusNodeId: node.id
  }
}

const findPreferredOutgoingEdge = (
  sourceNodeId: string,
  nodes: WorkflowNodeModel[],
  edges: WorkflowEdgeModel[]
): WorkflowEdgeModel | null => {
  const outgoing = edges.filter((edge) => edge.source === sourceNodeId)
  if (!outgoing.length) {
    return null
  }

  const edgeToEnd = outgoing.find((edge) => {
    const targetNode = nodes.find((node) => node.id === edge.target)
    return targetNode?.type === 'end'
  })

  return edgeToEnd || outgoing[0]
}

const hasBusinessNodeInBranch = (
  branchNodeId: string,
  joinNodeId: string,
  nodeMap: Map<string, WorkflowNodeModel>,
  edges: WorkflowEdgeModel[]
): boolean => {
  const queue = edges.filter((edge) => edge.source === branchNodeId).map((edge) => edge.target)
  const visited = new Set<string>()

  while (queue.length) {
    const currentId = queue.shift()!
    if (currentId === joinNodeId || visited.has(currentId)) {
      continue
    }
    visited.add(currentId)

    const currentNode = nodeMap.get(currentId)
    if (!currentNode) {
      continue
    }

    if (!['condition_join', 'parallel_join'].includes(currentNode.type)) {
      return true
    }

    const nextTargets = edges.filter((edge) => edge.source === currentId).map((edge) => edge.target)
    queue.push(...nextTargets)
  }

  return false
}

const validateStructuredGroups = (snapshot: GraphSnapshot): string | null => {
  const nodeMap = new Map(snapshot.nodes.map((node) => [node.id, node]))

  for (const node of snapshot.nodes) {
    if (node.type === 'condition' && node.config.branches.length < 2) {
      return `条件节点「${node.name}」至少需要 2 个分支`
    }

    if (node.type === 'parallel_fork') {
      if (node.config.branches.length < 2) {
        return `并行节点「${node.name}」至少需要 2 条分支`
      }

      const joinId = node.config.joinNodeId
      if (!joinId || !nodeMap.has(joinId)) {
        return `并行节点「${node.name}」缺少汇合节点`
      }

      const invalidBranch = node.config.branches.find((branch) => {
        if (!branch.nodeId || !nodeMap.has(branch.nodeId)) {
          return true
        }
        return !hasBusinessNodeInBranch(branch.nodeId, joinId, nodeMap, snapshot.edges)
      })

      if (invalidBranch) {
        return `并行节点「${node.name}」的分支「${invalidBranch.name}」至少配置 1 个有效节点`
      }

      const hasJoinOutgoing = snapshot.edges.some((edge) => edge.source === joinId && !edge.metadata?.locked)
      if (!hasJoinOutgoing) {
        return `并行节点「${node.name}」的汇合节点未连接后续流程`
      }
    }
  }

  return null
}

const WorkflowDesignerPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const canvasRef = useRef<WorkflowCanvasRef>(null)
  const dragStartSnapshotRef = useRef<GraphSnapshot | null>(null)
  const hasDragMovedRef = useRef(false)

  const [templateId, setTemplateId] = useState('')
  const [templateName, setTemplateName] = useState(DEFAULT_TEMPLATE_NAME)
  const [templateCode, setTemplateCode] = useState('')
  const [templateCategory, setTemplateCategory] = useState('')
  const [templateStatus, setTemplateStatus] = useState<WorkflowTemplateStatus>('draft')
  const [updatedAt, setUpdatedAt] = useState('')
  const [version, setVersion] = useState(1)
  const [historyState, setHistoryState] = useState<GraphHistoryState>({
    past: [],
    present: createEmptySnapshot(),
    future: []
  })
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null)
  const [zoom, setZoom] = useState(1)
  const [jsonModalOpen, setJsonModalOpen] = useState(false)
  const [savedJson, setSavedJson] = useState('')
  const [versionsOpen, setVersionsOpen] = useState(false)
  const [versions, setVersions] = useState<WorkflowTemplateVersionSnapshot[]>([])
  const [saving, setSaving] = useState(false)
  const [isDirty, setIsDirty] = useState(false)
  const [viewport, setViewport] = useState<Viewport>({ x: 0, y: 0, zoom: 1 })
  const [initialViewport, setInitialViewport] = useState<Viewport | undefined>(undefined)

  const modelNodes = historyState.present.nodes
  const modelEdges = historyState.present.edges
  const canUndo = historyState.past.length > 0
  const canRedo = historyState.future.length > 0

  const selectedNode = useMemo(
    () => modelNodes.find((node) => node.id === selectedNodeId) || null,
    [modelNodes, selectedNodeId]
  )

  const setPresentSnapshot = useCallback(
    (
      updater: (current: GraphSnapshot) => GraphSnapshot,
      options?: { recordHistory?: boolean; sync?: boolean }
    ) => {
      const recordHistory = options?.recordHistory !== false
      const sync = options?.sync !== false

      setHistoryState((prev) => {
        const rawNext = cloneSnapshot(updater(cloneSnapshot(prev.present)))
        const next = sync ? synchronizeWorkflowTopology(rawNext) : rawNext
        if (isSnapshotEqual(prev.present, next)) {
          return prev
        }

        if (!recordHistory) {
          return { ...prev, present: next }
        }

        return {
          past: [...prev.past, cloneSnapshot(prev.present)],
          present: next,
          future: []
        }
      })
    },
    []
  )

  const resetHistoryWithSnapshot = useCallback((snapshot: GraphSnapshot) => {
    const normalized = synchronizeWorkflowTopology(snapshot)
    const cloned = cloneSnapshot(normalized)
    setHistoryState({ past: [], present: cloned, future: [] })
  }, [])

  useEffect(() => {
    const templateIdFromRoute = String(id || '').trim()
    if (!templateIdFromRoute) {
      navigate('/workflow/templates', { replace: true })
      return
    }

    const init = async () => {
      try {
        const template = await getWorkflowTemplateDetail(templateIdFromRoute)
        console.log('[workflow-designer] loaded template detail', {
          templateId: templateIdFromRoute,
          hasDefinition: Boolean(template.definition),
          rawNodeCount: template.definition?.nodes?.length ?? template.nodes?.length ?? 0,
          rawEdgeCount: template.definition?.edges?.length ?? template.edges?.length ?? 0,
          definition: template.definition
        })
        const normalized = normalizeTemplateModel(template)
        console.log('[workflow-designer] normalized render snapshot', {
          templateId: normalized.templateId,
          nodeCount: normalized.nodes.length,
          edgeCount: normalized.edges.length
        })
        setTemplateId(normalized.templateId)
        setTemplateName(normalized.templateName)
        setTemplateCode(normalized.templateCode)
        setTemplateCategory(normalized.category)
        setTemplateStatus(normalized.status)
        setUpdatedAt(normalized.updatedAt)
        setVersion(normalized.version)
        resetHistoryWithSnapshot({ nodes: normalized.nodes, edges: normalized.edges })
        setSelectedNodeId(normalized.nodes.find((node) => node.type === 'approval')?.id || normalized.nodes[0]?.id || null)
        const loadedViewport = normalized.viewport || { x: 0, y: 0, zoom: 1 }
        setViewport(loadedViewport)
        setInitialViewport(loadedViewport)
        setIsDirty(false)
      } catch (error) {
        console.error(error)
        console.log('[workflow-designer] load failed, fallback to default template', {
          templateId: templateIdFromRoute
        })
        message.error('加载流程模板失败')
        const fallback = createDefaultTemplateModel({ templateId: templateIdFromRoute })
        setTemplateId(fallback.templateId)
        setTemplateName(fallback.templateName)
        setTemplateCode(fallback.templateCode)
        setTemplateCategory(fallback.category)
        setTemplateStatus(fallback.status)
        setUpdatedAt(fallback.updatedAt)
        setVersion(fallback.version)
        resetHistoryWithSnapshot({ nodes: fallback.nodes, edges: fallback.edges })
        setSelectedNodeId(fallback.nodes[0]?.id || null)
        const fallbackViewport = fallback.viewport || { x: 0, y: 0, zoom: 1 }
        setViewport(fallbackViewport)
        setInitialViewport(fallbackViewport)
        setIsDirty(false)
      }
    }

    void init()
  }, [id, navigate, resetHistoryWithSnapshot])

  const findBestInsertEdgeId = useCallback((): string | null => {
    if (!modelEdges.length) {
      return null
    }
    if (selectedNodeId) {
      const outgoing = modelEdges.find((edge) => edge.source === selectedNodeId)
      if (outgoing) {
        return outgoing.id
      }
      const incoming = modelEdges.find((edge) => edge.target === selectedNodeId)
      if (incoming) {
        return incoming.id
      }
    }

    const edgeToEnd = modelEdges.find((edge) => {
      const target = modelNodes.find((node) => node.id === edge.target)
      return target?.type === 'end'
    })

    return edgeToEnd?.id || modelEdges[0].id
  }, [modelEdges, modelNodes, selectedNodeId])

  const insertNodeBetweenEdge = useCallback(
    (edgeId: string, insertType: InsertableNodeType) => {
      const targetEdge = modelEdges.find((edge) => edge.id === edgeId)
      if (!targetEdge) {
        return
      }

      const sourceNode = modelNodes.find((node) => node.id === targetEdge.source)
      const targetNode = modelNodes.find((node) => node.id === targetEdge.target)

      const segment = createInsertedSegment(insertType, {
        x: sourceNode && targetNode ? (sourceNode.position.x + targetNode.position.x) / 2 : sourceNode?.position.x || 0,
        y: sourceNode && targetNode ? (sourceNode.position.y + targetNode.position.y) / 2 : (sourceNode?.position.y || 100) + 140
      })

      setPresentSnapshot((current) => ({
        nodes: [...current.nodes, ...segment.nodes],
        edges: current.edges
          .filter((edge) => edge.id !== targetEdge.id)
          .concat([
            createWorkflowEdge(targetEdge.source, segment.entryNodeId),
            ...segment.edges,
            createWorkflowEdge(segment.exitNodeId, targetEdge.target)
          ])
      }))

      setSelectedNodeId(segment.focusNodeId)
      setIsDirty(true)
      window.setTimeout(() => canvasRef.current?.fitView(), 40)
    },
    [modelEdges, modelNodes, setPresentSnapshot]
  )

  const appendNodeAfterAnchor = useCallback(
    (anchorNodeId: string, insertType: InsertableNodeType) => {
      const anchorNode = modelNodes.find((node) => node.id === anchorNodeId)
      if (!anchorNode || anchorNode.type === 'end') {
        return
      }

      const outgoing = findPreferredOutgoingEdge(anchorNodeId, modelNodes, modelEdges)
      const targetNode = outgoing
        ? modelNodes.find((node) => node.id === outgoing.target)
        : modelNodes.find((node) => node.type === 'end')

      const segment = createInsertedSegment(insertType, {
        x: targetNode ? (anchorNode.position.x + targetNode.position.x) / 2 : anchorNode.position.x,
        y: targetNode ? (anchorNode.position.y + targetNode.position.y) / 2 : anchorNode.position.y + 140
      })

      setPresentSnapshot((current) => {
        let nextEdges = [...current.edges]
        const currentOutgoing = findPreferredOutgoingEdge(anchorNodeId, current.nodes, current.edges)

        if (currentOutgoing) {
          nextEdges = nextEdges
            .filter((edge) => edge.id !== currentOutgoing.id)
            .concat([
              createWorkflowEdge(anchorNodeId, segment.entryNodeId),
              ...segment.edges,
              createWorkflowEdge(segment.exitNodeId, currentOutgoing.target)
            ])
        } else {
          nextEdges = nextEdges.concat(createWorkflowEdge(anchorNodeId, segment.entryNodeId), ...segment.edges)
          const endNode = current.nodes.find((node) => node.type === 'end')
          if (endNode && endNode.id !== segment.exitNodeId) {
            nextEdges = nextEdges.concat(createWorkflowEdge(segment.exitNodeId, endNode.id))
          }
        }

        const updatedNodes = [...current.nodes, ...segment.nodes]
        const appendedInBranchNodes = updatedNodes.map((node) => {
          if (node.id === anchorNodeId && node.type === 'condition_branch') {
            return {
              ...node,
              config: {
                ...node.config,
                childNodeIds: Array.from(new Set([...(node.config.childNodeIds || []), segment.entryNodeId]))
              }
            } as WorkflowNodeModel
          }
          if (node.id === anchorNodeId && node.type === 'parallel_branch') {
            return {
              ...node,
              config: {
                ...node.config,
                childNodeIds: Array.from(new Set([...(node.config.childNodeIds || []), segment.entryNodeId]))
              }
            } as WorkflowNodeModel
          }
          return node
        })

        return {
          nodes: appendedInBranchNodes,
          edges: nextEdges
        }
      })

      setSelectedNodeId(segment.focusNodeId)
      setIsDirty(true)
      window.setTimeout(() => canvasRef.current?.fitView(), 40)
    },
    [findPreferredOutgoingEdge, modelEdges, modelNodes, setPresentSnapshot]
  )

  const handleInsertFromPalette = useCallback(
    (nodeType: InsertableNodeType) => {
      const edgeId = findBestInsertEdgeId()
      if (!edgeId) {
        const starter = modelNodes.find((node) => node.type === 'starter')
        if (starter) {
          appendNodeAfterAnchor(starter.id, nodeType)
        }
        return
      }
      insertNodeBetweenEdge(edgeId, nodeType)
    },
    [appendNodeAfterAnchor, findBestInsertEdgeId, insertNodeBetweenEdge, modelNodes]
  )

  const syncConditionBranchNodeToParent = useCallback(
    (nodes: WorkflowNodeModel[], branchNode: ConditionBranchWorkflowNode): WorkflowNodeModel[] => {
      return nodes.map((node) => {
        if (node.id !== branchNode.config.gatewayId || node.type !== 'condition') {
          return node
        }

        const nextBranches = node.config.branches.map((branch) =>
          branch.id === branchNode.config.branchId
            ? {
                ...branch,
                nodeId: branchNode.id,
                name: branchNode.name,
                expression: branchNode.config.expression,
                priority: branchNode.config.priority,
                isDefault: branchNode.config.isDefault,
                remark: branchNode.config.remark,
                childNodeIds: branchNode.config.childNodeIds,
                collapsed: branchNode.config.collapsed
              }
            : {
                ...branch,
                isDefault: branch.id === node.config.defaultBranchId
              }
        )

        return {
          ...node,
          config: {
            ...node.config,
            branches: nextBranches,
            defaultBranchId: branchNode.config.isDefault ? branchNode.config.branchId : node.config.defaultBranchId
          }
        }
      })
    },
    []
  )

  const syncParallelBranchNodeToParent = useCallback(
    (nodes: WorkflowNodeModel[], branchNode: ParallelBranchWorkflowNode): WorkflowNodeModel[] => {
      return nodes.map((node) => {
        if (node.id !== branchNode.config.gatewayId || node.type !== 'parallel_fork') {
          return node
        }
        return {
          ...node,
          config: {
            ...node.config,
            branches: node.config.branches.map((branch) =>
              branch.id === branchNode.config.branchId
                ? {
                    ...branch,
                    nodeId: branchNode.id,
                    name: branchNode.name,
                    order: branchNode.config.order,
                    remark: branchNode.config.remark,
                    childNodeIds: branchNode.config.childNodeIds,
                    collapsed: branchNode.config.collapsed
                  }
                : branch
            )
          }
        }
      })
    },
    []
  )

  const handleNodeNameChange = useCallback(
    (nodeId: string, name: string) => {
      setPresentSnapshot((current) => {
        let nextNodes = current.nodes.map((node) => (node.id === nodeId ? { ...node, name } : node))
        const target = nextNodes.find((node) => node.id === nodeId)
        if (target?.type === 'condition_branch') {
          nextNodes = syncConditionBranchNodeToParent(nextNodes, target as ConditionBranchWorkflowNode)
        }
        if (target?.type === 'parallel_branch') {
          nextNodes = syncParallelBranchNodeToParent(nextNodes, target as ParallelBranchWorkflowNode)
        }
        return { nodes: nextNodes, edges: current.edges }
      })
      setIsDirty(true)
    },
    [setPresentSnapshot, syncConditionBranchNodeToParent, syncParallelBranchNodeToParent]
  )

  const handleNodeConfigChange = useCallback(
    (nodeId: string, patch: Record<string, unknown>) => {
      setPresentSnapshot((current) => {
        let nextNodes = current.nodes.map((node) => {
          if (node.id !== nodeId) {
            return node
          }
          return { ...node, config: { ...node.config, ...patch } } as WorkflowNodeModel
        })

        const target = nextNodes.find((node) => node.id === nodeId)
        if (target?.type === 'condition_branch') {
          nextNodes = syncConditionBranchNodeToParent(nextNodes, target as ConditionBranchWorkflowNode)
        }
        if (target?.type === 'parallel_branch') {
          nextNodes = syncParallelBranchNodeToParent(nextNodes, target as ParallelBranchWorkflowNode)
        }

        return { nodes: nextNodes, edges: current.edges }
      })
      setIsDirty(true)
    },
    [setPresentSnapshot, syncConditionBranchNodeToParent, syncParallelBranchNodeToParent]
  )

  const handleConditionBranchChange = useCallback(
    (ownerNodeId: string, branchId: string, patch: Partial<ConditionBranch>) => {
      setPresentSnapshot((current) => ({
        nodes: current.nodes.map((node) => {
          if (node.id === ownerNodeId && node.type === 'condition') {
            return {
              ...node,
              config: {
                ...node.config,
                branches: node.config.branches.map((branch) =>
                  branch.id === branchId ? { ...branch, ...patch } : branch
                )
              }
            }
          }

          if (node.type === 'condition_branch' && node.config.gatewayId === ownerNodeId && node.config.branchId === branchId) {
            return {
              ...node,
              name: patch.name ?? node.name,
              config: {
                ...node.config,
                expression: patch.expression ?? node.config.expression,
                priority: patch.priority ?? node.config.priority,
                remark: patch.remark ?? node.config.remark
              }
            } as WorkflowNodeModel
          }

          return node
        }),
        edges: current.edges
      }))
      setIsDirty(true)
    },
    [setPresentSnapshot]
  )

  const handleConditionSetDefaultBranch = useCallback(
    (ownerNodeId: string, branchId: string) => {
      setPresentSnapshot((current) => ({
        nodes: current.nodes.map((node) => {
          if (node.id === ownerNodeId && node.type === 'condition') {
            return {
              ...node,
              config: {
                ...node.config,
                defaultBranchId: branchId,
                branches: node.config.branches.map((branch) => ({
                  ...branch,
                  isDefault: branch.id === branchId
                }))
              }
            }
          }
          if (node.type === 'condition_branch' && node.config.gatewayId === ownerNodeId) {
            return {
              ...node,
              config: {
                ...node.config,
                isDefault: node.config.branchId === branchId
              }
            } as WorkflowNodeModel
          }
          return node
        }),
        edges: current.edges
      }))
      setIsDirty(true)
    },
    [setPresentSnapshot]
  )

  const handleParallelBranchChange = useCallback(
    (ownerNodeId: string, branchId: string, patch: Partial<ParallelBranch>) => {
      setPresentSnapshot((current) => ({
        nodes: current.nodes.map((node) => {
          if (node.id === ownerNodeId && node.type === 'parallel_fork') {
            return {
              ...node,
              config: {
                ...node.config,
                branches: node.config.branches.map((branch) =>
                  branch.id === branchId ? { ...branch, ...patch } : branch
                )
              }
            }
          }
          if (node.type === 'parallel_branch' && node.config.gatewayId === ownerNodeId && node.config.branchId === branchId) {
            return {
              ...node,
              name: patch.name ?? node.name,
              config: {
                ...node.config,
                order: patch.order ?? node.config.order,
                remark: patch.remark ?? node.config.remark
              }
            } as WorkflowNodeModel
          }
          return node
        }),
        edges: current.edges
      }))
      setIsDirty(true)
    },
    [setPresentSnapshot]
  )

  const handleToggleNodeCollapse = useCallback(
    (nodeId: string) => {
      setPresentSnapshot((current) => ({
        nodes: current.nodes.map((node) => {
          if (node.id === nodeId && node.type === 'condition_branch') {
            const nextCollapsed = !node.config.collapsed
            return {
              ...node,
              config: {
                ...node.config,
                collapsed: nextCollapsed
              }
            } as WorkflowNodeModel
          }
          if (node.id === nodeId && node.type === 'parallel_branch') {
            const nextCollapsed = !node.config.collapsed
            return {
              ...node,
              config: {
                ...node.config,
                collapsed: nextCollapsed
              }
            } as WorkflowNodeModel
          }
          if (node.type === 'condition') {
            const branch = node.config.branches.find((item) => item.nodeId === nodeId)
            if (branch) {
              return {
                ...node,
                config: {
                  ...node.config,
                  branches: node.config.branches.map((item) =>
                    item.nodeId === nodeId ? { ...item, collapsed: !item.collapsed } : item
                  )
                }
              } as WorkflowNodeModel
            }
          }
          if (node.type === 'parallel_fork') {
            const branch = node.config.branches.find((item) => item.nodeId === nodeId)
            if (branch) {
              return {
                ...node,
                config: {
                  ...node.config,
                  branches: node.config.branches.map((item) =>
                    item.nodeId === nodeId ? { ...item, collapsed: !item.collapsed } : item
                  )
                }
              } as WorkflowNodeModel
            }
          }
          return node
        }),
        edges: current.edges
      }))
      setIsDirty(true)
    },
    [setPresentSnapshot]
  )

  const handleDeleteNode = useCallback(
    (nodeId: string) => {
      const targetNode = modelNodes.find((node) => node.id === nodeId)
      if (!targetNode) {
        return
      }

      if (['starter', 'end', 'condition_join', 'parallel_join'].includes(targetNode.type)) {
        message.warning('该节点不允许直接删除')
        return
      }

      setPresentSnapshot((current) => {
        const removeNodeIds = new Set<string>([nodeId])

        if (targetNode.type === 'condition' && targetNode.config.joinNodeId) {
          removeNodeIds.add(targetNode.config.joinNodeId)
          targetNode.config.branches.forEach((branch) => {
            if (branch.nodeId) {
              removeNodeIds.add(branch.nodeId)
            }
          })
        }

        if (targetNode.type === 'parallel_fork' && targetNode.config.joinNodeId) {
          removeNodeIds.add(targetNode.config.joinNodeId)
          targetNode.config.branches.forEach((branch) => {
            if (branch.nodeId) {
              removeNodeIds.add(branch.nodeId)
            }
          })
        }

        if (targetNode.type === 'condition_branch') {
          const owner = current.nodes.find((node) => node.id === targetNode.config.gatewayId)
          if (owner?.type === 'condition' && owner.config.branches.length <= 2) {
            message.warning('条件分支至少保留 2 条')
            return current
          }
        }

        if (targetNode.type === 'parallel_branch') {
          const owner = current.nodes.find((node) => node.id === targetNode.config.gatewayId)
          if (owner?.type === 'parallel_fork' && owner.config.branches.length <= 2) {
            message.warning('并行分支至少保留 2 条')
            return current
          }
        }

        const nextNodes = current.nodes
          .filter((node) => !removeNodeIds.has(node.id))
          .map((node) => {
            if (targetNode.type === 'condition_branch' && node.id === targetNode.config.gatewayId && node.type === 'condition') {
              return {
                ...node,
                config: {
                  ...node.config,
                  branches: node.config.branches.filter((branch) => branch.id !== targetNode.config.branchId)
                }
              } as WorkflowNodeModel
            }
            if (targetNode.type === 'parallel_branch' && node.id === targetNode.config.gatewayId && node.type === 'parallel_fork') {
              return {
                ...node,
                config: {
                  ...node.config,
                  branches: node.config.branches.filter((branch) => branch.id !== targetNode.config.branchId)
                }
              } as WorkflowNodeModel
            }
            return node
          })

        return {
          nodes: nextNodes,
          edges: current.edges.filter((edge) => !removeNodeIds.has(edge.source) && !removeNodeIds.has(edge.target))
        }
      })

      setSelectedNodeId(null)
      setIsDirty(true)
    },
    [modelNodes, setPresentSnapshot]
  )

  const handleNodesChange = useCallback(
    (changes: NodeChange[]) => {
      if (!changes.length) {
        return
      }

      const safeChanges = changes.filter((change) =>
        'id' in change ? modelNodes.some((node) => node.id === change.id) : false
      )
      if (!safeChanges.length) {
        return
      }

      const changedPositionIds = new Set(
        safeChanges
          .filter((change) => change.type === 'position' && 'id' in change)
          .map((change) => change.id)
      )

      setPresentSnapshot(
        (current) => {
          const nextRuntimeNodes = applyNodeChanges(safeChanges, toRuntimeNodes(current.nodes))
          const nodeMap = new Map(current.nodes.map((node) => [node.id, node]))

          return {
            nodes: nextRuntimeNodes.map((runtimeNode) => {
              const raw = nodeMap.get(runtimeNode.id)!
              const manual = changedPositionIds.has(runtimeNode.id) ? 'manual' : raw.positionMode
              return {
                ...raw,
                position: runtimeNode.position,
                positionMode: manual
              } as WorkflowNodeModel
            }),
            edges: current.edges
          }
        },
        { recordHistory: false, sync: false }
      )

      if (changedPositionIds.size) {
        hasDragMovedRef.current = true
        setIsDirty(true)
      }
    },
    [modelNodes, setPresentSnapshot]
  )

  const handleEdgesChange = useCallback(
    (changes: EdgeChange[]) => {
      if (!changes.length) {
        return
      }

      const removeIds = new Set(
        changes
          .filter((change) => change.type === 'remove')
          .map((change) => ('id' in change ? change.id : ''))
          .filter(Boolean)
      )
      const removingLocked = modelEdges.some((edge) => removeIds.has(edge.id) && edge.metadata?.locked)
      if (removingLocked) {
        message.warning('结构化关键连线不允许直接删除')
        return
      }

      setPresentSnapshot((current) => ({
        nodes: current.nodes,
        edges: applyEdgeChanges(changes, toRuntimeEdges(current.edges)).map((edge) => ({
          ...(current.edges.find((item) => item.id === edge.id) || {
            id: edge.id,
            source: edge.source,
            target: edge.target
          }),
          id: edge.id,
          source: edge.source,
          target: edge.target
        }))
      }))
      setIsDirty(true)
    },
    [modelEdges, setPresentSnapshot]
  )

  const handleNodeDragStart = useCallback(() => {
    dragStartSnapshotRef.current = cloneSnapshot(historyState.present)
    hasDragMovedRef.current = false
  }, [historyState.present])

  const handleNodeDragStop = useCallback(() => {
    const start = dragStartSnapshotRef.current
    dragStartSnapshotRef.current = null
    if (!start || !hasDragMovedRef.current) {
      hasDragMovedRef.current = false
      return
    }

    hasDragMovedRef.current = false
    setHistoryState((prev) => ({
      past: [...prev.past, cloneSnapshot(start)],
      present: prev.present,
      future: []
    }))
  }, [])

  const handleUndo = useCallback(() => {
    setHistoryState((prev) => {
      if (!prev.past.length) {
        message.info('没有可撤销的操作')
        return prev
      }
      const previous = prev.past[prev.past.length - 1]
      return {
        past: prev.past.slice(0, -1),
        present: cloneSnapshot(previous),
        future: [cloneSnapshot(prev.present), ...prev.future]
      }
    })
    setIsDirty(true)
  }, [])

  const handleRedo = useCallback(() => {
    setHistoryState((prev) => {
      if (!prev.future.length) {
        message.info('没有可重做的操作')
        return prev
      }
      const next = prev.future[0]
      return {
        past: [...prev.past, cloneSnapshot(prev.present)],
        present: cloneSnapshot(next),
        future: prev.future.slice(1)
      }
    })
    setIsDirty(true)
  }, [])

  const flowElements = useMemo(
    () =>
      toReactFlowElements({
        modelNodes,
        modelEdges,
        selectedNodeId,
        onDeleteNode: handleDeleteNode,
        onAppendNode: appendNodeAfterAnchor,
        onAddNode: insertNodeBetweenEdge,
        onToggleCollapse: handleToggleNodeCollapse
      }),
    [
      appendNodeAfterAnchor,
      handleDeleteNode,
      handleToggleNodeCollapse,
      insertNodeBetweenEdge,
      modelEdges,
      modelNodes,
      selectedNodeId
    ]
  )

  const saveCurrentTemplate = useCallback(
    async (options?: { showJsonModal?: boolean; showMessage?: boolean }) => {
      const snapshot = cloneSnapshot(historyState.present)
      const liveViewport = canvasRef.current?.getViewport() || viewport
      if (!snapshot.nodes.length || !templateId) {
        message.warning('当前流程为空，无法保存')
        return null
      }

      const validationError = validateStructuredGroups(snapshot)
      if (validationError) {
        message.error(validationError)
        return null
      }

      setSaving(true)
      try {
        const payload = buildTemplatePayload({
          templateId,
          templateName: templateName.trim() || DEFAULT_TEMPLATE_NAME,
          templateCode,
          category: templateCategory,
          status: templateStatus,
          version,
          updatedAt,
          viewport: liveViewport,
          layout: { manualPositions: true },
          nodes: snapshot.nodes,
          edges: snapshot.edges
        })
        console.log('[workflow-designer] save payload preview', {
          templateId,
          selectedNodeId,
          version,
          nodeCount: snapshot.nodes.length,
          edgeCount: snapshot.edges.length,
          snapshotSummary: {
            viewport: liveViewport,
            hasDefinition: Boolean(payload.definition)
          },
          payload: {
            templateId: payload.templateId,
            templateName: payload.templateName,
            templateCode: payload.templateCode,
            category: payload.category,
            status: payload.status,
            version: payload.version,
            definitionNodeCount: payload.definition?.nodes?.length ?? 0,
            definitionEdgeCount: payload.definition?.edges?.length ?? 0
          }
        })
        const result = await saveWorkflowTemplate(payload)
        console.log('[workflow-designer] save response', {
          templateId: result.data.templateId,
          version: result.data.version,
          status: result.data.status,
          nodeCount: result.data.definition?.nodes?.length ?? result.data.nodes?.length ?? 0,
          edgeCount: result.data.definition?.edges?.length ?? result.data.edges?.length ?? 0
        })
        const normalized = normalizeTemplateModel(result.data)
        setVersion(normalized.version)
        setTemplateName(normalized.templateName)
        setTemplateCode(normalized.templateCode)
        setTemplateCategory(normalized.category)
        setTemplateStatus(normalized.status)
        setUpdatedAt(normalized.updatedAt)
        const persistedViewport = normalized.viewport || liveViewport
        setViewport(persistedViewport)
        setInitialViewport(persistedViewport)
        setIsDirty(false)

        if (options?.showJsonModal !== false) {
          setSavedJson(JSON.stringify(result.data, null, 2))
          setJsonModalOpen(true)
        }
        if (options?.showMessage !== false) {
          message.success('流程模板保存成功')
        }
        return result.data
      } catch (error) {
        console.error(error)
        message.error('保存失败，请稍后重试')
        return null
      } finally {
        setSaving(false)
      }
    },
    [
      historyState.present,
      selectedNodeId,
      templateId,
      templateName,
      templateCode,
      templateCategory,
      templateStatus,
      version,
      updatedAt,
      viewport
    ]
  )

  const handleSave = useCallback(async () => {
    await saveCurrentTemplate({ showJsonModal: true, showMessage: true })
  }, [saveCurrentTemplate])

  const handlePublish = useCallback(async () => {
    if (!templateId) {
      return
    }

    const snapshot = cloneSnapshot(historyState.present)
    const liveViewport = canvasRef.current?.getViewport() || viewport
    const validationError = validateStructuredGroups(snapshot)
    if (validationError) {
      message.error(`发布校验失败：${validationError}`)
      return
    }

    try {
      const payload = buildTemplatePayload({
        templateId,
        templateName: templateName.trim() || DEFAULT_TEMPLATE_NAME,
        templateCode,
        category: templateCategory,
        status: 'published',
        version,
        updatedAt,
        viewport: liveViewport,
        layout: { manualPositions: true },
        nodes: snapshot.nodes,
        edges: snapshot.edges
      })
      console.log('[workflow-designer] publish payload preview', {
        templateId,
        version,
        nodeCount: snapshot.nodes.length,
        edgeCount: snapshot.edges.length,
        snapshotSummary: {
          viewport: liveViewport,
          hasDefinition: Boolean(payload.definition)
        }
      })

      const result = await publishWorkflowTemplate(templateId, payload)
      console.log('[workflow-designer] publish response', {
        templateId,
        publishTime: result.publishTime,
        version: result.data?.version
      })
      if (result.data) {
        const normalized = normalizeTemplateModel(result.data)
        setVersion(normalized.version)
        setTemplateName(normalized.templateName)
        setTemplateCode(normalized.templateCode)
        setTemplateCategory(normalized.category)
        setTemplateStatus(normalized.status)
        setUpdatedAt(normalized.updatedAt)
        const persistedViewport = normalized.viewport || liveViewport
        setViewport(persistedViewport)
        setInitialViewport(persistedViewport)
      } else {
        setTemplateStatus('published')
        setUpdatedAt(result.publishTime)
      }
      setIsDirty(false)
      message.success('发布成功，当前版本已更新，正在返回模板列表')
      window.setTimeout(() => {
        navigate('/workflow/templates')
      }, 1000)
    } catch (error) {
      console.error(error)
      message.error('发布失败，请稍后重试')
    }
  }, [templateId, historyState.present, viewport, templateName, templateCode, templateCategory, version, updatedAt, navigate])

  const handleOpenHistory = useCallback(async () => {
    if (!templateId) {
      return
    }

    try {
      const history = await getWorkflowTemplateVersions(templateId)
      setVersions(history)
      setVersionsOpen(true)
    } catch (error) {
      console.error(error)
      message.error('加载历史版本失败')
    }
  }, [templateId])

  const handleTemplateNameChange = useCallback((value: string) => {
    setTemplateName(value)
    setIsDirty(true)
  }, [])

  return (
    <div className="workflow-designer-page">
      <DesignerToolbar
        templateName={templateName}
        templateCode={templateCode}
        version={version}
        zoom={zoom}
        canUndo={canUndo}
        canRedo={canRedo}
        onTemplateNameChange={handleTemplateNameChange}
        onHistory={handleOpenHistory}
        onUndo={handleUndo}
        onRedo={handleRedo}
        onSave={handleSave}
        onPublish={handlePublish}
        onZoomIn={() => canvasRef.current?.zoomIn()}
        onZoomOut={() => canvasRef.current?.zoomOut()}
        onFitView={() => canvasRef.current?.fitView()}
      />

      <div className="workflow-designer-main">
        <NodePalette onInsertNode={handleInsertFromPalette} />

        <section className="workflow-canvas-panel">
          <div className="workflow-canvas-panel-meta">
            <Space>
              <Text type="secondary">模板ID：{templateId}</Text>
              <Text type="secondary">分类：{templateCategory}</Text>
              <Tag color={statusLabelMap[templateStatus].color}>{statusLabelMap[templateStatus].text}</Tag>
              <Text type="secondary">更新时间：{updatedAt || '-'}</Text>
              {isDirty ? <Tag color="processing">未保存修改</Tag> : <Tag color="success">已保存</Tag>}
            </Space>
            <Button size="small" onClick={() => canvasRef.current?.fitView()}>
              閫傞厤鐢诲竷
            </Button>
          </div>

          {flowElements.nodes.length ? (
            <WorkflowCanvas
              ref={canvasRef}
              nodes={flowElements.nodes}
              edges={flowElements.edges}
              initialViewport={initialViewport}
              onSelectNode={setSelectedNodeId}
              onZoomChange={setZoom}
              onViewportChange={(nextViewport) => {
                setViewport(nextViewport)
              }}
              onNodesChange={handleNodesChange}
              onEdgesChange={handleEdgesChange}
              onNodeDragStart={handleNodeDragStart}
              onNodeDragStop={handleNodeDragStop}
            />
          ) : (
            <Empty description="暂无流程节点" image={Empty.PRESENTED_IMAGE_SIMPLE} />
          )}
        </section>

        <PropertyPanel
          selectedNode={selectedNode}
          onNodeNameChange={handleNodeNameChange}
          onNodeConfigChange={handleNodeConfigChange}
          onDeleteNode={handleDeleteNode}
          onConditionBranchChange={handleConditionBranchChange}
          onConditionSetDefaultBranch={handleConditionSetDefaultBranch}
          onParallelBranchChange={handleParallelBranchChange}
        />
      </div>

      <Modal
        title="保存结果 JSON"
        width={880}
        open={jsonModalOpen}
        onCancel={() => setJsonModalOpen(false)}
        footer={[
          <Button key="close" onClick={() => setJsonModalOpen(false)}>
            关闭
          </Button>
        ]}
      >
        <Paragraph type="secondary">已保存当前模板的结构化 JSON，可用于回显验证。</Paragraph>
        <pre className="workflow-json-preview">{savedJson}</pre>
      </Modal>

      <Drawer title="模板历史（当前模板）" width={520} open={versionsOpen} onClose={() => setVersionsOpen(false)}>
        <List
          dataSource={versions}
          locale={{ emptyText: '暂无历史版本' }}
          renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                title={`v${item.version} - ${item.updatedAt}`}
                description={`操作人：${item.operator}`}
              />
            </List.Item>
          )}
        />
      </Drawer>

      <Modal title="保存中" open={saving} closable={false} footer={null} centered width={260}>
        <Paragraph style={{ marginBottom: 0 }}>正在保存流程模板...</Paragraph>
      </Modal>
    </div>
  )
}

export default WorkflowDesignerPage
