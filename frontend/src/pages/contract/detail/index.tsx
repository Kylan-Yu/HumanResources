import React, { useState, useEffect } from 'react'
import { Card, Tabs, Descriptions, Tag, Button, Space, message, Table } from 'antd'
import { ArrowLeftOutlined, EditOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import { getContractById } from '@/api/contract/contract'
import type { Contract, ContractRecord } from '@/types/contract'

const { TabPane } = Tabs

const ContractDetail: React.FC = () => {
  const [contract, setContract] = useState<Contract | null>(null)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()

  useEffect(() => {
    if (id) {
      loadContract()
    }
  }, [id])

  const loadContract = async () => {
    setLoading(true)
    try {
      const response = await getContractById(Number(id))
      setContract(response.data)
    } catch (error) {
      message.error('加载合同详情失败')
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status: string) => {
    const colorMap: Record<string, string> = {
      'DRAFT': 'default',
      'ACTIVE': 'green',
      'EXPIRING': 'orange',
      'EXPIRED': 'red',
      'TERMINATED': 'gray'
    }
    return colorMap[status] || 'default'
  }

  const recordColumns = [
    {
      title: '记录类型',
      dataIndex: 'recordTypeDesc',
      key: 'recordTypeDesc',
      width: 120,
    },
    {
      title: '变更原因',
      dataIndex: 'changeReason',
      key: 'changeReason',
      ellipsis: true,
    },
    {
      title: '旧值',
      dataIndex: 'oldValue',
      key: 'oldValue',
      ellipsis: true,
      render: (text: string) => text || '-'
    },
    {
      title: '新值',
      dataIndex: 'newValue',
      key: 'newValue',
      ellipsis: true,
      render: (text: string) => text || '-'
    },
    {
      title: '操作人',
      dataIndex: 'operatorName',
      key: 'operatorName',
      width: 100,
    },
    {
      title: '操作时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 160,
      render: (time: string) => time ? new Date(time).toLocaleString() : '-',
    },
  ]

  if (!contract) {
    return <div>加载中...</div>
  }

  return (
    <div>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Button 
            icon={<ArrowLeftOutlined />} 
            onClick={() => navigate('/contract/list')}
          >
            返回
          </Button>
          <Button 
            type="primary" 
            icon={<EditOutlined />}
            onClick={() => navigate(`/contract/edit/${id}`)}
          >
            编辑
          </Button>
          <h2>合同详情 - {contract.contractNo}</h2>
        </Space>

        <Tabs defaultActiveKey="basic">
          <TabPane tab="基础信息" key="basic">
            <Descriptions bordered column={2}>
              <Descriptions.Item label="合同编号">{contract.contractNo}</Descriptions.Item>
              <Descriptions.Item label="员工姓名">{contract.employeeName}</Descriptions.Item>
              <Descriptions.Item label="员工编号">{contract.employeeNo}</Descriptions.Item>
              <Descriptions.Item label="合同类型">{contract.contractTypeDesc}</Descriptions.Item>
              <Descriptions.Item label="合同主体" span={2}>{contract.contractSubject}</Descriptions.Item>
              <Descriptions.Item label="开始日期">{contract.startDate}</Descriptions.Item>
              <Descriptions.Item label="结束日期">{contract.endDate}</Descriptions.Item>
              <Descriptions.Item label="签署日期">{contract.signDate}</Descriptions.Item>
              <Descriptions.Item label="续签次数">{contract.renewCount}</Descriptions.Item>
              <Descriptions.Item label="合同状态">
                <Tag color={getStatusColor(contract.contractStatus)}>
                  {contract.contractStatusDesc}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="行业类型">{contract.industryTypeDesc}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{new Date(contract.createdTime).toLocaleString()}</Descriptions.Item>
              <Descriptions.Item label="更新时间">{new Date(contract.updatedTime).toLocaleString()}</Descriptions.Item>
              <Descriptions.Item label="备注" span={2}>{contract.remark || '-'}</Descriptions.Item>
            </Descriptions>
          </TabPane>

          <TabPane tab="变更记录" key="records">
            <Table
              columns={recordColumns}
              dataSource={contract.records || []}
              rowKey="id"
              pagination={false}
              locale={{ emptyText: '暂无变更记录' }}
            />
          </TabPane>
        </Tabs>
      </Card>
    </div>
  )
}

export default ContractDetail
