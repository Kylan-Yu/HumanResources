import React, { useState, useEffect } from 'react'
import { Card, Table, Button, Space, Modal, Form, Input, Select, Switch, message, Popconfirm, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons'
import { getEmployeePage, deleteEmployee, updateEmployeeStatus } from '@/api/employee'
import type { Employee, PageResult } from '@/types/employee'

const EmployeeList: React.FC = () => {
  const [employees, setEmployees] = useState<PageResult<Employee>>({ list: [], total: 0 })
  const [loading, setLoading] = useState(false)
  const [searchParams, setSearchParams] = useState({
    pageNum: 1,
    pageSize: 10,
    employeeNo: '',
    name: '',
    mobile: '',
    employeeStatus: undefined,
    deptId: undefined,
    orgId: undefined,
    industryType: undefined
  })

  // 加载员工列表
  const loadEmployees = async () => {
    setLoading(true)
    try {
      const response = await getEmployeePage(searchParams)
      setEmployees(response.data)
    } catch (error) {
      message.error('加载员工列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadEmployees()
  }, [searchParams])

  // 搜索表单
  const SearchForm = () => (
    <Card style={{ marginBottom: 16 }}>
      <Form layout="inline" onFinish={(values) => {
        setSearchParams({ ...searchParams, ...values, pageNum: 1 })
      }}>
        <Form.Item name="employeeNo" label="员工编号">
          <Input placeholder="请输入员工编号" allowClear />
        </Form.Item>
        <Form.Item name="name" label="姓名">
          <Input placeholder="请输入姓名" allowClear />
        </Form.Item>
        <Form.Item name="mobile" label="手机号">
          <Input placeholder="请输入手机号" allowClear />
        </Form.Item>
        <Form.Item name="employeeStatus" label="员工状态">
          <Select placeholder="请选择状态" allowClear style={{ width: 120 }}>
            <Select.Option value={1}>在职</Select.Option>
            <Select.Option value={2}>离职</Select.Option>
            <Select.Option value={3}>退休</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="industryType" label="行业类型">
          <Select placeholder="请选择行业类型" allowClear style={{ width: 120 }}>
            <Select.Option value="company">企业</Select.Option>
            <Select.Option value="hospital">医院</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">搜索</Button>
            <Button onClick={() => {
              // 重置表单
              setSearchParams({ 
                pageNum: 1, 
                pageSize: 10, 
                employeeNo: '', 
                name: '', 
                mobile: '', 
                employeeStatus: undefined,
                deptId: undefined,
                orgId: undefined,
                industryType: undefined 
              })
            }}>重置</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  )

  // 表格列定义
  const columns = [
    {
      title: '员工编号',
      dataIndex: 'employeeNo',
      key: 'employeeNo',
      width: 120,
    },
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
      width: 100,
    },
    {
      title: '性别',
      dataIndex: 'genderDesc',
      key: 'genderDesc',
      width: 80,
    },
    {
      title: '年龄',
      dataIndex: 'age',
      key: 'age',
      width: 80,
    },
    {
      title: '手机号',
      dataIndex: 'mobile',
      key: 'mobile',
      width: 120,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      ellipsis: true,
    },
    {
      title: '所属组织',
      dataIndex: ['mainJob', 'orgName'],
      key: 'orgName',
      width: 120,
    },
    {
      title: '所属部门',
      dataIndex: ['mainJob', 'deptName'],
      key: 'deptName',
      width: 120,
    },
    {
      title: '岗位',
      dataIndex: ['mainJob', 'positionName'],
      key: 'positionName',
      width: 120,
    },
    {
      title: '员工状态',
      dataIndex: 'employeeStatus',
      key: 'employeeStatus',
      width: 100,
      render: (status: number, record: Employee) => {
        const statusMap = {
          1: { color: 'green', text: '在职' },
          2: { color: 'red', text: '离职' },
          3: { color: 'gray', text: '退休' }
        }
        const statusInfo = statusMap[status as keyof typeof statusMap]
        return <Tag color={statusInfo?.color}>{statusInfo?.text}</Tag>
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 160,
      render: (time: string) => time ? new Date(time).toLocaleString() : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right',
      render: (_: any, record: Employee) => (
        <Space>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => {
              // 跳转到详情页
              window.location.href = `/employee/detail/${record.id}`
            }}
          >
            详情
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => {
              // 跳转到编辑页
              window.location.href = `/employee/edit/${record.id}`
            }}
          >
            编辑
          </Button>
          <Button
            type="link"
            onClick={() => {
              const newStatus = record.employeeStatus === 1 ? 2 : 1
              Modal.confirm({
                title: '确认操作',
                content: `确定要${newStatus === 1 ? '启用' : '禁用'}该员工吗？`,
                onOk: async () => {
                  try {
                    await updateEmployeeStatus(record.id!, newStatus)
                    message.success('状态更新成功')
                    loadEmployees()
                  } catch (error) {
                    message.error('状态更新失败')
                  }
                },
              })
            }}
          >
            {record.employeeStatus === 1 ? '禁用' : '启用'}
          </Button>
          <Popconfirm
            title="确定要删除该员工吗？"
            content="删除后将无法恢复，请谨慎操作"
            onConfirm={async () => {
              try {
                await deleteEmployee(record.id!)
                message.success('删除员工成功')
                loadEmployees()
              } catch (error) {
                message.error('删除员工失败')
              }
            }}
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <SearchForm />
      <Card>
        <div style={{ marginBottom: 16 }}>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              window.location.href = '/employee/create'
            }}
          >
            新增员工
          </Button>
        </div>
        <Table
          columns={columns}
          dataSource={employees.list}
          rowKey="id"
          loading={loading}
          pagination={{
            current: searchParams.pageNum,
            pageSize: searchParams.pageSize,
            total: employees.total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, pageSize) => {
              setSearchParams({ ...searchParams, pageNum: page, pageSize })
            },
          }}
          scroll={{ x: 1400 }}
        />
      </Card>
    </div>
  )
}

export default EmployeeList
