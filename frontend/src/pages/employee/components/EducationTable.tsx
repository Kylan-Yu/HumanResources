import React, { useState, useEffect } from 'react'
import { Table, Button, Space, Modal, Form, Input, Select, DatePicker, message } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import type { EmployeeEducation } from '@/types/employee'

const { Option } = Select

interface EducationTableProps {
  employeeId: number
}

const EducationTable: React.FC<EducationTableProps> = ({ employeeId }) => {
  const [educations, setEducations] = useState<EmployeeEducation[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingEducation, setEditingEducation] = useState<EmployeeEducation | null>(null)
  const [form] = Form.useForm()

  useEffect(() => {
    loadEducations()
  }, [employeeId])

  const loadEducations = async () => {
    setLoading(true)
    try {
      // TODO: 调用API获取教育经历列表
      // const response = await getEmployeeEducationList(employeeId)
      // setEducations(response.data)
      setEducations([]) // 临时空数据
    } catch (error) {
      message.error('加载教育经历失败')
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setEditingEducation(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (education: EmployeeEducation) => {
    setEditingEducation(education)
    form.setFieldsValue(education)
    setModalVisible(true)
  }

  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确定要删除该教育经历吗？',
      onOk: async () => {
        try {
          // TODO: 调用删除API
          // await deleteEmployeeEducation(id)
          message.success('删除成功')
          loadEducations()
        } catch (error) {
          message.error('删除失败')
        }
      },
    })
  }

  const handleSubmit = async (values: any) => {
    try {
      const params = {
        ...values,
        startDate: values.startDate ? values.startDate.format('YYYY-MM-DD') : undefined,
        endDate: values.endDate ? values.endDate.format('YYYY-MM-DD') : undefined
      }
      
      if (editingEducation) {
        // TODO: 调用更新API
        // await updateEmployeeEducation(editingEducation.id, params)
        message.success('更新成功')
      } else {
        // TODO: 调用创建API
        // await createEmployeeEducation({ ...params, employeeId })
        message.success('添加成功')
      }
      setModalVisible(false)
      loadEducations()
    } catch (error) {
      message.error(editingEducation ? '更新失败' : '添加失败')
    }
  }

  const columns = [
    {
      title: '学校名称',
      dataIndex: 'schoolName',
      key: 'schoolName',
    },
    {
      title: '学历层次',
      dataIndex: 'educationLevelDesc',
      key: 'educationLevelDesc',
    },
    {
      title: '专业',
      dataIndex: 'major',
      key: 'major',
    },
    {
      title: '开始日期',
      dataIndex: 'startDate',
      key: 'startDate',
    },
    {
      title: '结束日期',
      dataIndex: 'endDate',
      key: 'endDate',
    },
    {
      title: '是否最高学历',
      dataIndex: 'isHighestDesc',
      key: 'isHighestDesc',
      render: (text: string) => <span style={{ color: text === '是' ? 'green' : 'inherit' }}>{text}</span>
    },
    {
      title: '学位类型',
      dataIndex: 'degreeTypeDesc',
      key: 'degreeTypeDesc',
    },
    {
      title: '毕业证书编号',
      dataIndex: 'graduationCertificate',
      key: 'graduationCertificate',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: EmployeeEducation) => (
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
          添加教育经历
        </Button>
      </div>
      
      <Table
        columns={columns}
        dataSource={educations}
        rowKey="id"
        loading={loading}
        pagination={false}
      />

      <Modal
        title={editingEducation ? '编辑教育经历' : '添加教育经历'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 16 }}>
            <Form.Item name="schoolName" label="学校名称" rules={[{ required: true, message: '请输入学校名称' }]}>
              <Input placeholder="请输入学校名称" />
            </Form.Item>
            <Form.Item name="educationLevel" label="学历层次" rules={[{ required: true, message: '请选择学历层次' }]}>
              <Select placeholder="请选择学历层次">
                <Option value="primary">小学</Option>
                <Option value="middle">初中</Option>
                <Option value="high">高中</Option>
                <Option value="college">大专</Option>
                <Option value="bachelor">本科</Option>
                <Option value="master">硕士</Option>
                <Option value="doctor">博士</Option>
              </Select>
            </Form.Item>
            <Form.Item name="major" label="专业">
              <Input placeholder="请输入专业" />
            </Form.Item>
            <Form.Item name="degreeType" label="学位类型">
              <Select placeholder="请选择学位类型">
                <Option value="bachelor">学士</Option>
                <Option value="master">硕士</Option>
                <Option value="doctor">博士</Option>
              </Select>
            </Form.Item>
            <Form.Item name="startDate" label="开始日期" rules={[{ required: true, message: '请选择开始日期' }]}>
              <DatePicker style={{ width: '100%' }} placeholder="请选择开始日期" />
            </Form.Item>
            <Form.Item name="endDate" label="结束日期">
              <DatePicker style={{ width: '100%' }} placeholder="请选择结束日期" />
            </Form.Item>
            <Form.Item name="isHighest" label="是否最高学历" rules={[{ required: true, message: '请选择是否最高学历' }]}>
              <Select placeholder="请选择是否最高学历">
                <Option value={1}>是</Option>
                <Option value={0}>否</Option>
              </Select>
            </Form.Item>
            <Form.Item name="graduationCertificate" label="毕业证书编号">
              <Input placeholder="请输入毕业证书编号" />
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

export default EducationTable
