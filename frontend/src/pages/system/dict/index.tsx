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
  Tabs,
  Divider
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'

interface Dict {
  id: number
  dictName: string
  dictType: string
  status: number
  remark: string
  createdBy: number
  createdTime: string
  updatedBy: number
  updatedTime: string
  deleted: number
}

interface DictItem {
  id: number
  dictSort: number
  dictLabel: string
  dictValue: string
  dictType: string
  cssClass: string
  listClass: string
  isDefault: string
  status: number
  remark: string
  createdBy: number
  createdTime: string
  updatedBy: number
  updatedTime: string
  deleted: number
}

const DictManagement: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [dictList, setDictList] = useState<Dict[]>([])
  const [dictItemList, setDictItemList] = useState<DictItem[]>([])
  const [selectedDictType, setSelectedDictType] = useState<string>('')
  const [activeTabKey, setActiveTabKey] = useState<string>('dict')
  const [isDictModalVisible, setIsDictModalVisible] = useState(false)
  const [isDictItemModalVisible, setIsDictItemModalVisible] = useState(false)
  const [editingDict, setEditingDict] = useState<Dict | null>(null)
  const [editingDictItem, setEditingDictItem] = useState<DictItem | null>(null)
  const [dictForm] = Form.useForm()
  const [dictItemForm] = Form.useForm()

  const authHeaders = () => {
    const token = localStorage.getItem('token')
    return token ? { Authorization: `Bearer ${token}` } : {}
  }

  // 获取字典列表
  const fetchDicts = async () => {
    setLoading(true)
    try {
      const response = await fetch('/api/dicts', {
        headers: authHeaders()
      })
      const result = await response.json()
      
      if (result.code === 200) {
        setDictList(result.data)
      } else {
        message.error(result.message || '获取字典列表失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  // 获取字典项列表
  const fetchDictItems = async (dictType: string) => {
    if (!dictType) return
    
    setLoading(true)
    try {
      const response = await fetch(`/api/dicts/${dictType}/items`, {
        headers: authHeaders()
      })
      const result = await response.json()
      
      if (result.code === 200) {
        setDictItemList(result.data)
      } else {
        message.error(result.message || '获取字典项列表失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchDicts()
  }, [])

  // 创建或更新字典
  const handleSaveDict = async (values: any) => {
    try {
      const url = editingDict ? `/api/dicts/${editingDict.id}` : '/api/dicts'
      const method = editingDict ? 'PUT' : 'POST'
      
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
        message.success(editingDict ? '更新成功' : '创建成功')
        setIsDictModalVisible(false)
        dictForm.resetFields()
        setEditingDict(null)
        fetchDicts()
      } else {
        message.error(result.message || '操作失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 创建或更新字典项
  const handleSaveDictItem = async (values: any) => {
    try {
      const url = editingDictItem ? `/api/dicts/items/${editingDictItem.id}` : '/api/dicts/items'
      const method = editingDictItem ? 'PUT' : 'POST'
      
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          ...authHeaders()
        },
        body: JSON.stringify({ ...values, dictType: selectedDictType })
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        message.success(editingDictItem ? '更新成功' : '创建成功')
        setIsDictItemModalVisible(false)
        dictItemForm.resetFields()
        setEditingDictItem(null)
        fetchDictItems(selectedDictType)
      } else {
        message.error(result.message || '操作失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 删除字典
  const handleDeleteDict = async (id: number) => {
    try {
      const response = await fetch(`/api/dicts/${id}`, {
        method: 'DELETE',
        headers: authHeaders()
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        message.success('删除成功')
        fetchDicts()
      } else {
        message.error(result.message || '删除失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 删除字典项
  const handleDeleteDictItem = async (id: number) => {
    try {
      const response = await fetch(`/api/dicts/items/${id}`, {
        method: 'DELETE',
        headers: authHeaders()
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        message.success('删除成功')
        fetchDictItems(selectedDictType)
      } else {
        message.error(result.message || '删除失败')
      }
    } catch (error) {
      message.error('网络错误，请稍后重试')
    }
  }

  // 编辑字典
  const handleEditDict = (record: Dict) => {
    setEditingDict(record)
    dictForm.setFieldsValue(record)
    setIsDictModalVisible(true)
  }

  // 编辑字典项
  const handleEditDictItem = (record: DictItem) => {
    setEditingDictItem(record)
    dictItemForm.setFieldsValue(record)
    setIsDictItemModalVisible(true)
  }

  // 选择字典类型
  const handleSelectDictType = (record: Dict) => {
    setSelectedDictType(record.dictType)
    fetchDictItems(record.dictType)
    setActiveTabKey('dictItem') // 自动切换到字典项Tab
  }

  const dictColumns: ColumnsType<Dict> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80
    },
    {
      title: '字典名称',
      dataIndex: 'dictName',
      key: 'dictName',
      width: 150
    },
    {
      title: '字典类型',
      dataIndex: 'dictType',
      key: 'dictType',
      width: 150
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
      )
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      ellipsis: true
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
      width: 200,
      render: (_, record: Dict) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            onClick={() => handleSelectDictType(record)}
          >
            查看项
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            size="small"
            onClick={() => handleEditDict(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个字典吗？"
            onConfirm={() => handleDeleteDict(record.id)}
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

  const dictItemColumns: ColumnsType<DictItem> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80
    },
    {
      title: '排序',
      dataIndex: 'dictSort',
      key: 'dictSort',
      width: 80
    },
    {
      title: '标签',
      dataIndex: 'dictLabel',
      key: 'dictLabel',
      width: 150
    },
    {
      title: '值',
      dataIndex: 'dictValue',
      key: 'dictValue',
      width: 120
    },
    {
      title: '样式',
      dataIndex: 'cssClass',
      key: 'cssClass',
      width: 100
    },
    {
      title: '表格样式',
      dataIndex: 'listClass',
      key: 'listClass',
      width: 120
    },
    {
      title: '默认',
      dataIndex: 'isDefault',
      key: 'isDefault',
      width: 80,
      render: (isDefault: string) => (
        <Tag color={isDefault === 'Y' ? 'green' : 'gray'}>
          {isDefault === 'Y' ? '是' : '否'}
        </Tag>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
      )
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      ellipsis: true
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record: DictItem) => (
        <Space size="small">
          <Button
            type="link"
            icon={<EditOutlined />}
            size="small"
            onClick={() => handleEditDictItem(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个字典项吗？"
            onConfirm={() => handleDeleteDictItem(record.id)}
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
        <Tabs
          activeKey={activeTabKey}
          onChange={(key) => setActiveTabKey(key)}
          items={[
            {
              key: 'dict',
              label: '字典管理',
              children: (
                <div>
                  <div style={{ marginBottom: 16 }}>
                    <Button
                      type="primary"
                      icon={<PlusOutlined />}
                      onClick={() => {
                        setEditingDict(null)
                        dictForm.resetFields()
                        setIsDictModalVisible(true)
                      }}
                    >
                      新建字典
                    </Button>
                    <Button
                      icon={<ReloadOutlined />}
                      onClick={fetchDicts}
                      style={{ marginLeft: 8 }}
                    >
                      刷新
                    </Button>
                  </div>

                  <Table
                    columns={dictColumns}
                    dataSource={dictList}
                    rowKey="id"
                    loading={loading}
                    pagination={false}
                  />
                </div>
              )
            },
            {
              key: 'dictItem',
              label: `字典项管理${selectedDictType ? ` - ${selectedDictType}` : ''}`,
              children: (
                <div>
                  {selectedDictType ? (
                    <>
                      <div style={{ marginBottom: 16 }}>
                        <Button
                          type="primary"
                          icon={<PlusOutlined />}
                          onClick={() => {
                            setEditingDictItem(null)
                            dictItemForm.resetFields()
                            setIsDictItemModalVisible(true)
                          }}
                        >
                          新建字典项
                        </Button>
                        <Button
                          icon={<ReloadOutlined />}
                          onClick={() => fetchDictItems(selectedDictType)}
                          style={{ marginLeft: 8 }}
                        >
                          刷新
                        </Button>
                      </div>

                      <Table
                        columns={dictItemColumns}
                        dataSource={dictItemList}
                        rowKey="id"
                        loading={loading}
                        pagination={false}
                      />
                    </>
                  ) : (
                    <div style={{ textAlign: 'center', padding: '50px' }}>
                      请先选择一个字典类型来管理字典项
                    </div>
                  )}
                </div>
              )
            }
          ]}
        />
      </Card>

      {/* 字典编辑弹窗 */}
      <Modal
        title={editingDict ? '编辑字典' : '新建字典'}
        open={isDictModalVisible}
        onCancel={() => {
          setIsDictModalVisible(false)
          dictForm.resetFields()
          setEditingDict(null)
        }}
        footer={null}
        width={600}
      >
        <Form
          form={dictForm}
          layout="vertical"
          onFinish={handleSaveDict}
        >
          <Form.Item
            name="dictName"
            label="字典名称"
            rules={[{ required: true, message: '请输入字典名称' }]}
          >
            <Input placeholder="请输入字典名称" />
          </Form.Item>
          <Form.Item
            name="dictType"
            label="字典类型"
            rules={[{ required: true, message: '请输入字典类型' }]}
          >
            <Input placeholder="请输入字典类型" />
          </Form.Item>
          <Form.Item
            name="remark"
            label="备注"
          >
            <Input.TextArea rows={4} placeholder="请输入备注" />
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
                {editingDict ? '更新' : '创建'}
              </Button>
              <Button
                onClick={() => {
                  setIsDictModalVisible(false)
                  dictForm.resetFields()
                  setEditingDict(null)
                }}
              >
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 字典项编辑弹窗 */}
      <Modal
        title={editingDictItem ? '编辑字典项' : '新建字典项'}
        open={isDictItemModalVisible}
        onCancel={() => {
          setIsDictItemModalVisible(false)
          dictItemForm.resetFields()
          setEditingDictItem(null)
        }}
        footer={null}
        width={600}
      >
        <Form
          form={dictItemForm}
          layout="vertical"
          onFinish={handleSaveDictItem}
        >
          <Form.Item
            name="dictLabel"
            label="标签"
            rules={[{ required: true, message: '请输入标签' }]}
          >
            <Input placeholder="请输入标签" />
          </Form.Item>
          <Form.Item
            name="dictValue"
            label="值"
            rules={[{ required: true, message: '请输入值' }]}
          >
            <Input placeholder="请输入值" />
          </Form.Item>
          <Form.Item
            name="dictSort"
            label="排序"
            initialValue={0}
          >
            <Input type="number" placeholder="请输入排序值" />
          </Form.Item>
          <Form.Item
            name="cssClass"
            label="样式"
          >
            <Input placeholder="请输入样式" />
          </Form.Item>
          <Form.Item
            name="listClass"
            label="表格样式"
          >
            <Input placeholder="请输入表格样式" />
          </Form.Item>
          <Form.Item
            name="isDefault"
            label="是否默认"
            initialValue="N"
          >
            <Select>
              <Select.Option value="Y">是</Select.Option>
              <Select.Option value="N">否</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="remark"
            label="备注"
          >
            <Input.TextArea rows={3} placeholder="请输入备注" />
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
                {editingDictItem ? '更新' : '创建'}
              </Button>
              <Button
                onClick={() => {
                  setIsDictItemModalVisible(false)
                  dictItemForm.resetFields()
                  setEditingDictItem(null)
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

export default DictManagement
