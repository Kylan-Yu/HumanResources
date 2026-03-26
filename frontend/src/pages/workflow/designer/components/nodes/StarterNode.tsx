import React from 'react'
import type { NodeProps } from '@xyflow/react'
import WorkflowNodeCard from './WorkflowNodeCard'

const StarterNode: React.FC<NodeProps> = (props) => {
  return <WorkflowNodeCard {...props} />
}

export default StarterNode
