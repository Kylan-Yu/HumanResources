import React, { useState, useEffect } from 'react'
import { Card, Table, Button, Space, Modal, Form, Input, Select, Switch, message, Popconfirm } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, KeyOutlined } from '@ant-design/icons'
import { getUserPage, createUser, updateUser, deleteUser, updateUserStatus, resetUserPassword } from '@/api/user'
import type { User, PageResult } from '@/types'

const UserManagement: React.FC = () => {
  const [users, setUsers] = useState<PageResult<User>>({ list: [], total: 0 })
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingUser, setEditingUser] = useState<User | null>(null)
  const [form] = Form.useForm()
  const [searchParams, setSearchParams] = useState({
    pageNum: 1,
    pageSize: 10,
    username: '',
    realName: '',
    status: undefined
  })

  // 加载用户列表
  const loadUsers = async () => {
    setLoading(true)
    try {
      const response = await getUserPage(searchParams)
      setUsers(response.data)
    } catch (error) {
      message.error('加载用户列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadUsers()
  }, [searchParams])

  // 搜索表单
  const SearchForm = () => (
    <Card style={{ marginBottom: 16 }}>
      <Form layout="inline" onFinish={(values) => {
        setSearchParams({ ...searchParams, ...values, pageNum: 1 })
      }}>
        <Form.Item name="username" label="用户名">
          <Input placeholder="请输入用户名" allowClear />
        </Form.Item>
        <Form.Item name="realName" label="真实姓名">
          <Input placeholder="请输入真实姓名" allowClear />
        </Form.Item>
        <Form.Item name="status" label="状态">
          <Select placeholder="请选择状态" allowClear style={{ width: 120 }}>
            <Select.Option value={1}>启用</Select.Option>
            <Select.Option value={0}>禁用</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">搜索</Button>
            <Button onClick={() => {
              form.resetFields()
              setSearchParams({ pageNum: 1, pageSize: 10, username: '', realName: '', status: undefined })
            }}>重置</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  )

  // 新增/编辑用户
  const UserModal = () => (
    <Modal
      title={editingUser ? '编辑用户' : '新增用户'}
      open={modalVisible}
      onCancel={() => {
        setModalVisible(false)
        setEditingUser(null)
        form.resetFields()
      }}
      onOk={async () => {
        try {
          const values = await form.validateFields()
          if (editingUser) {
            await updateUser(editingUser.id!, values)
            message.success('更新用户成功')
          } else {
            await createUser(values)
            message.success('创建用户成功')
          }
          setModalVisible(false)
          setEditingUser(null)
          form.resetFields()
          loadUsers()
        } catch (error) {
          message.error(editingUser ? '更新用户失败' : '创建用户失败')
        }
      }}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="username"
          label="用户名"
          rules={[
            { required: true, message: '请输入用户名' },
            { min: 2, max: 50, message: '用户名长度在2-50个字符之间' },
            { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线' }
          ]}
        >
          <Input placeholder="请输入用户名" disabled={!!editingUser} />
        </Form.Item>
        {!editingUser && (
          <Form.Item
            name="password"
            label="密码"
            rules={[
              { required: true, message: '请输入密码' },
              { min: 6, max: 20, message: '密码长度在6-20个字符之间' }
            ]}
          >
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
        )}
        <Form.Item
          name="realName"
          label="真实姓名"
          rules={[{ required: true, message: '请输入真实姓名' }]}
        >
          <Input placeholder="请输入真实姓名" />
        </Form.Item>
        <Form.Item
          name="mobile"
          label="手机号"
          rules={[{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]}
        >
          <Input placeholder="请输入手机号" />
        </Form.Item>
        <Form.Item
          name="email"
          label="邮箱"
          rules={[{ type: 'email', message: '邮箱格式不正确' }]}
        >
          <Input placeholder="请输入邮箱" />
        </Form.Item>
        <Form.Item name="orgId" label="所属组织">
          <Select placeholder="请选择所属组织" allowClear>
            {/* TODO: 加载组织数据 */}
          </Select>
        </Form.Item>
        <Form.Item name="deptId" label="所属部门">
          <Select placeholder="请选择所属部门" allowClear>
            {/* TODO: 加载部门数据 */}
          </Select>
        </Form.Item>
        <Form.Item name="positionId" label="岗位">
          <Select placeholder="请选择岗位" allowClear>
            {/* TODO: 加载岗位数据 */}
          </Select>
        </Form.Item>
        <Form.Item name="industryType" label="行业类型">
          <Select placeholder="请选择行业类型" allowClear>
            <Select.Option value="company">企业</Select.Option>
            <Select.Option value="hospital">医院</Select.Option>
          </Select>
        </Form.Item>
        {editingUser && (
          <Form.Item name="status" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        )}
        <Form.Item name="remark" label="备注">
          <Input.TextArea placeholder="请输入备注" rows={3} />
        </Form.Item>
      </Form>
    </Modal>
  )

  // 表格列定义
  const columns = [
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '真实姓名',
      dataIndex: 'realName',
      key: 'realName',
    },
    {
      title: '手机号',
      dataIndex: 'mobile',
      key: 'mobile',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: '所属组织',
      dataIndex: 'orgName',
      key: 'orgName',
    },
    {
      title: '所属部门',
      dataIndex: 'deptName',
      key: 'deptName',
    },
    {
      title: '岗位',
      dataIndex: 'positionName',
      key: 'positionName',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Switch checked={status === 1} disabled />
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      render: (time: string) => time ? new Date(time).toLocaleString() : '-',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: User) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => {
              setEditingUser(record)
              form.setFieldsValue(record)
              setModalVisible(true)
            }}
          >
            编辑
          </Button>
          <Button
            type="link"
            icon={<KeyOutlined />}
            onClick={async () => {
              Modal.confirm({
                title: '重置密码',
                content: '确定要重置该用户的密码吗？',
                onOk: async () => {
                  try {
                    await resetUserPassword(record.id!, '123456')
                    message.success('密码重置成功，新密码为：123456')
                  } catch (error) {
                    message.error('密码重置失败')
                  }
                },
              })
            }}
          >
            重置密码
          </Button>
          <Popconfirm
            title="确定要删除该用户吗？"
            onConfirm={async () => {
              try {
                await deleteUser(record.id!)
                message.success('删除用户成功')
                loadUsers()
              } catch (error) {
                message.error('删除用户失败')
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
              setEditingUser(null)
              form.resetFields()
              setModalVisible(true)
            }}
          >
            新增用户
          </Button>
        </div>
        <Table
          columns={columns}
          dataSource={users.list}
          rowKey="id"
          loading={loading}
          pagination={{
            current: searchParams.pageNum,
            pageSize: searchParams.pageSize,
            total: users.total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, pageSize) => {
              setSearchParams({ ...searchParams, pageNum: page, pageSize })
            },
          }}
        />
      </Card>
      <UserModal />
    </div>
  )
}

export default UserManagement
