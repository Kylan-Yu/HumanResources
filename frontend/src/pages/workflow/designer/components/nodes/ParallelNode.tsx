import React from 'react'
import type { NodeProps } from '@xyflow/react'
import WorkflowNodeCard from './WorkflowNodeCard'

const ParallelNode: React.FC<NodeProps> = (props) => {
  return <WorkflowNodeCard {...props} />
}

export default ParallelNode
