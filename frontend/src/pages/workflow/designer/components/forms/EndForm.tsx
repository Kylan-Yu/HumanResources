import React from 'react'
import { Form, Input } from 'antd'
import type { EndNodeConfig, EndWorkflowNode } from '../../types'

interface EndFormProps {
  node: EndWorkflowNode
  onNameChange: (name: string) => void
  onConfigChange: (patch: Partial<EndNodeConfig>) => void
}

const EndForm: React.FC<EndFormProps> = ({ node, onNameChange, onConfigChange }) => {
  return (
    <Form layout="vertical" className="workflow-property-form">
      <Form.Item label="节点名称">
        <Input value={node.name} onChange={(event) => onNameChange(event.target.value)} />
      </Form.Item>

      <Form.Item label="展示文案">
        <Input.TextArea
          rows={3}
          value={node.config.displayText}
          onChange={(event) => onConfigChange({ displayText: event.target.value })}
        />
      </Form.Item>
    </Form>
  )
}

export default EndForm
