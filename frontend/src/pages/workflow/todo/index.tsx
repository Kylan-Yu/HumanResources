import React, { useEffect, useState } from 'react'
import { Button, Card, Drawer, Form, Select, Space, Table, Tag, message } from 'antd'
import { getApplicationProgress, getTodoTaskPage, workflowTaskAction } from '@/api/leaveWorkflow'

const WorkflowTodoPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<any>({ pageNum: 1, pageSize: 10 })
  const [data, setData] = useState<any>({ list: [], total: 0 })
  const [detailOpen, setDetailOpen] = useState(false)
  const [detail, setDetail] = useState<any>(null)
  const [form] = Form.useForm()

  const loadData = async (extra?: any) => {
    const params = { ...query, ...(extra || {}) }
    setLoading(true)
    try {
      const res = await getTodoTaskPage(params)
      setData(res.data || { list: [], total: 0 })
      setQuery(params)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const openDetail = async (row: any) => {
    const progress = await getApplicationProgress(row.businessType, row.businessId)
    setDetail(progress.data)
    setDetailOpen(true)
  }

  const doAction = async (row: any, action: string) => {
    await workflowTaskAction(row.id, { action })
    message.success('处理成功')
    loadData()
    if (detail?.application?.id === row.businessId && detail?.application?.businessType === row.businessType) {
      const progress = await getApplicationProgress(row.businessType, row.businessId)
      setDetail(progress.data)
    }
  }

  return (
    <Card>
      <Form
        form={form}
        layout="inline"
        onFinish={(values) => loadData({ ...values, pageNum: 1 })}
        style={{ marginBottom: 16 }}
      >
        <Form.Item name="businessType" label="业务类型">
          <Select
            allowClear
            style={{ width: 180 }}
            options={[
              { value: 'LEAVE', label: '请假申请' },
              { value: 'PATCH', label: '补卡申请' },
              { value: 'OVERTIME', label: '加班申请' }
            ]}
          />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit">
            查询
          </Button>
        </Form.Item>
      </Form>

      <Table
        rowKey="id"
        loading={loading}
        dataSource={data.list || []}
        columns={[
          { title: '任务ID', dataIndex: 'id' },
          { title: '业务类型', dataIndex: 'businessType' },
          { title: '申请单号', dataIndex: 'applyNo' },
          { title: '申请人', dataIndex: 'applicantName' },
          { title: '节点', dataIndex: 'nodeName' },
          { title: '业务摘要', dataIndex: 'businessSummary' },
          {
            title: '状态',
            dataIndex: 'status',
            render: (v) => <Tag color={v === 'PENDING' ? 'blue' : 'default'}>{v}</Tag>
          },
          {
            title: '操作',
            render: (_: any, row: any) => (
              <Space>
                <Button type="link" onClick={() => openDetail(row)}>
                  详情
                </Button>
                <Button type="link" onClick={() => doAction(row, 'APPROVE')}>
                  同意
                </Button>
                <Button type="link" danger onClick={() => doAction(row, 'REJECT')}>
                  驳回
                </Button>
                <Button type="link" onClick={() => doAction(row, 'RETURN')}>
                  退回
                </Button>
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

      <Drawer title="审批进度" width={760} open={detailOpen} onClose={() => setDetailOpen(false)}>
        <p>
          <b>申请单号：</b>
          {detail?.application?.applyNo}
        </p>
        <p>
          <b>当前状态：</b>
          {detail?.application?.status}
        </p>
        <p>
          <b>当前节点：</b>
          {detail?.instance?.currentNodeName || '-'}
        </p>

        <h4>任务节点</h4>
        <Table
          rowKey="id"
          pagination={false}
          dataSource={detail?.tasks || []}
          columns={[
            { title: '节点顺序', dataIndex: 'nodeOrder' },
            { title: '节点名称', dataIndex: 'nodeName' },
            { title: '审批人', dataIndex: 'assigneeName' },
            { title: '状态', dataIndex: 'status' },
            { title: '结果', dataIndex: 'result' }
          ]}
        />

        <h4 style={{ marginTop: 16 }}>审批记录</h4>
        <Table
          rowKey="id"
          pagination={false}
          dataSource={detail?.records || []}
          columns={[
            { title: '审批人', dataIndex: 'approverName' },
            { title: '动作', dataIndex: 'action' },
            { title: '意见', dataIndex: 'comment' },
            { title: '时间', dataIndex: 'actionTime' }
          ]}
        />
      </Drawer>
    </Card>
  )
}

export default WorkflowTodoPage
