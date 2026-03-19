import React, { useState, useEffect } from 'react'
import { Card, Table, Button, Space, Modal, Form, Input, Select, message, Popconfirm, Tag, InputNumber } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons'
import { getPayrollStandardPage, deletePayrollStandard, updatePayrollStandardStatus } from '@/api/payroll/standard'
import type { PayrollStandard, PayrollStandardQueryParams } from '@/types/payroll'

const { Option } = Select

const PayrollStandardList: React.FC = () => {
  const [standards, setStandards] = useState<any>({ list: [], total: 0 })
  const [loading, setLoading] = useState(false)
  const [searchParams, setSearchParams] = useState<PayrollStandardQueryParams>({
    pageNum: 1,
    pageSize: 10,
    standardName: '',
    orgId: undefined,
    deptId: undefined,
    positionId: undefined,
    gradeLevel: '',
    status: '',
    industryType: ''
  })

  useEffect(() => {
    loadStandards()
  }, [searchParams])

  const loadStandards = async () => {
    setLoading(true)
    try {
      const response = await getPayrollStandardPage(searchParams)
      setStandards(response.data)
    } catch (error) {
      message.error('加载薪资标准列表失败')
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = (values: any) => {
    setSearchParams({ ...searchParams, ...values, pageNum: 1 })
  }

  const handleReset = () => {
    setSearchParams({ 
      pageNum: 1, 
      pageSize: 10,
      standardName: '',
      orgId: undefined,
      deptId: undefined,
      positionId: undefined,
      gradeLevel: '',
      status: '',
      industryType: ''
    })
  }

  const handleDelete = async (id: number) => {
    try {
      await deletePayrollStandard(id)
      message.success('删除成功')
      loadStandards()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleStatusChange = async (id: number, status: string) => {
    try {
      await updatePayrollStandardStatus(id, status)
      message.success('状态更新成功')
      loadStandards()
    } catch (error) {
      message.error('状态更新失败')
    }
  }

  const getStatusColor = (status: string) => {
    const colorMap: Record<string, string> = {
      'ACTIVE': 'green',
      'INACTIVE': 'red'
    }
    return colorMap[status] || 'default'
  }

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('zh-CN', {
      style: 'currency',
      currency: 'CNY'
    }).format(value)
  }

  const columns = [
    {
      title: '标准名称',
      dataIndex: 'standardName',
      key: 'standardName',
      width: 200,
      ellipsis: true,
    },
    {
      title: '组织/部门',
      key: 'orgDept',
      width: 150,
      render: (_: any, record: PayrollStandard) => (
        <div>
          <div>{record.orgName}</div>
          <div style={{ fontSize: '12px', color: '#999' }}>{record.deptName}</div>
        </div>
      )
    },
    {
      title: '职位',
      dataIndex: 'positionName',
      key: 'positionName',
      width: 120,
    },
    {
      title: '职级',
      dataIndex: 'gradeLevel',
      key: 'gradeLevel',
      width: 100,
    },
    {
      title: '基本薪资',
      dataIndex: 'baseSalary',
      key: 'baseSalary',
      width: 120,
      render: (value: number) => formatCurrency(value),
    },
    {
      title: '绩效薪资',
      dataIndex: 'performanceSalary',
      key: 'performanceSalary',
      width: 120,
      render: (value: number) => value ? formatCurrency(value) : '-',
    },
    {
      title: '岗位津贴',
      dataIndex: 'positionAllowance',
      key: 'positionAllowance',
      width: 120,
      render: (value: number) => value ? formatCurrency(value) : '-',
    },
    {
      title: '各项补贴',
      key: 'allowances',
      width: 120,
      render: (_: any, record: PayrollStandard) => {
        const allowances = [
          record.mealAllowance,
          record.transportAllowance,
          record.communicationAllowance,
          record.housingAllowance,
          record.otherAllowance
        ].filter(v => v && v > 0)
        
        if (allowances.length === 0) return '-'
        
        const total = allowances.reduce((sum, v) => sum + v, 0)
        return formatCurrency(total)
      },
    },
    {
      title: '总薪资',
      dataIndex: 'totalSalary',
      key: 'totalSalary',
      width: 120,
      render: (value: number) => (
        <span style={{ fontWeight: 'bold', color: '#1890ff' }}>
          {formatCurrency(value)}
        </span>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string, record: PayrollStandard) => (
        <Tag color={getStatusColor(status)}>
          {record.statusDesc}
        </Tag>
      )
    },
    {
      title: '行业类型',
      dataIndex: 'industryTypeDesc',
      key: 'industryTypeDesc',
      width: 100,
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 160,
      render: (time: string) => new Date(time).toLocaleString(),
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      fixed: 'right',
      render: (_: any, record: PayrollStandard) => (
        <Space>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => {
              window.location.href = `/payroll/standard/detail/${record.id}`
            }}
          >
            详情
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => {
              window.location.href = `/payroll/standard/edit/${record.id}`
            }}
          >
            编辑
          </Button>
          <Button
            type="link"
            onClick={() => {
              const newStatus = record.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
              Modal.confirm({
                title: '确认操作',
                content: `确定要${newStatus === 'ACTIVE' ? '启用' : '禁用'}该薪资标准吗？`,
                onOk: () => handleStatusChange(record.id!, newStatus),
              })
            }}
          >
            {record.status === 'ACTIVE' ? '禁用' : '启用'}
          </Button>
          <Popconfirm
            title="确定要删除该薪资标准吗？"
            onConfirm={() => handleDelete(record.id!)}
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
      <Card style={{ marginBottom: 16 }}>
        <Form layout="inline" onFinish={handleSearch}>
          <Form.Item name="standardName" label="标准名称">
            <Input placeholder="请输入标准名称" allowClear />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select placeholder="请选择状态" allowClear style={{ width: 120 }}>
              <Option value="ACTIVE">启用</Option>
              <Option value="INACTIVE">禁用</Option>
            </Select>
          </Form.Item>
          <Form.Item name="gradeLevel" label="职级">
            <Input placeholder="请输入职级" allowClear style={{ width: 120 }} />
          </Form.Item>
          <Form.Item name="industryType" label="行业类型">
            <Select placeholder="请选择行业类型" allowClear style={{ width: 120 }}>
              <Option value="company">企业</Option>
              <Option value="hospital">医院</Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">搜索</Button>
              <Button onClick={handleReset}>重置</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              window.location.href = '/payroll/standard/create'
            }}
          >
            新增薪资标准
          </Button>
        </div>
        
        <Table
          columns={columns}
          dataSource={standards.list}
          rowKey="id"
          loading={loading}
          pagination={{
            current: searchParams.pageNum,
            pageSize: searchParams.pageSize,
            total: standards.total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, pageSize) => {
              setSearchParams({ ...searchParams, pageNum: page, pageSize })
            },
          }}
          scroll={{ x: 1600 }}
        />
      </Card>
    </div>
  )
}

export default PayrollStandardList
