import React, { useEffect, useState } from 'react'
import { Button, Card, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag, message } from 'antd'
import { createRank, deleteRank, getRankPage, updateRank, updateRankStatus } from '@/api/rank'

const RankManagement: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<any>({ list: [], total: 0 })
  const [query, setQuery] = useState<any>({ pageNum: 1, pageSize: 10, rankName: '', rankCode: '', status: undefined })
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<any>(null)
  const [form] = Form.useForm()

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await getRankPage(query)
      setData(res.data || { list: [], total: 0 })
    } catch {
      message.error('加载职级数据失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [query.pageNum, query.pageSize, query.rankName, query.rankCode, query.status])

  const onSearch = (values: any) => {
    setQuery({ ...query, ...values, pageNum: 1 })
  }

  const onReset = () => {
    form.resetFields()
    setQuery({ pageNum: 1, pageSize: 10, rankName: '', rankCode: '', status: undefined })
  }

  const openCreate = () => {
    setEditing(null)
    form.setFieldsValue({ status: 1, rankLevel: 1, sortOrder: 0, industryType: 'company' })
    setOpen(true)
  }

  const openEdit = (record: any) => {
    setEditing(record)
    form.setFieldsValue(record)
    setOpen(true)
  }

  const onSubmit = async () => {
    const values = await form.validateFields()
    try {
      if (editing?.id) {
        await updateRank(editing.id, values)
        message.success('更新成功')
      } else {
        await createRank(values)
        message.success('创建成功')
      }
      setOpen(false)
      loadData()
    } catch {
      message.error('保存失败')
    }
  }

  const onDelete = async (id: number) => {
    await deleteRank(id)
    message.success('删除成功')
    loadData()
  }

  const onToggleStatus = async (record: any) => {
    const next = record.status === 1 ? 0 : 1
    await updateRankStatus(record.id, next)
    message.success('状态更新成功')
    loadData()
  }

  const columns: any[] = [
    { title: '职级编码', dataIndex: 'rankCode', key: 'rankCode', width: 140 },
    { title: '职级名称', dataIndex: 'rankName', key: 'rankName', width: 140 },
    { title: '职级序列', dataIndex: 'rankSeries', key: 'rankSeries', width: 140 },
    { title: '等级', dataIndex: 'rankLevel', key: 'rankLevel', width: 80 },
    { title: '说明', dataIndex: 'description', key: 'description', ellipsis: true },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (v: number) => <Tag color={v === 1 ? 'green' : 'default'}>{v === 1 ? '启用' : '禁用'}</Tag>
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => openEdit(record)}>编辑</Button>
          <Button type="link" onClick={() => onToggleStatus(record)}>{record.status === 1 ? '禁用' : '启用'}</Button>
          <Popconfirm title="确认删除该职级吗？" onConfirm={() => onDelete(record.id)}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div>
      <Card style={{ marginBottom: 16 }}>
        <Form layout="inline" onFinish={onSearch}>
          <Form.Item name="rankName" label="职级名称">
            <Input allowClear placeholder="请输入职级名称" />
          </Form.Item>
          <Form.Item name="rankCode" label="职级编码">
            <Input allowClear placeholder="请输入职级编码" />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select allowClear style={{ width: 120 }} options={[{ value: 1, label: '启用' }, { value: 0, label: '禁用' }]} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">查询</Button>
              <Button onClick={onReset}>重置</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <Button type="primary" onClick={openCreate}>新增职级</Button>
        </div>
        <Table
          rowKey="id"
          columns={columns}
          dataSource={data.list}
          loading={loading}
          pagination={{
            current: query.pageNum,
            pageSize: query.pageSize,
            total: data.total,
            onChange: (page, pageSize) => setQuery({ ...query, pageNum: page, pageSize })
          }}
          scroll={{ x: 1000 }}
        />
      </Card>

      <Modal title={editing ? '编辑职级' : '新增职级'} open={open} onCancel={() => setOpen(false)} onOk={onSubmit}>
        <Form form={form} layout="vertical">
          {!editing && (
            <Form.Item name="rankCode" label="职级编码" rules={[{ required: true, message: '请输入职级编码' }]}>
              <Input />
            </Form.Item>
          )}
          <Form.Item name="rankName" label="职级名称" rules={[{ required: true, message: '请输入职级名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="rankSeries" label="职级序列">
            <Input />
          </Form.Item>
          <Form.Item name="rankLevel" label="等级">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="sortOrder" label="排序">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select options={[{ value: 1, label: '启用' }, { value: 0, label: '禁用' }]} />
          </Form.Item>
          <Form.Item name="description" label="说明">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default RankManagement

