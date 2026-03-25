import React, { useEffect, useState } from 'react'
import { Button, Card, DatePicker, Form, Input, InputNumber, Popconfirm, Select, Table, Tag, message } from 'antd'
import dayjs from 'dayjs'
import { createOvertimeApplication, getOvertimeMyPage, withdrawOvertimeApplication } from '@/api/leaveWorkflow'

const OvertimeApplyPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<any>({ pageNum: 1, pageSize: 10, month: dayjs().format('YYYY-MM') })
  const [data, setData] = useState<any>({ list: [], total: 0 })
  const [createForm] = Form.useForm()
  const [queryForm] = Form.useForm()

  const loadData = async (extra?: any) => {
    const params = { ...query, ...(extra || {}) }
    setLoading(true)
    try {
      const res = await getOvertimeMyPage(params)
      setData(res.data || { list: [], total: 0 })
      setQuery(params)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    queryForm.setFieldsValue({ monthPicker: dayjs(query.month, 'YYYY-MM') })
    loadData()
  }, [])

  const submit = async () => {
    const values = await createForm.validateFields()
    await createOvertimeApplication({
      overtimeDate: values.overtimeDate.format('YYYY-MM-DD'),
      startTime: values.startTime.format('YYYY-MM-DD HH:mm:ss'),
      endTime: values.endTime.format('YYYY-MM-DD HH:mm:ss'),
      hours: values.hours,
      reason: values.reason
    })
    message.success('加班申请提交成功')
    createForm.resetFields()
    loadData({ ...query, pageNum: 1 })
  }

  return (
    <Card title="加班申请">
      <Card size="small" style={{ marginBottom: 16 }}>
        <Form form={createForm} layout="vertical">
          <Form.Item name="overtimeDate" label="日期" rules={[{ required: true, message: '请选择日期' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="startTime" label="开始时间" rules={[{ required: true, message: '请选择开始时间' }]}>
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="endTime" label="结束时间" rules={[{ required: true, message: '请选择结束时间' }]}>
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="hours"
            label="时长(小时)"
            rules={[{ required: true, message: '请输入时长' }, { type: 'number', min: 0.5, message: '时长必须大于0' }]}
          >
            <InputNumber min={0.5} step={0.5} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="reason" label="原因" rules={[{ required: true, message: '请填写原因' }]}>
            <Input.TextArea rows={3} />
          </Form.Item>
          <Button type="primary" onClick={submit}>
            提交申请
          </Button>
        </Form>
      </Card>

      <Form
        form={queryForm}
        layout="inline"
        onFinish={(values) =>
          loadData({
            pageNum: 1,
            status: values.status,
            month: values.monthPicker ? values.monthPicker.format('YYYY-MM') : undefined
          })
        }
        style={{ marginBottom: 16 }}
      >
        <Form.Item name="monthPicker" label="月份">
          <DatePicker picker="month" />
        </Form.Item>
        <Form.Item name="status" label="状态">
          <Select
            allowClear
            style={{ width: 180 }}
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
        rowKey="id"
        loading={loading}
        dataSource={data.list || []}
        columns={[
          { title: '申请单号', dataIndex: 'applyNo' },
          { title: '日期', dataIndex: 'overtimeDate' },
          { title: '开始时间', dataIndex: 'startTime' },
          { title: '结束时间', dataIndex: 'endTime' },
          { title: '时长', dataIndex: 'hours' },
          { title: '原因', dataIndex: 'reason' },
          {
            title: '状态',
            dataIndex: 'status',
            render: (v) => <Tag>{v}</Tag>
          },
          { title: '当前节点', dataIndex: 'currentNodeName' },
          {
            title: '操作',
            render: (_: any, row: any) =>
              row.status === 'IN_APPROVAL' || row.status === 'SUBMITTED' ? (
                <Popconfirm
                  title="确认撤回该申请？"
                  onConfirm={async () => {
                    await withdrawOvertimeApplication(row.id)
                    message.success('撤回成功')
                    loadData()
                  }}
                >
                  <Button type="link" danger>
                    撤回
                  </Button>
                </Popconfirm>
              ) : (
                '-'
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
    </Card>
  )
}

export default OvertimeApplyPage
