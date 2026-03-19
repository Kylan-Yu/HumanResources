import React, { useState, useEffect } from 'react'
import { Table, Button, Space, Modal, Form, Input, DatePicker, message } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import type { EmployeeWorkExperience } from '@/types/employee'

interface WorkExperienceTableProps {
  employeeId: number
}

const WorkExperienceTable: React.FC<WorkExperienceTableProps> = ({ employeeId }) => {
  const [workExperiences, setWorkExperiences] = useState<EmployeeWorkExperience[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingExperience, setEditingExperience] = useState<EmployeeWorkExperience | null>(null)
  const [form] = Form.useForm()

  useEffect(() => {
    loadWorkExperiences()
  }, [employeeId])

  const loadWorkExperiences = async () => {
    setLoading(true)
    try {
      // TODO: 调用API获取工作经历列表
      // const response = await getEmployeeWorkExperienceList(employeeId)
      // setWorkExperiences(response.data)
      setWorkExperiences([]) // 临时空数据
    } catch (error) {
      message.error('加载工作经历失败')
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setEditingExperience(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (experience: EmployeeWorkExperience) => {
    setEditingExperience(experience)
    form.setFieldsValue(experience)
    setModalVisible(true)
  }

  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确定要删除该工作经历吗？',
      onOk: async () => {
        try {
          // TODO: 调用删除API
          // await deleteEmployeeWorkExperience(id)
          message.success('删除成功')
          loadWorkExperiences()
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
      
      if (editingExperience) {
        // TODO: 调用更新API
        // await updateEmployeeWorkExperience(editingExperience.id, params)
        message.success('更新成功')
      } else {
        // TODO: 调用创建API
        // await createEmployeeWorkExperience({ ...params, employeeId })
        message.success('添加成功')
      }
      setModalVisible(false)
      loadWorkExperiences()
    } catch (error) {
      message.error(editingExperience ? '更新失败' : '添加失败')
    }
  }

  const calculateWorkMonths = (startDate: string, endDate?: string) => {
    if (!startDate) return 0
    const start = new Date(startDate)
    const end = endDate ? new Date(endDate) : new Date()
    const months = (end.getFullYear() - start.getFullYear()) * 12 + (end.getMonth() - start.getMonth())
    return months > 0 ? months : 0
  }

  const columns = [
    {
      title: '公司名称',
      dataIndex: 'companyName',
      key: 'companyName',
    },
    {
      title: '职位',
      dataIndex: 'position',
      key: 'position',
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
      render: (text: string) => text || '至今'
    },
    {
      title: '工作时长',
      dataIndex: 'workMonths',
      key: 'workMonths',
      render: (_: any, record: EmployeeWorkExperience) => {
        const months = calculateWorkMonths(record.startDate, record.endDate)
        return `${months}个月`
      }
    },
    {
      title: '工作描述',
      dataIndex: 'jobDescription',
      key: 'jobDescription',
      ellipsis: true,
    },
    {
      title: '离职原因',
      dataIndex: 'resignReason',
      key: 'resignReason',
      ellipsis: true,
    },
    {
      title: '证明人',
      dataIndex: 'witness',
      key: 'witness',
    },
    {
      title: '证明人电话',
      dataIndex: 'witnessMobile',
      key: 'witnessMobile',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: EmployeeWorkExperience) => (
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
          添加工作经历
        </Button>
      </div>
      
      <Table
        columns={columns}
        dataSource={workExperiences}
        rowKey="id"
        loading={loading}
        pagination={false}
      />

      <Modal
        title={editingExperience ? '编辑工作经历' : '添加工作经历'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 16 }}>
            <Form.Item name="companyName" label="公司名称" rules={[{ required: true, message: '请输入公司名称' }]}>
              <Input placeholder="请输入公司名称" />
            </Form.Item>
            <Form.Item name="position" label="职位" rules={[{ required: true, message: '请输入职位' }]}>
              <Input placeholder="请输入职位" />
            </Form.Item>
            <Form.Item name="startDate" label="开始日期" rules={[{ required: true, message: '请选择开始日期' }]}>
              <DatePicker style={{ width: '100%' }} placeholder="请选择开始日期" />
            </Form.Item>
            <Form.Item name="endDate" label="结束日期">
              <DatePicker style={{ width: '100%' }} placeholder="请选择结束日期（留空表示至今）" />
            </Form.Item>
            <Form.Item name="witness" label="证明人">
              <Input placeholder="请输入证明人" />
            </Form.Item>
            <Form.Item name="witnessMobile" label="证明人电话">
              <Input placeholder="请输入证明人电话" />
            </Form.Item>
          </div>
          <Form.Item name="jobDescription" label="工作描述">
            <Input.TextArea rows={3} placeholder="请输入工作描述" />
          </Form.Item>
          <Form.Item name="resignReason" label="离职原因">
            <Input.TextArea rows={2} placeholder="请输入离职原因" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default WorkExperienceTable
