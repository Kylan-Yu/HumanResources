import React from 'react'
import type { NodeProps } from '@xyflow/react'
import WorkflowNodeCard from './WorkflowNodeCard'

const EndNode: React.FC<NodeProps> = (props) => {
  return <WorkflowNodeCard {...props} />
}

export default EndNode
