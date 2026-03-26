import React from 'react'
import {
  AimOutlined,
  HistoryOutlined,
  MinusOutlined,
  PlusOutlined,
  RedoOutlined,
  SaveOutlined,
  SendOutlined,
  UndoOutlined
} from '@ant-design/icons'
import { Breadcrumb, Button, Input, Space, Typography } from 'antd'

interface DesignerToolbarProps {
  templateName: string
  templateCode: string
  version: number
  zoom: number
  canUndo: boolean
  canRedo: boolean
  onTemplateNameChange: (value: string) => void
  onHistory: () => void
  onUndo: () => void
  onRedo: () => void
  onSave: () => void
  onPublish: () => void
  onZoomIn: () => void
  onZoomOut: () => void
  onFitView: () => void
}

const { Title, Text } = Typography

const DesignerToolbar: React.FC<DesignerToolbarProps> = ({
  templateName,
  templateCode,
  version,
  zoom,
  canUndo,
  canRedo,
  onTemplateNameChange,
  onHistory,
  onUndo,
  onRedo,
  onSave,
  onPublish,
  onZoomIn,
  onZoomOut,
  onFitView
}) => {
  return (
    <header className="workflow-designer-toolbar">
      <div className="workflow-designer-toolbar-left">
        <Title level={4} style={{ margin: 0, color: '#f5f7fa' }}>
          流程设计
        </Title>

        <Breadcrumb
          className="workflow-designer-breadcrumb"
          items={[
            { title: '首页' },
            { title: '审批管理' },
            { title: '流程模板' },
            { title: '流程设计' }
          ]}
        />

        <Input
          value={templateName}
          onChange={(event) => onTemplateNameChange(event.target.value)}
          className="workflow-template-name-input"
          placeholder="请输入流程模板名称"
        />

        <Text className="workflow-template-meta-text">编码：{templateCode}</Text>
        <Text className="workflow-template-meta-text">版本：v{version}</Text>
      </div>

      <div className="workflow-designer-toolbar-right">
        <Space size={8}>
          <Button icon={<HistoryOutlined />} onClick={onHistory}>
            历史记录
          </Button>
          <Button icon={<UndoOutlined />} onClick={onUndo} disabled={!canUndo}>
            撤销
          </Button>
          <Button icon={<RedoOutlined />} onClick={onRedo} disabled={!canRedo}>
            重做
          </Button>
          <Button type="primary" icon={<SaveOutlined />} onClick={onSave}>
            保存
          </Button>
          <Button icon={<SendOutlined />} onClick={onPublish}>
            发布
          </Button>
        </Space>

        <Space size={6}>
          <Text className="workflow-zoom-text">{Math.round(zoom * 100)}%</Text>
          <Button icon={<PlusOutlined />} onClick={onZoomIn} />
          <Button icon={<MinusOutlined />} onClick={onZoomOut} />
          <Button icon={<AimOutlined />} onClick={onFitView}>
            适配画布
          </Button>
        </Space>
      </div>
    </header>
  )
}

export default DesignerToolbar
