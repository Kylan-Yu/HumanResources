import React, { useState } from 'react'
import { Breadcrumb, Button, Card, Form, Input, Select, Space, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { WORKFLOW_CATEGORY_OPTIONS } from '../../designer/constants'
import { createWorkflowTemplate } from '../../designer/mockApi'

interface FormValues {
  templateName: string
  templateCode: string
  category: string
}

const WorkflowTemplateCreatePage: React.FC = () => {
  const [submitting, setSubmitting] = useState(false)
  const [form] = Form.useForm<FormValues>()
  const navigate = useNavigate()

  const handleSubmit = async (values: FormValues) => {
    setSubmitting(true)

    try {
      const created = await createWorkflowTemplate(values)
      message.success('模板创建成功，进入流程设计')
      navigate(`/workflow/templates/${created.templateId}/design`)
    } catch (error) {
      message.error((error as Error).message || '创建失败')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Card title="新建流程模板">
      <Breadcrumb
        style={{ marginBottom: 16 }}
        items={[
          { title: '首页' },
          { title: '审批管理' },
          { title: '流程模板' },
          { title: '新建模板' }
        ]}
      />

      <Form
        form={form}
        layout="vertical"
        style={{ maxWidth: 560 }}
        initialValues={{ category: '通用' }}
        onFinish={handleSubmit}
      >
        <Form.Item
          name="templateName"
          label="模板名称"
          rules={[{ required: true, message: '请输入模板名称' }]}
        >
          <Input placeholder="例如：请假审批流程" />
        </Form.Item>

        <Form.Item
          name="templateCode"
          label="模板编码"
          rules={[{ required: true, message: '请输入模板编码' }]}
          extra="建议使用英文大写与下划线，例如 LEAVE_PROCESS_001"
        >
          <Input placeholder="LEAVE_PROCESS_001" />
        </Form.Item>

        <Form.Item
          name="category"
          label="流程分类"
          rules={[{ required: true, message: '请选择流程分类' }]}
        >
          <Select options={WORKFLOW_CATEGORY_OPTIONS} />
        </Form.Item>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={submitting}>
              创建并进入设计器
            </Button>
            <Button onClick={() => navigate('/workflow/templates')}>取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  )
}

export default WorkflowTemplateCreatePage
