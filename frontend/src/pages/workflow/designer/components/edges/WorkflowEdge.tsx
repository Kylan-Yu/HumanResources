import React from 'react'
import {
  BaseEdge,
  EdgeLabelRenderer,
  getSmoothStepPath,
  type EdgeProps
} from '@xyflow/react'
import AddNodeMenu from '../AddNodeMenu'
import type { WorkflowEdgeRenderData } from '../../types'

const WorkflowEdge: React.FC<EdgeProps> = ({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  markerEnd,
  style,
  data
}) => {
  const edgeData = (data || {}) as unknown as WorkflowEdgeRenderData
  const [edgePath, labelX, labelY] = getSmoothStepPath({
    sourceX,
    sourceY,
    sourcePosition,
    targetX,
    targetY,
    targetPosition,
    borderRadius: 12
  })

  const canInsert = Boolean(edgeData.addable && !edgeData.virtual && edgeData.onAddNode)
  const actionEdgeId = edgeData.proxyEdgeId || id

  return (
    <>
      <BaseEdge id={id} path={edgePath} markerEnd={markerEnd} style={style} />

      {canInsert ? (
        <EdgeLabelRenderer>
          <div
            style={{
              position: 'absolute',
              pointerEvents: 'all',
              transform: `translate(-50%, -50%) translate(${labelX}px, ${labelY}px)`
            }}
            className="nodrag nopan"
          >
            <AddNodeMenu
              onSelect={(nodeType) => {
                edgeData.onAddNode?.(actionEdgeId, nodeType)
              }}
            />
          </div>
        </EdgeLabelRenderer>
      ) : null}
    </>
  )
}

export default WorkflowEdge
