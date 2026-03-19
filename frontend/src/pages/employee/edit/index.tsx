import React, { useState, useEffect } from 'react'
import { Card, Form, Input, Select, Radio, DatePicker, Button, message, Space } from 'antd'
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import { getEmployeeDetail, updateEmployee } from '@/api/employee'
import type { Employee, EmployeeUpdateParams } from '@/types/employee'

const { TextArea } = Input
const { Option } = Select

const EmployeeEdit: React.FC = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [employee, setEmployee] = useState<Employee | null>(null)
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()

  useEffect(() => {
    if (id) {
      loadEmployee()
    }
  }, [id])

  const loadEmployee = async () => {
    try {
      const response = await getEmployeeDetail(Number(id))
      const employeeData = response.data
      setEmployee(employeeData)
      
      // 填充表单数据
      form.setFieldsValue({
        name: employeeData.name,
        gender: employeeData.gender,
        birthday: employeeData.birthday ? new Date(employeeData.birthday) : undefined,
        idCardNo: employeeData.idCardNo,
        mobile: employeeData.mobile,
        email: employeeData.email,
        maritalStatus: employeeData.maritalStatus,
        nationality: employeeData.nationality,
        domicileAddress: employeeData.domicileAddress,
        currentAddress: employeeData.currentAddress,
        employeeStatus: employeeData.employeeStatus,
        industryType: employeeData.industryType,
        remark: employeeData.remark
      })
    } catch (error) {
      message.error('加载员工信息失败')
    }
  }

  const handleSubmit = async (values: any) => {
    if (!id) return
    
    setLoading(true)
    try {
      const params: EmployeeUpdateParams = {
        ...values,
        birthday: values.birthday ? values.birthday.format('YYYY-MM-DD') : undefined
      }
      
      await updateEmployee(Number(id), params)
      message.success('更新员工成功')
      navigate('/employee/list')
    } catch (error) {
      message.error('更新员工失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Button 
            icon={<ArrowLeftOutlined />} 
            onClick={() => navigate('/employee/list')}
          >
            返回
          </Button>
          <h2>编辑员工 - {employee?.name}</h2>
        </Space>
        
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          {/* 基础信息 */}
          <Card title="基础信息" style={{ marginBottom: 16 }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16 }}>
              <Form.Item
                name="name"
                label="姓名"
                rules={[{ required: true, message: '请输入姓名' }]}
              >
                <Input placeholder="请输入姓名" />
              </Form.Item>
              
              <Form.Item
                name="gender"
                label="性别"
                rules={[{ required: true, message: '请选择性别' }]}
              >
                <Radio.Group>
                  <Radio value={1}>男</Radio>
                  <Radio value={2}>女</Radio>
                </Radio.Group>
              </Form.Item>
              
              <Form.Item
                name="birthday"
                label="出生日期"
              >
                <DatePicker style={{ width: '100%' }} placeholder="请选择出生日期" />
              </Form.Item>
              
              <Form.Item
                name="idCardNo"
                label="身份证号"
                rules={[
                  { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/, message: '身份证号格式不正确' }
                ]}
              >
                <Input placeholder="请输入身份证号" />
              </Form.Item>
              
              <Form.Item
                name="mobile"
                label="手机号"
                rules={[
                  { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
                ]}
              >
                <Input placeholder="请输入手机号" />
              </Form.Item>
              
              <Form.Item
                name="email"
                label="邮箱"
                rules={[{ type: 'email', message: '邮箱格式不正确' }]}
              >
                <Input placeholder="请输入邮箱" />
              </Form.Item>
              
              <Form.Item
                name="maritalStatus"
                label="婚姻状况"
              >
                <Select placeholder="请选择婚姻状况" allowClear>
                  <Option value={1}>未婚</Option>
                  <Option value={2}>已婚</Option>
                  <Option value={3}>离异</Option>
                  <Option value={4}>丧偶</Option>
                </Select>
              </Form.Item>
              
              <Form.Item
                name="nationality"
                label="国籍"
              >
                <Input placeholder="请输入国籍" />
              </Form.Item>
              
              <Form.Item
                name="industryType"
                label="行业类型"
                rules={[{ required: true, message: '请选择行业类型' }]}
              >
                <Select placeholder="请选择行业类型">
                  <Option value="company">企业</Option>
                  <Option value="hospital">医院</Option>
                </Select>
              </Form.Item>
            </div>
            
            <Form.Item
              name="domicileAddress"
              label="户籍地址"
            >
              <Input placeholder="请输入户籍地址" />
            </Form.Item>
            
            <Form.Item
              name="currentAddress"
              label="现住址"
            >
              <Input placeholder="请输入现住址" />
            </Form.Item>
            
            <Form.Item
              name="remark"
              label="备注"
            >
              <TextArea rows={3} placeholder="请输入备注" />
            </Form.Item>
          </Card>

          <div style={{ marginTop: 24, textAlign: 'center' }}>
            <Space>
              <Button onClick={() => navigate('/employee/list')}>
                取消
              </Button>
              <Button 
                type="primary" 
                htmlType="submit" 
                loading={loading}
                icon={<SaveOutlined />}
              >
                保存
              </Button>
            </Space>
          </div>
        </Form>
      </Card>
    </div>
  )
}

export default EmployeeEdit
