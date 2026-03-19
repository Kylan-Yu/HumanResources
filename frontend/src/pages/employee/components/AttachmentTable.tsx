import React, { useState, useEffect } from 'react'
import { Table, Button, Space, Modal, Form, Input, Select, Upload, message } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, UploadOutlined, FileOutlined } from '@ant-design/icons'
import type { EmployeeAttachment } from '@/types/employee'

const { Option } = Select
const { TextArea } = Input

interface AttachmentTableProps {
  employeeId: number
}

const AttachmentTable: React.FC<AttachmentTableProps> = ({ employeeId }) => {
  const [attachments, setAttachments] = useState<EmployeeAttachment[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingAttachment, setEditingAttachment] = useState<EmployeeAttachment | null>(null)
  const [form] = Form.useForm()

  useEffect(() => {
    loadAttachments()
  }, [employeeId])

  const loadAttachments = async () => {
    setLoading(true)
    try {
      // TODO: 调用API获取附件列表
      // const response = await getEmployeeAttachmentList(employeeId)
      // setAttachments(response.data)
      setAttachments([]) // 临时空数据
    } catch (error) {
      message.error('加载附件失败')
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setEditingAttachment(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (attachment: EmployeeAttachment) => {
    setEditingAttachment(attachment)
    form.setFieldsValue(attachment)
    setModalVisible(true)
  }

  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确定要删除该附件吗？',
      onOk: async () => {
        try {
          // TODO: 调用删除API
          // await deleteEmployeeAttachment(id)
          message.success('删除成功')
          loadAttachments()
        } catch (error) {
          message.error('删除失败')
        }
      },
    })
  }

  const handleSubmit = async (values: any) => {
    try {
      if (editingAttachment) {
        // TODO: 调用更新API
        // await updateEmployeeAttachment(editingAttachment.id, values)
        message.success('更新成功')
      } else {
        // TODO: 调用创建API
        // await createEmployeeAttachment({ ...values, employeeId })
        message.success('上传成功')
      }
      setModalVisible(false)
      loadAttachments()
    } catch (error) {
      message.error(editingAttachment ? '更新失败' : '上传失败')
    }
  }

  const formatFileSize = (size?: number) => {
    if (!size) return '-'
    const units = ['B', 'KB', 'MB', 'GB']
    let unitIndex = 0
    let fileSize = size
    
    while (fileSize >= 1024 && unitIndex < units.length - 1) {
      fileSize /= 1024
      unitIndex++
    }
    
    return `${fileSize.toFixed(2)} ${units[unitIndex]}`
  }

  const columns = [
    {
      title: '文件类型',
      dataIndex: 'attachmentTypeDesc',
      key: 'attachmentTypeDesc',
    },
    {
      title: '文件名',
      dataIndex: 'fileName',
      key: 'fileName',
      render: (text: string, record: EmployeeAttachment) => (
        <Space>
          <FileOutlined />
          <a href={record.filePath} target="_blank" rel="noopener noreferrer">
            {text}
          </a>
        </Space>
      )
    },
    {
      title: '文件大小',
      dataIndex: 'fileSizeDesc',
      key: 'fileSizeDesc',
    },
    {
      title: '文件类型',
      dataIndex: 'fileType',
      key: 'fileType',
    },
    {
      title: '上传时间',
      dataIndex: 'uploadTime',
      key: 'uploadTime',
      render: (time: string) => time ? new Date(time).toLocaleString() : '-',
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: EmployeeAttachment) => (
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

  const uploadProps = {
    name: 'file',
    action: '/api/upload', // TODO: 替换为实际的上传接口
    onChange(info: any) {
      if (info.file.status === 'done') {
        message.success(`${info.file.name} 上传成功`)
        form.setFieldsValue({
          fileName: info.file.name,
          filePath: info.file.response?.data?.filePath,
          fileSize: info.file.size,
          fileType: info.file.type
        })
      } else if (info.file.status === 'error') {
        message.error(`${info.file.name} 上传失败`)
      }
    },
  }

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          上传附件
        </Button>
      </div>
      
      <Table
        columns={columns}
        dataSource={attachments}
        rowKey="id"
        loading={loading}
        pagination={false}
      />

      <Modal
        title={editingAttachment ? '编辑附件' : '上传附件'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="attachmentType" label="附件类型" rules={[{ required: true, message: '请选择附件类型' }]}>
            <Select placeholder="请选择附件类型">
              <Option value="id_card">身份证</Option>
              <Option value="diploma">毕业证</Option>
              <Option value="degree">学位证</Option>
              <Option value="contract">劳动合同</Option>
              <Option value="resume">简历</Option>
              <Option value="other">其他</Option>
            </Select>
          </Form.Item>
          
          {!editingAttachment && (
            <Form.Item label="文件上传" rules={[{ required: true, message: '请上传文件' }]}>
              <Upload {...uploadProps}>
                <Button icon={<UploadOutlined />}>点击上传</Button>
              </Upload>
            </Form.Item>
          )}
          
          <Form.Item name="fileName" label="文件名" hidden>
            <Input />
          </Form.Item>
          
          <Form.Item name="filePath" label="文件路径" hidden>
            <Input />
          </Form.Item>
          
          <Form.Item name="fileSize" label="文件大小" hidden>
            <Input />
          </Form.Item>
          
          <Form.Item name="fileType" label="文件类型" hidden>
            <Input />
          </Form.Item>
          
          <Form.Item name="remark" label="备注">
            <TextArea rows={3} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default AttachmentTable
