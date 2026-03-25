import React, { useEffect, useState } from 'react'
import { Button, Card, Form, Input, InputNumber, message, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd'
import {
  createWorkflowTemplate,
  deleteWorkflowTemplate,
  getWorkflowRoleList,
  getWorkflowTemplateNodes,
  getWorkflowTemplatePage,
  getWorkflowUserPage,
  saveWorkflowTemplateNodes,
  updateWorkflowTemplate
} from '@/api/leaveWorkflow'

type NodeType = 'APPROVAL' | 'CC'

interface NodeRow {
  id?: number
  nodeOrder: number
  nodeName: string
  nodeType: NodeType
  approvalMode: 'ANY' | 'ALL' | 'SEQUENTIAL'
  approverBuiltinTypes: string[]
  approverRoleCodes: string[]
  approverUserIds: number[]
  ccRoleCodes: string[]
  ccUserIds: number[]
  ccTiming: string
  conditionExpression?: string | null
  requiredFlag: number
}

const normalizeNodeOrders = (list: NodeRow[]): NodeRow[] =>
  list.map((item, index) => ({ ...item, nodeOrder: index + 1 }))

const createDefaultApprovalNode = (nodeOrder: number): NodeRow => ({
  nodeOrder,
  nodeName: '',
  nodeType: 'APPROVAL',
  approvalMode: 'ANY',
  approverBuiltinTypes: ['DIRECT_LEADER'],
  approverRoleCodes: [],
  approverUserIds: [],
  ccRoleCodes: [],
  ccUserIds: [],
  ccTiming: 'AFTER_APPROVAL',
  conditionExpression: null,
  requiredFlag: 1
})

const createDefaultCcNode = (nodeOrder: number): NodeRow => ({
  nodeOrder,
  nodeName: '',
  nodeType: 'CC',
  approvalMode: 'ANY',
  approverBuiltinTypes: [],
  approverRoleCodes: [],
  approverUserIds: [],
  ccRoleCodes: [],
  ccUserIds: [],
  ccTiming: 'AFTER_APPROVAL',
  conditionExpression: null,
  requiredFlag: 1
})

const normalizeNodeFromApi = (node: any, index: number): NodeRow => {
  const approvers = Array.isArray(node?.approvers) ? node.approvers : []
  const ccUsers = Array.isArray(node?.ccUsers) ? node.ccUsers : Array.isArray(node?.ccs) ? node.ccs : []
  return {
    id: node.id,
    nodeOrder: Number(node.nodeOrder || index + 1),
    nodeName: String(node.nodeName || ''),
    nodeType: String(node.nodeType || 'APPROVAL').toUpperCase() === 'CC' ? 'CC' : 'APPROVAL',
    approvalMode: (() => {
      const mode = String(node.approvalMode || 'ANY').toUpperCase()
      if (mode === 'ALL' || mode === 'SEQUENTIAL') {
        return mode
      }
      return 'ANY'
    })(),
    approverBuiltinTypes: approvers
      .map((a: any) => String(a?.approverType || '').toUpperCase())
      .filter((type: string) => type === 'DIRECT_LEADER' || type === 'SELF'),
    approverRoleCodes: approvers
      .filter((a: any) => ['SPECIFIED_ROLE', 'ROLE'].includes(String(a?.approverType || '').toUpperCase()))
      .map((a: any) => String(a?.approverRoleCode || ''))
      .filter(Boolean),
    approverUserIds: approvers
      .filter((a: any) => ['SPECIFIED_USER', 'USER'].includes(String(a?.approverType || '').toUpperCase()))
      .map((a: any) => Number(a?.approverUserId))
      .filter((id: number) => Number.isFinite(id)),
    ccRoleCodes: ccUsers
      .filter((c: any) => ['SPECIFIED_ROLE', 'ROLE'].includes(String(c?.ccType || '').toUpperCase()))
      .map((c: any) => String(c?.ccRoleCode || ''))
      .filter(Boolean),
    ccUserIds: ccUsers
      .filter((c: any) => ['SPECIFIED_USER', 'USER'].includes(String(c?.ccType || '').toUpperCase()))
      .map((c: any) => Number(c?.ccUserId))
      .filter((id: number) => Number.isFinite(id)),
    ccTiming: String(ccUsers?.[0]?.ccTiming || 'AFTER_APPROVAL'),
    conditionExpression: node.conditionExpression ?? null,
    requiredFlag: Number(node.requiredFlag ?? 1)
  }
}

const WorkflowTemplatePage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<any>({ pageNum: 1, pageSize: 10 })
  const [data, setData] = useState<any>({ list: [], total: 0 })
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<any>(null)
  const [nodeOpen, setNodeOpen] = useState(false)
  const [nodes, setNodes] = useState<NodeRow[]>([])
  const [editingTemplateId, setEditingTemplateId] = useState<number | null>(null)
  const [roleOptions, setRoleOptions] = useState<Array<{ label: string; value: string }>>([])
  const [userOptions, setUserOptions] = useState<Array<{ label: string; value: number }>>([])
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

  const loadSelectorOptions = async () => {
    const [roleRes, userRes] = await Promise.all([
      getWorkflowRoleList(),
      getWorkflowUserPage({ pageNum: 1, pageSize: 200, status: 1 })
    ])

    const roleList = Array.isArray(roleRes.data) ? roleRes.data : []
    setRoleOptions(
      roleList.map((role: any) => ({
        label: `${role.roleName || role.roleCode} (${role.roleCode})`,
        value: role.roleCode
      }))
    )

    const userPage: any = userRes.data || {}
    const users = Array.isArray(userPage.list)
      ? userPage.list
      : Array.isArray(userPage.records)
      ? userPage.records
      : []
    setUserOptions(
      users.map((user: any) => ({
        label: `${user.realName || user.username || user.id} (${user.username || user.id})`,
        value: Number(user.id)
      }))
    )
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
    message.success('保存成功')
    setOpen(false)
    loadData()
  }

  const openNodes = async (row: any) => {
    setEditingTemplateId(row.id)
    await loadSelectorOptions()
    const res = await getWorkflowTemplateNodes(row.id)
    const list = Array.isArray(res.data) ? res.data.map(normalizeNodeFromApi) : []
    setNodes(normalizeNodeOrders(list))
    setNodeOpen(true)
  }

  const updateNode = (index: number, patch: Partial<NodeRow>) => {
    setNodes((prev) => {
      const list = [...prev]
      list[index] = { ...list[index], ...patch }
      return list
    })
  }

  const addApprovalNode = () => {
    setNodes((prev) => normalizeNodeOrders([...prev, createDefaultApprovalNode(prev.length + 1)]))
  }

  const addCcNode = () => {
    setNodes((prev) => normalizeNodeOrders([...prev, createDefaultCcNode(prev.length + 1)]))
  }

  const removeNode = (index: number) => {
    setNodes((prev) => normalizeNodeOrders(prev.filter((_, i) => i !== index)))
  }

  const moveNode = (index: number, direction: -1 | 1) => {
    setNodes((prev) => {
      const target = index + direction
      if (target < 0 || target >= prev.length) {
        return prev
      }
      const list = [...prev]
      const [moving] = list.splice(index, 1)
      list.splice(target, 0, moving)
      return normalizeNodeOrders(list)
    })
  }

  const saveNodes = async () => {
    if (!editingTemplateId) {
      return
    }
    const normalized = normalizeNodeOrders(nodes)
    const payload = normalized.map((node) => {
      const approvers: any[] = []
      node.approverBuiltinTypes.forEach((type) => approvers.push({ approverType: type }))
      node.approverRoleCodes.forEach((roleCode) => approvers.push({ approverType: 'SPECIFIED_ROLE', approverRoleCode: roleCode }))
      node.approverUserIds.forEach((userId) => approvers.push({ approverType: 'SPECIFIED_USER', approverUserId: userId }))

      const ccUsers: any[] = []
      node.ccRoleCodes.forEach((roleCode) => ccUsers.push({ ccType: 'SPECIFIED_ROLE', ccRoleCode: roleCode, ccTiming: node.ccTiming }))
      node.ccUserIds.forEach((userId) => ccUsers.push({ ccType: 'SPECIFIED_USER', ccUserId: userId, ccTiming: node.ccTiming }))

      return {
        nodeOrder: node.nodeOrder,
        nodeName: node.nodeName?.trim(),
        nodeType: node.nodeType,
        approvalMode: node.approvalMode,
        conditionExpression: node.conditionExpression || null,
        requiredFlag: node.requiredFlag ?? 1,
        approvers,
        ccUsers
      }
    })

    for (const node of payload) {
      if (!node.nodeName) {
        message.error('节点名称不能为空')
        return
      }
      if (node.nodeType === 'APPROVAL' && node.approvers.length === 0) {
        message.error(`审批节点 [${node.nodeName}] 至少需要配置一个审批人`)
        return
      }
      if (node.nodeType === 'CC' && node.ccUsers.length === 0) {
        message.error(`抄送节点 [${node.nodeName}] 至少需要配置一个抄送人`)
        return
      }
    }

    await saveWorkflowTemplateNodes(editingTemplateId, payload)
    message.success('节点保存成功')
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
          {
            title: '状态',
            dataIndex: 'status',
            render: (value) => <Tag color={value === 'ENABLED' ? 'green' : 'default'}>{value}</Tag>
          },
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
                <Button type="link" onClick={() => openNodes(row)}>
                  节点配置
                </Button>
                <Popconfirm
                  title="确定删除该模板吗？"
                  onConfirm={async () => {
                    await deleteWorkflowTemplate(row.id)
                    loadData()
                  }}
                >
                  <Button type="link" danger>
                    删除
                  </Button>
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

      <Modal
        title="节点配置"
        width={1300}
        open={nodeOpen}
        onCancel={() => setNodeOpen(false)}
        onOk={saveNodes}
      >
        <Space style={{ marginBottom: 12 }}>
          <Button onClick={addApprovalNode}>新增审批节点</Button>
          <Button onClick={addCcNode}>新增抄送节点</Button>
        </Space>

        <Table
          rowKey={(_, index) => String(index)}
          pagination={false}
          dataSource={nodes}
          scroll={{ x: 1400 }}
          columns={[
            {
              title: '顺序',
              width: 70,
              dataIndex: 'nodeOrder'
            },
            {
              title: '节点名称',
              width: 180,
              dataIndex: 'nodeName',
              render: (value, row, index) => (
                <Input value={value} onChange={(e) => updateNode(index, { nodeName: e.target.value })} />
              )
            },
            {
              title: '节点类型',
              width: 130,
              dataIndex: 'nodeType',
              render: (value, row, index) => (
                <Select
                  value={value}
                  style={{ width: 120 }}
                  options={[
                    { value: 'APPROVAL', label: '审批节点' },
                    { value: 'CC', label: '抄送节点' }
                  ]}
                  onChange={(nextType: NodeType) => {
                    if (nextType === 'APPROVAL') {
                      updateNode(index, {
                        nodeType: 'APPROVAL',
                        ccRoleCodes: [],
                        ccUserIds: [],
                        ccTiming: 'AFTER_APPROVAL'
                      })
                    } else {
                      updateNode(index, {
                        nodeType: 'CC',
                        approverBuiltinTypes: [],
                        approverRoleCodes: [],
                        approverUserIds: []
                      })
                    }
                  }}
                />
              )
            },
            {
              title: '审批方式',
              width: 150,
              dataIndex: 'approvalMode',
              render: (value, row, index) => (
                <Select
                  disabled={row.nodeType !== 'APPROVAL'}
                  value={value}
                  style={{ width: 140 }}
                  options={[
                    { value: 'ANY', label: '任一通过' },
                    { value: 'ALL', label: '全部通过' },
                    { value: 'SEQUENTIAL', label: '顺序审批' }
                  ]}
                  onChange={(nextValue) => updateNode(index, { approvalMode: nextValue })}
                />
              )
            },
            {
              title: '审批人配置',
              width: 320,
              render: (_, row, index) =>
                row.nodeType !== 'APPROVAL' ? (
                  '-'
                ) : (
                  <Space direction="vertical" style={{ width: '100%' }}>
                    <Select
                      mode="multiple"
                      allowClear
                      value={row.approverBuiltinTypes}
                      style={{ width: '100%' }}
                      options={[
                        { value: 'DIRECT_LEADER', label: '直属上级' },
                        { value: 'SELF', label: '发起人本人' }
                      ]}
                      placeholder="内置审批人（可选）"
                      onChange={(values) => updateNode(index, { approverBuiltinTypes: values })}
                    />
                    <Select
                      mode="multiple"
                      allowClear
                      value={row.approverRoleCodes}
                      style={{ width: '100%' }}
                      options={roleOptions}
                      placeholder="指定角色（可多选）"
                      onChange={(values) => updateNode(index, { approverRoleCodes: values })}
                    />
                    <Select
                      mode="multiple"
                      allowClear
                      showSearch
                      optionFilterProp="label"
                      value={row.approverUserIds}
                      style={{ width: '100%' }}
                      options={userOptions}
                      placeholder="指定用户（可多选）"
                      onChange={(values) => updateNode(index, { approverUserIds: values })}
                    />
                  </Space>
                )
            },
            {
              title: '抄送配置',
              width: 320,
              render: (_, row, index) =>
                row.nodeType !== 'CC' ? (
                  '-'
                ) : (
                  <Space direction="vertical" style={{ width: '100%' }}>
                    <Select
                      mode="multiple"
                      allowClear
                      value={row.ccRoleCodes}
                      style={{ width: '100%' }}
                      options={roleOptions}
                      placeholder="抄送角色（可多选）"
                      onChange={(values) => updateNode(index, { ccRoleCodes: values })}
                    />
                    <Select
                      mode="multiple"
                      allowClear
                      showSearch
                      optionFilterProp="label"
                      value={row.ccUserIds}
                      style={{ width: '100%' }}
                      options={userOptions}
                      placeholder="抄送用户（可多选）"
                      onChange={(values) => updateNode(index, { ccUserIds: values })}
                    />
                    <Select
                      value={row.ccTiming}
                      style={{ width: '100%' }}
                      options={[
                        { value: 'BEFORE_APPROVAL', label: '审批前抄送' },
                        { value: 'AFTER_APPROVAL', label: '审批后抄送' },
                        { value: 'AFTER_FINISH', label: '流程结束抄送' }
                      ]}
                      onChange={(value) => updateNode(index, { ccTiming: value })}
                    />
                  </Space>
                )
            },
            {
              title: '条件表达式',
              width: 180,
              dataIndex: 'conditionExpression',
              render: (value, row, index) => (
                <Input
                  value={value || ''}
                  placeholder="例如 days > 3"
                  onChange={(e) => updateNode(index, { conditionExpression: e.target.value })}
                />
              )
            },
            {
              title: '是否必经',
              width: 110,
              dataIndex: 'requiredFlag',
              render: (value, row, index) => (
                <Select
                  value={value}
                  style={{ width: 100 }}
                  options={[
                    { value: 1, label: '是' },
                    { value: 0, label: '否' }
                  ]}
                  onChange={(nextValue) => updateNode(index, { requiredFlag: nextValue })}
                />
              )
            },
            {
              title: '操作',
              width: 180,
              render: (_, row, index) => (
                <Space>
                  <Button type="link" disabled={index === 0} onClick={() => moveNode(index, -1)}>
                    上移
                  </Button>
                  <Button type="link" disabled={index === nodes.length - 1} onClick={() => moveNode(index, 1)}>
                    下移
                  </Button>
                  <Button type="link" danger onClick={() => removeNode(index)}>
                    删除
                  </Button>
                </Space>
              )
            }
          ]}
        />
      </Modal>
    </Card>
  )
}

export default WorkflowTemplatePage
