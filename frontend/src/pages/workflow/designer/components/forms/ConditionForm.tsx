import React, { useMemo, useState } from 'react'
import { Button, Form, Input, Select, Space, Tag } from 'antd'
import { DeleteOutlined, MenuOutlined, PlusOutlined } from '@ant-design/icons'
import { CONDITION_SORT_STRATEGY_OPTIONS } from '../../constants'
import type { ConditionBranch, ConditionNodeConfig, ConditionWorkflowNode } from '../../types'

interface ConditionFormProps {
  node: ConditionWorkflowNode
  onNameChange: (name: string) => void
  onConfigChange: (patch: Partial<ConditionNodeConfig>) => void
}

const createBranchId = (): string => `branch_${Date.now()}_${Math.floor(Math.random() * 1000)}`

const normalizePriority = (branches: ConditionBranch[]): ConditionBranch[] => {
  return branches.map((branch, index) => ({
    ...branch,
    priority: index + 1
  }))
}

const ConditionForm: React.FC<ConditionFormProps> = ({ node, onNameChange, onConfigChange }) => {
  const [draggingBranchId, setDraggingBranchId] = useState<string | null>(null)
  const { branches, defaultBranchId, expressionRemark, sortStrategy, conditionField } = node.config

  const sortedBranches = useMemo(() => {
    return [...branches].sort((a, b) => a.priority - b.priority)
  }, [branches])

  const commitBranches = (next: ConditionBranch[], nextDefaultId = defaultBranchId) => {
    const normalized = normalizePriority(next)
    const safeDefaultId = nextDefaultId && normalized.some((item) => item.id === nextDefaultId)
      ? nextDefaultId
      : normalized[0]?.id

    onConfigChange({
      branches: normalized.map((branch) => ({
        ...branch,
        isDefault: branch.id === safeDefaultId
      })),
      defaultBranchId: safeDefaultId
    })
  }

  const addBranch = () => {
    const nextBranch: ConditionBranch = {
      id: createBranchId(),
      nodeId: '',
      name: `分支 ${sortedBranches.length + 1}`,
      expression: '',
      priority: sortedBranches.length + 1,
      isDefault: false,
      remark: '',
      childNodeIds: [],
      childNodes: [],
      collapsed: true
    }

    commitBranches([...sortedBranches, nextBranch])
  }

  const removeBranch = (branchId: string) => {
    if (sortedBranches.length <= 2) {
      return
    }

    const next = sortedBranches.filter((item) => item.id !== branchId)
    commitBranches(next, defaultBranchId === branchId ? next[0]?.id : defaultBranchId)
  }

  const moveBranch = (dragId: string, targetId: string) => {
    if (dragId === targetId) {
      return
    }

    const sourceIndex = sortedBranches.findIndex((item) => item.id === dragId)
    const targetIndex = sortedBranches.findIndex((item) => item.id === targetId)
    if (sourceIndex < 0 || targetIndex < 0) {
      return
    }

    const next = [...sortedBranches]
    const [moved] = next.splice(sourceIndex, 1)
    next.splice(targetIndex, 0, moved)
    commitBranches(next)
  }

  return (
    <Form layout="vertical" className="workflow-property-form">
      <Form.Item label="节点名称">
        <Input value={node.name} onChange={(event) => onNameChange(event.target.value)} />
      </Form.Item>

      <Form.Item label="条件字段摘要">
        <Input
          value={conditionField}
          onChange={(event) => onConfigChange({ conditionField: event.target.value })}
          placeholder="例如：days / amount"
        />
      </Form.Item>

      <Form.Item label="分支排序策略">
        <Select
          value={sortStrategy}
          options={CONDITION_SORT_STRATEGY_OPTIONS}
          onChange={(value) => onConfigChange({ sortStrategy: value })}
        />
      </Form.Item>

      <Form.Item label="默认分支">
        <Select
          value={defaultBranchId}
          options={sortedBranches.map((branch) => ({ value: branch.id, label: branch.name }))}
          onChange={(value) => {
            commitBranches(sortedBranches, value)
          }}
        />
      </Form.Item>

      <Form.Item label="分支列表（拖拽排序）">
        <Space direction="vertical" style={{ width: '100%' }} size={8}>
          {sortedBranches.map((branch) => (
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
                <span>{branch.name || '未命名分支'}</span>
                <Tag>优先级 {branch.priority}</Tag>
                {defaultBranchId === branch.id ? <Tag color="success">默认</Tag> : null}
              </div>
              <div className="workflow-branch-list-row-right">
                <Button
                  icon={<DeleteOutlined />}
                  danger
                  type="text"
                  onClick={() => removeBranch(branch.id)}
                  disabled={sortedBranches.length <= 2}
                />
              </div>
            </div>
          ))}

          <Button icon={<PlusOutlined />} block onClick={addBranch}>
            新增分支
          </Button>
        </Space>
      </Form.Item>

      <Form.Item label="表达式说明">
        <Input.TextArea
          rows={3}
          value={expressionRemark}
          onChange={(event) => onConfigChange({ expressionRemark: event.target.value })}
          placeholder="可选"
        />
      </Form.Item>
    </Form>
  )
}

export default ConditionForm
