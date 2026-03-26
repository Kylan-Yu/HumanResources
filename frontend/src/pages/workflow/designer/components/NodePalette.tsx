import React from 'react'
import { NODE_COLOR_MAP, NODE_PALETTE_ITEMS } from '../constants'
import type { InsertableNodeType } from '../types'

interface NodePaletteProps {
  onInsertNode: (nodeType: InsertableNodeType) => void
}

const paletteColorMap: Record<InsertableNodeType, string> = {
  approval: NODE_COLOR_MAP.approval,
  cc: NODE_COLOR_MAP.cc,
  condition: NODE_COLOR_MAP.condition,
  parallel: NODE_COLOR_MAP.parallel_fork
}

const NodePalette: React.FC<NodePaletteProps> = ({ onInsertNode }) => {
  return (
    <aside className="workflow-node-palette">
      <div className="workflow-side-title">节点面板</div>
      <div className="workflow-node-palette-list">
        {NODE_PALETTE_ITEMS.map((item) => (
          <button
            type="button"
            key={item.type}
            className="workflow-node-palette-item"
            onClick={() => onInsertNode(item.type)}
          >
            <div className="workflow-node-palette-item-head">
              <span
                className="workflow-node-palette-dot"
                style={{ backgroundColor: paletteColorMap[item.type] }}
              />
              <span>{item.title}</span>
            </div>
            <div className="workflow-node-palette-item-desc">{item.description}</div>
          </button>
        ))}
      </div>
    </aside>
  )
}

export default NodePalette
