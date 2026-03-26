import React, { forwardRef, useEffect, useImperativeHandle, useRef } from 'react'
import {
  Background,
  MarkerType,
  ReactFlow,
  type EdgeChange,
  type Node,
  type NodeChange,
  type ReactFlowInstance,
  type Viewport
} from '@xyflow/react'
import '@xyflow/react/dist/style.css'
import type { WorkflowFlowEdge, WorkflowFlowNode } from '../types'
import WorkflowEdge from './edges/WorkflowEdge'
import ApprovalNode from './nodes/ApprovalNode'
import BranchPreviewNode from './nodes/BranchPreviewNode'
import CcNode from './nodes/CcNode'
import ConditionJoinNode from './nodes/ConditionJoinNode'
import ConditionNode from './nodes/ConditionNode'
import EndNode from './nodes/EndNode'
import ParallelForkNode from './nodes/ParallelForkNode'
import ParallelJoinNode from './nodes/ParallelJoinNode'
import StarterNode from './nodes/StarterNode'

export interface WorkflowCanvasRef {
  zoomIn: () => void
  zoomOut: () => void
  fitView: () => void
  getZoom: () => number
  getViewport: () => Viewport
}

interface WorkflowCanvasProps {
  nodes: WorkflowFlowNode[]
  edges: WorkflowFlowEdge[]
  initialViewport?: Viewport
  onSelectNode: (nodeId: string | null) => void
  onZoomChange: (zoom: number) => void
  onViewportChange?: (viewport: Viewport) => void
  onNodesChange?: (changes: NodeChange[]) => void
  onEdgesChange?: (changes: EdgeChange[]) => void
  onNodeDragStart?: () => void
  onNodeDragStop?: () => void
}

const nodeTypes = {
  starter: StarterNode,
  approval: ApprovalNode,
  cc: CcNode,
  condition: ConditionNode,
  condition_branch: BranchPreviewNode,
  condition_join: ConditionJoinNode,
  parallel_fork: ParallelForkNode,
  parallel_branch: BranchPreviewNode,
  parallel_join: ParallelJoinNode,
  end: EndNode
}

const edgeTypes = {
  workflowEdge: WorkflowEdge
}

const isSelectableNode = (_node: Node): boolean => true

const WorkflowCanvas = forwardRef<WorkflowCanvasRef, WorkflowCanvasProps>(
  (
    {
      nodes,
      edges,
      initialViewport,
      onSelectNode,
      onZoomChange,
      onViewportChange,
      onNodesChange,
      onEdgesChange,
      onNodeDragStart,
      onNodeDragStop
    },
    ref
  ) => {
    const flowInstanceRef = useRef<ReactFlowInstance | null>(null)
    const hasFittedOnceRef = useRef(false)

    useImperativeHandle(
      ref,
      () => ({
        zoomIn: () => flowInstanceRef.current?.zoomIn({ duration: 200 }),
        zoomOut: () => flowInstanceRef.current?.zoomOut({ duration: 200 }),
        fitView: () => flowInstanceRef.current?.fitView({ duration: 240, padding: 0.2 }),
        getZoom: () => flowInstanceRef.current?.getZoom() ?? 1,
        getViewport: () => flowInstanceRef.current?.getViewport() ?? { x: 0, y: 0, zoom: 1 }
      }),
      []
    )

    useEffect(() => {
      if (!flowInstanceRef.current || hasFittedOnceRef.current || initialViewport) {
        return
      }

      flowInstanceRef.current.fitView({ duration: 240, padding: 0.2 })
      hasFittedOnceRef.current = true
    }, [initialViewport, nodes.length])

    useEffect(() => {
      if (!flowInstanceRef.current || !initialViewport) {
        return
      }

      flowInstanceRef.current.setViewport(initialViewport, { duration: 0 })
      hasFittedOnceRef.current = true
    }, [initialViewport?.x, initialViewport?.y, initialViewport?.zoom])

    const markerEnd = {
      type: MarkerType.ArrowClosed,
      color: '#6e89ad',
      width: 18,
      height: 18
    }

    return (
      <div className="workflow-canvas-wrap">
        <ReactFlow
          nodes={nodes as unknown as Node[]}
          edges={edges.map((edge) => ({
            ...edge,
            markerEnd
          }))}
          nodeTypes={nodeTypes}
          edgeTypes={edgeTypes}
          fitView
          nodesDraggable
          nodesConnectable={false}
          elementsSelectable
          minZoom={0.5}
          maxZoom={1.8}
          colorMode="dark"
          onNodesChange={(changes) => {
            onNodesChange?.(changes)
          }}
          onEdgesChange={(changes) => {
            onEdgesChange?.(changes)
          }}
          onNodeDragStart={() => {
            onNodeDragStart?.()
          }}
          onNodeDragStop={() => {
            onNodeDragStop?.()
          }}
          onNodeClick={(_, node) => {
            onSelectNode(isSelectableNode(node) ? node.id : null)
          }}
          onPaneClick={() => onSelectNode(null)}
          onInit={(instance) => {
            flowInstanceRef.current = instance
            onZoomChange(instance.getZoom())
            if (initialViewport) {
              instance.setViewport(initialViewport, { duration: 0 })
              hasFittedOnceRef.current = true
            }
          }}
          onMove={(_, viewport: Viewport) => {
            onZoomChange(viewport.zoom)
            onViewportChange?.(viewport)
          }}
          proOptions={{ hideAttribution: true }}
          defaultViewport={initialViewport || { x: 0, y: 0, zoom: 1 }}
        >
          <Background color="#2b3950" gap={24} size={1} />
        </ReactFlow>
      </div>
    )
  }
)

WorkflowCanvas.displayName = 'WorkflowCanvas'

export default WorkflowCanvas
