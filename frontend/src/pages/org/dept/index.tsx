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
  ReloadOutlined,
  FolderOutlined,
  FileOutlined
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'

interface Dept {
  id: number
  deptCode: string
  deptName: string
  orgId: number
  parentId: number
  deptType: string
  manager: string
  phone: string
  email: string
  address: string
  description: string
  status: number
  sortOrder: number
  extJson: string
  createdBy: number
  createdTime: string
  updatedBy: number
  updatedTime: string
  deleted: number
  children?: Dept[]
}

interface Org {
  id: number
  orgName: string
}

const DeptManagement: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [deptTree, setDeptTree] = useState<Dept[]>([])
  const [deptList, setDeptList] = useState<Dept[]>([])
  const [orgList, setOrgList] = useState<Org[]>([])
  const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([])
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [editingDept, setEditingDept] = useState<Dept | null>(null)
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

  // 获取部门树
  const fetchDeptTree = async () => {
    setLoading(true)
    try {
      const response = await fetch('/api/dept/tree', {
        headers: authHeaders()
      })
      const result = await response.json()
      
      if (result.code === 200) {
        setDeptTree(result.data)
        setDeptList(flattenTree(result.data))
        // 默认展开所有行
        const allKeys = flattenTree(result.data).map(item => item.id)
        setExpandedRowKeys(allKeys)
      } else {
        message.error(result.message || '获取部门树失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchOrgList()
    fetchDeptTree()
  }, [])

  // 扁平化树形数据
  const flattenTree = (tree: Dept[]): Dept[] => {
    const result: Dept[] = []
    const traverse = (nodes: Dept[]) => {
      nodes.forEach(node => {
        result.push(node)
        if (node.children && node.children.length > 0) {
          traverse(node.children)
        }
      })
    }
    traverse(tree)
    return result
  }

  // 全部展开
  const handleExpandAll = () => {
    const allKeys = deptList.map(item => item.id)
    setExpandedRowKeys(allKeys)
  }

  // 全部折叠
  const handleCollapseAll = () => {
    setExpandedRowKeys([])
  }

  // 创建或更新部门
  const handleSave = async (values: any) => {
    try {
      const url = editingDept ? `/api/dept/${editingDept.id}` : '/api/dept'
      const method = editingDept ? 'PUT' : 'POST'
      
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
        message.success(editingDept ? '更新成功' : '创建成功')
        setIsModalVisible(false)
        form.resetFields()
        setEditingDept(null)
        fetchDeptTree()
      } else {
        message.error(result.message || '操作失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 删除部门
  const handleDelete = async (id: number) => {
    try {
      const response = await fetch(`/api/dept/${id}`, {
        method: 'DELETE',
        headers: authHeaders()
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        message.success('删除成功')
        fetchDeptTree()
      } else {
        message.error(result.message || '删除失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 更新部门状态
  const handleStatusChange = async (id: number, status: number) => {
    try {
      const response = await fetch(`/api/dept/${id}/status?status=${status}`, {
        method: 'PUT',
        headers: authHeaders()
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        message.success('状态更新成功')
        fetchDeptTree()
      } else {
        message.error(result.message || '状态更新失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 编辑部门
  const handleEdit = (record: Dept) => {
    setEditingDept(record)
    form.setFieldsValue(record)
    setIsModalVisible(true)
  }

  const columns: ColumnsType<Dept> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80
    },
    {
      title: '部门名称',
      dataIndex: 'deptName',
      key: 'deptName',
      width: 200,
      render: (text: string, record: Dept) => {
        // 如果有子部门，显示目录图标
        if (record.children && record.children.length > 0) {
          return (
            <Space>
              <FolderOutlined style={{ color: '#1890ff' }} />
              <span style={{ fontWeight: 'bold', color: '#1890ff' }}>{text}</span>
            </Space>
          )
        }
        
        return (
          <Space>
            <FileOutlined style={{ color: '#52c41a' }} />
            <span style={{ color: '#52c41a' }}>{text}</span>
          </Space>
        )
      }
    },
    {
      title: '部门编码',
      dataIndex: 'deptCode',
      key: 'deptCode',
      width: 120
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
      title: '部门类型',
      dataIndex: 'deptType',
      key: 'deptType',
      width: 100,
      render: (type: string) => {
        const typeMap: { [key: string]: { text: string; color: string } } = {
          'office': { text: '办公室', color: 'blue' },
          'business': { text: '业务部', color: 'green' },
          'tech': { text: '技术部', color: 'orange' },
          'hr': { text: '人事部', color: 'purple' }
        }
        const config = typeMap[type] || { text: type, color: 'default' }
        return <Tag color={config.color}>{config.text}</Tag>
      }
    },
    {
      title: '负责人',
      dataIndex: 'manager',
      key: 'manager',
      width: 100
    },
    {
      title: '联系电话',
      dataIndex: 'phone',
      key: 'phone',
      width: 120
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number, record: Dept) => (
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
      render: (_, record: Dept) => (
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
            title="确定要删除这个部门吗？"
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
        <div style={{ marginBottom: 16 }}>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditingDept(null)
              form.resetFields()
              setIsModalVisible(true)
            }}
          >
            新建部门
          </Button>
          <Button
            icon={<FolderOutlined />}
            onClick={handleExpandAll}
            style={{ marginLeft: 8 }}
          >
            全部展开
          </Button>
          <Button
            icon={<FileOutlined />}
            onClick={handleCollapseAll}
            style={{ marginLeft: 8 }}
          >
            全部折叠
          </Button>
          <Button
            icon={<ReloadOutlined />}
            onClick={fetchDeptTree}
            style={{ marginLeft: 8 }}
          >
            刷新
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={deptTree}
          rowKey="id"
          loading={loading}
          pagination={false}
          scroll={{ x: 1200 }}
          expandable={{
            expandedRowKeys,
            onExpandedRowsChange: (keys) => setExpandedRowKeys(keys as React.Key[]),
            indentSize: 20
          }}
        />
      </Card>

      <Modal
        title={editingDept ? '编辑部门' : '新建部门'}
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false)
          form.resetFields()
          setEditingDept(null)
        }}
        footer={null}
        width={800}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSave}
        >
          <Form.Item
            name="deptCode"
            label="部门编码"
            rules={[{ required: true, message: '请输入部门编码' }]}
          >
            <Input placeholder="请输入部门编码" />
          </Form.Item>
          <Form.Item
            name="deptName"
            label="部门名称"
            rules={[{ required: true, message: '请输入部门名称' }]}
          >
            <Input placeholder="请输入部门名称" />
          </Form.Item>
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
          <Form.Item
            name="parentId"
            label="父部门"
            initialValue={0}
          >
            <Select placeholder="请选择父部门" allowClear>
              <Select.Option value={0}>根部门</Select.Option>
              {deptList.map(dept => (
                <Select.Option key={dept.id} value={dept.id}>
                  {dept.deptName}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="deptType"
            label="部门类型"
          >
            <Select placeholder="请选择部门类型">
              <Select.Option value="office">办公室</Select.Option>
              <Select.Option value="business">业务部</Select.Option>
              <Select.Option value="tech">技术部</Select.Option>
              <Select.Option value="hr">人事部</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="manager"
            label="负责人"
          >
            <Input placeholder="请输入负责人姓名" />
          </Form.Item>
          <Form.Item
            name="phone"
            label="联系电话"
          >
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          <Form.Item
            name="email"
            label="邮箱"
          >
            <Input placeholder="请输入邮箱" />
          </Form.Item>
          <Form.Item
            name="address"
            label="地址"
          >
            <Input placeholder="请输入地址" />
          </Form.Item>
          <Form.Item
            name="description"
            label="描述"
          >
            <Input.TextArea rows={3} placeholder="请输入部门描述" />
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
                {editingDept ? '更新' : '创建'}
              </Button>
              <Button
                onClick={() => {
                  setIsModalVisible(false)
                  form.resetFields()
                  setEditingDept(null)
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

export default DeptManagement
