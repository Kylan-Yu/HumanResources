import React, { useMemo, useState } from 'react'
import { Button, Form, Input, Space, Tag } from 'antd'
import { DeleteOutlined, MenuOutlined, PlusOutlined } from '@ant-design/icons'
import type { ParallelBranch, ParallelForkNodeConfig, ParallelForkWorkflowNode } from '../../types'

interface ParallelFormProps {
  node: ParallelForkWorkflowNode
  onNameChange: (name: string) => void
  onConfigChange: (patch: Partial<ParallelForkNodeConfig>) => void
}

const createParallelBranchId = (): string => `parallel_branch_${Date.now()}_${Math.floor(Math.random() * 1000)}`

const normalizeBranchOrder = (branches: ParallelBranch[]): ParallelBranch[] => {
  return branches.map((branch, index) => ({
    ...branch,
    order: index + 1
  }))
}

const ParallelForm: React.FC<ParallelFormProps> = ({ node, onNameChange, onConfigChange }) => {
  const [draggingBranchId, setDraggingBranchId] = useState<string | null>(null)
  const branches = useMemo(() => [...node.config.branches].sort((a, b) => a.order - b.order), [node.config.branches])

  const commitBranches = (next: ParallelBranch[]) => {
    onConfigChange({ branches: normalizeBranchOrder(next) })
  }

  const addBranch = () => {
    const nextBranch: ParallelBranch = {
      id: createParallelBranchId(),
      nodeId: '',
      name: `并行分支${branches.length + 1}`,
      order: branches.length + 1,
      remark: '',
      childNodeIds: [],
      childNodes: [],
      collapsed: true
    }

    commitBranches([...branches, nextBranch])
  }

  const removeBranch = (branchId: string) => {
    if (branches.length <= 2) {
      return
    }

    commitBranches(branches.filter((item) => item.id !== branchId))
  }

  const moveBranch = (dragId: string, targetId: string) => {
    if (dragId === targetId) {
      return
    }

    const sourceIndex = branches.findIndex((item) => item.id === dragId)
    const targetIndex = branches.findIndex((item) => item.id === targetId)
    if (sourceIndex < 0 || targetIndex < 0) {
      return
    }

    const next = [...branches]
    const [moved] = next.splice(sourceIndex, 1)
    next.splice(targetIndex, 0, moved)
    commitBranches(next)
  }

  return (
    <Form layout="vertical" className="workflow-property-form">
      <Form.Item label="并行组名称">
        <Input value={node.name} onChange={(event) => onNameChange(event.target.value)} />
      </Form.Item>

      <Form.Item label="分支列表（拖拽排序）">
        <Space direction="vertical" style={{ width: '100%' }} size={8}>
          {branches.map((branch) => (
            <div
              key={branch.id}
              className="workflow-branch-list-row"
              draggable
              onDragStart={() => setDraggingBranchId(branch.id)}
              onDragOver={(event) => event.preventDefault()}
              onDrop={() => {
                if (draggingBranchId) {
                  moveBranch(draggingBranchId, branch.id)
                }
                setDraggingBranchId(null)
              }}
            >
              <div className="workflow-branch-list-row-left">
                <MenuOutlined />
                <span>{branch.name || '未命名并行分支'}</span>
                <Tag>顺序 {branch.order}</Tag>
                <Tag color="blue">节点 {branch.childNodeIds.length}</Tag>
              </div>
              <div className="workflow-branch-list-row-right">
                <Button
                  icon={<DeleteOutlined />}
                  danger
                  type="text"
                  onClick={() => removeBranch(branch.id)}
                  disabled={branches.length <= 2}
                />
              </div>
            </div>
          ))}

          <Button icon={<PlusOutlined />} block onClick={addBranch}>
            新增并行分支
          </Button>
        </Space>
      </Form.Item>
    </Form>
  )
}

export default ParallelForm
