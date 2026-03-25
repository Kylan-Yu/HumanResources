import React, { useEffect, useMemo, useState } from 'react'
import {
  Button,
  Card,
  DatePicker,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tabs,
  TimePicker
} from 'antd'
import dayjs from 'dayjs'
import {
  createAttendanceAppeal,
  createAttendanceRecord,
  createAttendanceShift,
  deleteAttendanceAppeal,
  deleteAttendanceRecord,
  deleteAttendanceShift,
  getAttendanceAppealPage,
  getAttendanceEmployeeOptions,
  getAttendanceMonthlyStats,
  getAttendanceRecordPage,
  getAttendanceShiftOptions,
  getAttendanceShiftPage,
  updateAttendanceAppealStatus,
  updateAttendanceRecord,
  updateAttendanceShift
} from '@/api/attendance'

const AttendancePage: React.FC = () => {
  const [shiftQuery, setShiftQuery] = useState<any>({ pageNum: 1, pageSize: 10 })
  const [recordQuery, setRecordQuery] = useState<any>({ pageNum: 1, pageSize: 10 })
  const [appealQuery, setAppealQuery] = useState<any>({ pageNum: 1, pageSize: 10 })
  const [statsMonth, setStatsMonth] = useState(dayjs().format('YYYY-MM'))

  const [shiftData, setShiftData] = useState<any>({ list: [], total: 0 })
  const [recordData, setRecordData] = useState<any>({ list: [], total: 0 })
  const [appealData, setAppealData] = useState<any>({ list: [], total: 0 })
  const [statsData, setStatsData] = useState<any[]>([])

  const [employeeOptions, setEmployeeOptions] = useState<any[]>([])
  const [shiftOptions, setShiftOptions] = useState<any[]>([])

  const [shiftModalOpen, setShiftModalOpen] = useState(false)
  const [recordModalOpen, setRecordModalOpen] = useState(false)
  const [appealModalOpen, setAppealModalOpen] = useState(false)
  const [editingShift, setEditingShift] = useState<any>(null)
  const [editingRecord, setEditingRecord] = useState<any>(null)

  const [shiftForm] = Form.useForm()
  const [recordForm] = Form.useForm()
  const [appealForm] = Form.useForm()

  const loadShiftPage = async () => {
    const res = await getAttendanceShiftPage(shiftQuery)
    setShiftData(res.data || { list: [], total: 0 })
  }

  const loadRecordPage = async () => {
    const res = await getAttendanceRecordPage(recordQuery)
    setRecordData(res.data || { list: [], total: 0 })
  }

  const loadAppealPage = async () => {
    const res = await getAttendanceAppealPage(appealQuery)
    setAppealData(res.data || { list: [], total: 0 })
  }

  const loadStats = async () => {
    const res = await getAttendanceMonthlyStats({ month: statsMonth })
    setStatsData(res.data || [])
  }

  const loadOptions = async () => {
    const [employees, shifts] = await Promise.all([
      getAttendanceEmployeeOptions(),
      getAttendanceShiftOptions()
    ])
    setEmployeeOptions(employees.data || [])
    setShiftOptions(shifts.data || [])
  }

  useEffect(() => {
    loadOptions()
  }, [])

  useEffect(() => {
    loadShiftPage()
  }, [shiftQuery.pageNum, shiftQuery.pageSize])

  useEffect(() => {
    loadRecordPage()
  }, [recordQuery.pageNum, recordQuery.pageSize])

  useEffect(() => {
    loadAppealPage()
  }, [appealQuery.pageNum, appealQuery.pageSize])

  useEffect(() => {
    loadStats()
  }, [statsMonth])

  const shiftNameMap = useMemo(() => {
    const map: Record<string, string> = {}
    shiftOptions.forEach((item) => {
      map[String(item.id)] = item.shiftName
    })
    return map
  }, [shiftOptions])

  const employeeNameMap = useMemo(() => {
    const map: Record<string, string> = {}
    employeeOptions.forEach((item) => {
      map[String(item.id)] = `${item.employeeNo} ${item.employeeName}`
    })
    return map
  }, [employeeOptions])

  const openShiftCreate = () => {
    setEditingShift(null)
    shiftForm.resetFields()
    shiftForm.setFieldsValue({
      workDays: '1,2,3,4,5',
      workHours: 8,
      lateToleranceMinutes: 5,
      earlyToleranceMinutes: 5,
      status: 'ACTIVE',
      industryType: 'company',
      sortOrder: 0
    })
    setShiftModalOpen(true)
  }

  const openShiftEdit = (record: any) => {
    setEditingShift(record)
    shiftForm.setFieldsValue({
      ...record,
      startTime: record.startTime ? dayjs(record.startTime, 'HH:mm:ss') : undefined,
      endTime: record.endTime ? dayjs(record.endTime, 'HH:mm:ss') : undefined
    })
    setShiftModalOpen(true)
  }

  const submitShift = async () => {
    const values = await shiftForm.validateFields()
    const payload = {
      ...values,
      startTime: values.startTime?.format('HH:mm:ss'),
      endTime: values.endTime?.format('HH:mm:ss')
    }
    if (editingShift?.id) {
      await updateAttendanceShift(editingShift.id, payload)
      message.success('班次更新成功')
    } else {
      await createAttendanceShift(payload)
      message.success('班次创建成功')
    }
    setShiftModalOpen(false)
    loadShiftPage()
    loadOptions()
  }

  const openRecordCreate = () => {
    setEditingRecord(null)
    recordForm.resetFields()
    recordForm.setFieldsValue({
      attendanceDate: dayjs(),
      sourceType: 'MANUAL'
    })
    setRecordModalOpen(true)
  }

  const openRecordEdit = (record: any) => {
    setEditingRecord(record)
    recordForm.setFieldsValue({
      ...record,
      attendanceDate: record.attendanceDate ? dayjs(record.attendanceDate) : undefined,
      checkInTime: record.checkInTime ? dayjs(record.checkInTime) : undefined,
      checkOutTime: record.checkOutTime ? dayjs(record.checkOutTime) : undefined
    })
    setRecordModalOpen(true)
  }

  const submitRecord = async () => {
    const values = await recordForm.validateFields()
    const payload = {
      ...values,
      attendanceDate: values.attendanceDate?.format('YYYY-MM-DD'),
      checkInTime: values.checkInTime?.format('YYYY-MM-DD HH:mm:ss'),
      checkOutTime: values.checkOutTime?.format('YYYY-MM-DD HH:mm:ss')
    }
    if (editingRecord?.id) {
      await updateAttendanceRecord(editingRecord.id, payload)
      message.success('记录更新成功')
    } else {
      await createAttendanceRecord(payload)
      message.success('记录创建成功')
    }
    setRecordModalOpen(false)
    loadRecordPage()
  }

  const submitAppeal = async () => {
    const values = await appealForm.validateFields()
    await createAttendanceAppeal({
      ...values,
      requestedCheckInTime: values.requestedCheckInTime?.format('YYYY-MM-DD HH:mm:ss'),
      requestedCheckOutTime: values.requestedCheckOutTime?.format('YYYY-MM-DD HH:mm:ss')
    })
    message.success('申诉提交成功')
    setAppealModalOpen(false)
    loadAppealPage()
  }

  const shiftColumns: any[] = [
    { title: '班次编码', dataIndex: 'shiftCode', key: 'shiftCode' },
    { title: '班次名称', dataIndex: 'shiftName', key: 'shiftName' },
    { title: '上班时间', dataIndex: 'startTime', key: 'startTime' },
    { title: '下班时间', dataIndex: 'endTime', key: 'endTime' },
    { title: '工作日', dataIndex: 'workDays', key: 'workDays' },
    { title: '状态', dataIndex: 'status', key: 'status' },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => openShiftEdit(record)}>编辑</Button>
          <Popconfirm title="确认删除该班次？" onConfirm={async () => { await deleteAttendanceShift(record.id); loadShiftPage(); loadOptions() }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  const recordColumns: any[] = [
    { title: '日期', dataIndex: 'attendanceDate', key: 'attendanceDate' },
    { title: '员工', dataIndex: 'employeeId', key: 'employeeId', render: (v) => employeeNameMap[String(v)] || '-' },
    { title: '班次', dataIndex: 'shiftId', key: 'shiftId', render: (v) => shiftNameMap[String(v)] || '-' },
    { title: '上班打卡', dataIndex: 'checkInTime', key: 'checkInTime' },
    { title: '下班打卡', dataIndex: 'checkOutTime', key: 'checkOutTime' },
    { title: '状态', dataIndex: 'attendanceStatus', key: 'attendanceStatus' },
    { title: '迟到(分)', dataIndex: 'lateMinutes', key: 'lateMinutes' },
    { title: '早退(分)', dataIndex: 'earlyLeaveMinutes', key: 'earlyLeaveMinutes' },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => openRecordEdit(record)}>编辑</Button>
          <Popconfirm title="确认删除该记录？" onConfirm={async () => { await deleteAttendanceRecord(record.id); loadRecordPage() }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  const appealColumns: any[] = [
    { title: '申诉单号', dataIndex: 'appealNo', key: 'appealNo' },
    { title: '员工', dataIndex: 'employeeId', key: 'employeeId', render: (v) => employeeNameMap[String(v)] || '-' },
    { title: '类型', dataIndex: 'appealType', key: 'appealType' },
    { title: '申请上班时间', dataIndex: 'requestedCheckInTime', key: 'requestedCheckInTime' },
    { title: '申请下班时间', dataIndex: 'requestedCheckOutTime', key: 'requestedCheckOutTime' },
    { title: '状态', dataIndex: 'status', key: 'status' },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          {record.status === 'PENDING' && (
            <>
              <Button type="link" onClick={async () => { await updateAttendanceAppealStatus(record.id, { status: 'APPROVED' }); loadAppealPage() }}>通过</Button>
              <Button type="link" danger onClick={async () => { await updateAttendanceAppealStatus(record.id, { status: 'REJECTED' }); loadAppealPage() }}>驳回</Button>
            </>
          )}
          <Popconfirm title="确认删除该申诉？" onConfirm={async () => { await deleteAttendanceAppeal(record.id); loadAppealPage() }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  const statsColumns: any[] = [
    { title: '员工编号', dataIndex: 'employeeNo', key: 'employeeNo' },
    { title: '员工姓名', dataIndex: 'employeeName', key: 'employeeName' },
    { title: '记录天数', dataIndex: 'totalDays', key: 'totalDays' },
    { title: '出勤天数', dataIndex: 'attendedDays', key: 'attendedDays' },
    { title: '迟到天数', dataIndex: 'lateDays', key: 'lateDays' },
    { title: '早退天数', dataIndex: 'earlyLeaveDays', key: 'earlyLeaveDays' },
    { title: '缺勤天数', dataIndex: 'absentDays', key: 'absentDays' },
    { title: '工时', dataIndex: 'totalWorkHours', key: 'totalWorkHours' },
    { title: '加班工时', dataIndex: 'overtimeHours', key: 'overtimeHours' }
  ]

  return (
    <div>
      <Tabs
        items={[
          {
            key: 'shift',
            label: '班次管理',
            children: (
              <Card>
                <div style={{ marginBottom: 16 }}>
                  <Button type="primary" onClick={openShiftCreate}>新增班次</Button>
                </div>
                <Table
                  rowKey="id"
                  columns={shiftColumns}
                  dataSource={shiftData.list || []}
                  pagination={{
                    current: shiftQuery.pageNum,
                    pageSize: shiftQuery.pageSize,
                    total: shiftData.total || 0,
                    onChange: (page, pageSize) => setShiftQuery({ ...shiftQuery, pageNum: page, pageSize })
                  }}
                />
              </Card>
            )
          },
          {
            key: 'record',
            label: '打卡记录',
            children: (
              <Card>
                <div style={{ marginBottom: 16 }}>
                  <Button type="primary" onClick={openRecordCreate}>新增记录</Button>
                </div>
                <Table
                  rowKey="id"
                  columns={recordColumns}
                  dataSource={recordData.list || []}
                  pagination={{
                    current: recordQuery.pageNum,
                    pageSize: recordQuery.pageSize,
                    total: recordData.total || 0,
                    onChange: (page, pageSize) => setRecordQuery({ ...recordQuery, pageNum: page, pageSize })
                  }}
                />
              </Card>
            )
          },
          {
            key: 'appeal',
            label: '考勤申诉',
            children: (
              <Card>
                <div style={{ marginBottom: 16 }}>
                  <Button type="primary" onClick={() => { appealForm.resetFields(); appealForm.setFieldsValue({ appealType: 'BOTH' }); setAppealModalOpen(true) }}>发起申诉</Button>
                </div>
                <Table
                  rowKey="id"
                  columns={appealColumns}
                  dataSource={appealData.list || []}
                  pagination={{
                    current: appealQuery.pageNum,
                    pageSize: appealQuery.pageSize,
                    total: appealData.total || 0,
                    onChange: (page, pageSize) => setAppealQuery({ ...appealQuery, pageNum: page, pageSize })
                  }}
                />
              </Card>
            )
          },
          {
            key: 'stats',
            label: '月度统计',
            children: (
              <Card>
                <Space style={{ marginBottom: 16 }}>
                  <DatePicker picker="month" value={dayjs(statsMonth)} onChange={(v) => setStatsMonth((v || dayjs()).format('YYYY-MM'))} />
                  <Button onClick={loadStats}>刷新</Button>
                </Space>
                <Table rowKey="employeeId" columns={statsColumns} dataSource={statsData} pagination={false} />
              </Card>
            )
          }
        ]}
      />

      <Modal title={editingShift ? '编辑班次' : '新增班次'} open={shiftModalOpen} onOk={submitShift} onCancel={() => setShiftModalOpen(false)}>
        <Form form={shiftForm} layout="vertical">
          {!editingShift && <Form.Item name="shiftCode" label="班次编码"><Input placeholder="留空自动生成" /></Form.Item>}
          <Form.Item name="shiftName" label="班次名称" rules={[{ required: true, message: '请输入班次名称' }]}><Input /></Form.Item>
          <Form.Item name="startTime" label="上班时间" rules={[{ required: true, message: '请选择上班时间' }]}><TimePicker format="HH:mm:ss" style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="endTime" label="下班时间" rules={[{ required: true, message: '请选择下班时间' }]}><TimePicker format="HH:mm:ss" style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="workDays" label="工作日"><Input placeholder="如: 1,2,3,4,5" /></Form.Item>
          <Form.Item name="workHours" label="标准工时"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="lateToleranceMinutes" label="迟到容错(分钟)"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="earlyToleranceMinutes" label="早退容错(分钟)"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="status" label="状态"><Select options={[{ value: 'ACTIVE', label: '启用' }, { value: 'INACTIVE', label: '禁用' }]} /></Form.Item>
        </Form>
      </Modal>

      <Modal title={editingRecord ? '编辑记录' : '新增记录'} open={recordModalOpen} onOk={submitRecord} onCancel={() => setRecordModalOpen(false)} width={720}>
        <Form form={recordForm} layout="vertical">
          <Form.Item name="employeeId" label="员工" rules={[{ required: true, message: '请选择员工' }]}>
            <Select
              showSearch
              options={employeeOptions.map((item) => ({ value: item.id, label: `${item.employeeNo} ${item.employeeName}` }))}
            />
          </Form.Item>
          <Form.Item name="attendanceDate" label="考勤日期" rules={[{ required: true, message: '请选择考勤日期' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="shiftId" label="班次"><Select allowClear options={shiftOptions.map((item) => ({ value: item.id, label: item.shiftName }))} /></Form.Item>
          <Form.Item name="checkInTime" label="上班打卡"><DatePicker showTime style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="checkOutTime" label="下班打卡"><DatePicker showTime style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="attendanceStatus" label="考勤状态">
            <Select
              allowClear
              options={[
                { value: 'NORMAL', label: '正常' },
                { value: 'LATE', label: '迟到' },
                { value: 'EARLY_LEAVE', label: '早退' },
                { value: 'LATE_EARLY', label: '迟到+早退' },
                { value: 'ABSENT', label: '缺勤' }
              ]}
            />
          </Form.Item>
          <Form.Item name="sourceType" label="来源"><Select options={[{ value: 'MANUAL', label: '手工' }, { value: 'DEVICE', label: '设备' }]} /></Form.Item>
          <Form.Item name="remark" label="备注"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>

      <Modal title="发起申诉" open={appealModalOpen} onOk={submitAppeal} onCancel={() => setAppealModalOpen(false)}>
        <Form form={appealForm} layout="vertical">
          <Form.Item name="employeeId" label="员工" rules={[{ required: true, message: '请选择员工' }]}>
            <Select options={employeeOptions.map((item) => ({ value: item.id, label: `${item.employeeNo} ${item.employeeName}` }))} />
          </Form.Item>
          <Form.Item name="recordId" label="关联记录ID"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="appealType" label="申诉类型"><Select options={[{ value: 'CHECK_IN', label: '上班补卡' }, { value: 'CHECK_OUT', label: '下班补卡' }, { value: 'BOTH', label: '上下班补卡' }]} /></Form.Item>
          <Form.Item name="requestedCheckInTime" label="申请上班时间"><DatePicker showTime style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="requestedCheckOutTime" label="申请下班时间"><DatePicker showTime style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="reason" label="申诉原因" rules={[{ required: true, message: '请输入申诉原因' }]}><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default AttendancePage
