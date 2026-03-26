import React from 'react'
import { Handle, Position } from '@xyflow/react'
import type { NodeProps } from '@xyflow/react'

const MergePreviewNode: React.FC<NodeProps> = () => {
  return (
    <div className="workflow-group-bridge-node">
      <Handle type="target" position={Position.Top} className="workflow-node-handle" />
      <span>桥接</span>
      <Handle type="source" position={Position.Bottom} className="workflow-node-handle" />
    </div>
  )
}

export default MergePreviewNode
