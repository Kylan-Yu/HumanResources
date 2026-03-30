import React from 'react'
import { Form, Input, InputNumber, Select, Switch } from 'antd'
import {
  APPROVAL_MODE_OPTIONS,
  ASSIGNEE_TYPE_OPTIONS,
  TIMEOUT_ACTION_OPTIONS
} from '../../constants'
import type { ApprovalNodeConfig, ApprovalWorkflowNode } from '../../types'
import { useWorkflowActorOptions } from '../../hooks/useWorkflowActorOptions'

interface ApprovalFormProps {
  node: ApprovalWorkflowNode
  onNameChange: (name: string) => void
  onConfigChange: (patch: Partial<ApprovalNodeConfig>) => void
}

const ApprovalForm: React.FC<ApprovalFormProps> = ({ node, onNameChange, onConfigChange }) => {
  const { config } = node
  const { roleOptions, userOptions, positionOptions, loading } = useWorkflowActorOptions()

  return (
    <Form layout="vertical" className="workflow-property-form">
      <Form.Item label="节点名称">
        <Input value={node.name} onChange={(event) => onNameChange(event.target.value)} />
      </Form.Item>

      <Form.Item label="审批方式">
        <Select
          value={config.approvalMode}
          options={APPROVAL_MODE_OPTIONS}
          onChange={(value) => onConfigChange({ approvalMode: value })}
        />
      </Form.Item>

      <Form.Item label="审批人来源">
        <Select
          value={config.assigneeType}
          options={ASSIGNEE_TYPE_OPTIONS}
          onChange={(value) =>
            onConfigChange({
              assigneeType: value,
              roleIds: [],
              positionIds: [],
              userIds: []
            })
          }
        />
      </Form.Item>

      {config.assigneeType === 'level_leader' ? (
        <Form.Item label="上级层级 N">
          <InputNumber
            min={1}
            max={10}
            style={{ width: '100%' }}
            value={config.leaderLevel}
            onChange={(value) => onConfigChange({ leaderLevel: Number(value || 1) })}
          />
        </Form.Item>
      ) : null}

      {config.assigneeType === 'role' ? (
        <Form.Item label="指定角色（多选）">
          <Select
            mode="multiple"
            value={config.roleIds}
            options={roleOptions}
            loading={loading}
            onChange={(value) => onConfigChange({ roleIds: value })}
          />
        </Form.Item>
      ) : null}

      {config.assigneeType === 'position' ? (
        <Form.Item label="指定岗位（多选）">
          <Select
            mode="multiple"
            value={config.positionIds}
            options={positionOptions}
            loading={loading}
            onChange={(value) => onConfigChange({ positionIds: value })}
          />
        </Form.Item>
      ) : null}

      {config.assigneeType === 'user' ? (
        <Form.Item label="指定用户（多选）">
          <Select
            mode="multiple"
            value={config.userIds}
            options={userOptions}
            loading={loading}
            onChange={(value) => onConfigChange({ userIds: value })}
          />
        </Form.Item>
      ) : null}

      <Form.Item label="是否必经" valuePropName="checked">
        <Switch
          checked={config.required}
          onChange={(checked) => onConfigChange({ required: checked })}
        />
      </Form.Item>

      <Form.Item label="审批时限（小时）">
        <InputNumber
          min={1}
          max={720}
          style={{ width: '100%' }}
          value={config.timeoutHours}
          onChange={(value) => onConfigChange({ timeoutHours: value ? Number(value) : undefined })}
          placeholder="可选"
        />
      </Form.Item>

      <Form.Item label="超时处理方式">
        <Select
          value={config.timeoutAction}
          options={TIMEOUT_ACTION_OPTIONS}
          onChange={(value) => onConfigChange({ timeoutAction: value })}
        />
      </Form.Item>

      <Form.Item label="审批备注说明">
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

export default ApprovalForm
