import React, { useEffect, useMemo, useState } from 'react'
import { Breadcrumb, Button, Card, Empty, List, Modal, Popconfirm, Space, Tag, Typography, message } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import {
  getWorkflowTemplateDetail,
  getWorkflowTemplateVersionDetail,
  getWorkflowTemplateVersions,
  restoreWorkflowTemplateVersion
} from '../../designer/mockApi'
import WorkflowCanvas from '../../designer/components/WorkflowCanvas'
import { toReactFlowElements } from '../../designer/utils/flowData'
import type { WorkflowTemplateVersionSnapshot } from '../../designer/types'

const { Text } = Typography

const actionLabelMap: Record<string, { text: string; color: string }> = {
  save: { text: '保存', color: 'blue' },
  publish: { text: '发布', color: 'green' },
  restore: { text: '恢复', color: 'gold' }
}

const statusLabelMap: Record<string, { text: string; color: string }> = {
  draft: { text: '草稿', color: 'default' },
  published: { text: '已发布', color: 'success' },
  disabled: { text: '停用', color: 'warning' }
}

const WorkflowTemplateHistoryPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [templateName, setTemplateName] = useState('')
  const [versions, setVersions] = useState<WorkflowTemplateVersionSnapshot[]>([])
  const [loading, setLoading] = useState(false)
  const [previewOpen, setPreviewOpen] = useState(false)
  const [previewVersion, setPreviewVersion] = useState<WorkflowTemplateVersionSnapshot | null>(null)
  const [previewSelectedNodeId, setPreviewSelectedNodeId] = useState<string | null>(null)

  const load = async (templateId: string) => {
    setLoading(true)
    try {
      const [detail, history] = await Promise.all([
        getWorkflowTemplateDetail(templateId),
        getWorkflowTemplateVersions(templateId)
      ])
      setTemplateName(detail.templateName)
      setVersions(history)
    } catch (error) {
      message.error((error as Error).message || '加载历史失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    const templateId = String(id || '').trim()
    if (!templateId) {
      navigate('/workflow/templates', { replace: true })
      return
    }

    void load(templateId)
  }, [id, navigate])

  const previewElements = useMemo(() => {
    if (!previewVersion?.payload) {
      return { nodes: [], edges: [] }
    }

    return toReactFlowElements({
      modelNodes: previewVersion.payload.nodes,
      modelEdges: previewVersion.payload.edges,
      selectedNodeId: previewSelectedNodeId
    })
  }, [previewSelectedNodeId, previewVersion])

  const handlePreview = async (item: WorkflowTemplateVersionSnapshot) => {
    const templateId = String(id || '').trim()
    if (!templateId) {
      return
    }

    try {
      const detail = await getWorkflowTemplateVersionDetail(templateId, item.version)
      setPreviewVersion(detail)
      setPreviewSelectedNodeId(detail.payload.nodes[0]?.id || null)
      setPreviewOpen(true)
    } catch (error) {
      message.error((error as Error).message || '加载版本快照失败')
    }
  }

  const handleRestore = async (item: WorkflowTemplateVersionSnapshot) => {
    const templateId = String(id || '').trim()
    if (!templateId) {
      return
    }

    try {
      await restoreWorkflowTemplateVersion(templateId, item.version)
      message.success(`已恢复为版本 v${item.version}`)
      await load(templateId)
    } catch (error) {
      message.error((error as Error).message || '恢复失败')
    }
  }

  return (
    <Card
      title="模板历史版本"
      extra={
        <Space>
          <Button onClick={() => navigate('/workflow/templates')}>返回模板列表</Button>
          <Button type="primary" onClick={() => navigate(`/workflow/templates/${id}/design`)}>
            返回设计页
          </Button>
        </Space>
      }
    >
      <Breadcrumb
        style={{ marginBottom: 16 }}
        items={[
          { title: '首页' },
          { title: '审批管理' },
          { title: '流程模板' },
          { title: '模板历史' }
        ]}
      />

      <Space style={{ marginBottom: 12 }}>
        <Text>模板名称：{templateName || '-'}</Text>
        <Tag color="blue">模板ID：{id}</Tag>
      </Space>

      {versions.length ? (
        <List
          loading={loading}
          dataSource={versions}
          renderItem={(item) => {
            const action = actionLabelMap[item.actionType || 'save'] || actionLabelMap.save
            const status = statusLabelMap[item.status || 'draft'] || statusLabelMap.draft

            return (
              <List.Item
                actions={[
                  <Button key="preview" type="link" onClick={() => void handlePreview(item)}>
                    预览快照
                  </Button>,
                  <Popconfirm
                    key="restore"
                    title={`确认恢复到版本 v${item.version} 吗？`}
                    onConfirm={() => void handleRestore(item)}
                  >
                    <Button type="link">恢复此版本</Button>
                  </Popconfirm>
                ]}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      <Text>版本 v{item.version}</Text>
                      <Tag color={action.color}>{action.text}</Tag>
                      <Tag color={status.color}>{status.text}</Tag>
                    </Space>
                  }
                  description={
                    <Space>
                      <Text type="secondary">时间：{item.updatedAt}</Text>
                      <Text type="secondary">操作人：{item.operator}</Text>
                      {item.remark ? <Text type="secondary">备注：{item.remark}</Text> : null}
                    </Space>
                  }
                />
              </List.Item>
            )
          }}
        />
      ) : (
        <Empty description="暂无历史版本" image={Empty.PRESENTED_IMAGE_SIMPLE} />
      )}

      <Modal
        title={previewVersion ? `版本快照预览 v${previewVersion.version}` : '版本快照预览'}
        width={1200}
        open={previewOpen}
        onCancel={() => setPreviewOpen(false)}
        footer={[<Button key="close" onClick={() => setPreviewOpen(false)}>关闭</Button>]}
      >
        <div style={{ height: 620 }}>
          <WorkflowCanvas
            nodes={previewElements.nodes}
            edges={previewElements.edges}
            initialViewport={previewVersion?.payload.viewport}
            onSelectNode={setPreviewSelectedNodeId}
            onZoomChange={() => undefined}
            onViewportChange={() => undefined}
          />
        </div>
      </Modal>
    </Card>
  )
}

export default WorkflowTemplateHistoryPage
