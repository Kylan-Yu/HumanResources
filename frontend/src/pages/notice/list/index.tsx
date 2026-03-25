import React, { useEffect, useState } from 'react'
import { Button, Card, Drawer, Form, Input, Select, Space, Table, Tag, Typography } from 'antd'
import { getCurrentNoticePage, getNoticeDetail, markNoticeRead } from '@/api/notice'

const NoticeListPage: React.FC = () => {
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
      const res = await getCurrentNoticePage(params)
      setData(res.data || { list: [], total: 0 })
      setQuery(params)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const openDetail = async (record: any) => {
    const res = await getNoticeDetail(record.id)
    setDetail(res.data)
    setDetailOpen(true)
    if (!record.readFlag) {
      await markNoticeRead(record.id)
      loadData()
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
        <Form.Item name="title" label="公告标题">
          <Input allowClear placeholder="请输入标题" />
        </Form.Item>
        <Form.Item name="category" label="分类">
          <Select
            allowClear
            style={{ width: 160 }}
            options={[
              { value: 'COMPANY', label: '公司公告' },
              { value: 'POLICY', label: '制度通知' },
              { value: 'TRAINING', label: '培训通知' },
              { value: 'HOLIDAY', label: '节假日安排' }
            ]}
          />
        </Form.Item>
        <Form.Item name="readStatus" label="已读状态">
          <Select
            allowClear
            style={{ width: 140 }}
            options={[
              { value: 1, label: '已读' },
              { value: 0, label: '未读' }
            ]}
          />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">查询</Button>
            <Button
              onClick={() => {
                form.resetFields()
                loadData({ pageNum: 1, pageSize: query.pageSize })
              }}
            >
              重置
            </Button>
          </Space>
        </Form.Item>
      </Form>

      <Table
        rowKey="id"
        loading={loading}
        dataSource={data.list || []}
        columns={[
          {
            title: '标题',
            dataIndex: 'title',
            render: (v: any, row: any) => (
              <Space>
                {row.topFlag === 1 && <Tag color="red">置顶</Tag>}
                {!row.readFlag && <Tag color="blue">未读</Tag>}
                <a onClick={() => openDetail(row)}>{v}</a>
              </Space>
            )
          },
          {
            title: '分类',
            dataIndex: 'category'
          },
          {
            title: '发布时间',
            dataIndex: 'publishedTime'
          }
        ]}
        pagination={{
          current: query.pageNum,
          pageSize: query.pageSize,
          total: data.total || 0,
          onChange: (page, pageSize) => loadData({ ...query, pageNum: page, pageSize })
        }}
      />

      <Drawer
        title={detail?.title || '公告详情'}
        open={detailOpen}
        width={720}
        onClose={() => setDetailOpen(false)}
      >
        <Space style={{ marginBottom: 12 }}>
          <Tag>{detail?.category}</Tag>
          {detail?.topFlag === 1 && <Tag color="red">置顶</Tag>}
        </Space>
        <Typography.Paragraph>{detail?.content}</Typography.Paragraph>
      </Drawer>
    </Card>
  )
}

export default NoticeListPage
