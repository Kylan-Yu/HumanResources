import React from 'react'
import { Form, Input, InputNumber, Select } from 'antd'
import {
  PARALLEL_FAIL_STRATEGY_OPTIONS,
  PARALLEL_JOIN_STRATEGY_OPTIONS,
  PARALLEL_TIMEOUT_STRATEGY_OPTIONS
} from '../../constants'
import type { ParallelJoinNodeConfig, ParallelJoinWorkflowNode } from '../../types'

interface ParallelJoinFormProps {
  node: ParallelJoinWorkflowNode
  onNameChange: (name: string) => void
  onConfigChange: (patch: Partial<ParallelJoinNodeConfig>) => void
}

const ParallelJoinForm: React.FC<ParallelJoinFormProps> = ({ node, onNameChange, onConfigChange }) => {
  const { strategy, requiredCount, failStrategy, timeoutStrategy } = node.config

  return (
    <Form layout="vertical" className="workflow-property-form">
      <Form.Item label="汇合节点名称">
        <Input value={node.name} onChange={(event) => onNameChange(event.target.value)} />
      </Form.Item>

      <Form.Item label="汇合策略">
        <Select
          value={strategy}
          options={PARALLEL_JOIN_STRATEGY_OPTIONS}
          onChange={(value) => onConfigChange({ strategy: value })}
        />
      </Form.Item>

      {strategy === 'n_of_m' ? (
        <Form.Item label="至少完成分支数 N">
          <InputNumber
            min={1}
            value={requiredCount}
            style={{ width: '100%' }}
            onChange={(value) => onConfigChange({ requiredCount: Number(value || 1) })}
          />
        </Form.Item>
      ) : null}

      <Form.Item label="失败策略">
        <Select
          value={failStrategy}
          options={PARALLEL_FAIL_STRATEGY_OPTIONS}
          onChange={(value) => onConfigChange({ failStrategy: value })}
        />
      </Form.Item>

      <Form.Item label="超时策略">
        <Select
          value={timeoutStrategy}
          options={PARALLEL_TIMEOUT_STRATEGY_OPTIONS}
          onChange={(value) => onConfigChange({ timeoutStrategy: value })}
        />
      </Form.Item>
    </Form>
  )
}

export default ParallelJoinForm
