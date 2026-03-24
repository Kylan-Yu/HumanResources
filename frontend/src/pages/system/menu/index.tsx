import React, { useState, useEffect } from 'react'
import {
  Card,
  Table,
  Button,
  Space,
  Form,
  Input,
  Select,
  TreeSelect,
  Modal,
  message,
  Popconfirm,
  Tag,
  Tree
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  FolderOutlined,
  FileOutlined,
  AppstoreOutlined,
  CaretRightOutlined,
  CaretDownOutlined
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { DataNode } from 'antd/es/tree'

interface Menu {
  id: number
  parentId: number
  menuName: string
  menuType: number
  path: string
  component: string
  permission: string
  icon: string
  sortOrder: number
  visible: number
  status: number
  createdBy: number
  createdTime: string
  updatedBy: number
  updatedTime: string
  deleted: number
  children?: Menu[]
}

const MenuManagement: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [menuTree, setMenuTree] = useState<Menu[]>([])
  const [menuList, setMenuList] = useState<Menu[]>([])
  const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([])
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [editingMenu, setEditingMenu] = useState<Menu | null>(null)
  const [form] = Form.useForm()

  // 获取菜单树
  const fetchMenuTree = async () => {
    setLoading(true)
    try {
      const response = await fetch('/api/menus/tree')
      const result = await response.json()
      
      if (result.code === 200) {
        setMenuTree(result.data)
        setMenuList(flattenTree(result.data))
        // 默认展开所有行
        const allKeys = flattenTree(result.data).map(item => item.id)
        setExpandedRowKeys(allKeys)
      } else {
        message.error(result.message || '获取菜单树失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  // 全部展开
  const handleExpandAll = () => {
    const allKeys = menuList.map(item => item.id)
    setExpandedRowKeys(allKeys)
  }

  // 全部折叠
  const handleCollapseAll = () => {
    setExpandedRowKeys([])
  }

  useEffect(() => {
    fetchMenuTree()
  }, [])

  // 扁平化树形数据
  const flattenTree = (tree: Menu[]): Menu[] => {
    const result: Menu[] = []
    const traverse = (nodes: Menu[]) => {
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

  // 创建或更新菜单
  const handleSave = async (values: any) => {
    try {
      const url = editingMenu ? `/api/menus/${editingMenu.id}` : '/api/menus'
      const method = editingMenu ? 'PUT' : 'POST'
      
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(values)
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        message.success(editingMenu ? '更新成功' : '创建成功')
        setIsModalVisible(false)
        form.resetFields()
        setEditingMenu(null)
        fetchMenuTree()
      } else {
        message.error(result.message || '操作失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 删除菜单
  const handleDelete = async (id: number) => {
    try {
      const response = await fetch(`/api/menus/${id}`, {
        method: 'DELETE'
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        message.success('删除成功')
        fetchMenuTree()
      } else {
        message.error(result.message || '删除失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 更新菜单状态
  const handleStatusChange = async (id: number, status: number) => {
    try {
      const response = await fetch(`/api/menus/${id}/status?status=${status}`, {
        method: 'PUT'
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        message.success('状态更新成功')
        fetchMenuTree()
      } else {
        message.error(result.message || '状态更新失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 编辑菜单
  const handleEdit = (record: Menu) => {
    setEditingMenu(record)
    form.setFieldsValue(record)
    setIsModalVisible(true)
  }

  const columns: ColumnsType<Menu> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80
    },
    {
      title: '菜单名称',
      dataIndex: 'menuName',
      key: 'menuName',
      width: 250,
      render: (text: string, record: Menu) => {
        const getIcon = (menuType: number) => {
          switch (menuType) {
            case 1: return <FolderOutlined style={{ color: '#1890ff' }} />
            case 2: return <FileOutlined style={{ color: '#52c41a' }} />
            case 3: return <AppstoreOutlined style={{ color: '#fa8c16' }} />
            default: return <FileOutlined />
          }
        }
        
        const getTypeColor = (menuType: number) => {
          switch (menuType) {
            case 1: return '#1890ff'
            case 2: return '#52c41a'
            case 3: return '#fa8c16'
            default: return '#666'
          }
        }
        
        // 如果有子菜单，显示目录图标
        if (record.children && record.children.length > 0) {
          return (
            <Space>
              <FolderOutlined style={{ color: getTypeColor(record.menuType) }} />
              <span style={{ fontWeight: 'bold', color: getTypeColor(record.menuType) }}>{text}</span>
            </Space>
          )
        }
        
        return (
          <Space>
            {getIcon(record.menuType)}
            <span style={{ color: getTypeColor(record.menuType) }}>{text}</span>
          </Space>
        )
      }
    },
    {
      title: '菜单类型',
      dataIndex: 'menuType',
      key: 'menuType',
      width: 100,
      render: (type: number) => {
        const typeMap = {
          1: { text: '目录', color: 'blue' },
          2: { text: '菜单', color: 'green' },
          3: { text: '按钮', color: 'orange' }
        }
        const config = typeMap[type as keyof typeof typeMap]
        return <Tag color={config.color}>{config.text}</Tag>
      }
    },
    {
      title: '路径',
      dataIndex: 'path',
      key: 'path',
      width: 150,
      ellipsis: true
    },
    {
      title: '组件',
      dataIndex: 'component',
      key: 'component',
      width: 150,
      ellipsis: true
    },
    {
      title: '权限标识',
      dataIndex: 'permission',
      key: 'permission',
      width: 150,
      ellipsis: true
    },
    {
      title: '图标',
      dataIndex: 'icon',
      key: 'icon',
      width: 100
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
      render: (status: number, record: Menu) => (
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
      title: '可见',
      dataIndex: 'visible',
      key: 'visible',
      width: 80,
      render: (visible: number) => (
        <Tag color={visible === 1 ? 'green' : 'gray'}>
          {visible === 1 ? '显示' : '隐藏'}
        </Tag>
      )
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record: Menu) => (
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
            title="确定要删除这个菜单吗？"
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
  const convertToTreeData = (menus: any[]): DataNode[] => {
    return menus.map(menu => ({
      title: menu.menuName,
      key: menu.id,
      children: menu.children ? convertToTreeData(menu.children) : undefined
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
              setEditingMenu(null)
              form.resetFields()
              setIsModalVisible(true)
            }}
          >
            新建菜单
          </Button>
          <Button
            icon={<CaretDownOutlined />}
            onClick={handleExpandAll}
            style={{ marginLeft: 8 }}
          >
            全部展开
          </Button>
          <Button
            icon={<CaretRightOutlined />}
            onClick={handleCollapseAll}
            style={{ marginLeft: 8 }}
          >
            全部折叠
          </Button>
          <Button
            icon={<ReloadOutlined />}
            onClick={fetchMenuTree}
            style={{ marginLeft: 8 }}
          >
            刷新
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={menuTree}
          rowKey="id"
          loading={loading}
          pagination={false}
          scroll={{ x: 1200 }}
          expandable={{
            expandedRowKeys,
            onExpandedRowsChange: (keys) => setExpandedRowKeys(keys as React.Key[]),
            indentSize: 20,
            expandIcon: ({ expanded, onExpand, record }) => {
              if (record.children && record.children.length > 0) {
                return (
                  <Button
                    type="link"
                    size="small"
                    icon={expanded ? <CaretDownOutlined /> : <CaretRightOutlined />}
                    onClick={(e) => {
                      e.stopPropagation()
                      onExpand(record, e)
                    }}
                    style={{ marginRight: 8, color: '#1890ff' }}
                  />
                )
              }
              return <span style={{ display: 'inline-block', width: 24 }} />
            }
          }}
        />
      </Card>

      <Modal
        title={editingMenu ? '编辑菜单' : '新建菜单'}
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false)
          form.resetFields()
          setEditingMenu(null)
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
            name="parentId"
            label="父菜单"
            initialValue={0}
          >
            <CustomTreeSelect
              treeData={convertToTreeData([{ id: 0, menuName: '根菜单', children: menuTree }])}
              placeholder="请选择父菜单"
              allowClear
            />
          </Form.Item>
          <Form.Item
            name="menuName"
            label="菜单名称"
            rules={[{ required: true, message: '请输入菜单名称' }]}
          >
            <Input placeholder="请输入菜单名称" />
          </Form.Item>
          <Form.Item
            name="menuType"
            label="菜单类型"
            rules={[{ required: true, message: '请选择菜单类型' }]}
            initialValue={2}
          >
            <Select>
              <Select.Option value={1}>目录</Select.Option>
              <Select.Option value={2}>菜单</Select.Option>
              <Select.Option value={3}>按钮</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="path"
            label="路径"
          >
            <Input placeholder="请输入路径" />
          </Form.Item>
          <Form.Item
            name="component"
            label="组件"
          >
            <Input placeholder="请输入组件路径" />
          </Form.Item>
          <Form.Item
            name="permission"
            label="权限标识"
          >
            <Input placeholder="请输入权限标识" />
          </Form.Item>
          <Form.Item
            name="icon"
            label="图标"
          >
            <Input placeholder="请输入图标名称" />
          </Form.Item>
          <Form.Item
            name="sortOrder"
            label="排序"
            initialValue={0}
          >
            <Input type="number" placeholder="请输入排序值" />
          </Form.Item>
          <Form.Item
            name="visible"
            label="是否可见"
            initialValue={1}
          >
            <Select>
              <Select.Option value={1}>显示</Select.Option>
              <Select.Option value={0}>隐藏</Select.Option>
            </Select>
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
                {editingMenu ? '更新' : '创建'}
              </Button>
              <Button
                onClick={() => {
                  setIsModalVisible(false)
                  form.resetFields()
                  setEditingMenu(null)
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

// 自定义TreeSelect组件
const CustomTreeSelect: React.FC<{
  treeData: DataNode[]
  placeholder?: string
  allowClear?: boolean
  value?: number
  onChange?: (value: number) => void
}> = ({ treeData, placeholder, allowClear, value, onChange }) => {
  return (
    <TreeSelect
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      allowClear={allowClear}
      treeDefaultExpandAll
      treeData={treeData}
    />
  )
}

export default MenuManagement
