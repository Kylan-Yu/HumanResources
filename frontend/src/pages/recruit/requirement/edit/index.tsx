import React, { useEffect } from 'react'
import { Button, Card, DatePicker, Form, Input, InputNumber, Select, Space, message } from 'antd'
import dayjs from 'dayjs'
import { useNavigate, useParams } from 'react-router-dom'
import { getRecruitRequirementById, updateRecruitRequirement } from '@/api/recruit/requirement'

const RecruitRequirementEdit: React.FC = () => {
  const [form] = Form.useForm()
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()

  const loadDetail = async () => {
    if (!id) return
    const res = await getRecruitRequirementById(Number(id))
    const data = res.data
    form.setFieldsValue({
      ...data,
      expectedEntryDate: data.expectedEntryDate ? dayjs(data.expectedEntryDate) : undefined
    })
  }

  useEffect(() => {
    loadDetail()
  }, [id])

  const onSubmit = async () => {
    if (!id) return
    const values = await form.validateFields()
    const payload = {
      ...values,
      expectedEntryDate: values.expectedEntryDate?.format('YYYY-MM-DD')
    }
    await updateRecruitRequirement(Number(id), payload)
    message.success('更新成功')
    navigate('/recruit/requirement/list')
  }

  return (
    <Card title="编辑招聘需求">
      <Form form={form} layout="vertical">
        <Form.Item name="title" label="需求标题" rules={[{ required: true, message: '请输入需求标题' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="orgId" label="组织ID" rules={[{ required: true, message: '请输入组织ID' }]}>
          <InputNumber style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="deptId" label="部门ID" rules={[{ required: true, message: '请输入部门ID' }]}>
          <InputNumber style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="positionId" label="岗位ID" rules={[{ required: true, message: '请输入岗位ID' }]}>
          <InputNumber style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="headcount" label="招聘人数">
          <InputNumber min={1} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="urgencyLevel" label="紧急程度">
          <Select options={[{ value: 'HIGH', label: '高' }, { value: 'MEDIUM', label: '中' }, { value: 'LOW', label: '低' }]} />
        </Form.Item>
        <Form.Item name="expectedEntryDate" label="期望入职日期">
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="industryType" label="行业类型">
          <Select options={[{ value: 'company', label: '企业' }, { value: 'hospital', label: '医院' }]} />
        </Form.Item>
        <Form.Item name="reason" label="招聘原因">
          <Input.TextArea rows={3} />
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input.TextArea rows={3} />
        </Form.Item>
        <Space>
          <Button type="primary" onClick={onSubmit}>保存</Button>
          <Button onClick={() => navigate('/recruit/requirement/list')}>返回</Button>
        </Space>
      </Form>
    </Card>
  )
}

export default RecruitRequirementEdit

