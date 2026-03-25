import React, { useState, useEffect } from 'react'
import {
  Card,
  Table,
  Button,
  Space,
  Form,
  Input,
  Select,
  Modal,
  message,
  Popconfirm,
  Tag,
  Row,
  Col
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'

interface Position {
  id: number
  positionCode: string
  positionName: string
  orgId: number
  deptId: number
  positionCategory: string
  rankGrade: string
  rankSeries: string
  jobDescription: string
  requirements: string
  status: number
  sortOrder: number
  extJson: string
  createdBy: number
  createdTime: string
  updatedBy: number
  updatedTime: string
  deleted: number
}

interface Org {
  id: number
  orgName: string
}

interface Dept {
  id: number
  deptName: string
}

const PositionManagement: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<Position[]>([])
  const [total, setTotal] = useState(0)
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [orgList, setOrgList] = useState<Org[]>([])
  const [deptList, setDeptList] = useState<Dept[]>([])
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [editingPosition, setEditingPosition] = useState<Position | null>(null)
  const [form] = Form.useForm()

  const authHeaders = () => {
    const token = localStorage.getItem('token')
    return token ? { Authorization: `Bearer ${token}` } : {}
  }

  // 获取组织列表
  const fetchOrgList = async () => {
    try {
      const response = await fetch('/api/org/list', {
        headers: authHeaders()
      })
      const result = await response.json()
      
      if (result.code === 200) {
        setOrgList(result.data)
      } else {
        message.error(result.message || '获取组织列表失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 获取部门列表
  const fetchDeptList = async () => {
    try {
      const response = await fetch('/api/dept/list', {
        headers: authHeaders()
      })
      const result = await response.json()
      
      if (result.code === 200) {
        setDeptList(result.data)
      } else {
        message.error(result.message || '获取部门列表失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 获取岗位列表
  const fetchPositions = async (params: any = {}) => {
    setLoading(true)
    try {
      const queryParams = new URLSearchParams({
        pageNum: String(params.pageNum || currentPage),
        pageSize: String(params.pageSize || pageSize),
        ...(params.positionName && { positionName: params.positionName }),
        ...(params.positionCode && { positionCode: params.positionCode }),
        ...(params.orgId && { orgId: String(params.orgId) }),
        ...(params.deptId && { deptId: String(params.deptId) }),
        ...(params.status !== undefined && { status: String(params.status) })
      })

      const response = await fetch(`/api/position/page?${queryParams}`, {
        headers: authHeaders()
      })
      const result = await response.json()
      
      if (result.code === 200) {
        setData(result.data.records)
        setTotal(result.data.total)
      } else {
        message.error(result.message || '获取岗位列表失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchOrgList()
    fetchDeptList()
    fetchPositions()
  }, [])

  // 创建或更新岗位
  const handleSave = async (values: any) => {
    try {
      const url = editingPosition ? `/api/position/${editingPosition.id}` : '/api/position'
      const method = editingPosition ? 'PUT' : 'POST'
      
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          ...authHeaders()
        },
        body: JSON.stringify(values)
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        message.success(editingPosition ? '更新成功' : '创建成功')
        setIsModalVisible(false)
        form.resetFields()
        setEditingPosition(null)
        fetchPositions()
      } else {
        message.error(result.message || '操作失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 删除岗位
  const handleDelete = async (id: number) => {
    try {
      const response = await fetch(`/api/position/${id}`, {
        method: 'DELETE',
        headers: authHeaders()
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        message.success('删除成功')
        fetchPositions()
      } else {
        message.error(result.message || '删除失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 更新岗位状态
  const handleStatusChange = async (id: number, status: number) => {
    try {
      const response = await fetch(`/api/position/${id}/status?status=${status}`, {
        method: 'PUT',
        headers: authHeaders()
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        message.success('状态更新成功')
        fetchPositions()
      } else {
        message.error(result.message || '状态更新失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 编辑岗位
  const handleEdit = (record: Position) => {
    setEditingPosition(record)
    form.setFieldsValue(record)
    setIsModalVisible(true)
  }

  // 搜索
  const handleSearch = (values: any) => {
    setCurrentPage(1)
    fetchPositions({ ...values, pageNum: 1 })
  }

  // 重置
  const handleReset = () => {
    form.resetFields()
    setCurrentPage(1)
    fetchPositions({ pageNum: 1 })
  }

  const columns: ColumnsType<Position> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80
    },
    {
      title: '岗位编码',
      dataIndex: 'positionCode',
      key: 'positionCode',
      width: 120
    },
    {
      title: '岗位名称',
      dataIndex: 'positionName',
      key: 'positionName',
      width: 150
    },
    {
      title: '所属组织',
      dataIndex: 'orgId',
      key: 'orgId',
      width: 120,
      render: (orgId: number) => {
        const org = orgList.find(o => o.id === orgId)
        return org ? org.orgName : '-'
      }
    },
    {
      title: '所属部门',
      dataIndex: 'deptId',
      key: 'deptId',
      width: 120,
      render: (deptId: number) => {
        const dept = deptList.find(d => d.id === deptId)
        return dept ? dept.deptName : '-'
      }
    },
    {
      title: '岗位类别',
      dataIndex: 'positionCategory',
      key: 'positionCategory',
      width: 100,
      render: (category: string) => {
        const categoryMap: { [key: string]: { text: string; color: string } } = {
          'management': { text: '管理岗', color: 'blue' },
          'technical': { text: '技术岗', color: 'green' },
          'operation': { text: '运营岗', color: 'orange' },
          'support': { text: '支持岗', color: 'purple' }
        }
        const config = categoryMap[category] || { text: category, color: 'default' }
        return <Tag color={config.color}>{config.text}</Tag>
      }
    },
    {
      title: '职级',
      dataIndex: 'rankGrade',
      key: 'rankGrade',
      width: 80
    },
    {
      title: '职系',
      dataIndex: 'rankSeries',
      key: 'rankSeries',
      width: 80
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number, record: Position) => (
        <Tag
          color={status === 1 ? 'green' : 'red'}
          style={{ cursor: 'pointer' }}
          onClick={() => handleStatusChange(record.id, status === 1 ? 0 : 1)}
        >
          {status === 1 ? '启用' : '禁用'}
        </Tag>
      )
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 180,
      render: (time: string) => new Date(time).toLocaleString()
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record: Position) => (
        <Space size="small">
          <Button
            type="link"
            icon={<EditOutlined />}
            size="small"
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个岗位吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              danger
              icon={<DeleteOutlined />}
              size="small"
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div>
      <Card>
        <Form
          form={form}
          layout="inline"
          onFinish={handleSearch}
          style={{ marginBottom: 16 }}
        >
          <Form.Item name="positionName" label="岗位名称">
            <Input placeholder="请输入岗位名称" allowClear />
          </Form.Item>
          <Form.Item name="positionCode" label="岗位编码">
            <Input placeholder="请输入岗位编码" allowClear />
          </Form.Item>
          <Form.Item name="orgId" label="所属组织">
            <Select placeholder="请选择组织" allowClear style={{ width: 150 }}>
              {orgList.map(org => (
                <Select.Option key={org.id} value={org.id}>
                  {org.orgName}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="deptId" label="所属部门">
            <Select placeholder="请选择部门" allowClear style={{ width: 150 }}>
              {deptList.map(dept => (
                <Select.Option key={dept.id} value={dept.id}>
                  {dept.deptName}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select placeholder="请选择状态" allowClear style={{ width: 120 }}>
              <Select.Option value={1}>启用</Select.Option>
              <Select.Option value={0}>禁用</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                搜索
              </Button>
              <Button onClick={handleReset} icon={<ReloadOutlined />}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>

        <div style={{ marginBottom: 16 }}>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditingPosition(null)
              form.resetFields()
              setIsModalVisible(true)
            }}
          >
            新建岗位
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          pagination={{
            current: currentPage,
            pageSize,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
            onChange: (page, size) => {
              setCurrentPage(page)
              setPageSize(size)
              fetchPositions({ pageNum: page, pageSize: size })
            }
          }}
        />
      </Card>

      <Modal
        title={editingPosition ? '编辑岗位' : '新建岗位'}
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false)
          form.resetFields()
          setEditingPosition(null)
        }}
        footer={null}
        width={800}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSave}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="positionCode"
                label="岗位编码"
                rules={[{ required: true, message: '请输入岗位编码' }]}
              >
                <Input placeholder="请输入岗位编码" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="positionName"
                label="岗位名称"
                rules={[{ required: true, message: '请输入岗位名称' }]}
              >
                <Input placeholder="请输入岗位名称" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="orgId"
                label="所属组织"
                rules={[{ required: true, message: '请选择所属组织' }]}
              >
                <Select placeholder="请选择所属组织">
                  {orgList.map(org => (
                    <Select.Option key={org.id} value={org.id}>
                      {org.orgName}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="deptId"
                label="所属部门"
                rules={[{ required: true, message: '请选择所属部门' }]}
              >
                <Select placeholder="请选择所属部门">
                  {deptList.map(dept => (
                    <Select.Option key={dept.id} value={dept.id}>
                      {dept.deptName}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="positionCategory"
                label="岗位类别"
              >
                <Select placeholder="请选择岗位类别">
                  <Select.Option value="management">管理岗</Select.Option>
                  <Select.Option value="technical">技术岗</Select.Option>
                  <Select.Option value="operation">运营岗</Select.Option>
                  <Select.Option value="support">支持岗</Select.Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="rankGrade"
                label="职级"
              >
                <Input placeholder="请输入职级" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="rankSeries"
                label="职系"
              >
                <Input placeholder="请输入职系" />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item
            name="jobDescription"
            label="岗位职责"
          >
            <Input.TextArea rows={4} placeholder="请输入岗位职责" />
          </Form.Item>
          <Form.Item
            name="requirements"
            label="任职要求"
          >
            <Input.TextArea rows={4} placeholder="请输入任职要求" />
          </Form.Item>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="sortOrder"
                label="排序"
                initialValue={0}
              >
                <Input type="number" placeholder="请输入排序值" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="status"
                label="状态"
                initialValue={1}
              >
                <Select>
                  <Select.Option value={1}>启用</Select.Option>
                  <Select.Option value={0}>禁用</Select.Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingPosition ? '更新' : '创建'}
              </Button>
              <Button
                onClick={() => {
                  setIsModalVisible(false)
                  form.resetFields()
                  setEditingPosition(null)
                }}
              >
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default PositionManagement
