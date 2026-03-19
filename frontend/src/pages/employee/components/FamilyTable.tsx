import React, { useState, useEffect } from 'react'
import { Table, Button, Space, Modal, Form, Input, Select, message } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import type { EmployeeFamily } from '@/types/employee'

const { Option } = Select

interface FamilyTableProps {
  employeeId: number
}

const FamilyTable: React.FC<FamilyTableProps> = ({ employeeId }) => {
  const [families, setFamilies] = useState<EmployeeFamily[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingFamily, setEditingFamily] = useState<EmployeeFamily | null>(null)
  const [form] = Form.useForm()

  useEffect(() => {
    loadFamilies()
  }, [employeeId])

  const loadFamilies = async () => {
    setLoading(true)
    try {
      // TODO: 调用API获取家庭成员列表
      // const response = await getEmployeeFamilyList(employeeId)
      // setFamilies(response.data)
      setFamilies([]) // 临时空数据
    } catch (error) {
      message.error('加载家庭成员失败')
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setEditingFamily(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (family: EmployeeFamily) => {
    setEditingFamily(family)
    form.setFieldsValue(family)
    setModalVisible(true)
  }

  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确定要删除该家庭成员吗？',
      onOk: async () => {
        try {
          // TODO: 调用删除API
          // await deleteEmployeeFamily(id)
          message.success('删除成功')
          loadFamilies()
        } catch (error) {
          message.error('删除失败')
        }
      },
    })
  }

  const handleSubmit = async (values: any) => {
    try {
      if (editingFamily) {
        // TODO: 调用更新API
        // await updateEmployeeFamily(editingFamily.id, values)
        message.success('更新成功')
      } else {
        // TODO: 调用创建API
        // await createEmployeeFamily({ ...values, employeeId })
        message.success('添加成功')
      }
      setModalVisible(false)
      loadFamilies()
    } catch (error) {
      message.error(editingFamily ? '更新失败' : '添加失败')
    }
  }

  const columns = [
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '关系',
      dataIndex: 'relationshipDesc',
      key: 'relationshipDesc',
    },
    {
      title: '性别',
      dataIndex: 'genderDesc',
      key: 'genderDesc',
    },
    {
      title: '年龄',
      dataIndex: 'age',
      key: 'age',
    },
    {
      title: '身份证号',
      dataIndex: 'idCardNo',
      key: 'idCardNo',
    },
    {
      title: '手机号',
      dataIndex: 'mobile',
      key: 'mobile',
    },
    {
      title: '职业',
      dataIndex: 'occupation',
      key: 'occupation',
    },
    {
      title: '工作单位',
      dataIndex: 'workUnit',
      key: 'workUnit',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: EmployeeFamily) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id!)}>
            删除
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          添加家庭成员
        </Button>
      </div>
      
      <Table
        columns={columns}
        dataSource={families}
        rowKey="id"
        loading={loading}
        pagination={false}
      />

      <Modal
        title={editingFamily ? '编辑家庭成员' : '添加家庭成员'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 16 }}>
            <Form.Item name="name" label="姓名" rules={[{ required: true, message: '请输入姓名' }]}>
              <Input placeholder="请输入姓名" />
            </Form.Item>
            <Form.Item name="relationship" label="关系" rules={[{ required: true, message: '请选择关系' }]}>
              <Select placeholder="请选择关系">
                <Option value="father">父亲</Option>
                <Option value="mother">母亲</Option>
                <Option value="spouse">配偶</Option>
                <Option value="child">子女</Option>
              </Select>
            </Form.Item>
            <Form.Item name="gender" label="性别">
              <Select placeholder="请选择性别">
                <Option value={1}>男</Option>
                <Option value={2}>女</Option>
              </Select>
            </Form.Item>
            <Form.Item name="birthday" label="出生日期">
              <Input placeholder="请输入出生日期" />
            </Form.Item>
            <Form.Item name="idCardNo" label="身份证号">
              <Input placeholder="请输入身份证号" />
            </Form.Item>
            <Form.Item name="mobile" label="手机号">
              <Input placeholder="请输入手机号" />
            </Form.Item>
            <Form.Item name="occupation" label="职业">
              <Input placeholder="请输入职业" />
            </Form.Item>
            <Form.Item name="workUnit" label="工作单位">
              <Input placeholder="请输入工作单位" />
            </Form.Item>
          </div>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default FamilyTable
