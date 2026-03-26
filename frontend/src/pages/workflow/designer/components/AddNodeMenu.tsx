import React from 'react'
import { PlusOutlined } from '@ant-design/icons'
import { Dropdown } from 'antd'
import type { MenuProps } from 'antd'
import type { InsertableNodeType } from '../types'

interface AddNodeMenuProps {
  onSelect: (nodeType: InsertableNodeType) => void
}

const insertMenuItems: Array<{ key: InsertableNodeType; label: string }> = [
  { key: 'approval', label: '审批人串行' },
  { key: 'parallel', label: '审批人并行' },
  { key: 'cc', label: '抄送人' },
  { key: 'condition', label: '条件分支' }
]

const AddNodeMenu: React.FC<AddNodeMenuProps> = ({ onSelect }) => {
  const menu: MenuProps = {
    items: insertMenuItems.map((item) => ({ key: item.key, label: item.label })),
    onClick: ({ key, domEvent }) => {
      domEvent.stopPropagation()
      onSelect(key as InsertableNodeType)
    }
  }

  return (
    <Dropdown menu={menu} trigger={['click']}>
      <button
        type="button"
        className="workflow-add-node-btn"
        onClick={(event) => {
          event.preventDefault()
          event.stopPropagation()
        }}
      >
        <PlusOutlined />
      </button>
    </Dropdown>
  )
}

export default AddNodeMenu
