import React from 'react'
import { Form, Input, Tag } from 'antd'
import type { ParallelBranch } from '../../types'

interface ParallelBranchFormProps {
  branch: ParallelBranch
  onChange: (patch: Partial<ParallelBranch>) => void
}

const ParallelBranchForm: React.FC<ParallelBranchFormProps> = ({ branch, onChange }) => {
  return (
    <Form layout="vertical" className="workflow-property-form">
      <Form.Item label="分支名称">
        <Input value={branch.name} onChange={(event) => onChange({ name: event.target.value })} />
      </Form.Item>

      <Form.Item label="分支说明">
        <Input.TextArea
          rows={3}
          value={branch.remark}
          onChange={(event) => onChange({ remark: event.target.value })}
        />
      </Form.Item>

      <Form.Item label="分支摘要">
        <>
          <Tag color="blue">当前分支已挂载 {(branch.childNodes || []).length} 个子节点</Tag>
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

export default ParallelBranchForm
