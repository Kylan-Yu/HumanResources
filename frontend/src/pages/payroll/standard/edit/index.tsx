import React, { useEffect } from 'react'
import { Button, Card, Form, Input, InputNumber, Select, Space, message } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import { getPayrollStandardById, updatePayrollStandard } from '@/api/payroll/standard'

const PayrollStandardEdit: React.FC = () => {
  const [form] = Form.useForm()
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()

  const loadDetail = async () => {
    if (!id) return
    const res = await getPayrollStandardById(Number(id))
    form.setFieldsValue(res.data)
  }

  useEffect(() => {
    loadDetail()
  }, [id])

  const onSubmit = async () => {
    if (!id) return
    const values = await form.validateFields()
    await updatePayrollStandard(Number(id), values)
    message.success('更新成功')
    navigate('/payroll/standard/list')
  }

  return (
    <Card title="编辑薪资标准">
      <Form form={form} layout="vertical">
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
        <Form.Item name="baseSalary" label="基础工资">
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

export default PayrollStandardEdit

