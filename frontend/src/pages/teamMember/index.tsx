import React, { useEffect, useState } from 'react'
import { Button, Card, Form, Input, Table } from 'antd'
import { getTeamMemberPage } from '@/api/employee'

const TeamMemberPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<any>({ pageNum: 1, pageSize: 10 })
  const [data, setData] = useState<any>({ list: [], total: 0 })
  const [form] = Form.useForm()

  const loadData = async (extra?: any) => {
    const params = { ...query, ...(extra || {}) }
    setLoading(true)
    try {
      const res = await getTeamMemberPage(params)
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
    <Card title="团队成员">
      <Form
        form={form}
        layout="inline"
        onFinish={(values) => loadData({ ...values, pageNum: 1 })}
        style={{ marginBottom: 16 }}
      >
        <Form.Item name="keyword" label="姓名/岗位/电话">
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
          { title: '姓名', dataIndex: 'employeeName' },
          { title: '岗位', dataIndex: 'positionName' },
          { title: '电话', dataIndex: 'mobile' },
          { title: '入职时间', dataIndex: 'entryDate' }
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

export default TeamMemberPage
