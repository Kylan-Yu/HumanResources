import React, { useState, useEffect } from 'react'
import { Card, Table, Button, Space, Modal, Form, Input, Select, Switch, message, Tree } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, ApartmentOutlined } from '@ant-design/icons'
import { getOrganizationTree, createOrganization, updateOrganization, deleteOrganization, updateOrganizationStatus } from '@/api/org'
import type { Organization } from '@/types'

const OrganizationManagement: React.FC = () => {
  const [organizations, setOrganizations] = useState<Organization[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingOrg, setEditingOrg] = useState<Organization | null>(null)
  const [form] = Form.useForm()

  // 加载组织树
  const loadOrganizations = async () => {
    setLoading(true)
    try {
      const response = await getOrganizationTree()
      setOrganizations(response.data)
    } catch (error) {
      message.error('加载组织树失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadOrganizations()
  }, [])

  // 新增/编辑组织
  const OrganizationModal = () => (
    <Modal
      title={editingOrg ? '编辑组织' : '新增组织'}
      open={modalVisible}
      onCancel={() => {
        setModalVisible(false)
        setEditingOrg(null)
        form.resetFields()
      }}
      onOk={async () => {
        try {
          const values = await form.validateFields()
          if (editingOrg) {
            await updateOrganization(editingOrg.id!, values)
            message.success('更新组织成功')
          } else {
            await createOrganization(values)
            message.success('创建组织成功')
          }
          setModalVisible(false)
          setEditingOrg(null)
          form.resetFields()
          loadOrganizations()
        } catch (error) {
          message.error(editingOrg ? '更新组织失败' : '创建组织失败')
        }
      }}
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="orgCode"
          label="组织编码"
          rules={[
            { required: true, message: '请输入组织编码' },
            { max: 50, message: '组织编码长度不能超过50个字符' }
          ]}
        >
          <Input placeholder="请输入组织编码" disabled={!!editingOrg} />
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
            <Select.Option value="hospital">医院</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="parentId" label="父组织">
          <Select placeholder="请选择父组织" allowClear>
            {organizations.map(org => (
              <Select.Option key={org.id} value={org.id}>
                {org.orgName}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item
          name="industryType"
          label="行业类型"
          rules={[{ required: true, message: '请选择行业类型' }]}
        >
          <Select placeholder="请选择行业类型">
            <Select.Option value="company">企业</Select.Option>
            <Select.Option value="hospital">医院</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="leaderId" label="负责人">
          <Select placeholder="请选择负责人" allowClear>
            {/* TODO: 加载员工数据 */}
          </Select>
        </Form.Item>
        <Form.Item name="contactPhone" label="联系电话">
          <Input placeholder="请输入联系电话" />
        </Form.Item>
        <Form.Item name="address" label="地址">
          <Input placeholder="请输入地址" />
        </Form.Item>
        {editingOrg && (
          <Form.Item name="status" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        )}
      </Form>
    </Modal>
  )

  // 树形表格列定义
  const columns = [
    {
      title: '组织编码',
      dataIndex: 'orgCode',
      key: 'orgCode',
      width: 150,
    },
    {
      title: '组织名称',
      dataIndex: 'orgName',
      key: 'orgName',
      width: 200,
    },
    {
      title: '组织类型',
      dataIndex: 'orgType',
      key: 'orgType',
      width: 100,
      render: (type: string) => type === 'company' ? '公司' : '医院',
    },
    {
      title: '行业类型',
      dataIndex: 'industryType',
      key: 'industryType',
      width: 100,
      render: (type: string) => type === 'company' ? '企业' : '医院',
    },
    {
      title: '负责人',
      dataIndex: 'leaderName',
      key: 'leaderName',
      width: 120,
    },
    {
      title: '联系电话',
      dataIndex: 'contactPhone',
      key: 'contactPhone',
      width: 120,
    },
    {
      title: '地址',
      dataIndex: 'address',
      key: 'address',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: number) => (
        <Switch checked={status === 1} disabled />
      ),
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
      render: (_: any, record: Organization) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => {
              setEditingOrg(record)
              form.setFieldsValue(record)
              setModalVisible(true)
            }}
          >
            编辑
          </Button>
          <Button
            type="link"
            icon={<ApartmentOutlined />}
            onClick={() => {
              // TODO: 显示下级组织
              message.info('查看下级组织功能待实现')
            }}
          >
            下级
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={async () => {
              Modal.confirm({
                title: '确定要删除该组织吗？',
                content: '删除后将无法恢复，请谨慎操作',
                onOk: async () => {
                  try {
                    await deleteOrganization(record.id!)
                    message.success('删除组织成功')
                    loadOrganizations()
                  } catch (error) {
                    message.error('删除组织失败')
                  }
                },
              })
            }}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ]

  // 树形数据转换
  const treeData = organizations.map(org => ({
    key: org.id,
    title: org.orgName,
    children: org.children?.map(child => ({
      key: child.id,
      title: child.orgName,
      children: child.children?.map(grandChild => ({
        key: grandChild.id,
        title: grandChild.orgName,
      })),
    })),
  }))

  return (
    <div>
      <Card>
        <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                setEditingOrg(null)
                form.resetFields()
                setModalVisible(true)
              }}
            >
              新增组织
            </Button>
          </div>
        </div>
        
        <div style={{ display: 'flex', gap: 16 }}>
          <div style={{ width: 300, border: '1px solid #f0f0f0', padding: 16, borderRadius: 6 }}>
            <h4>组织架构</h4>
            <Tree
              treeData={treeData}
              defaultExpandAll
              showLine
            />
          </div>
          
          <div style={{ flex: 1 }}>
            <Table
              columns={columns}
              dataSource={organizations}
              rowKey="id"
              loading={loading}
              pagination={false}
              scroll={{ x: 1200 }}
            />
          </div>
        </div>
      </Card>
      <OrganizationModal />
    </div>
  )
}

export default OrganizationManagement
