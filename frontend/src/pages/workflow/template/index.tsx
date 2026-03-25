import React, { useEffect, useState } from 'react'
import { Button, Card, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table } from 'antd'
import {
  createWorkflowTemplate,
  deleteWorkflowTemplate,
  getWorkflowTemplateNodes,
  getWorkflowTemplatePage,
  saveWorkflowTemplateNodes,
  updateWorkflowTemplate
} from '@/api/leaveWorkflow'

const WorkflowTemplatePage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<any>({ pageNum: 1, pageSize: 10 })
  const [data, setData] = useState<any>({ list: [], total: 0 })
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<any>(null)
  const [nodeOpen, setNodeOpen] = useState(false)
  const [nodes, setNodes] = useState<any[]>([])
  const [editingTemplateId, setEditingTemplateId] = useState<number | null>(null)
  const [form] = Form.useForm()

  const loadData = async (extra?: any) => {
    const params = { ...query, ...(extra || {}) }
    setLoading(true)
    try {
      const res = await getWorkflowTemplatePage(params)
      setData(res.data || { list: [], total: 0 })
      setQuery(params)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const saveTemplate = async () => {
    const values = await form.validateFields()
    if (editing?.id) {
      await updateWorkflowTemplate(editing.id, values)
    } else {
      await createWorkflowTemplate(values)
    }
    setOpen(false)
    loadData()
  }

  const openNodes = async (row: any) => {
    setEditingTemplateId(row.id)
    const res = await getWorkflowTemplateNodes(row.id)
    setNodes(res.data || [])
    setNodeOpen(true)
  }

  const saveNodes = async () => {
    if (!editingTemplateId) {
      return
    }
    await saveWorkflowTemplateNodes(editingTemplateId, nodes)
    setNodeOpen(false)
  }

  return (
    <Card>
      <Space style={{ marginBottom: 16 }}>
        <Button
          type="primary"
          onClick={() => {
            setEditing(null)
            form.resetFields()
            form.setFieldsValue({ businessType: 'LEAVE', status: 'ENABLED', versionNo: 1 })
            setOpen(true)
          }}
        >
          新建模板
        </Button>
      </Space>

      <Table
        rowKey="id"
        loading={loading}
        dataSource={data.list || []}
        columns={[
          { title: '模板名称', dataIndex: 'templateName' },
          { title: '业务类型', dataIndex: 'businessType' },
          { title: '状态', dataIndex: 'status' },
          { title: '版本', dataIndex: 'versionNo' },
          {
            title: '操作',
            render: (_: any, row: any) => (
              <Space>
                <Button
                  type="link"
                  onClick={() => {
                    setEditing(row)
                    form.setFieldsValue(row)
                    setOpen(true)
                  }}
                >
                  编辑
                </Button>
                <Button type="link" onClick={() => openNodes(row)}>节点配置</Button>
                <Popconfirm title="确定删除该模板吗？" onConfirm={async () => { await deleteWorkflowTemplate(row.id); loadData() }}>
                  <Button type="link" danger>删除</Button>
                </Popconfirm>
              </Space>
            )
          }
        ]}
        pagination={{
          current: query.pageNum,
          pageSize: query.pageSize,
          total: data.total || 0,
          onChange: (page, pageSize) => loadData({ ...query, pageNum: page, pageSize })
        }}
      />

      <Modal title={editing ? '编辑模板' : '新建模板'} open={open} onCancel={() => setOpen(false)} onOk={saveTemplate}>
        <Form form={form} layout="vertical">
          <Form.Item name="templateName" label="模板名称" rules={[{ required: true, message: '请输入模板名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="businessType" label="业务类型" rules={[{ required: true, message: '请选择业务类型' }]}>
            <Select options={[{ value: 'LEAVE', label: '请假' }, { value: 'PATCH', label: '补卡' }, { value: 'OVERTIME', label: '加班' }]} />
          </Form.Item>
          <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
            <Select options={[{ value: 'ENABLED', label: '启用' }, { value: 'DISABLED', label: '停用' }]} />
          </Form.Item>
          <Form.Item name="versionNo" label="版本号" rules={[{ required: true, message: '请输入版本号' }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="节点配置" width={900} open={nodeOpen} onCancel={() => setNodeOpen(false)} onOk={saveNodes}>
        <Space style={{ marginBottom: 12 }}>
          <Button
            onClick={() => {
              setNodes([
                ...nodes,
                {
                  nodeOrder: nodes.length + 1,
                  nodeName: '',
                  approverType: 'DIRECT_LEADER',
                  approverRoleCode: null,
                  approverUserId: null,
                  conditionExpression: null,
                  requiredFlag: 1
                }
              ])
            }}
          >
            新增节点
          </Button>
        </Space>

        <Table
          rowKey={(_, index) => String(index)}
          pagination={false}
          dataSource={nodes}
          columns={[
            {
              title: '顺序',
              dataIndex: 'nodeOrder',
              render: (v, row, index) => (
                <InputNumber
                  value={v}
                  min={1}
                  onChange={(val) => {
                    const list = [...nodes]
                    list[index].nodeOrder = val
                    setNodes(list)
                  }}
                />
              )
            },
            {
              title: '节点名称',
              dataIndex: 'nodeName',
              render: (v, row, index) => (
                <Input
                  value={v}
                  onChange={(e) => {
                    const list = [...nodes]
                    list[index].nodeName = e.target.value
                    setNodes(list)
                  }}
                />
              )
            },
            {
              title: '审批人类型',
              dataIndex: 'approverType',
              render: (v, row, index) => (
                <Select
                  value={v}
                  style={{ width: 160 }}
                  options={[
                    { value: 'SELF', label: '发起人本人' },
                    { value: 'DIRECT_LEADER', label: '直属上级' },
                    { value: 'SPECIFIED_ROLE', label: '指定角色' },
                    { value: 'SPECIFIED_USER', label: '指定用户' }
                  ]}
                  onChange={(val) => {
                    const list = [...nodes]
                    list[index].approverType = val
                    setNodes(list)
                  }}
                />
              )
            },
            {
              title: '角色编码',
              dataIndex: 'approverRoleCode',
              render: (v, row, index) => (
                <Input
                  value={v}
                  placeholder="如 HR"
                  onChange={(e) => {
                    const list = [...nodes]
                    list[index].approverRoleCode = e.target.value
                    setNodes(list)
                  }}
                />
              )
            },
            {
              title: '指定用户ID',
              dataIndex: 'approverUserId',
              render: (v, row, index) => (
                <InputNumber
                  value={v}
                  style={{ width: 120 }}
                  onChange={(val) => {
                    const list = [...nodes]
                    list[index].approverUserId = val
                    setNodes(list)
                  }}
                />
              )
            },
            {
              title: '条件表达式',
              dataIndex: 'conditionExpression',
              render: (v, row, index) => (
                <Input
                  value={v}
                  placeholder="例如 days > 3"
                  onChange={(e) => {
                    const list = [...nodes]
                    list[index].conditionExpression = e.target.value
                    setNodes(list)
                  }}
                />
              )
            },
            {
              title: '是否必经',
              dataIndex: 'requiredFlag',
              render: (v, row, index) => (
                <Select
                  value={v}
                  style={{ width: 100 }}
                  options={[{ value: 1, label: '是' }, { value: 0, label: '否' }]}
                  onChange={(val) => {
                    const list = [...nodes]
                    list[index].requiredFlag = val
                    setNodes(list)
                  }}
                />
              )
            },
            {
              title: '操作',
              render: (_, row, index) => (
                <Button
                  type="link"
                  danger
                  onClick={() => {
                    const list = [...nodes]
                    list.splice(index, 1)
                    setNodes(list)
                  }}
                >
                  删除
                </Button>
              )
            }
          ]}
        />
      </Modal>
    </Card>
  )
}

export default WorkflowTemplatePage
