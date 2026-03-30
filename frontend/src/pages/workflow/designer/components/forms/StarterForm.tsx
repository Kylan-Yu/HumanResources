import React from 'react'
import { Form, Input, Select } from 'antd'
import { INITIATOR_SCOPE_OPTIONS } from '../../constants'
import type { StarterNodeConfig, StarterWorkflowNode } from '../../types'
import { useWorkflowActorOptions } from '../../hooks/useWorkflowActorOptions'

interface StarterFormProps {
  node: StarterWorkflowNode
  onNameChange: (name: string) => void
  onConfigChange: (patch: Partial<StarterNodeConfig>) => void
}

const StarterForm: React.FC<StarterFormProps> = ({ node, onNameChange, onConfigChange }) => {
  const { config } = node
  const { roleOptions, userOptions, deptOptions, loading } = useWorkflowActorOptions()

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
            options={roleOptions}
            loading={loading}
            onChange={(value) => onConfigChange({ roleIds: value })}
          />
        </Form.Item>
      ) : null}

      {config.initiatorScopeType === 'department' ? (
        <Form.Item label="指定部门">
          <Select
            mode="multiple"
            value={config.departmentIds}
            options={deptOptions}
            loading={loading}
            onChange={(value) => onConfigChange({ departmentIds: value })}
          />
        </Form.Item>
      ) : null}

      {config.initiatorScopeType === 'user' ? (
        <Form.Item label="指定用户">
          <Select
            mode="multiple"
            value={config.userIds}
            options={userOptions}
            loading={loading}
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
