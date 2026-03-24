import React, { useEffect } from 'react'
import { Button, Card, DatePicker, Form, Input, Select, Space, message } from 'antd'
import dayjs from 'dayjs'
import { useNavigate, useParams } from 'react-router-dom'
import { getContractById, updateContract } from '@/api/contract/contract'

const ContractEdit: React.FC = () => {
  const [form] = Form.useForm()
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()

  const loadDetail = async () => {
    if (!id) return
    const res = await getContractById(Number(id))
    const data = res.data
    form.setFieldsValue({
      ...data,
      startDate: data.startDate ? dayjs(data.startDate) : undefined,
      endDate: data.endDate ? dayjs(data.endDate) : undefined,
      signDate: data.signDate ? dayjs(data.signDate) : undefined
    })
  }

  useEffect(() => {
    loadDetail().catch(() => message.error('加载合同详情失败'))
  }, [id])

  const onSubmit = async () => {
    if (!id) return
    const values = await form.validateFields()
    const payload = {
      ...values,
      startDate: values.startDate?.format('YYYY-MM-DD'),
      endDate: values.endDate?.format('YYYY-MM-DD'),
      signDate: values.signDate?.format('YYYY-MM-DD')
    }
    await updateContract(Number(id), payload)
    message.success('更新成功')
    navigate('/contract/list')
  }

  return (
    <Card title="编辑合同">
      <Form form={form} layout="vertical">
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

export default ContractEdit
