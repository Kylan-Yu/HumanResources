import React, { useEffect, useState } from 'react'
import { Button, Card, DatePicker, Form, Input, Table } from 'antd'
import dayjs from 'dayjs'
import { getTeamAttendanceSummaryPage } from '@/api/attendance'

const TeamAttendancePage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<any>({ pageNum: 1, pageSize: 10, month: dayjs().format('YYYY-MM') })
  const [data, setData] = useState<any>({ list: [], total: 0 })
  const [form] = Form.useForm()

  const loadData = async (extra?: any) => {
    const params = { ...query, ...(extra || {}) }
    setLoading(true)
    try {
      const res = await getTeamAttendanceSummaryPage(params)
      setData(res.data || { list: [], total: 0 })
      setQuery(params)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    form.setFieldsValue({ monthPicker: dayjs(query.month, 'YYYY-MM') })
    loadData()
  }, [])

  return (
    <Card title="团队考勤">
      <Form
        form={form}
        layout="inline"
        onFinish={(values) =>
          loadData({
            pageNum: 1,
            keyword: values.keyword,
            month: values.monthPicker ? values.monthPicker.format('YYYY-MM') : undefined
          })
        }
        style={{ marginBottom: 16 }}
      >
        <Form.Item name="monthPicker" label="月份">
          <DatePicker picker="month" />
        </Form.Item>
        <Form.Item name="keyword" label="姓名/工号">
          <Input allowClear />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit">
            查询
          </Button>
        </Form.Item>
      </Form>

      <Table
        rowKey="employeeId"
        loading={loading}
        dataSource={data.list || []}
        columns={[
          { title: '工号', dataIndex: 'employeeNo' },
          { title: '姓名', dataIndex: 'employeeName' },
          { title: '岗位', dataIndex: 'positionName' },
          { title: '记录天数', dataIndex: 'totalDays' },
          { title: '出勤天数', dataIndex: 'attendedDays' },
          { title: '迟到天数', dataIndex: 'lateDays' },
          { title: '早退天数', dataIndex: 'earlyLeaveDays' },
          { title: '缺勤天数', dataIndex: 'absentDays' },
          { title: '缺卡天数', dataIndex: 'missingCardDays' },
          { title: '加班小时', dataIndex: 'overtimeHours' }
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

export default TeamAttendancePage
