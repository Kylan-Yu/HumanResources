import React, { useState, useEffect } from 'react'
import { Card, Tabs, Descriptions, Tag, Button, Space, message } from 'antd'
import { ArrowLeftOutlined, EditOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import { getEmployeeDetail } from '@/api/employee'
import type { Employee } from '@/types/employee'
import FamilyTable from '../components/FamilyTable'
import EducationTable from '../components/EducationTable'
import WorkExperienceTable from '../components/WorkExperienceTable'
import AttachmentTable from '../components/AttachmentTable'

const { TabPane } = Tabs

const EmployeeDetail: React.FC = () => {
  const [employee, setEmployee] = useState<Employee | null>(null)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()

  useEffect(() => {
    if (id) {
      loadEmployee()
    }
  }, [id])

  const loadEmployee = async () => {
    setLoading(true)
    try {
      const response = await getEmployeeDetail(Number(id))
      setEmployee(response.data)
    } catch (error) {
      message.error('加载员工详情失败')
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status: number) => {
    const colorMap = {
      1: 'green',
      2: 'red',
      3: 'gray'
    }
    return colorMap[status as keyof typeof colorMap] || 'default'
  }

  const getStatusText = (status: number) => {
    const textMap = {
      1: '在职',
      2: '离职',
      3: '退休'
    }
    return textMap[status as keyof typeof textMap] || '未知'
  }

  if (!employee) {
    return <div>加载中...</div>
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
          <Button 
            type="primary" 
            icon={<EditOutlined />}
            onClick={() => navigate(`/employee/edit/${id}`)}
          >
            编辑
          </Button>
          <h2>员工详情 - {employee.name}</h2>
        </Space>

        <Tabs defaultActiveKey="basic">
          <TabPane tab="基础信息" key="basic">
            <Descriptions bordered column={2}>
              <Descriptions.Item label="员工编号">{employee.employeeNo}</Descriptions.Item>
              <Descriptions.Item label="姓名">{employee.name}</Descriptions.Item>
              <Descriptions.Item label="性别">{employee.genderDesc}</Descriptions.Item>
              <Descriptions.Item label="年龄">{employee.age}</Descriptions.Item>
              <Descriptions.Item label="出生日期">{employee.birthday || '-'}</Descriptions.Item>
              <Descriptions.Item label="身份证号">{employee.idCardNo || '-'}</Descriptions.Item>
              <Descriptions.Item label="手机号">{employee.mobile || '-'}</Descriptions.Item>
              <Descriptions.Item label="邮箱">{employee.email || '-'}</Descriptions.Item>
              <Descriptions.Item label="婚姻状况">{employee.maritalStatusDesc || '-'}</Descriptions.Item>
              <Descriptions.Item label="国籍">{employee.nationality || '-'}</Descriptions.Item>
              <Descriptions.Item label="户籍地址" span={2}>{employee.domicileAddress || '-'}</Descriptions.Item>
              <Descriptions.Item label="现住址" span={2}>{employee.currentAddress || '-'}</Descriptions.Item>
              <Descriptions.Item label="员工状态">
                <Tag color={getStatusColor(employee.employeeStatus)}>
                  {getStatusText(employee.employeeStatus)}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="行业类型">{employee.industryTypeDesc}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{new Date(employee.createdTime).toLocaleString()}</Descriptions.Item>
              <Descriptions.Item label="更新时间">{new Date(employee.updatedTime).toLocaleString()}</Descriptions.Item>
              <Descriptions.Item label="备注" span={2}>{employee.remark || '-'}</Descriptions.Item>
            </Descriptions>
          </TabPane>

          <TabPane tab="任职信息" key="job">
            {employee.mainJob && (
              <Descriptions bordered column={2}>
                <Descriptions.Item label="所属组织">{employee.mainJob.orgName}</Descriptions.Item>
                <Descriptions.Item label="所属部门">{employee.mainJob.deptName}</Descriptions.Item>
                <Descriptions.Item label="岗位">{employee.mainJob.positionName}</Descriptions.Item>
                <Descriptions.Item label="职级">{employee.mainJob.rankName || '-'}</Descriptions.Item>
                <Descriptions.Item label="员工类型">{employee.mainJob.employeeTypeDesc}</Descriptions.Item>
                <Descriptions.Item label="用工类型">{employee.mainJob.employmentTypeDesc}</Descriptions.Item>
                <Descriptions.Item label="入职日期">{employee.mainJob.entryDate}</Descriptions.Item>
                <Descriptions.Item label="转正日期">{employee.mainJob.regularDate || '-'}</Descriptions.Item>
                <Descriptions.Item label="工作地点">{employee.mainJob.workLocation || '-'}</Descriptions.Item>
                <Descriptions.Item label="是否主任职">{employee.mainJob.isMainJobDesc}</Descriptions.Item>
              </Descriptions>
            )}
            {!employee.mainJob && <div>暂无任职信息</div>}
          </TabPane>

          <TabPane tab="家庭成员" key="family">
            <FamilyTable employeeId={Number(id)} />
          </TabPane>

          <TabPane tab="教育经历" key="education">
            <EducationTable employeeId={Number(id)} />
          </TabPane>

          <TabPane tab="工作经历" key="work">
            <WorkExperienceTable employeeId={Number(id)} />
          </TabPane>

          <TabPane tab="附件" key="attachment">
            <AttachmentTable employeeId={Number(id)} />
          </TabPane>

          <TabPane tab="异动记录" key="change">
            <div>异动记录功能待实现</div>
          </TabPane>
        </Tabs>
      </Card>
    </div>
  )
}

export default EmployeeDetail
