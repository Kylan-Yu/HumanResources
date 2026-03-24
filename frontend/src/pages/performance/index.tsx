import React, { useEffect, useState } from 'react'
import { Button, Card, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tabs, Tag, message } from 'antd'
import {
  createPerformancePlan,
  createPerformanceRecord,
  deletePerformancePlan,
  deletePerformanceRecord,
  getPerformancePlanPage,
  getPerformanceRecordPage,
  updatePerformancePlan,
  updatePerformancePlanStatus,
  updatePerformanceRecord
} from '@/api/performance'

const PerformancePage: React.FC = () => {
  const [planQuery, setPlanQuery] = useState<any>({ pageNum: 1, pageSize: 10 })
  const [recordQuery, setRecordQuery] = useState<any>({ pageNum: 1, pageSize: 10 })
  const [planData, setPlanData] = useState<any>({ list: [], total: 0 })
  const [recordData, setRecordData] = useState<any>({ list: [], total: 0 })
  const [loadingPlan, setLoadingPlan] = useState(false)
  const [loadingRecord, setLoadingRecord] = useState(false)

  const [planOpen, setPlanOpen] = useState(false)
  const [planEditing, setPlanEditing] = useState<any>(null)
  const [planForm] = Form.useForm()

  const [recordOpen, setRecordOpen] = useState(false)
  const [recordEditing, setRecordEditing] = useState<any>(null)
  const [recordForm] = Form.useForm()

  const loadPlans = async () => {
    setLoadingPlan(true)
    try {
      const res = await getPerformancePlanPage(planQuery)
      setPlanData(res.data || { list: [], total: 0 })
    } finally {
      setLoadingPlan(false)
    }
  }

  const loadRecords = async () => {
    setLoadingRecord(true)
    try {
      const res = await getPerformanceRecordPage(recordQuery)
      setRecordData(res.data || { list: [], total: 0 })
    } finally {
      setLoadingRecord(false)
    }
  }

  useEffect(() => { loadPlans() }, [planQuery.pageNum, planQuery.pageSize])
  useEffect(() => { loadRecords() }, [recordQuery.pageNum, recordQuery.pageSize])

  const submitPlan = async () => {
    const values = await planForm.validateFields()
    if (planEditing?.id) {
      await updatePerformancePlan(planEditing.id, values)
      message.success('更新计划成功')
    } else {
      await createPerformancePlan(values)
      message.success('创建计划成功')
    }
    setPlanOpen(false)
    loadPlans()
  }

  const submitRecord = async () => {
    const values = await recordForm.validateFields()
    if (recordEditing?.id) {
      await updatePerformanceRecord(recordEditing.id, values)
      message.success('更新记录成功')
    } else {
      await createPerformanceRecord(values)
      message.success('创建记录成功')
    }
    setRecordOpen(false)
    loadRecords()
  }

  const planColumns: any[] = [
    { title: '计划名称', dataIndex: 'planName', key: 'planName' },
    { title: '年度', dataIndex: 'planYear', key: 'planYear', width: 90 },
    { title: '周期', dataIndex: 'planPeriod', key: 'planPeriod', width: 90 },
    { title: '组织', dataIndex: 'orgName', key: 'orgName', width: 120 },
    { title: '部门', dataIndex: 'deptName', key: 'deptName', width: 120 },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (v: string) => <Tag color={v === 'RUNNING' ? 'green' : v === 'COMPLETED' ? 'blue' : 'default'}>{v}</Tag>
    },
    {
      title: '操作',
      key: 'action',
      width: 240,
      render: (_, r) => (
        <Space>
          <Button type="link" onClick={() => { setPlanEditing(r); planForm.setFieldsValue(r); setPlanOpen(true) }}>编辑</Button>
          <Button type="link" onClick={async () => { await updatePerformancePlanStatus(r.id, r.status === 'RUNNING' ? 'COMPLETED' : 'RUNNING'); loadPlans() }}>
            切换状态
          </Button>
          <Popconfirm title="确认删除该计划吗？" onConfirm={async () => { await deletePerformancePlan(r.id); loadPlans() }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  const recordColumns: any[] = [
    { title: '员工编号', dataIndex: 'employeeNo', key: 'employeeNo', width: 140 },
    { title: '员工姓名', dataIndex: 'employeeName', key: 'employeeName', width: 120 },
    { title: '所属计划', dataIndex: 'planName', key: 'planName', width: 180 },
    { title: '得分', dataIndex: 'score', key: 'score', width: 90 },
    { title: '等级', dataIndex: 'grade', key: 'grade', width: 80 },
    { title: '结果状态', dataIndex: 'resultStatus', key: 'resultStatus', width: 120 },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, r) => (
        <Space>
          <Button type="link" onClick={() => { setRecordEditing(r); recordForm.setFieldsValue(r); setRecordOpen(true) }}>编辑</Button>
          <Popconfirm title="确认删除该记录吗？" onConfirm={async () => { await deletePerformanceRecord(r.id); loadRecords() }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div>
      <Tabs
        items={[
          {
            key: 'plan',
            label: '绩效计划',
            children: (
              <Card>
                <div style={{ marginBottom: 16 }}>
                  <Button type="primary" onClick={() => { setPlanEditing(null); planForm.resetFields(); planForm.setFieldsValue({ status: 'DRAFT', planYear: new Date().getFullYear(), planPeriod: 'Q1' }); setPlanOpen(true) }}>新增计划</Button>
                </div>
                <Table
                  rowKey="id"
                  columns={planColumns}
                  dataSource={planData.list}
                  loading={loadingPlan}
                  pagination={{
                    current: planQuery.pageNum,
                    pageSize: planQuery.pageSize,
                    total: planData.total,
                    onChange: (page, pageSize) => setPlanQuery({ ...planQuery, pageNum: page, pageSize })
                  }}
                />
              </Card>
            )
          },
          {
            key: 'record',
            label: '绩效记录',
            children: (
              <Card>
                <div style={{ marginBottom: 16 }}>
                  <Button type="primary" onClick={() => { setRecordEditing(null); recordForm.resetFields(); recordForm.setFieldsValue({ resultStatus: 'PENDING' }); setRecordOpen(true) }}>新增记录</Button>
                </div>
                <Table
                  rowKey="id"
                  columns={recordColumns}
                  dataSource={recordData.list}
                  loading={loadingRecord}
                  pagination={{
                    current: recordQuery.pageNum,
                    pageSize: recordQuery.pageSize,
                    total: recordData.total,
                    onChange: (page, pageSize) => setRecordQuery({ ...recordQuery, pageNum: page, pageSize })
                  }}
                />
              </Card>
            )
          }
        ]}
      />

      <Modal title={planEditing ? '编辑绩效计划' : '新增绩效计划'} open={planOpen} onCancel={() => setPlanOpen(false)} onOk={submitPlan}>
        <Form form={planForm} layout="vertical">
          <Form.Item name="planName" label="计划名称" rules={[{ required: true, message: '请输入计划名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="planYear" label="计划年度" rules={[{ required: true, message: '请输入计划年度' }]}>
            <InputNumber min={2000} max={2100} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="planPeriod" label="周期" rules={[{ required: true, message: '请输入周期' }]}>
            <Select options={[{ value: 'Q1', label: 'Q1' }, { value: 'Q2', label: 'Q2' }, { value: 'Q3', label: 'Q3' }, { value: 'Q4', label: 'Q4' }]} />
          </Form.Item>
          <Form.Item name="orgId" label="组织ID"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="deptId" label="部门ID"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="status" label="状态"><Select options={[{ value: 'DRAFT', label: '草稿' }, { value: 'RUNNING', label: '进行中' }, { value: 'COMPLETED', label: '已完成' }]} /></Form.Item>
          <Form.Item name="description" label="说明"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>

      <Modal title={recordEditing ? '编辑绩效记录' : '新增绩效记录'} open={recordOpen} onCancel={() => setRecordOpen(false)} onOk={submitRecord}>
        <Form form={recordForm} layout="vertical">
          <Form.Item name="planId" label="计划ID" rules={[{ required: true, message: '请输入计划ID' }]}><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="employeeId" label="员工ID" rules={[{ required: true, message: '请输入员工ID' }]}><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="employeeNo" label="员工编号" rules={[{ required: true, message: '请输入员工编号' }]}><Input /></Form.Item>
          <Form.Item name="employeeName" label="员工姓名" rules={[{ required: true, message: '请输入员工姓名' }]}><Input /></Form.Item>
          <Form.Item name="score" label="得分"><InputNumber min={0} max={100} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="grade" label="等级"><Input /></Form.Item>
          <Form.Item name="resultStatus" label="结果状态"><Select options={[{ value: 'PENDING', label: '待评估' }, { value: 'COMPLETED', label: '已完成' }]} /></Form.Item>
          <Form.Item name="managerComment" label="经理评语"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default PerformancePage

