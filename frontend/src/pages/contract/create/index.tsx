import React from 'react'
import { Button, Card, DatePicker, Form, Input, InputNumber, Select, Space, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { createContract } from '@/api/contract/contract'

const ContractCreate: React.FC = () => {
  const [form] = Form.useForm()
  const navigate = useNavigate()

  const onSubmit = async () => {
    const values = await form.validateFields()
    const payload = {
      ...values,
      startDate: values.startDate?.format('YYYY-MM-DD'),
      endDate: values.endDate?.format('YYYY-MM-DD'),
      signDate: values.signDate?.format('YYYY-MM-DD')
    }
    await createContract(payload)
    message.success('创建成功')
    navigate('/contract/list')
  }

  return (
    <Card title="新增合同">
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          contractType: 'LABOR_CONTRACT',
          contractStatus: 'DRAFT',
          industryType: 'company'
        }}
      >
        <Form.Item name="employeeId" label="员工ID" rules={[{ required: true, message: '请输入员工ID' }]}>
          <InputNumber min={1} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="contractType" label="合同类型" rules={[{ required: true, message: '请选择合同类型' }]}>
          <Select
            options={[
              { value: 'LABOR_CONTRACT', label: '劳动合同' },
              { value: 'CONFIDENTIALITY_AGREEMENT', label: '保密协议' },
              { value: 'NON_COMPETE_AGREEMENT', label: '竞业协议' },
              { value: 'SERVICE_AGREEMENT', label: '劳务协议' },
              { value: 'REEMPLOYMENT_AGREEMENT', label: '返聘协议' },
              { value: 'POSITION_APPOINTMENT_AGREEMENT', label: '岗位聘任协议' }
            ]}
          />
        </Form.Item>
        <Form.Item name="contractSubject" label="合同主体" rules={[{ required: true, message: '请输入合同主体' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="startDate" label="开始日期" rules={[{ required: true, message: '请选择开始日期' }]}>
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="endDate" label="结束日期" rules={[{ required: true, message: '请选择结束日期' }]}>
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="signDate" label="签订日期" rules={[{ required: true, message: '请选择签订日期' }]}>
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="contractStatus" label="合同状态">
          <Select
            options={[
              { value: 'DRAFT', label: '草稿' },
              { value: 'ACTIVE', label: '生效' },
              { value: 'TERMINATED', label: '终止' }
            ]}
          />
        </Form.Item>
        <Form.Item name="industryType" label="行业类型">
          <Select options={[{ value: 'company', label: '企业' }, { value: 'hospital', label: '医院' }]} />
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input.TextArea rows={3} />
        </Form.Item>
        <Space>
          <Button type="primary" onClick={onSubmit}>保存</Button>
          <Button onClick={() => navigate('/contract/list')}>返回</Button>
        </Space>
      </Form>
    </Card>
  )
}

export default ContractCreate
