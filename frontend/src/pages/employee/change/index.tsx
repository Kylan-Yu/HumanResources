import React, { useEffect, useState } from 'react'
import { Button, Card, DatePicker, Form, Input, Modal, Popconfirm, Select, Space, Table, Tag, message } from 'antd'
import dayjs from 'dayjs'
import { createEmployeeChange, deleteEmployeeChange, getEmployeeChangePage, updateEmployeeChange } from '@/api/employeeChange'

const changeTypeOptions = [
  { value: 'entry', label: '入职' },
  { value: 'transfer', label: '调动' },
  { value: 'promotion', label: '晋升' },
  { value: 'demotion', label: '降级' },
  { value: 'resign', label: '离职' },
  { value: 'retire', label: '退休' }
]

const EmployeeChangePage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<any>({ list: [], total: 0 })
  const [query, setQuery] = useState<any>({ pageNum: 1, pageSize: 10, employeeName: '', changeType: undefined })
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<any>(null)
  const [form] = Form.useForm()

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await getEmployeeChangePage(query)
      setData(res.data || { list: [], total: 0 })
    } catch {
      message.error('加载员工异动数据失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [query.pageNum, query.pageSize, query.employeeName, query.changeType])

  const onSearch = (values: any) => {
    setQuery({ ...query, ...values, pageNum: 1 })
  }

  const onReset = () => {
    form.resetFields()
    setQuery({ pageNum: 1, pageSize: 10, employeeName: '', changeType: undefined })
  }

  const openCreate = () => {
    setEditing(null)
    form.setFieldsValue({ changeDate: dayjs() })
    setOpen(true)
  }

  const openEdit = (record: any) => {
    setEditing(record)
    form.setFieldsValue({ ...record, changeDate: record.changeDate ? dayjs(record.changeDate) : undefined })
    setOpen(true)
  }

  const onSubmit = async () => {
    const values = await form.validateFields()
    const payload = { ...values, changeDate: values.changeDate?.format('YYYY-MM-DD') }
    try {
      if (editing?.id) {
        await updateEmployeeChange(editing.id, payload)
        message.success('更新成功')
      } else {
        await createEmployeeChange(payload)
        message.success('创建成功')
      }
      setOpen(false)
      loadData()
    } catch {
      message.error('保存失败')
    }
  }

  const onDelete = async (id: number) => {
    await deleteEmployeeChange(id)
    message.success('删除成功')
    loadData()
  }

  const columns: any[] = [
    { title: '员工编号', dataIndex: 'employeeNo', key: 'employeeNo', width: 150 },
    { title: '员工姓名', dataIndex: 'employeeName', key: 'employeeName', width: 120 },
    {
      title: '异动类型',
      dataIndex: 'changeType',
      key: 'changeType',
      width: 120,
      render: (v: string) => {
        const item = changeTypeOptions.find((x) => x.value === v)
        return <Tag color="blue">{item?.label || v}</Tag>
      }
    },
    { title: '异动日期', dataIndex: 'changeDate', key: 'changeDate', width: 120 },
    { title: '异动原因', dataIndex: 'changeReason', key: 'changeReason', ellipsis: true },
    { title: '变更前', dataIndex: 'beforeValue', key: 'beforeValue', ellipsis: true },
    { title: '变更后', dataIndex: 'afterValue', key: 'afterValue', ellipsis: true },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => openEdit(record)}>编辑</Button>
          <Popconfirm title="确认删除该异动记录吗？" onConfirm={() => onDelete(record.id)}>
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
          <Form.Item name="employeeName" label="员工姓名">
            <Input allowClear placeholder="请输入员工姓名" />
          </Form.Item>
          <Form.Item name="changeType" label="异动类型">
            <Select allowClear style={{ width: 160 }} options={changeTypeOptions} />
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
          <Button type="primary" onClick={openCreate}>新增异动</Button>
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
        />
      </Card>

      <Modal title={editing ? '编辑异动' : '新增异动'} open={open} onCancel={() => setOpen(false)} onOk={onSubmit}>
        <Form form={form} layout="vertical">
          <Form.Item name="employeeId" label="员工ID" rules={[{ required: true, message: '请输入员工ID' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="changeType" label="异动类型" rules={[{ required: true, message: '请选择异动类型' }]}>
            <Select options={changeTypeOptions} />
          </Form.Item>
          <Form.Item name="changeDate" label="异动日期" rules={[{ required: true, message: '请选择异动日期' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="beforeValue" label="变更前">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="afterValue" label="变更后">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="changeReason" label="异动原因">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default EmployeeChangePage

