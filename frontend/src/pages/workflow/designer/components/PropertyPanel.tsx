import React from 'react'
import { Button, Empty, Typography } from 'antd'
import { NODE_LABEL_MAP } from '../constants'
import type {
  ApprovalNodeConfig,
  ApprovalWorkflowNode,
  CcNodeConfig,
  CcWorkflowNode,
  ConditionBranch,
  ConditionBranchWorkflowNode,
  ConditionJoinNodeConfig,
  ConditionJoinWorkflowNode,
  ConditionNodeConfig,
  ConditionWorkflowNode,
  EndNodeConfig,
  EndWorkflowNode,
  ParallelBranch,
  ParallelBranchWorkflowNode,
  ParallelForkNodeConfig,
  ParallelForkWorkflowNode,
  ParallelJoinNodeConfig,
  ParallelJoinWorkflowNode,
  StarterNodeConfig,
  StarterWorkflowNode,
  WorkflowNodeModel
} from '../types'
import ApprovalForm from './forms/ApprovalForm'
import CcForm from './forms/CcForm'
import ConditionBranchForm from './forms/ConditionBranchForm'
import ConditionForm from './forms/ConditionForm'
import ConditionJoinForm from './forms/ConditionJoinForm'
import EndForm from './forms/EndForm'
import ParallelBranchForm from './forms/ParallelBranchForm'
import ParallelForm from './forms/ParallelForm'
import ParallelJoinForm from './forms/ParallelJoinForm'
import StarterForm from './forms/StarterForm'

interface PropertyPanelProps {
  selectedNode: WorkflowNodeModel | null
  onNodeNameChange: (nodeId: string, name: string) => void
  onNodeConfigChange: (nodeId: string, patch: Record<string, unknown>) => void
  onDeleteNode: (nodeId: string) => void
  onConditionBranchChange: (ownerNodeId: string, branchId: string, patch: Partial<ConditionBranch>) => void
  onConditionSetDefaultBranch: (ownerNodeId: string, branchId: string) => void
  onParallelBranchChange: (ownerNodeId: string, branchId: string, patch: Partial<ParallelBranch>) => void
}

const { Text } = Typography

