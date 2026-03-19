import React, { useState, useEffect } from 'react'
import { Card, Table, Button, Space, Select, Tag, message } from 'antd'
import { ArrowLeftOutlined, EyeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getExpireWarningContracts } from '@/api/contract/contract'
import type { Contract } from '@/types/contract'

const { Option } = Select

const ContractExpireWarning: React.FC = () => {
  const [contracts, setContracts] = useState<any>({ list: [], total: 0 })
  const [loading, setLoading] = useState(false)
  const [warningDays, setWarningDays] = useState(30)
  const navigate = useNavigate()

  useEffect(() => {
    loadContracts()
  }, [warningDays])

  const loadContracts = async () => {
    setLoading(true)
    try {
      const response = await getExpireWarningContracts({
        pageNum: 1,
        pageSize: 50,
        warningDays
      })
      setContracts(response.data)
    } catch (error) {
      message.error('加载预警合同失败')
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

  const getWarningColor = (days: number) => {
    if (days <= 7) return 'red'
    if (days <= 15) return 'orange'
    if (days <= 30) return 'gold'
    return 'blue'
  }

  const calculateRemainingDays = (endDate: string) => {
    const end = new Date(endDate)
    const today = new Date()
    const diffTime = end.getTime() - today.getTime()
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    return diffDays > 0 ? diffDays : 0
  }

  const columns = [
    {
      title: '合同编号',
      dataIndex: 'contractNo',
      key: 'contractNo',
      width: 150,
    },
    {
      title: '员工',
      key: 'employee',
      width: 150,
      render: (_: any, record: Contract) => (
        <div>
          <div>{record.employeeName}</div>
          <div style={{ fontSize: '12px', color: '#999' }}>{record.employeeNo}</div>
        </div>
      )
    },
    {
      title: '合同类型',
      dataIndex: 'contractTypeDesc',
      key: 'contractTypeDesc',
      width: 120,
    },
    {
      title: '合同主体',
      dataIndex: 'contractSubject',
      key: 'contractSubject',
      ellipsis: true,
      width: 200,
    },
    {
      title: '结束日期',
      dataIndex: 'endDate',
      key: 'endDate',
      width: 120,
    },
    {
      title: '剩余天数',
      key: 'remainingDays',
      width: 100,
      render: (_: any, record: Contract) => {
        const days = calculateRemainingDays(record.endDate)
        return (
          <Tag color={getWarningColor(days)}>
            {days}天
          </Tag>
        )
      }
    },
    {
      title: '续签次数',
      dataIndex: 'renewCount',
      key: 'renewCount',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'contractStatus',
      key: 'contractStatus',
      width: 100,
      render: (status: string, record: Contract) => (
        <Tag color={getStatusColor(status)}>
          {record.contractStatusDesc}
        </Tag>
      )
    },
    {
      title: '行业类型',
      dataIndex: 'industryTypeDesc',
      key: 'industryTypeDesc',
      width: 100,
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      fixed: 'right',
      render: (_: any, record: Contract) => (
        <Space>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => {
              navigate(`/contract/detail/${record.id}`)
            }}
          >
            详情
          </Button>
        </Space>
      ),
    },
  ]

  const warningStats = contracts.list.reduce((acc: any, contract: Contract) => {
    const days = calculateRemainingDays(contract.endDate)
    if (days <= 7) acc.critical = (acc.critical || 0) + 1
    else if (days <= 15) acc.warning = (acc.warning || 0) + 1
    else if (days <= 30) acc.notice = (acc.notice || 0) + 1
    return acc
  }, {})

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
          <h2>合同到期预警</h2>
        </Space>

        <div style={{ marginBottom: 16, display: 'flex', gap: 16 }}>
          <Card size="small" style={{ flex: 1, textAlign: 'center' }}>
            <div style={{ fontSize: 24, color: '#f5222d', fontWeight: 'bold' }}>
              {warningStats.critical || 0}
            </div>
            <div style={{ color: '#999' }}>7天内到期</div>
          </Card>
          <Card size="small" style={{ flex: 1, textAlign: 'center' }}>
            <div style={{ fontSize: 24, color: '#fa8c16', fontWeight: 'bold' }}>
              {warningStats.warning || 0}
            </div>
            <div style={{ color: '#999' }}>15天内到期</div>
          </Card>
          <Card size="small" style={{ flex: 1, textAlign: 'center' }}>
            <div style={{ fontSize: 24, color: '#faad14', fontWeight: 'bold' }}>
              {warningStats.notice || 0}
            </div>
            <div style={{ color: '#999' }}>30天内到期</div>
          </Card>
          <Card size="small" style={{ flex: 1, textAlign: 'center' }}>
            <div style={{ fontSize: 24, color: '#1890ff', fontWeight: 'bold' }}>
              {contracts.total}
            </div>
            <div style={{ color: '#999' }}>总计预警</div>
          </Card>
        </div>

        <div style={{ marginBottom: 16 }}>
          <Space>
            <span>预警天数：</span>
            <Select
              value={warningDays}
              onChange={setWarningDays}
              style={{ width: 120 }}
            >
              <Option value={7}>7天内</Option>
              <Option value={15}>15天内</Option>
              <Option value={30}>30天内</Option>
              <Option value={60}>60天内</Option>
            </Select>
          </Space>
        </div>
        
        <Table
          columns={columns}
          dataSource={contracts.list}
          rowKey="id"
          loading={loading}
          pagination={false}
          scroll={{ x: 1200 }}
        />
      </Card>
    </div>
  )
}

export default ContractExpireWarning
