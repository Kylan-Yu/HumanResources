import React from 'react'
import { Form, Select, DatePicker, Input, Radio } from 'antd'
import type { Organization } from '@/types'

const { Option } = Select

interface JobFormProps {
  organizations: Organization[]
  departments: any[]
  positions: any[]
  onOrgChange: (orgId: number) => void
  onDeptChange: (deptId: number) => void
}

const JobForm: React.FC<JobFormProps> = ({
  organizations,
  departments,
  positions,
  onOrgChange,
  onDeptChange
}) => {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16 }}>
      <Form.Item
        name={['jobInfo', 'orgId']}
        label="所属组织"
        rules={[{ required: true, message: '请选择所属组织' }]}
      >
        <Select 
          placeholder="请选择所属组织" 
          onChange={onOrgChange}
        >
          {organizations.map(org => (
            <Option key={org.id} value={org.id}>
              {org.orgName}
            </Option>
          ))}
        </Select>
      </Form.Item>
      
      <Form.Item
        name={['jobInfo', 'deptId']}
        label="所属部门"
        rules={[{ required: true, message: '请选择所属部门' }]}
      >
        <Select 
          placeholder="请选择所属部门" 
          onChange={onDeptChange}
          disabled={departments.length === 0}
        >
          {departments.map(dept => (
            <Option key={dept.id} value={dept.id}>
              {dept.deptName}
            </Option>
          ))}
        </Select>
      </Form.Item>
      
      <Form.Item
        name={['jobInfo', 'positionId']}
        label="岗位"
        rules={[{ required: true, message: '请选择岗位' }]}
      >
        <Select 
          placeholder="请选择岗位" 
          disabled={positions.length === 0}
        >
          {positions.map(pos => (
            <Option key={pos.id} value={pos.id}>
              {pos.positionName}
            </Option>
          ))}
        </Select>
      </Form.Item>
      
      <Form.Item
        name={['jobInfo', 'employeeType']}
        label="员工类型"
        rules={[{ required: true, message: '请选择员工类型' }]}
      >
        <Select placeholder="请选择员工类型">
          <Option value="formal">正式工</Option>
          <Option value="contract">合同工</Option>
          <Option value="intern">实习生</Option>
        </Select>
      </Form.Item>
      
      <Form.Item
        name={['jobInfo', 'employmentType']}
        label="用工类型"
        rules={[{ required: true, message: '请选择用工类型' }]}
      >
        <Select placeholder="请选择用工类型">
          <Option value="fulltime">全职</Option>
          <Option value="parttime">兼职</Option>
        </Select>
      </Form.Item>
      
      <Form.Item
        name={['jobInfo', 'entryDate']}
        label="入职日期"
        rules={[{ required: true, message: '请选择入职日期' }]}
      >
        <DatePicker style={{ width: '100%' }} placeholder="请选择入职日期" />
      </Form.Item>
      
      <Form.Item
        name={['jobInfo', 'regularDate']}
        label="转正日期"
      >
        <DatePicker style={{ width: '100%' }} placeholder="请选择转正日期" />
      </Form.Item>
      
      <Form.Item
        name={['jobInfo', 'workLocation']}
        label="工作地点"
      >
        <Input placeholder="请输入工作地点" />
      </Form.Item>
      
      <Form.Item
        name={['jobInfo', 'isMainJob']}
        label="是否主任职"
        rules={[{ required: true, message: '请选择是否主任职' }]}
      >
        <Radio.Group>
          <Radio value={1}>是</Radio>
          <Radio value={0}>否</Radio>
        </Radio.Group>
      </Form.Item>
    </div>
  )
}

export default JobForm
