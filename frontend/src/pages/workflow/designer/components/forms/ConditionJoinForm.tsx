import React from 'react'
import { Form, Input } from 'antd'
import type { ConditionJoinNodeConfig, ConditionJoinWorkflowNode } from '../../types'

interface ConditionJoinFormProps {
  node: ConditionJoinWorkflowNode
  onNameChange: (name: string) => void
  onConfigChange: (patch: Partial<ConditionJoinNodeConfig>) => void
}

const ConditionJoinForm: React.FC<ConditionJoinFormProps> = ({ node, onNameChange, onConfigChange }) => {
  return (
    <Form layout="vertical" className="workflow-property-form">
      <Form.Item label="汇合节点名称">
        <Input value={node.name} onChange={(event) => onNameChange(event.target.value)} />
      </Form.Item>

      <Form.Item label="展示文案">
        <Input
          value={node.config.displayText}
          onChange={(event) => onConfigChange({ displayText: event.target.value })}
        />
      </Form.Item>
    </Form>
  )
}

export default ConditionJoinForm
