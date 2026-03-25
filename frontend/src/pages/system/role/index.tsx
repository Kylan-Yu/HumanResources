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
  Tag
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { useNavigate } from 'react-router-dom'
import { get, post, put, del } from '../../../utils/request'

interface Role {
  id: number
  roleCode: string
  roleName: string
  description: string
  status: number
  sortOrder: number
  createdBy: number
  createdTime: string
  updatedBy: number
  updatedTime: string
  deleted: number
}

const RoleManagement: React.FC = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<Role[]>([])
  const [total, setTotal] = useState(0)
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [editingRole, setEditingRole] = useState<Role | null>(null)
  const [form] = Form.useForm()

  // 获取角色列表
  const fetchRoles = async (params: any = {}) => {
    setLoading(true)
    try {
      const queryParams = {
        pageNum: params.pageNum || currentPage,
        pageSize: params.pageSize || pageSize,
        ...(params.roleName && { roleName: params.roleName }),
        ...(params.roleCode && { roleCode: params.roleCode }),
        ...(params.status !== undefined && { status: params.status })
      }

      const result = await get('/roles/page', queryParams)
      
      if (result.code === 200) {
        setData(result.data.records)
        setTotal(result.data.total)
      } else {
        message.error(result.message || '获取角色列表失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchRoles()
  }, [])

  // 创建或更新角色
  const handleSave = async (values: any) => {
    try {
      const url = editingRole ? `/roles/${editingRole.id}` : '/roles'
      
      const result = editingRole ? await put(url, values) : await post(url, values)
      
      if (result.code === 200) {
        message.success(editingRole ? '更新成功' : '创建成功')
        setIsModalVisible(false)
        form.resetFields()
        setEditingRole(null)
        fetchRoles()
      } else {
        message.error(result.message || '操作失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 删除角色
  const handleDelete = async (id: number) => {
    try {
      const result = await del(`/roles/${id}`)
      
      if (result.code === 200) {
        message.success('删除成功')
        fetchRoles()
      } else {
        message.error(result.message || '删除失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 更新角色状态
  const handleStatusChange = async (id: number, status: number) => {
    try {
      const result = await put(`/roles/${id}/status`, { status })
      
      if (result.code === 200) {
        message.success('状态更新成功')
        fetchRoles()
      } else {
        message.error(result.message || '状态更新失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 编辑角色
  const handleEdit = (record: Role) => {
    setEditingRole(record)
    form.setFieldsValue(record)
    setIsModalVisible(true)
  }

  // 搜索
  const handleSearch = (values: any) => {
    setCurrentPage(1)
    fetchRoles({ ...values, pageNum: 1 })
  }

  // 重置
  const handleReset = () => {
    form.resetFields()
    setCurrentPage(1)
    fetchRoles({ pageNum: 1 })
  }

  const columns: ColumnsType<Role> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80
    },
    {
      title: '角色编码',
      dataIndex: 'roleCode',
      key: 'roleCode',
      width: 120
    },
    {
      title: '角色名称',
      dataIndex: 'roleName',
      key: 'roleName',
      width: 150
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      key: 'sortOrder',
      width: 80
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number, record: Role) => (
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
      render: (_, record: Role) => (
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
            title="确定要删除这个角色吗？"
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
          <Form.Item name="roleName" label="角色名称">
            <Input placeholder="请输入角色名称" allowClear />
          </Form.Item>
          <Form.Item name="roleCode" label="角色编码">
            <Input placeholder="请输入角色编码" allowClear />
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
              setEditingRole(null)
              form.resetFields()
              setIsModalVisible(true)
            }}
          >
            新建角色
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
              fetchRoles({ pageNum: page, pageSize: size })
            }
          }}
        />
      </Card>

      <Modal
        title={editingRole ? '编辑角色' : '新建角色'}
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false)
          form.resetFields()
          setEditingRole(null)
        }}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSave}
        >
          <Form.Item
            name="roleCode"
            label="角色编码"
            rules={[{ required: true, message: '请输入角色编码' }]}
          >
            <Input placeholder="请输入角色编码" />
          </Form.Item>
          <Form.Item
            name="roleName"
            label="角色名称"
            rules={[{ required: true, message: '请输入角色名称' }]}
          >
            <Input placeholder="请输入角色名称" />
          </Form.Item>
          <Form.Item
            name="description"
            label="描述"
          >
            <Input.TextArea rows={4} placeholder="请输入角色描述" />
          </Form.Item>
          <Form.Item
            name="sortOrder"
            label="排序"
            initialValue={0}
          >
            <Input type="number" placeholder="请输入排序值" />
          </Form.Item>
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
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingRole ? '更新' : '创建'}
              </Button>
              <Button
                onClick={() => {
                  setIsModalVisible(false)
                  form.resetFields()
                  setEditingRole(null)
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

export default RoleManagement
