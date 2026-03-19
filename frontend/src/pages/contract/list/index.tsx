import React, { useState, useEffect } from 'react'
import { Card, Table, Button, Space, Modal, Form, Input, Select, DatePicker, message, Popconfirm, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined, RedoOutlined } from '@ant-design/icons'
import { getContractPage, deleteContract, updateContractStatus, renewContract } from '@/api/contract/contract'
import type { Contract, ContractQueryParams, ContractRenewParams } from '@/types/contract'
import ContractRenewModal from '../components/ContractRenewModal'

const { Option } = Select
const { RangePicker } = DatePicker

const ContractList: React.FC = () => {
  const [contracts, setContracts] = useState<any>({ list: [], total: 0 })
  const [loading, setLoading] = useState(false)
  const [renewModalVisible, setRenewModalVisible] = useState(false)
  const [selectedContract, setSelectedContract] = useState<Contract | null>(null)
  const [searchParams, setSearchParams] = useState<ContractQueryParams>({
    pageNum: 1,
    pageSize: 10,
    employeeNo: '',
    employeeName: '',
    contractNo: '',
    contractType: '',
    contractStatus: '',
    industryType: '',
    dateRange: []
  })

  useEffect(() => {
    loadContracts()
  }, [searchParams])

  const loadContracts = async () => {
    setLoading(true)
    try {
      const params = {
        ...searchParams,
        startDateBegin: searchParams.dateRange?.[0],
        startDateEnd: searchParams.dateRange?.[1]
      }
      delete params.dateRange
      const response = await getContractPage(params)
      setContracts(response.data)
    } catch (error) {
      message.error('加载合同列表失败')
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = (values: any) => {
    setSearchParams({ ...searchParams, ...values, pageNum: 1 })
  }

  const handleReset = () => {
    setSearchParams({ 
      pageNum: 1, 
      pageSize: 10,
      employeeNo: '',
      employeeName: '',
      contractNo: '',
      contractType: '',
      contractStatus: '',
      industryType: '',
      dateRange: []
    })
  }

  const handleDelete = async (id: number) => {
    try {
      await deleteContract(id)
      message.success('删除成功')
      loadContracts()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleStatusChange = async (id: number, status: string) => {
    try {
      await updateContractStatus(id, status)
      message.success('状态更新成功')
      loadContracts()
    } catch (error) {
      message.error('状态更新失败')
    }
  }

  const handleRenew = (contract: Contract) => {
    setSelectedContract(contract)
    setRenewModalVisible(true)
  }

  const handleRenewSubmit = async (values: ContractRenewParams) => {
    if (!selectedContract) return
    
    try {
      await renewContract(selectedContract.id, values)
      message.success('续签成功')
      setRenewModalVisible(false)
      setSelectedContract(null)
      loadContracts()
    } catch (error) {
      message.error('续签失败')
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
      title: '合同期限',
      key: 'period',
      width: 180,
      render: (_: any, record: Contract) => (
        <div>
          <div>{record.startDate} 至</div>
          <div>{record.endDate}</div>
        </div>
      )
    },
    {
      title: '签署日期',
      dataIndex: 'signDate',
      key: 'signDate',
      width: 120,
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
      width: 250,
      fixed: 'right',
      render: (_: any, record: Contract) => (
        <Space>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => {
              window.location.href = `/contract/detail/${record.id}`
            }}
          >
            详情
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => {
              window.location.href = `/contract/edit/${record.id}`
            }}
          >
            编辑
          </Button>
          {record.contractStatus === 'ACTIVE' && (
            <Button
              type="link"
              icon={<RedoOutlined />}
              onClick={() => handleRenew(record)}
            >
              续签
            </Button>
          )}
          <Button
            type="link"
            onClick={() => {
              const newStatus = record.contractStatus === 'ACTIVE' ? 'TERMINATED' : 'ACTIVE'
              Modal.confirm({
                title: '确认操作',
                content: `确定要${newStatus === 'ACTIVE' ? '激活' : '终止'}该合同吗？`,
                onOk: () => handleStatusChange(record.id!, newStatus),
              })
            }}
          >
            {record.contractStatus === 'ACTIVE' ? '终止' : '激活'}
          </Button>
          <Popconfirm
            title="确定要删除该合同吗？"
            onConfirm={() => handleDelete(record.id!)}
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Card style={{ marginBottom: 16 }}>
        <Form layout="inline" onFinish={handleSearch}>
          <Form.Item name="employeeNo" label="员工编号">
            <Input placeholder="请输入员工编号" allowClear />
          </Form.Item>
          <Form.Item name="employeeName" label="员工姓名">
            <Input placeholder="请输入员工姓名" allowClear />
          </Form.Item>
          <Form.Item name="contractNo" label="合同编号">
            <Input placeholder="请输入合同编号" allowClear />
          </Form.Item>
          <Form.Item name="contractType" label="合同类型">
            <Select placeholder="请选择合同类型" allowClear style={{ width: 150 }}>
              <Option value="LABOR_CONTRACT">劳动合同</Option>
              <Option value="CONFIDENTIALITY_AGREEMENT">保密协议</Option>
              <Option value="NON_COMPETE_AGREEMENT">竞业协议</Option>
              <Option value="SERVICE_AGREEMENT">劳务协议</Option>
              <Option value="REEMPLOYMENT_AGREEMENT">返聘协议</Option>
              <Option value="POSITION_APPOINTMENT_AGREEMENT">岗位聘任协议</Option>
            </Select>
          </Form.Item>
          <Form.Item name="contractStatus" label="合同状态">
            <Select placeholder="请选择状态" allowClear style={{ width: 120 }}>
              <Option value="DRAFT">草稿</Option>
              <Option value="ACTIVE">生效</Option>
              <Option value="EXPIRING">即将到期</Option>
              <Option value="EXPIRED">已到期</Option>
              <Option value="TERMINATED">终止</Option>
            </Select>
          </Form.Item>
          <Form.Item name="industryType" label="行业类型">
            <Select placeholder="请选择行业类型" allowClear style={{ width: 120 }}>
              <Option value="company">企业</Option>
              <Option value="hospital">医院</Option>
            </Select>
          </Form.Item>
          <Form.Item name="dateRange" label="签署日期">
            <RangePicker />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">搜索</Button>
              <Button onClick={handleReset}>重置</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              window.location.href = '/contract/create'
            }}
          >
            新增合同
          </Button>
        </div>
        
        <Table
          columns={columns}
          dataSource={contracts.list}
          rowKey="id"
          loading={loading}
          pagination={{
            current: searchParams.pageNum,
            pageSize: searchParams.pageSize,
            total: contracts.total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, pageSize) => {
              setSearchParams({ ...searchParams, pageNum: page, pageSize })
            },
          }}
          scroll={{ x: 1400 }}
        />
      </Card>

      <ContractRenewModal
        visible={renewModalVisible}
        contract={selectedContract}
        onSubmit={handleRenewSubmit}
        onCancel={() => {
          setRenewModalVisible(false)
          setSelectedContract(null)
        }}
      />
    </div>
  )
}

export default ContractList
