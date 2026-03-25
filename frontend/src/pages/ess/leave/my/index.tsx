import React, { useEffect, useState } from 'react'
import { Button, Card, Drawer, Form, Input, Select, Table, Tag } from 'antd'
import { getApplicationProgress, getMyApplicationPage } from '@/api/leaveWorkflow'

const LeaveMyPage: React.FC = () => {
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
      const res = await getMyApplicationPage(params)
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
    const res = await getApplicationProgress(row.businessType, row.businessId)
    setDetail(res.data)
    setDetailOpen(true)
  }

  return (
    <Card title="我的申请">
      <Form
        form={form}
        layout="inline"
        onFinish={(values) => loadData({ ...values, pageNum: 1 })}
        style={{ marginBottom: 16 }}
      >
        <Form.Item name="applyNo" label="申请单号">
          <Input allowClear />
        </Form.Item>
        <Form.Item name="businessType" label="申请类型">
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
        <Form.Item name="status" label="状态">
          <Select
            allowClear
            style={{ width: 160 }}
            options={[
              { value: 'IN_APPROVAL', label: '审批中' },
              { value: 'APPROVED', label: '已通过' },
              { value: 'REJECTED', label: '已驳回' },
              { value: 'WITHDRAWN', label: '已撤回' }
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
        rowKey={(row) => `${row.businessType}-${row.businessId}`}
        loading={loading}
        dataSource={data.list || []}
        columns={[
          { title: '申请单号', dataIndex: 'applyNo' },
          { title: '类型', dataIndex: 'applyTypeName' },
          { title: '摘要', dataIndex: 'businessSummary' },
          {
            title: '状态',
            dataIndex: 'status',
            render: (v) => <Tag>{v}</Tag>
          },
          { title: '当前节点', dataIndex: 'currentNodeName' },
          { title: '创建时间', dataIndex: 'createdTime' },
          {
            title: '操作',
            render: (_: any, row: any) => (
              <Button type="link" onClick={() => openDetail(row)}>
                查看进度
              </Button>
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
          <b>状态：</b>
          {detail?.application?.status}
        </p>
        <p>
          <b>当前节点：</b>
          {detail?.instance?.currentNodeName || '-'}
        </p>

        <h4>审批节点</h4>
        <Table
          rowKey="id"
          pagination={false}
          dataSource={detail?.tasks || []}
          columns={[
            { title: '顺序', dataIndex: 'nodeOrder' },
            { title: '节点', dataIndex: 'nodeName' },
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

export default LeaveMyPage
