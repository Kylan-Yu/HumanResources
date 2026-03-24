import React, { useEffect, useState } from 'react'
import { Button, Card, Descriptions, Tag } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import { getPayrollStandardById } from '@/api/payroll/standard'

const PayrollStandardDetail: React.FC = () => {
  const [detail, setDetail] = useState<any>(null)
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()

  const loadDetail = async () => {
    if (!id) return
    const res = await getPayrollStandardById(Number(id))
    setDetail(res.data)
  }

  useEffect(() => {
    loadDetail()
  }, [id])

  if (!detail) {
    return <Card>加载中...</Card>
  }

  return (
    <Card title="薪资标准详情" extra={<Button onClick={() => navigate('/payroll/standard/list')}>返回</Button>}>
      <Descriptions column={2} bordered>
        <Descriptions.Item label="标准名称">{detail.standardName}</Descriptions.Item>
        <Descriptions.Item label="状态"><Tag color={detail.status === 'ACTIVE' ? 'green' : 'default'}>{detail.statusDesc}</Tag></Descriptions.Item>
        <Descriptions.Item label="组织">{detail.orgName || '-'}</Descriptions.Item>
        <Descriptions.Item label="部门">{detail.deptName || '-'}</Descriptions.Item>
        <Descriptions.Item label="岗位">{detail.positionName || '-'}</Descriptions.Item>
        <Descriptions.Item label="职级">{detail.gradeLevel || '-'}</Descriptions.Item>
        <Descriptions.Item label="基础工资">{detail.baseSalary}</Descriptions.Item>
        <Descriptions.Item label="绩效工资">{detail.performanceSalary}</Descriptions.Item>
        <Descriptions.Item label="岗位津贴">{detail.positionAllowance}</Descriptions.Item>
        <Descriptions.Item label="餐补">{detail.mealAllowance}</Descriptions.Item>
        <Descriptions.Item label="交通补贴">{detail.transportAllowance}</Descriptions.Item>
        <Descriptions.Item label="通讯补贴">{detail.communicationAllowance}</Descriptions.Item>
        <Descriptions.Item label="住房补贴">{detail.housingAllowance}</Descriptions.Item>
        <Descriptions.Item label="其他补贴">{detail.otherAllowance}</Descriptions.Item>
        <Descriptions.Item label="总额" span={2}>{detail.totalSalary}</Descriptions.Item>
        <Descriptions.Item label="备注" span={2}>{detail.remark || '-'}</Descriptions.Item>
      </Descriptions>
    </Card>
  )
}

export default PayrollStandardDetail

