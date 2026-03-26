import { BRANCH_GAP_X, MAIN_NODE_GAP_Y, MAIN_NODE_START_Y, MAIN_NODE_X } from '../constants'
import type { WorkflowEdgeModel, WorkflowNodeModel } from '../types'

export const findMainChainNodeIds = (nodes: WorkflowNodeModel[], edges: WorkflowEdgeModel[]): string[] => {
  if (!nodes.length) {
    return []
  }

  const nodeMap = new Map(nodes.map((node) => [node.id, node]))
  const outgoingMap = new Map<string, WorkflowEdgeModel[]>()

  edges.forEach((edge) => {
    const list = outgoingMap.get(edge.source) ?? []
    list.push(edge)
    outgoingMap.set(edge.source, list)
  })

  const starter = nodes.find((node) => node.type === 'starter') ?? nodes[0]
  const chain: string[] = []
  const visited = new Set<string>()

  let current: WorkflowNodeModel | undefined = starter
  while (current && !visited.has(current.id)) {
    chain.push(current.id)
    visited.add(current.id)

    const edgesFromCurrent = outgoingMap.get(current.id) ?? []
    if (!edgesFromCurrent.length || current.type === 'end') {
      break
    }

    const nextEdge =
      edgesFromCurrent.find((edge) => !visited.has(edge.target) && nodeMap.has(edge.target)) ||
      edgesFromCurrent.find((edge) => nodeMap.has(edge.target))

    if (!nextEdge) {
      break
    }

    current = nodeMap.get(nextEdge.target)
  }

  const rest = nodes
    .filter((node) => !visited.has(node.id))
    .sort((a, b) => a.position.y - b.position.y)
    .map((node) => node.id)

  return [...chain, ...rest]
}

export const layoutMainChain = (nodes: WorkflowNodeModel[], edges: WorkflowEdgeModel[]): WorkflowNodeModel[] => {
  const nodeMap = new Map(nodes.map((node) => [node.id, node]))
  const chainIds = findMainChainNodeIds(nodes, edges)

  return chainIds
    .map((nodeId, index) => {
      const node = nodeMap.get(nodeId)
      if (!node) {
        return null
      }

      return {
        ...node,
        position: {
          x: MAIN_NODE_X,
          y: MAIN_NODE_START_Y + index * MAIN_NODE_GAP_Y
        }
      }
    })
    .filter((node): node is WorkflowNodeModel => Boolean(node))
}

export const getSymmetricOffsets = (count: number, gap = BRANCH_GAP_X): number[] => {
  if (count <= 0) {
    return []
  }

  const mid = (count - 1) / 2
  return Array.from({ length: count }).map((_, index) => (index - mid) * gap)
}
