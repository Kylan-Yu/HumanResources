import React from 'react'
import { DownOutlined, RightOutlined } from '@ant-design/icons'
import { Handle, Position } from '@xyflow/react'
import type { NodeProps } from '@xyflow/react'
import AddNodeMenu from '../AddNodeMenu'
import type { WorkflowNodeRenderData } from '../../types'

const BranchPreviewNode: React.FC<NodeProps> = ({ id, data, selected }) => {
  const nodeData = data as WorkflowNodeRenderData
  const collapsed = nodeData.collapsed !== false
  const summary = nodeData.summary || []
  const mainSummary = summary[0] || ''
  const extraSummary = summary.slice(1)

  return (
    <div className={`workflow-branch-card ${selected ? 'is-selected' : ''} ${nodeData.groupSelected ? 'is-group-selected' : ''}`}>
      <Handle type="target" position={Position.Top} className="workflow-node-handle" />

      <div className="workflow-branch-card-head">
        <span className="workflow-branch-card-title" title={nodeData.title}>
          {nodeData.title}
        </span>
        <div className="workflow-branch-card-head-right">
          {nodeData.branchIsDefault ? <span className="workflow-branch-badge">默认</span> : null}
          <button
            type="button"
            className="workflow-branch-collapse-btn"
            onClick={(event) => {
              event.preventDefault()
              event.stopPropagation()
              nodeData.onToggleCollapse?.(id)
            }}
          >
            {collapsed ? <RightOutlined /> : <DownOutlined />}
          </button>
        </div>
      </div>

      <div className="workflow-branch-card-meta">
        <div title={nodeData.branchExpression || mainSummary}>
          {nodeData.branchExpression ? `条件：${nodeData.branchExpression}` : mainSummary}
        </div>
        {typeof nodeData.branchPriority === 'number' ? <div>序号：{nodeData.branchPriority}</div> : null}
        {!collapsed && extraSummary.map((line, index) => (
          <div key={`${id}_extra_${index}`} title={line}>
            {line}
          </div>
        ))}
      </div>

      <div className="workflow-branch-card-footer">
        <div className="workflow-branch-card-add nodrag nopan">
          {nodeData.onAppendNode ? (
            <AddNodeMenu
              onSelect={(type) => {
                nodeData.onAppendNode?.(id, type)
              }}
            />
          ) : null}
        </div>
      </div>

      <Handle type="source" position={Position.Bottom} className="workflow-node-handle" />
    </div>
  )
}

export default BranchPreviewNode
