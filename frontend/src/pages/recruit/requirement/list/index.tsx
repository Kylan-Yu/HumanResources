import React, { useState, useEffect } from 'react'
import { Card, Table, Button, Space, Modal, Form, Input, Select, DatePicker, message, Popconfirm, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons'
import { getRecruitRequirementPage, deleteRecruitRequirement, updateRecruitRequirementStatus } from '@/api/recruit/requirement'
import type { RecruitRequirement, RecruitRequirementQueryParams } from '@/types/recruit'

const { Option } = Select
const { RangePicker } = DatePicker

const RecruitRequirementList: React.FC = () => {
  const [requirements, setRequirements] = useState<any>({ list: [], total: 0 })
  const [loading, setLoading] = useState(false)
  const [searchParams, setSearchParams] = useState<RecruitRequirementQueryParams>({
    pageNum: 1,
    pageSize: 10,
    title: '',
    orgId: undefined,
    deptId: undefined,
    positionId: undefined,
    requirementStatus: '',
    urgencyLevel: '',
    industryType: '',
    dateRange: []
  })

  useEffect(() => {
    loadRequirements()
  }, [searchParams])

  const loadRequirements = async () => {
    setLoading(true)
    try {
      const params = {
        ...searchParams,
        expectedEntryDateBegin: searchParams.dateRange?.[0],
        expectedEntryDateEnd: searchParams.dateRange?.[1]
      }
      delete params.dateRange
      const response = await getRecruitRequirementPage(params)
      setRequirements(response.data)
    } catch (error) {
      message.error('加载招聘需求列表失败')
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
      title: '',
      orgId: undefined,
      deptId: undefined,
      positionId: undefined,
      requirementStatus: '',
      urgencyLevel: '',
      industryType: '',
      dateRange: []
    })
  }

  const handleDelete = async (id: number) => {
    try {
      await deleteRecruitRequirement(id)
      message.success('删除成功')
      loadRequirements()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleStatusChange = async (id: number, status: string) => {
    try {
      await updateRecruitRequirementStatus(id, status)
      message.success('状态更新成功')
      loadRequirements()
    } catch (error) {
      message.error('状态更新失败')
    }
  }

  const getStatusColor = (status: string) => {
    const colorMap: Record<string, string> = {
      'DRAFT': 'default',
      'OPEN': 'green',
      'CLOSED': 'red',
      'CANCELLED': 'gray'
    }
    return colorMap[status] || 'default'
  }

  const getUrgencyColor = (urgency: string) => {
    const colorMap: Record<string, string> = {
      'HIGH': 'red',
      'MEDIUM': 'orange',
      'LOW': 'blue'
    }
    return colorMap[urgency] || 'default'
  }

  const columns: any[] = [
    {
      title: '需求编号',
      dataIndex: 'requirementNo',
      key: 'requirementNo',
      width: 150,
    },
    {
      title: '需求标题',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
      width: 200,
    },
    {
      title: '组织/部门',
      key: 'orgDept',
      width: 150,
      render: (_: any, record: RecruitRequirement) => (
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
      title: '招聘人数',
      dataIndex: 'headcount',
      key: 'headcount',
      width: 100,
    },
    {
      title: '紧急程度',
      dataIndex: 'urgencyLevel',
      key: 'urgencyLevel',
      width: 100,
      render: (urgency: string, record: RecruitRequirement) => (
        <Tag color={getUrgencyColor(urgency)}>
          {record.urgencyLevelDesc}
        </Tag>
      )
    },
    {
      title: '状态',
      dataIndex: 'requirementStatus',
      key: 'requirementStatus',
      width: 100,
      render: (status: string, record: RecruitRequirement) => (
        <Tag color={getStatusColor(status)}>
          {record.requirementStatusDesc}
        </Tag>
      )
    },
    {
      title: '期望入职日期',
      dataIndex: 'expectedEntryDate',
      key: 'expectedEntryDate',
      width: 120,
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
      render: (_: any, record: RecruitRequirement) => (
        <Space>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => {
              window.location.href = `/recruit/requirement/detail/${record.id}`
            }}
          >
            详情
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => {
              window.location.href = `/recruit/requirement/edit/${record.id}`
            }}
          >
            编辑
          </Button>
          <Button
            type="link"
            onClick={() => {
              const newStatus = record.requirementStatus === 'OPEN' ? 'CLOSED' : 'OPEN'
              Modal.confirm({
                title: '确认操作',
                content: `确定要${newStatus === 'OPEN' ? '开放' : '关闭'}该需求吗？`,
                onOk: () => handleStatusChange(record.id!, newStatus),
              })
            }}
          >
            {record.requirementStatus === 'OPEN' ? '关闭' : '开放'}
          </Button>
          <Popconfirm
            title="确定要删除该招聘需求吗？"
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
          <Form.Item name="title" label="需求标题">
            <Input placeholder="请输入需求标题" allowClear />
          </Form.Item>
          <Form.Item name="requirementStatus" label="需求状态">
            <Select placeholder="请选择状态" allowClear style={{ width: 120 }}>
              <Option value="DRAFT">草稿</Option>
              <Option value="OPEN">开放</Option>
              <Option value="CLOSED">关闭</Option>
              <Option value="CANCELLED">取消</Option>
            </Select>
          </Form.Item>
          <Form.Item name="urgencyLevel" label="紧急程度">
            <Select placeholder="请选择紧急程度" allowClear style={{ width: 120 }}>
              <Option value="HIGH">高</Option>
              <Option value="MEDIUM">中</Option>
              <Option value="LOW">低</Option>
            </Select>
          </Form.Item>
          <Form.Item name="industryType" label="行业类型">
            <Select placeholder="请选择行业类型" allowClear style={{ width: 120 }}>
              <Option value="company">企业</Option>
              <Option value="hospital">医院</Option>
            </Select>
          </Form.Item>
          <Form.Item name="dateRange" label="期望入职日期">
            <RangePicker />
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
              window.location.href = '/recruit/requirement/create'
            }}
          >
            新增招聘需求
          </Button>
        </div>
        
        <Table
          columns={columns}
          dataSource={requirements.list}
          rowKey="id"
          loading={loading}
          pagination={{
            current: searchParams.pageNum,
            pageSize: searchParams.pageSize,
            total: requirements.total,
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

export default RecruitRequirementList
