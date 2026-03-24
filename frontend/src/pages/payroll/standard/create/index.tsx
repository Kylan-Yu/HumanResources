import React from 'react'
import { Button, Card, Form, Input, InputNumber, Select, Space, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { createPayrollStandard } from '@/api/payroll/standard'

const PayrollStandardCreate: React.FC = () => {
  const [form] = Form.useForm()
  const navigate = useNavigate()

  const onSubmit = async () => {
    const values = await form.validateFields()
    await createPayrollStandard(values)
    message.success('创建成功')
    navigate('/payroll/standard/list')
  }

  return (
    <Card title="新增薪资标准">
      <Form
        form={form}
        layout="vertical"
        initialValues={{ status: 'ACTIVE', industryType: 'company', baseSalary: 0, performanceSalary: 0 }}
      >
        <Form.Item name="standardName" label="标准名称" rules={[{ required: true, message: '请输入标准名称' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="orgId" label="组织ID">
          <InputNumber style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="deptId" label="部门ID">
          <InputNumber style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="positionId" label="岗位ID">
          <InputNumber style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="gradeLevel" label="职级">
          <Input />
        </Form.Item>
        <Form.Item name="baseSalary" label="基础工资" rules={[{ required: true, message: '请输入基础工资' }]}>
          <InputNumber min={0} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="performanceSalary" label="绩效工资">
          <InputNumber min={0} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="positionAllowance" label="岗位津贴">
          <InputNumber min={0} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="mealAllowance" label="餐补">
          <InputNumber min={0} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="transportAllowance" label="交通补贴">
          <InputNumber min={0} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="communicationAllowance" label="通讯补贴">
          <InputNumber min={0} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="housingAllowance" label="住房补贴">
          <InputNumber min={0} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="otherAllowance" label="其他补贴">
          <InputNumber min={0} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="status" label="状态">
          <Select options={[{ value: 'ACTIVE', label: '启用' }, { value: 'INACTIVE', label: '禁用' }]} />
        </Form.Item>
        <Form.Item name="industryType" label="行业类型">
          <Select options={[{ value: 'company', label: '企业' }, { value: 'hospital', label: '医院' }]} />
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input.TextArea rows={3} />
        </Form.Item>
        <Space>
          <Button type="primary" onClick={onSubmit}>保存</Button>
          <Button onClick={() => navigate('/payroll/standard/list')}>返回</Button>
        </Space>
      </Form>
    </Card>
  )
}

export default PayrollStandardCreate

