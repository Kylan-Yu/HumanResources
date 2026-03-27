import React from 'react'
import { Form, Input, Select, Switch } from 'antd'
import { CC_TARGET_OPTIONS } from '../../constants'
import type { CcNodeConfig, CcWorkflowNode } from '../../types'
import { useWorkflowActorOptions } from '../../hooks/useWorkflowActorOptions'

interface CcFormProps {
  node: CcWorkflowNode
  onNameChange: (name: string) => void
  onConfigChange: (patch: Partial<CcNodeConfig>) => void
}

const CcForm: React.FC<CcFormProps> = ({ node, onNameChange, onConfigChange }) => {
  const { config } = node
  const { roleOptions, userOptions, positionOptions, loading } = useWorkflowActorOptions()

  return (
    <Form layout="vertical" className="workflow-property-form">
      <Form.Item label="节点名称">
        <Input value={node.name} onChange={(event) => onNameChange(event.target.value)} />
      </Form.Item>

      <Form.Item label="抄送对象类型">
        <Select
          value={config.targetType}
          options={CC_TARGET_OPTIONS}
          onChange={(value) =>
            onConfigChange({
              targetType: value,
              roleIds: [],
              positionIds: [],
              userIds: []
            })
          }
        />
      </Form.Item>

      {config.targetType === 'role' ? (
        <Form.Item label="抄送角色">
          <Select
            mode="multiple"
            value={config.roleIds}
            options={roleOptions}
            loading={loading}
            onChange={(value) => onConfigChange({ roleIds: value })}
          />
        </Form.Item>
      ) : null}

      {config.targetType === 'position' ? (
        <Form.Item label="抄送岗位">
          <Select
            mode="multiple"
            value={config.positionIds}
            options={positionOptions}
            loading={loading}
            onChange={(value) => onConfigChange({ positionIds: value })}
          />
        </Form.Item>
      ) : null}

      {config.targetType === 'user' ? (
        <Form.Item label="抄送用户">
          <Select
            mode="multiple"
            value={config.userIds}
            options={userOptions}
            loading={loading}
            onChange={(value) => onConfigChange({ userIds: value })}
          />
        </Form.Item>
      ) : null}

      <Form.Item label="是否允许查看全部审批意见" valuePropName="checked">
        <Switch
          checked={config.canViewAllComments}
          onChange={(checked) => onConfigChange({ canViewAllComments: checked })}
        />
      </Form.Item>

      <Form.Item label="抄送说明">
        <Input.TextArea
          rows={3}
          value={config.remark}
          onChange={(event) => onConfigChange({ remark: event.target.value })}
        />
      </Form.Item>
    </Form>
  )
}

export default CcForm
