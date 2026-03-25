import React, { useEffect, useState } from 'react'
import { Button, Card, DatePicker, Form, Table, Tag } from 'antd'
import dayjs from 'dayjs'
import { getMyAttendancePage } from '@/api/attendance'

const AttendanceSelfPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<any>({ pageNum: 1, pageSize: 10, month: dayjs().format('YYYY-MM') })
  const [data, setData] = useState<any>({ list: [], total: 0 })
  const [form] = Form.useForm()

  const loadData = async (extra?: any) => {
    const params = { ...query, ...(extra || {}) }
    setLoading(true)
    try {
      const res = await getMyAttendancePage(params)
      setData(res.data || { list: [], total: 0 })
      setQuery(params)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  return (
    <Card title="我的考勤">
      <Form
        form={form}
        layout="inline"
        initialValues={{ monthPicker: dayjs(query.month, 'YYYY-MM') }}
        onFinish={(values) => {
          loadData({
            pageNum: 1,
            month: values.monthPicker ? values.monthPicker.format('YYYY-MM') : undefined
          })
        }}
        style={{ marginBottom: 16 }}
      >
        <Form.Item name="monthPicker" label="月份">
          <DatePicker picker="month" />
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
          { title: '日期', dataIndex: 'attendanceDate' },
          { title: '上班时间', dataIndex: 'checkInTime' },
          { title: '下班时间', dataIndex: 'checkOutTime' },
          {
            title: '状态',
            dataIndex: 'attendanceStatus',
            render: (v) => <Tag>{v || '-'}</Tag>
          },
          { title: '迟到(分钟)', dataIndex: 'lateMinutes' },
          { title: '早退(分钟)', dataIndex: 'earlyLeaveMinutes' },
          {
            title: '缺卡',
            dataIndex: 'missingCardFlag',
            render: (v) => (v === 1 ? <Tag color="red">是</Tag> : <Tag color="green">否</Tag>)
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

export default AttendanceSelfPage
