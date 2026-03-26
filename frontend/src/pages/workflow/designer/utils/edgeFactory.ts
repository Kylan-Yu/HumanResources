import type { WorkflowEdgeModel } from '../types'

let edgeCounter = 0

const nextEdgeSeq = (): number => {
  edgeCounter += 1
  return edgeCounter
}

export const createEdgeId = (source: string, target: string): string => {
  return `edge_${source}_${target}_${nextEdgeSeq()}`
}

export const createWorkflowEdge = (source: string, target: string): WorkflowEdgeModel => {
  return {
    id: createEdgeId(source, target),
    source,
    target
  }
}