const PropertyPanel: React.FC<PropertyPanelProps> = ({
  selectedNode,
  onNodeNameChange,
  onNodeConfigChange,
  onDeleteNode,
  onConditionBranchChange,
  onConditionSetDefaultBranch,
  onParallelBranchChange
}) => {
  if (!selectedNode) {
    return (
      <aside className="workflow-property-panel">
        <Empty description="点击画布中的节点进行配置" image={Empty.PRESENTED_IMAGE_SIMPLE} />
      </aside>
    )
  }

  const renderForm = (): React.ReactNode => {
    if (selectedNode.type === 'starter') {
      return (
        <StarterForm
          node={selectedNode as StarterWorkflowNode}
          onNameChange={(name) => onNodeNameChange(selectedNode.id, name)}
          onConfigChange={(patch: Partial<StarterNodeConfig>) =>
            onNodeConfigChange(selectedNode.id, patch as Record<string, unknown>)
          }
        />
      )
    }

    if (selectedNode.type === 'approval') {
      return (
        <ApprovalForm
          node={selectedNode as ApprovalWorkflowNode}
          onNameChange={(name) => onNodeNameChange(selectedNode.id, name)}
          onConfigChange={(patch: Partial<ApprovalNodeConfig>) =>
            onNodeConfigChange(selectedNode.id, patch as Record<string, unknown>)
          }
        />
      )
    }

    if (selectedNode.type === 'cc') {
      return (
        <CcForm
          node={selectedNode as CcWorkflowNode}
          onNameChange={(name) => onNodeNameChange(selectedNode.id, name)}
          onConfigChange={(patch: Partial<CcNodeConfig>) =>
            onNodeConfigChange(selectedNode.id, patch as Record<string, unknown>)
          }
        />
      )
    }

    if (selectedNode.type === 'condition') {
      return (
        <ConditionForm
          node={selectedNode as ConditionWorkflowNode}
          onNameChange={(name) => onNodeNameChange(selectedNode.id, name)}
          onConfigChange={(patch: Partial<ConditionNodeConfig>) =>
            onNodeConfigChange(selectedNode.id, patch as Record<string, unknown>)
          }
        />
      )
    }

    if (selectedNode.type === 'condition_branch') {
      const branchNode = selectedNode as ConditionBranchWorkflowNode
      const branchEntity: ConditionBranch = {
        id: branchNode.config.branchId,
        nodeId: branchNode.id,
        name: branchNode.name,
        expression: branchNode.config.expression,
        priority: branchNode.config.priority,
        isDefault: branchNode.config.isDefault,
        remark: branchNode.config.remark,
        childNodeIds: branchNode.config.childNodeIds,
        childNodes: branchNode.config.childNodeIds.map((childId) => ({
          id: childId,
          type: 'approval',
          name: childId
        })),
        collapsed: branchNode.config.collapsed
      }

      return (
        <ConditionBranchForm
          branch={branchEntity}
          isDefault={branchNode.config.isDefault}
          onChange={(patch) => onConditionBranchChange(branchNode.config.gatewayId, branchNode.config.branchId, patch)}
          onSetDefault={() => onConditionSetDefaultBranch(branchNode.config.gatewayId, branchNode.config.branchId)}
        />
      )
    }

    if (selectedNode.type === 'condition_join') {
      return (
        <ConditionJoinForm
          node={selectedNode as ConditionJoinWorkflowNode}
          onNameChange={(name) => onNodeNameChange(selectedNode.id, name)}
          onConfigChange={(patch: Partial<ConditionJoinNodeConfig>) =>
            onNodeConfigChange(selectedNode.id, patch as Record<string, unknown>)
          }
        />
      )
    }

    if (selectedNode.type === 'parallel_fork') {
      return (
        <ParallelForm
          node={selectedNode as ParallelForkWorkflowNode}
          onNameChange={(name) => onNodeNameChange(selectedNode.id, name)}
          onConfigChange={(patch: Partial<ParallelForkNodeConfig>) =>
            onNodeConfigChange(selectedNode.id, patch as Record<string, unknown>)
          }
        />
      )
    }

    if (selectedNode.type === 'parallel_branch') {
      const branchNode = selectedNode as ParallelBranchWorkflowNode
      const branchEntity: ParallelBranch = {
        id: branchNode.config.branchId,
        nodeId: branchNode.id,
        name: branchNode.name,
        order: branchNode.config.order,
        remark: branchNode.config.remark,
        childNodeIds: branchNode.config.childNodeIds,
        childNodes: branchNode.config.childNodeIds.map((childId) => ({
          id: childId,
          type: 'approval',
          name: childId
        })),
        collapsed: branchNode.config.collapsed
      }

      return (
        <ParallelBranchForm
          branch={branchEntity}
          onChange={(patch) => onParallelBranchChange(branchNode.config.gatewayId, branchNode.config.branchId, patch)}
        />
      )
    }

    if (selectedNode.type === 'parallel_join') {
      return (
        <ParallelJoinForm
          node={selectedNode as ParallelJoinWorkflowNode}
          onNameChange={(name) => onNodeNameChange(selectedNode.id, name)}
          onConfigChange={(patch: Partial<ParallelJoinNodeConfig>) =>
            onNodeConfigChange(selectedNode.id, patch as Record<string, unknown>)
          }
        />
      )
    }

    return (
      <EndForm
        node={selectedNode as EndWorkflowNode}
        onNameChange={(name) => onNodeNameChange(selectedNode.id, name)}
        onConfigChange={(patch: Partial<EndNodeConfig>) =>
          onNodeConfigChange(selectedNode.id, patch as Record<string, unknown>)
        }
      />
    )
  }

  const canDelete = ![
    'starter',
    'end',
    'condition_join',
    'parallel_join',
    'condition_branch',
    'parallel_branch'
  ].includes(selectedNode.type)

  return (
    <aside className="workflow-property-panel">
      <div className="workflow-property-panel-header">
        <Text className="workflow-property-panel-title">{NODE_LABEL_MAP[selectedNode.type]}</Text>
        <Text type="secondary" className="workflow-property-panel-subtitle">
          ID: {selectedNode.id}
        </Text>
      </div>

      {renderForm()}

      {canDelete ? (
        <Button danger block onClick={() => onDeleteNode(selectedNode.id)}>
          删除当前节点
        </Button>
      ) : null}
    </aside>
  )
}

export default PropertyPanel
