import React from 'react'
import { DeleteOutlined } from '@ant-design/icons'
import { Handle, Position } from '@xyflow/react'
import type { NodeProps } from '@xyflow/react'
import { Tooltip } from 'antd'
import { NODE_COLOR_MAP } from '../../constants'
import type { WorkflowNodeRenderData } from '../../types'
import AddNodeMenu from '../AddNodeMenu'

const WorkflowNodeCard: React.FC<NodeProps> = ({ id, data, selected }) => {
  const nodeData = data as WorkflowNodeRenderData
  const color = NODE_COLOR_MAP[nodeData.nodeType]
  const className = `workflow-node-card ${selected ? 'is-selected' : ''} ${
    !selected && nodeData.groupSelected ? 'is-group-selected' : ''
  } is-${nodeData.nodeType}`

  return (
    <div
      className={className}
      style={{ '--node-accent-color': color } as React.CSSProperties}
    >
      {nodeData.nodeType !== 'starter' && (
        <Handle type="target" position={Position.Top} className="workflow-node-handle" />
      )}

      <div className="workflow-node-card-header">
        <span>{nodeData.label}</span>
        {nodeData.deletable && nodeData.onDelete ? (
          <Tooltip title="删除节点">
            <button
              type="button"
              className="workflow-node-delete-btn"
              onClick={(event) => {
                event.preventDefault()
                event.stopPropagation()
                nodeData.onDelete?.(id)
              }}
            >
              <DeleteOutlined />
            </button>
          </Tooltip>
        ) : null}
      </div>

      <div className="workflow-node-card-title">{nodeData.title}</div>
      <div className="workflow-node-card-summary">
        {nodeData.summary.map((text, index) => (
          <div key={`${id}_summary_${index}`} className="workflow-node-card-summary-line">
            {text}
          </div>
        ))}
      </div>

      {nodeData.appendable && nodeData.onAppendNode ? (
        <div className="workflow-node-card-bottom-add nodrag nopan">
          <AddNodeMenu
            onSelect={(nodeType) => {
              nodeData.onAppendNode?.(id, nodeType)
            }}
          />
        </div>
      ) : null}

      {nodeData.nodeType !== 'end' && (
        <Handle type="source" position={Position.Bottom} className="workflow-node-handle" />
      )}
    </div>
  )
}

export default WorkflowNodeCard

