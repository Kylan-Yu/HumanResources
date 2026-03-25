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

interface Org {
  id: number
  orgCode: string
  orgName: string
  orgType: string
  parentId: number
  legalPerson: string
  unifiedSocialCreditCode: string
  address: string
  phone: string
  email: string
  industryType: string
  status: number
  sortOrder: number
  extJson: string
  createdBy: number
  createdTime: string
  updatedBy: number
  updatedTime: string
  deleted: number
  children?: Org[]
}

const OrgTree: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [orgTree, setOrgTree] = useState<Org[]>([])
  const [orgList, setOrgList] = useState<Org[]>([])
  const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([])
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [editingOrg, setEditingOrg] = useState<Org | null>(null)
  const [form] = Form.useForm()

  const authHeaders = () => {
    const token = localStorage.getItem('token')
    return token ? { Authorization: `Bearer ${token}` } : {}
  }

  // 获取组织树
  const fetchOrgTree = async () => {
    setLoading(true)
    try {
      const response = await fetch('/api/org/tree', {
        headers: authHeaders()
      })
      const result = await response.json()
      
      if (result.code === 200) {
        setOrgTree(result.data)
        setOrgList(flattenTree(result.data))
        // 默认展开所有行
        const allKeys = flattenTree(result.data).map(item => item.id)
        setExpandedRowKeys(allKeys)
      } else {
        message.error(result.message || '获取组织树失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchOrgTree()
  }, [])

  // 扁平化树形数据
  const flattenTree = (tree: Org[]): Org[] => {
    const result: Org[] = []
    const traverse = (nodes: Org[]) => {
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
    const allKeys = orgList.map(item => item.id)
    setExpandedRowKeys(allKeys)
  }

  // 全部折叠
  const handleCollapseAll = () => {
    setExpandedRowKeys([])
  }

  // 创建或更新组织
  const handleSave = async (values: any) => {
    try {
      const url = editingOrg ? `/api/org/${editingOrg.id}` : '/api/org'
      const method = editingOrg ? 'PUT' : 'POST'
      
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
        message.success(editingOrg ? '更新成功' : '创建成功')
        setIsModalVisible(false)
        form.resetFields()
        setEditingOrg(null)
        fetchOrgTree()
      } else {
        message.error(result.message || '操作失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 删除组织
  const handleDelete = async (id: number) => {
    try {
      const response = await fetch(`/api/org/${id}`, {
        method: 'DELETE',
        headers: authHeaders()
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        message.success('删除成功')
        fetchOrgTree()
      } else {
        message.error(result.message || '删除失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 更新组织状态
  const handleStatusChange = async (id: number, status: number) => {
    try {
      const response = await fetch(`/api/org/${id}/status?status=${status}`, {
        method: 'PUT',
        headers: authHeaders()
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        message.success('状态更新成功')
        fetchOrgTree()
      } else {
        message.error(result.message || '状态更新失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 编辑组织
  const handleEdit = (record: Org) => {
    setEditingOrg(record)
    form.setFieldsValue(record)
    setIsModalVisible(true)
  }

  const columns: ColumnsType<Org> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80
    },
    {
      title: '组织名称',
      dataIndex: 'orgName',
      key: 'orgName',
      width: 200,
      render: (text: string, record: Org) => {
        // 如果有子组织，显示目录图标
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
      title: '组织编码',
      dataIndex: 'orgCode',
      key: 'orgCode',
      width: 120
    },
    {
      title: '组织类型',
      dataIndex: 'orgType',
      key: 'orgType',
      width: 100,
      render: (type: string) => {
        const typeMap: { [key: string]: { text: string; color: string } } = {
          'company': { text: '公司', color: 'blue' },
          'department': { text: '部门', color: 'green' },
          'branch': { text: '分公司', color: 'orange' }
        }
        const config = typeMap[type] || { text: type, color: 'default' }
        return <Tag color={config.color}>{config.text}</Tag>
      }
    },
    {
      title: '法人',
      dataIndex: 'legalPerson',
      key: 'legalPerson',
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
      render: (status: number, record: Org) => (
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
      render: (_, record: Org) => (
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
            title="确定要删除这个组织吗？"
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

  // 转换为树形数据
  const convertToTreeData = (orgs: Org[]) => {
    return orgs.map(org => ({
      title: org.orgName,
      key: org.id,
      value: org.id,
      children: org.children ? convertToTreeData(org.children) : undefined
    }))
  }

  return (
    <div>
      <Card>
        <div style={{ marginBottom: 16 }}>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditingOrg(null)
              form.resetFields()
              setIsModalVisible(true)
            }}
          >
            新建组织
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
            onClick={fetchOrgTree}
            style={{ marginLeft: 8 }}
          >
            刷新
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={orgTree}
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
        title={editingOrg ? '编辑组织' : '新建组织'}
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false)
          form.resetFields()
          setEditingOrg(null)
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
            name="orgCode"
            label="组织编码"
            rules={[{ required: true, message: '请输入组织编码' }]}
          >
            <Input placeholder="请输入组织编码" />
          </Form.Item>
          <Form.Item
            name="orgName"
            label="组织名称"
            rules={[{ required: true, message: '请输入组织名称' }]}
          >
            <Input placeholder="请输入组织名称" />
          </Form.Item>
          <Form.Item
            name="orgType"
            label="组织类型"
            rules={[{ required: true, message: '请选择组织类型' }]}
          >
            <Select placeholder="请选择组织类型">
              <Select.Option value="company">公司</Select.Option>
              <Select.Option value="department">部门</Select.Option>
              <Select.Option value="branch">分公司</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="parentId"
            label="父组织"
            initialValue={0}
          >
            <Select placeholder="请选择父组织" allowClear>
              <Select.Option value={0}>根组织</Select.Option>
              {orgList.map(org => (
                <Select.Option key={org.id} value={org.id}>
                  {org.orgName}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="legalPerson"
            label="法人"
          >
            <Input placeholder="请输入法人姓名" />
          </Form.Item>
          <Form.Item
            name="unifiedSocialCreditCode"
            label="统一社会信用代码"
          >
            <Input placeholder="请输入统一社会信用代码" />
          </Form.Item>
          <Form.Item
            name="address"
            label="地址"
          >
            <Input placeholder="请输入地址" />
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
            name="industryType"
            label="行业类型"
          >
            <Input placeholder="请输入行业类型" />
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
                {editingOrg ? '更新' : '创建'}
              </Button>
              <Button
                onClick={() => {
                  setIsModalVisible(false)
                  form.resetFields()
                  setEditingOrg(null)
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

export default OrgTree
