import React, { useEffect, useState } from 'react'
import { Button, Card, Descriptions, Tag } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import { getRecruitRequirementById } from '@/api/recruit/requirement'

const RecruitRequirementDetail: React.FC = () => {
  const [detail, setDetail] = useState<any>(null)
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()

  const loadDetail = async () => {
    if (!id) return
    const res = await getRecruitRequirementById(Number(id))
    setDetail(res.data)
  }

  useEffect(() => {
    loadDetail()
  }, [id])

  if (!detail) {
    return <Card>加载中...</Card>
  }

  return (
    <Card title="招聘需求详情" extra={<Button onClick={() => navigate('/recruit/requirement/list')}>返回</Button>}>
      <Descriptions column={2} bordered>
        <Descriptions.Item label="需求编号">{detail.requirementNo}</Descriptions.Item>
        <Descriptions.Item label="需求标题">{detail.title}</Descriptions.Item>
        <Descriptions.Item label="组织">{detail.orgName}</Descriptions.Item>
        <Descriptions.Item label="部门">{detail.deptName}</Descriptions.Item>
        <Descriptions.Item label="岗位">{detail.positionName}</Descriptions.Item>
        <Descriptions.Item label="招聘人数">{detail.headcount}</Descriptions.Item>
        <Descriptions.Item label="紧急程度">{detail.urgencyLevelDesc}</Descriptions.Item>
        <Descriptions.Item label="状态"><Tag color="blue">{detail.requirementStatusDesc}</Tag></Descriptions.Item>
        <Descriptions.Item label="期望入职日期">{detail.expectedEntryDate}</Descriptions.Item>
        <Descriptions.Item label="行业类型">{detail.industryTypeDesc}</Descriptions.Item>
        <Descriptions.Item label="招聘原因" span={2}>{detail.reason || '-'}</Descriptions.Item>
        <Descriptions.Item label="备注" span={2}>{detail.remark || '-'}</Descriptions.Item>
      </Descriptions>
    </Card>
  )
}

export default RecruitRequirementDetail

