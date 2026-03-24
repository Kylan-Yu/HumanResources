import React, { useEffect, useState } from 'react'
import { Button, Card, DatePicker, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tabs } from 'antd'
import dayjs from 'dayjs'
import {
  createTrainingCourse,
  createTrainingEnrollment,
  createTrainingSession,
  deleteTrainingCourse,
  deleteTrainingEnrollment,
  deleteTrainingSession,
  getTrainingCoursePage,
  getTrainingEnrollmentPage,
  getTrainingSessionPage,
  updateTrainingCourse,
  updateTrainingEnrollment,
  updateTrainingSession
} from '@/api/training'

const TrainingPage: React.FC = () => {
  const [courseQuery, setCourseQuery] = useState<any>({ pageNum: 1, pageSize: 10 })
  const [sessionQuery, setSessionQuery] = useState<any>({ pageNum: 1, pageSize: 10 })
  const [enrollQuery, setEnrollQuery] = useState<any>({ pageNum: 1, pageSize: 10 })
  const [courseData, setCourseData] = useState<any>({ list: [], total: 0 })
  const [sessionData, setSessionData] = useState<any>({ list: [], total: 0 })
  const [enrollData, setEnrollData] = useState<any>({ list: [], total: 0 })

  const [courseOpen, setCourseOpen] = useState(false)
  const [courseEditing, setCourseEditing] = useState<any>(null)
  const [courseForm] = Form.useForm()

  const [sessionOpen, setSessionOpen] = useState(false)
  const [sessionEditing, setSessionEditing] = useState<any>(null)
  const [sessionForm] = Form.useForm()

  const [enrollOpen, setEnrollOpen] = useState(false)
  const [enrollEditing, setEnrollEditing] = useState<any>(null)
  const [enrollForm] = Form.useForm()

  const loadCourses = async () => {
    const res = await getTrainingCoursePage(courseQuery)
    setCourseData(res.data || { list: [], total: 0 })
  }
  const loadSessions = async () => {
    const res = await getTrainingSessionPage(sessionQuery)
    setSessionData(res.data || { list: [], total: 0 })
  }
  const loadEnrollments = async () => {
    const res = await getTrainingEnrollmentPage(enrollQuery)
    setEnrollData(res.data || { list: [], total: 0 })
  }

  useEffect(() => { loadCourses() }, [courseQuery.pageNum, courseQuery.pageSize])
  useEffect(() => { loadSessions() }, [sessionQuery.pageNum, sessionQuery.pageSize])
  useEffect(() => { loadEnrollments() }, [enrollQuery.pageNum, enrollQuery.pageSize])

  const submitCourse = async () => {
    const values = await courseForm.validateFields()
    if (courseEditing?.id) {
      await updateTrainingCourse(courseEditing.id, values)
    } else {
      await createTrainingCourse(values)
    }
    setCourseOpen(false)
    loadCourses()
  }

  const submitSession = async () => {
    const values = await sessionForm.validateFields()
    const payload = {
      ...values,
      startTime: values.startTime?.format('YYYY-MM-DD HH:mm:ss'),
      endTime: values.endTime?.format('YYYY-MM-DD HH:mm:ss')
    }
    if (sessionEditing?.id) {
      await updateTrainingSession(sessionEditing.id, payload)
    } else {
      await createTrainingSession(payload)
    }
    setSessionOpen(false)
    loadSessions()
  }

  const submitEnroll = async () => {
    const values = await enrollForm.validateFields()
    if (enrollEditing?.id) {
      await updateTrainingEnrollment(enrollEditing.id, values)
    } else {
      await createTrainingEnrollment(values)
    }
    setEnrollOpen(false)
    loadEnrollments()
  }

  const courseColumns: any[] = [
    { title: '课程编码', dataIndex: 'courseCode', key: 'courseCode', width: 120 },
    { title: '课程名称', dataIndex: 'courseName', key: 'courseName', width: 180 },
    { title: '课程类型', dataIndex: 'courseType', key: 'courseType', width: 120 },
    { title: '讲师', dataIndex: 'lecturer', key: 'lecturer', width: 120 },
    { title: '课时', dataIndex: 'durationHours', key: 'durationHours', width: 90 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, r) => (
        <Space>
          <Button type="link" onClick={() => { setCourseEditing(r); courseForm.setFieldsValue(r); setCourseOpen(true) }}>编辑</Button>
          <Popconfirm title="确认删除该课程吗？" onConfirm={async () => { await deleteTrainingCourse(r.id); loadCourses() }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  const sessionColumns: any[] = [
    { title: '班次名称', dataIndex: 'sessionName', key: 'sessionName', width: 180 },
    { title: '课程', dataIndex: 'courseName', key: 'courseName', width: 180 },
    { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 180 },
    { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 180 },
    { title: '地点', dataIndex: 'location', key: 'location', width: 160 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, r) => (
        <Space>
          <Button type="link" onClick={() => { setSessionEditing(r); sessionForm.setFieldsValue({ ...r, startTime: r.startTime ? dayjs(r.startTime) : undefined, endTime: r.endTime ? dayjs(r.endTime) : undefined }); setSessionOpen(true) }}>编辑</Button>
          <Popconfirm title="确认删除该班次吗？" onConfirm={async () => { await deleteTrainingSession(r.id); loadSessions() }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  const enrollColumns: any[] = [
    { title: '班次', dataIndex: 'sessionName', key: 'sessionName', width: 180 },
    { title: '员工编号', dataIndex: 'employeeNo', key: 'employeeNo', width: 140 },
    { title: '员工姓名', dataIndex: 'employeeName', key: 'employeeName', width: 120 },
    { title: '出勤状态', dataIndex: 'attendanceStatus', key: 'attendanceStatus', width: 120 },
    { title: '成绩', dataIndex: 'score', key: 'score', width: 80 },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, r) => (
        <Space>
          <Button type="link" onClick={() => { setEnrollEditing(r); enrollForm.setFieldsValue(r); setEnrollOpen(true) }}>编辑</Button>
          <Popconfirm title="确认删除该报名吗？" onConfirm={async () => { await deleteTrainingEnrollment(r.id); loadEnrollments() }}>
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
            key: 'course',
            label: '课程管理',
            children: (
              <Card>
                <div style={{ marginBottom: 16 }}>
                  <Button type="primary" onClick={() => { setCourseEditing(null); courseForm.resetFields(); courseForm.setFieldsValue({ status: 'ACTIVE', courseType: 'GENERAL', industryType: 'company' }); setCourseOpen(true) }}>新增课程</Button>
                </div>
                <Table
                  rowKey="id"
                  columns={courseColumns}
                  dataSource={courseData.list}
                  pagination={{ current: courseQuery.pageNum, pageSize: courseQuery.pageSize, total: courseData.total, onChange: (page, pageSize) => setCourseQuery({ ...courseQuery, pageNum: page, pageSize }) }}
                />
              </Card>
            )
          },
          {
            key: 'session',
            label: '培训班次',
            children: (
              <Card>
                <div style={{ marginBottom: 16 }}>
                  <Button type="primary" onClick={() => { setSessionEditing(null); sessionForm.resetFields(); sessionForm.setFieldsValue({ status: 'PLANNED', industryType: 'company' }); setSessionOpen(true) }}>新增班次</Button>
                </div>
                <Table
                  rowKey="id"
                  columns={sessionColumns}
                  dataSource={sessionData.list}
                  pagination={{ current: sessionQuery.pageNum, pageSize: sessionQuery.pageSize, total: sessionData.total, onChange: (page, pageSize) => setSessionQuery({ ...sessionQuery, pageNum: page, pageSize }) }}
                />
              </Card>
            )
          },
          {
            key: 'enrollment',
            label: '培训报名',
            children: (
              <Card>
                <div style={{ marginBottom: 16 }}>
                  <Button type="primary" onClick={() => { setEnrollEditing(null); enrollForm.resetFields(); enrollForm.setFieldsValue({ attendanceStatus: 'REGISTERED' }); setEnrollOpen(true) }}>新增报名</Button>
                </div>
                <Table
                  rowKey="id"
                  columns={enrollColumns}
                  dataSource={enrollData.list}
                  pagination={{ current: enrollQuery.pageNum, pageSize: enrollQuery.pageSize, total: enrollData.total, onChange: (page, pageSize) => setEnrollQuery({ ...enrollQuery, pageNum: page, pageSize }) }}
                />
              </Card>
            )
          }
        ]}
      />

      <Modal title={courseEditing ? '编辑课程' : '新增课程'} open={courseOpen} onCancel={() => setCourseOpen(false)} onOk={submitCourse}>
        <Form form={courseForm} layout="vertical">
          {!courseEditing && <Form.Item name="courseCode" label="课程编码" rules={[{ required: true, message: '请输入课程编码' }]}><Input /></Form.Item>}
          <Form.Item name="courseName" label="课程名称" rules={[{ required: true, message: '请输入课程名称' }]}><Input /></Form.Item>
          <Form.Item name="courseType" label="课程类型"><Select options={[{ value: 'GENERAL', label: '通用' }, { value: 'ONBOARDING', label: '入职' }, { value: 'PROFESSIONAL', label: '专业' }]} /></Form.Item>
          <Form.Item name="lecturer" label="讲师"><Input /></Form.Item>
          <Form.Item name="durationHours" label="课时"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="status" label="状态"><Select options={[{ value: 'ACTIVE', label: '启用' }, { value: 'INACTIVE', label: '禁用' }]} /></Form.Item>
          <Form.Item name="description" label="描述"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>

      <Modal title={sessionEditing ? '编辑班次' : '新增班次'} open={sessionOpen} onCancel={() => setSessionOpen(false)} onOk={submitSession}>
        <Form form={sessionForm} layout="vertical">
          <Form.Item name="courseId" label="课程ID" rules={[{ required: true, message: '请输入课程ID' }]}><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="sessionName" label="班次名称" rules={[{ required: true, message: '请输入班次名称' }]}><Input /></Form.Item>
          <Form.Item name="startTime" label="开始时间"><DatePicker showTime style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="endTime" label="结束时间"><DatePicker showTime style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="location" label="地点"><Input /></Form.Item>
          <Form.Item name="capacity" label="容量"><InputNumber min={1} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="status" label="状态"><Select options={[{ value: 'PLANNED', label: '计划中' }, { value: 'RUNNING', label: '进行中' }, { value: 'FINISHED', label: '已结束' }]} /></Form.Item>
        </Form>
      </Modal>

      <Modal title={enrollEditing ? '编辑报名' : '新增报名'} open={enrollOpen} onCancel={() => setEnrollOpen(false)} onOk={submitEnroll}>
        <Form form={enrollForm} layout="vertical">
          <Form.Item name="sessionId" label="班次ID" rules={[{ required: true, message: '请输入班次ID' }]}><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="employeeId" label="员工ID" rules={[{ required: true, message: '请输入员工ID' }]}><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="attendanceStatus" label="出勤状态"><Select options={[{ value: 'REGISTERED', label: '已报名' }, { value: 'ATTENDED', label: '已参加' }, { value: 'ABSENT', label: '缺席' }]} /></Form.Item>
          <Form.Item name="score" label="成绩"><InputNumber min={0} max={100} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="feedback" label="反馈"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default TrainingPage

