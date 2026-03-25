import React from 'react'
import { Button, Card, DatePicker, Form, Input, InputNumber, Select, message } from 'antd'
import dayjs from 'dayjs'
import { createLeaveApplication } from '@/api/leaveWorkflow'

const LeaveApplyPage: React.FC = () => {
  const [form] = Form.useForm()

  const submit = async () => {
    const values = await form.validateFields()
    await createLeaveApplication({
      leaveType: values.leaveType,
      startTime: values.startTime.format('YYYY-MM-DD HH:mm:ss'),
      endTime: values.endTime.format('YYYY-MM-DD HH:mm:ss'),
      leaveDays: values.leaveDays,
      reason: values.reason
    })
    message.success('请假申请提交成功')
    form.resetFields()
    form.setFieldsValue({ leaveType: 'ANNUAL', leaveDays: 1 })
  }

  return (
    <Card title="请假申请">
      <Form form={form} layout="vertical" initialValues={{ leaveType: 'ANNUAL', leaveDays: 1 }}>
        <Form.Item name="leaveType" label="请假类型" rules={[{ required: true, message: '请选择请假类型' }]}>
          <Select
            options={[
              { value: 'ANNUAL', label: '年假' },
              { value: 'SICK', label: '病假' },
              { value: 'PERSONAL', label: '事假' },
              { value: 'OTHER', label: '其他' }
            ]}
          />
        </Form.Item>
        <Form.Item name="startTime" label="开始时间" rules={[{ required: true, message: '请选择开始时间' }]}>
          <DatePicker showTime style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="endTime" label="结束时间" rules={[{ required: true, message: '请选择结束时间' }]}>
          <DatePicker showTime style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="leaveDays" label="请假天数" rules={[{ required: true, message: '请输入请假天数' }]}>
          <InputNumber min={0.5} step={0.5} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="reason" label="请假原因" rules={[{ required: true, message: '请填写请假原因' }]}>
          <Input.TextArea rows={4} />
        </Form.Item>
        <Button type="primary" onClick={submit}>提交申请</Button>
      </Form>
    </Card>
  )
}

export default LeaveApplyPage
