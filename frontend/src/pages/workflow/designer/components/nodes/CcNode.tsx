import React from 'react'
import type { NodeProps } from '@xyflow/react'
import WorkflowNodeCard from './WorkflowNodeCard'

const CcNode: React.FC<NodeProps> = (props) => {
  return <WorkflowNodeCard {...props} />
}

export default CcNode
