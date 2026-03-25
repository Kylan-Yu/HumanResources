import React, { useEffect, useState } from 'react'
import { Button, Card, DatePicker, Form, Input, Popconfirm, Select, Space, Table, Tag, message } from 'antd'
import dayjs from 'dayjs'
import { createPatchApplication, getPatchMyPage, withdrawPatchApplication } from '@/api/leaveWorkflow'

const PatchApplyPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<any>({ pageNum: 1, pageSize: 10, month: dayjs().format('YYYY-MM') })
  const [data, setData] = useState<any>({ list: [], total: 0 })
  const [createForm] = Form.useForm()
  const [queryForm] = Form.useForm()

  const loadData = async (extra?: any) => {
    const params = { ...query, ...(extra || {}) }
    setLoading(true)
    try {
      const res = await getPatchMyPage(params)
      setData(res.data || { list: [], total: 0 })
      setQuery(params)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    createForm.setFieldsValue({ patchType: 'CHECK_IN' })
    queryForm.setFieldsValue({ monthPicker: dayjs(query.month, 'YYYY-MM') })
    loadData()
  }, [])

  const submit = async () => {
    const values = await createForm.validateFields()
    await createPatchApplication({
      attendanceDate: values.attendanceDate.format('YYYY-MM-DD'),
      patchTime: values.patchTime.format('YYYY-MM-DD HH:mm:ss'),
      patchType: values.patchType,
      reason: values.reason
    })
    message.success('补卡申请提交成功')
    createForm.resetFields()
    createForm.setFieldsValue({ patchType: 'CHECK_IN' })
    loadData({ ...query, pageNum: 1 })
  }

  return (
    <Card title="补卡申请">
      <Card size="small" style={{ marginBottom: 16 }}>
        <Form form={createForm} layout="vertical">
          <Form.Item name="attendanceDate" label="日期" rules={[{ required: true, message: '请选择日期' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="patchTime" label="补卡时间" rules={[{ required: true, message: '请选择补卡时间' }]}>
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="patchType" label="类型" rules={[{ required: true, message: '请选择类型' }]}>
            <Select
              options={[
                { value: 'CHECK_IN', label: '上班' },
                { value: 'CHECK_OUT', label: '下班' }
              ]}
            />
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
          { title: '日期', dataIndex: 'attendanceDate' },
          { title: '补卡时间', dataIndex: 'patchTime' },
          {
            title: '类型',
            dataIndex: 'patchType',
            render: (v) => (v === 'CHECK_IN' ? '上班' : v === 'CHECK_OUT' ? '下班' : v)
          },
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
                    await withdrawPatchApplication(row.id)
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

export default PatchApplyPage
