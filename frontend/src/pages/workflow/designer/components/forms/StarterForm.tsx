import React from 'react'
import { Form, Input, Select } from 'antd'
import {
  INITIATOR_SCOPE_OPTIONS,
  MOCK_DEPARTMENT_OPTIONS,
  MOCK_ROLE_OPTIONS,
  MOCK_USER_OPTIONS
} from '../../constants'
import type { StarterNodeConfig, StarterWorkflowNode } from '../../types'

interface StarterFormProps {
  node: StarterWorkflowNode
  onNameChange: (name: string) => void
  onConfigChange: (patch: Partial<StarterNodeConfig>) => void
}

const StarterForm: React.FC<StarterFormProps> = ({ node, onNameChange, onConfigChange }) => {
  const { config } = node

  return (
    <Form layout="vertical" className="workflow-property-form">
      <Form.Item label="节点名称">
        <Input value={node.name} onChange={(event) => onNameChange(event.target.value)} />
      </Form.Item>

      <Form.Item label="发起范围">
        <Select
          value={config.initiatorScopeType}
          options={INITIATOR_SCOPE_OPTIONS}
          onChange={(value) =>
            onConfigChange({
              initiatorScopeType: value,
              roleIds: [],
              departmentIds: [],
              userIds: []
            })
          }
        />
      </Form.Item>

      {config.initiatorScopeType === 'role' ? (
        <Form.Item label="指定角色">
          <Select
            mode="multiple"
            value={config.roleIds}
            options={MOCK_ROLE_OPTIONS}
            onChange={(value) => onConfigChange({ roleIds: value })}
          />
        </Form.Item>
      ) : null}

      {config.initiatorScopeType === 'department' ? (
        <Form.Item label="指定部门">
          <Select
            mode="multiple"
            value={config.departmentIds}
            options={MOCK_DEPARTMENT_OPTIONS}
            onChange={(value) => onConfigChange({ departmentIds: value })}
          />
        </Form.Item>
      ) : null}

      {config.initiatorScopeType === 'user' ? (
        <Form.Item label="指定用户">
          <Select
            mode="multiple"
            value={config.userIds}
            options={MOCK_USER_OPTIONS}
            onChange={(value) => onConfigChange({ userIds: value })}
          />
        </Form.Item>
      ) : null}

      <Form.Item label="发起说明">
        <Input.TextArea
          rows={3}
          value={config.remark}
          onChange={(event) => onConfigChange({ remark: event.target.value })}
          placeholder="可选"
        />
      </Form.Item>
    </Form>
  )
}

export default StarterForm
