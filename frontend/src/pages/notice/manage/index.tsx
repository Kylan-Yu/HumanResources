import React, { useEffect, useState } from 'react'
import { Button, Card, Form, Input, Modal, Popconfirm, Select, Space, Switch, Table } from 'antd'
import { createNotice, deleteNotice, getNoticePage, updateNotice } from '@/api/notice'

const NoticeManagePage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<any>({ pageNum: 1, pageSize: 10 })
  const [data, setData] = useState<any>({ list: [], total: 0 })
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<any>(null)
  const [searchForm] = Form.useForm()
  const [form] = Form.useForm()

  const loadData = async (extra?: any) => {
    const params = { ...query, ...(extra || {}) }
    setLoading(true)
    try {
      const res = await getNoticePage(params)
      setData(res.data || { list: [], total: 0 })
      setQuery(params)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const submit = async () => {
    const values = await form.validateFields()
    const payload = {
      ...values,
      topFlag: values.topFlag ? 1 : 0,
      targetDeptIds: values.publishScope === 'DEPT' ? values.targetDeptIds : null
    }
    if (editing?.id) {
      await updateNotice(editing.id, payload)
    } else {
      await createNotice(payload)
    }
    setOpen(false)
    loadData()
  }

  return (
    <Card>
      <Form
        form={searchForm}
        layout="inline"
        onFinish={(values) => loadData({ ...values, pageNum: 1 })}
        style={{ marginBottom: 16 }}
      >
        <Form.Item name="title" label="标题">
          <Input allowClear />
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
        <Form.Item name="status" label="状态">
          <Select
            allowClear
            style={{ width: 140 }}
            options={[
              { value: 'PUBLISHED', label: '已发布' },
              { value: 'DRAFT', label: '草稿' }
            ]}
          />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">查询</Button>
            <Button
              onClick={() => {
                setEditing(null)
                form.resetFields()
                form.setFieldsValue({ status: 'PUBLISHED', category: 'COMPANY', publishScope: 'ALL', topFlag: false })
                setOpen(true)
              }}
            >
              新建公告
            </Button>
          </Space>
        </Form.Item>
      </Form>

      <Table
        rowKey="id"
        loading={loading}
        dataSource={data.list || []}
        columns={[
          { title: '标题', dataIndex: 'title' },
          { title: '分类', dataIndex: 'category' },
          { title: '范围', dataIndex: 'publishScope' },
          { title: '状态', dataIndex: 'status' },
          { title: '发布时间', dataIndex: 'publishedTime' },
          {
            title: '操作',
            render: (_: any, row: any) => (
              <Space>
                <Button
                  type="link"
                  onClick={() => {
                    setEditing(row)
                    form.setFieldsValue({
                      ...row,
                      topFlag: row.topFlag === 1
                    })
                    setOpen(true)
                  }}
                >
                  编辑
                </Button>
                <Popconfirm title="确定删除该公告吗？" onConfirm={async () => { await deleteNotice(row.id); loadData() }}>
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

      <Modal
        title={editing ? '编辑公告' : '新建公告'}
        open={open}
        width={720}
        onCancel={() => setOpen(false)}
        onOk={submit}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label="标题" rules={[{ required: true, message: '请输入标题' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="content" label="内容" rules={[{ required: true, message: '请输入内容' }]}>
            <Input.TextArea rows={6} />
          </Form.Item>
          <Form.Item name="category" label="分类" rules={[{ required: true, message: '请选择分类' }]}>
            <Select
              options={[
                { value: 'COMPANY', label: '公司公告' },
                { value: 'POLICY', label: '制度通知' },
                { value: 'TRAINING', label: '培训通知' },
                { value: 'HOLIDAY', label: '节假日安排' }
              ]}
            />
          </Form.Item>
          <Form.Item name="publishScope" label="发布范围" rules={[{ required: true, message: '请选择发布范围' }]}>
            <Select options={[{ value: 'ALL', label: '全员' }, { value: 'DEPT', label: '部门定向' }]} />
          </Form.Item>
          <Form.Item noStyle shouldUpdate={(prev, curr) => prev.publishScope !== curr.publishScope}>
            {({ getFieldValue }) =>
              getFieldValue('publishScope') === 'DEPT' ? (
                <Form.Item name="targetDeptIds" label="部门ID列表(逗号分隔)">
                  <Input placeholder="例如：1,3,5" />
                </Form.Item>
              ) : null
            }
          </Form.Item>
          <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
            <Select options={[{ value: 'PUBLISHED', label: '发布' }, { value: 'DRAFT', label: '草稿' }]} />
          </Form.Item>
          <Form.Item name="topFlag" label="置顶" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item name="attachmentJson" label="附件预留(JSON)">
            <Input.TextArea rows={2} placeholder='例如：[{"name":"附件1","url":"..."}]' />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}

export default NoticeManagePage
