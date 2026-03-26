import React from 'react'
import { Button, Form, Input, InputNumber, Space, Tag } from 'antd'
import type { ConditionBranch } from '../../types'

interface ConditionBranchFormProps {
  branch: ConditionBranch
  isDefault: boolean
  onChange: (patch: Partial<ConditionBranch>) => void
  onSetDefault: () => void
}

const ConditionBranchForm: React.FC<ConditionBranchFormProps> = ({
  branch,
  isDefault,
  onChange,
  onSetDefault
}) => {
  return (
    <Form layout="vertical" className="workflow-property-form">
      <Form.Item label="分支名称">
        <Input value={branch.name} onChange={(event) => onChange({ name: event.target.value })} />
      </Form.Item>

      <Form.Item label="条件表达式">
        <Input
          value={branch.expression}
          onChange={(event) => onChange({ expression: event.target.value })}
          placeholder="例如：days > 3"
        />
      </Form.Item>

      <Form.Item label="优先级">
        <InputNumber
          min={1}
          value={branch.priority}
          style={{ width: '100%' }}
          onChange={(value) => onChange({ priority: Number(value || 1) })}
        />
      </Form.Item>

      <Form.Item label="分支说明">
        <Input.TextArea
          rows={3}
          value={branch.remark}
          onChange={(event) => onChange({ remark: event.target.value })}
          placeholder="可选"
        />
      </Form.Item>

      <Form.Item label="分支状态">
        <Space>
          {isDefault ? <Tag color="success">默认分支</Tag> : <Tag>普通分支</Tag>}
          {!isDefault ? (
            <Button type="primary" ghost onClick={onSetDefault}>
              设为默认分支
            </Button>
          ) : null}
        </Space>
      </Form.Item>

      <Form.Item label="分支内节点">
        <>
          <Tag color="blue">当前已挂载 {(branch.childNodes || []).length} 个子节点</Tag>
          {(branch.childNodes || []).map((item) => (
            <Tag key={item.id} style={{ marginTop: 8 }}>
              {item.name}
            </Tag>
          ))}
        </>
      </Form.Item>
    </Form>
  )
}

export default ConditionBranchForm
