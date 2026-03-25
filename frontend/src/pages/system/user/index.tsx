import React, { useEffect, useMemo, useState } from 'react'
import {
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Popconfirm,
  Select,
  Space,
  Switch,
  Table,
  Tag,
  DatePicker
} from 'antd'
import dayjs from 'dayjs'
import {
  createUser,
  createUserCustomField,
  deleteUser,
  deleteUserCustomField,
  getUserCustomFields,
  getUserDetail,
  getUserPage,
  resetUserPassword,
  updateUser,
  updateUserCustomField,
  updateUserStatus
} from '@/api/user'
import { get } from '@/utils/request'

interface UserRow {
  id: number
  username: string
  realName: string
  mobile?: string
  phone?: string
  email?: string
  status: number
  industryType?: string
  extJson?: any
  customFields?: Record<string, any>
  orgId?: number
  deptId?: number
  positionId?: number
  remark?: string
  createdTime?: string
}

interface CustomFieldDef {
  id: number
  fieldKey: string
  fieldName: string
  fieldType: 'TEXT' | 'NUMBER' | 'DATE' | 'SELECT' | 'BOOLEAN'
  requiredFlag?: number
  placeholder?: string
  optionsJson?: string
  defaultValue?: string
  industryType?: string
  sortOrder?: number
  status?: number
}

const UserManagement: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [users, setUsers] = useState<UserRow[]>([])
  const [total, setTotal] = useState(0)
  const [query, setQuery] = useState<any>({ pageNum: 1, pageSize: 10, username: '', realName: '', status: undefined })

  const [orgList, setOrgList] = useState<any[]>([])
  const [deptList, setDeptList] = useState<any[]>([])
  const [positionList, setPositionList] = useState<any[]>([])
  const [customFields, setCustomFields] = useState<CustomFieldDef[]>([])

  const [userModalOpen, setUserModalOpen] = useState(false)
  const [fieldManagerOpen, setFieldManagerOpen] = useState(false)
  const [fieldEditorOpen, setFieldEditorOpen] = useState(false)
  const [editingUser, setEditingUser] = useState<UserRow | null>(null)
  const [editingField, setEditingField] = useState<CustomFieldDef | null>(null)

  const [searchForm] = Form.useForm()
  const [userForm] = Form.useForm()
  const [fieldForm] = Form.useForm()

  const orgMap = useMemo(() => {
    const m: Record<number, string> = {}
    orgList.forEach((item) => {
      m[item.id] = item.orgName
    })
    return m
  }, [orgList])

  const deptMap = useMemo(() => {
    const m: Record<number, string> = {}
    deptList.forEach((item) => {
      m[item.id] = item.deptName
    })
    return m
  }, [deptList])

  const positionMap = useMemo(() => {
    const m: Record<number, string> = {}
    positionList.forEach((item) => {
      m[item.id] = item.positionName
    })
    return m
  }, [positionList])

  const activeCustomFields = useMemo(
    () => [...customFields].filter((item) => item.status !== 0).sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0)),
    [customFields]
  )

  const parseExtJson = (extJson: any) => {
    if (!extJson) return {}
    if (typeof extJson === 'object') return extJson
    try {
      return JSON.parse(extJson)
    } catch {
      return {}
    }
  }

  const normalizeUser = (row: any): UserRow => {
    const ext = parseExtJson(row.extJson)
    return {
      ...row,
      mobile: row.mobile || row.phone,
      customFields: ext.customFields || {},
      orgId: ext.orgId ?? row.orgId,
      deptId: ext.deptId ?? row.deptId,
      positionId: ext.positionId ?? row.positionId,
      remark: ext.remark ?? row.remark
    }
  }

  const loadOptions = async () => {
    const [orgRes, deptRes, posRes] = await Promise.all([
      get<any[]>('/org/list'),
      get<any[]>('/dept/list'),
      get<any[]>('/position/list')
    ])
    setOrgList(orgRes.data || [])
    setDeptList(deptRes.data || [])
    setPositionList(posRes.data || [])
  }

  const loadCustomFields = async (industryType = 'company') => {
    const res = await getUserCustomFields({ industryType })
    setCustomFields(res.data || [])
  }

  const loadUsers = async () => {
    setLoading(true)
    try {
      const res = await getUserPage(query)
      const pageData: any = res.data || {}
      const rows = (pageData.list || pageData.records || []).map(normalizeUser)
      setUsers(rows)
      setTotal(pageData.total || 0)
    } catch {
      message.error('加载用户列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadOptions()
    loadCustomFields()
  }, [])

  useEffect(() => {
    loadUsers()
  }, [query.pageNum, query.pageSize, query.username, query.realName, query.status])

  const toFieldOptions = (optionsJson?: string) => {
    if (!optionsJson) return []
    try {
      const arr = JSON.parse(optionsJson)
      if (Array.isArray(arr)) {
        return arr.map((item) => {
          if (typeof item === 'string') return { label: item, value: item }
          return { label: item.label, value: item.value }
        })
      }
      return []
    } catch {
      return optionsJson
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean)
        .map((s) => ({ label: s, value: s }))
    }
  }

  const openCreateUser = () => {
    setEditingUser(null)
    userForm.resetFields()
    userForm.setFieldsValue({
      status: 1,
      industryType: 'company',
      customFields: {}
    })
    setUserModalOpen(true)
  }

  const openEditUser = async (record: UserRow) => {
    setEditingUser(record)
    const detailRes = await getUserDetail(record.id)
    const detail = normalizeUser(detailRes.data || record)
    const customValues: Record<string, any> = { ...(detail.customFields || {}) }
    activeCustomFields.forEach((field) => {
      if (field.fieldType === 'DATE' && customValues[field.fieldKey]) {
        customValues[field.fieldKey] = dayjs(customValues[field.fieldKey])
      }
    })
    userForm.setFieldsValue({
      username: detail.username,
      realName: detail.realName,
      mobile: detail.mobile,
      email: detail.email,
      status: detail.status,
      industryType: detail.industryType || 'company',
      orgId: detail.orgId,
      deptId: detail.deptId,
      positionId: detail.positionId,
      remark: detail.remark,
      customFields: customValues
    })
    setUserModalOpen(true)
  }

  const submitUser = async () => {
    const values = await userForm.validateFields()
    const customValues: Record<string, any> = {}
    const rawCustom = values.customFields || {}
    Object.keys(rawCustom).forEach((key) => {
      const value = rawCustom[key]
      if (dayjs.isDayjs(value)) {
        customValues[key] = value.format('YYYY-MM-DD')
      } else {
        customValues[key] = value
      }
    })

    const payload: any = {
      username: values.username,
      realName: values.realName,
      mobile: values.mobile,
      email: values.email,
      status: values.status,
      industryType: values.industryType || 'company',
      extJson: JSON.stringify({
        orgId: values.orgId,
        deptId: values.deptId,
        positionId: values.positionId,
        remark: values.remark,
        customFields: customValues
      })
    }

    if (!editingUser) {
      payload.password = values.password
      await createUser(payload)
      message.success('用户创建成功')
    } else {
      await updateUser(editingUser.id, payload)
      message.success('用户更新成功')
    }

    setUserModalOpen(false)
    loadUsers()
  }

  const submitField = async () => {
    const values = await fieldForm.validateFields()
    const optionsText = values.optionsText || ''
    const options = optionsText
      .split(',')
      .map((s: string) => s.trim())
      .filter(Boolean)
    const payload = {
      fieldKey: values.fieldKey,
      fieldName: values.fieldName,
      fieldType: values.fieldType,
      requiredFlag: values.requiredFlag ? 1 : 0,
      placeholder: values.placeholder,
      optionsJson: values.fieldType === 'SELECT' ? JSON.stringify(options) : null,
      defaultValue: values.defaultValue,
      industryType: values.industryType,
      sortOrder: values.sortOrder || 0,
      status: values.status ? 1 : 0
    }

    if (editingField?.id) {
      await updateUserCustomField(editingField.id, payload)
      message.success('字段更新成功')
    } else {
      await createUserCustomField(payload)
      message.success('字段创建成功')
    }
    setFieldEditorOpen(false)
    await loadCustomFields(userForm.getFieldValue('industryType') || 'company')
  }

  const dynamicColumns = activeCustomFields.map((field) => ({
    title: field.fieldName,
    key: `cf-${field.fieldKey}`,
    render: (_: any, record: UserRow) => {
      const value = record.customFields?.[field.fieldKey]
      if (value === undefined || value === null || value === '') return '-'
      if (typeof value === 'boolean') return value ? '是' : '否'
      return String(value)
    }
  }))

  const columns: any[] = [
    { title: '用户名', dataIndex: 'username', key: 'username', width: 140 },
    { title: '姓名', dataIndex: 'realName', key: 'realName', width: 140 },
    { title: '手机号', dataIndex: 'mobile', key: 'mobile', width: 140 },
    { title: '邮箱', dataIndex: 'email', key: 'email', width: 180 },
    { title: '组织', dataIndex: 'orgId', key: 'orgId', width: 120, render: (v: number) => orgMap[v] || '-' },
    { title: '部门', dataIndex: 'deptId', key: 'deptId', width: 120, render: (v: number) => deptMap[v] || '-' },
    { title: '岗位', dataIndex: 'positionId', key: 'positionId', width: 120, render: (v: number) => positionMap[v] || '-' },
    ...dynamicColumns,
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: number, record: UserRow) => (
        <Switch
          checked={status === 1}
          checkedChildren="启用"
          unCheckedChildren="禁用"
          onChange={async (checked) => {
            await updateUserStatus(record.id, checked ? 1 : 0)
            loadUsers()
          }}
        />
      )
    },
    {
      title: '操作',
      key: 'action',
      width: 260,
      fixed: 'right',
      render: (_: any, record: UserRow) => (
        <Space>
          <Button type="link" onClick={() => openEditUser(record)}>编辑</Button>
          <Button
            type="link"
            onClick={async () => {
              await resetUserPassword(record.id, '123456')
              message.success('密码已重置为 123456')
            }}
          >
            重置密码
          </Button>
          <Popconfirm title="确认删除该用户？" onConfirm={async () => { await deleteUser(record.id); loadUsers() }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  const renderCustomFieldInput = (field: CustomFieldDef) => {
    if (field.fieldType === 'NUMBER') return <InputNumber style={{ width: '100%' }} />
    if (field.fieldType === 'DATE') return <DatePicker style={{ width: '100%' }} />
    if (field.fieldType === 'SELECT') return <Select options={toFieldOptions(field.optionsJson)} />
    if (field.fieldType === 'BOOLEAN') {
      return <Select options={[{ label: '是', value: true }, { label: '否', value: false }]} />
    }
    return <Input placeholder={field.placeholder} />
  }

  return (
    <div>
      <Card style={{ marginBottom: 16 }}>
        <Form
          form={searchForm}
          layout="inline"
          onFinish={(values) => {
            setQuery({ ...query, ...values, pageNum: 1 })
          }}
        >
          <Form.Item name="username" label="用户名"><Input allowClear /></Form.Item>
          <Form.Item name="realName" label="姓名"><Input allowClear /></Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              allowClear
              style={{ width: 120 }}
              options={[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]}
            />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">查询</Button>
              <Button
                onClick={() => {
                  searchForm.resetFields()
                  setQuery({ ...query, pageNum: 1, username: '', realName: '', status: undefined })
                }}
              >
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Button type="primary" onClick={openCreateUser}>新增用户</Button>
          <Button onClick={() => setFieldManagerOpen(true)}>自定义字段</Button>
          <Tag color="blue">字段配置后立即生效，无需改代码</Tag>
        </Space>
        <Table
          rowKey="id"
          columns={columns}
          dataSource={users}
          loading={loading}
          scroll={{ x: 1400 }}
          pagination={{
            current: query.pageNum,
            pageSize: query.pageSize,
            total,
            showSizeChanger: true,
            onChange: (page, pageSize) => setQuery({ ...query, pageNum: page, pageSize })
          }}
        />
      </Card>

      <Modal
        title={editingUser ? '编辑用户' : '新增用户'}
        open={userModalOpen}
        width={760}
        onCancel={() => setUserModalOpen(false)}
        onOk={submitUser}
      >
        <Form form={userForm} layout="vertical">
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input disabled={!!editingUser} />
          </Form.Item>
          {!editingUser && (
            <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password />
            </Form.Item>
          )}
          <Form.Item name="realName" label="姓名" rules={[{ required: true, message: '请输入姓名' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="mobile" label="手机号"><Input /></Form.Item>
          <Form.Item name="email" label="邮箱"><Input /></Form.Item>
          <Form.Item name="industryType" label="行业类型">
            <Select
              options={[{ label: '企业', value: 'company' }, { label: '医院', value: 'hospital' }]}
              onChange={(value) => loadCustomFields(value)}
            />
          </Form.Item>
          <Form.Item name="orgId" label="组织"><Select allowClear options={orgList.map((i) => ({ label: i.orgName, value: i.id }))} /></Form.Item>
          <Form.Item name="deptId" label="部门"><Select allowClear options={deptList.map((i) => ({ label: i.deptName, value: i.id }))} /></Form.Item>
          <Form.Item name="positionId" label="岗位"><Select allowClear options={positionList.map((i) => ({ label: i.positionName, value: i.id }))} /></Form.Item>
          <Form.Item name="status" label="状态"><Select options={[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]} /></Form.Item>
          <Form.Item name="remark" label="备注"><Input.TextArea rows={2} /></Form.Item>

          {activeCustomFields.length > 0 && (
            <Card title="自定义信息" size="small">
              {activeCustomFields.map((field) => (
                <Form.Item
                  key={field.fieldKey}
                  name={['customFields', field.fieldKey]}
                  label={field.fieldName}
                  rules={field.requiredFlag ? [{ required: true, message: `请输入${field.fieldName}` }] : []}
                >
                  {renderCustomFieldInput(field)}
                </Form.Item>
              ))}
            </Card>
          )}
        </Form>
      </Modal>

      <Modal
        title="自定义字段管理"
        open={fieldManagerOpen}
        width={900}
        footer={null}
        onCancel={() => setFieldManagerOpen(false)}
      >
        <Space style={{ marginBottom: 12 }}>
          <Button
            type="primary"
            onClick={() => {
              setEditingField(null)
              fieldForm.resetFields()
              fieldForm.setFieldsValue({ fieldType: 'TEXT', requiredFlag: false, status: true, sortOrder: 0 })
              setFieldEditorOpen(true)
            }}
          >
            新增字段
          </Button>
        </Space>
        <Table
          rowKey="id"
          pagination={false}
          dataSource={customFields}
          columns={[
            { title: '编码', dataIndex: 'fieldKey', key: 'fieldKey' },
            { title: '名称', dataIndex: 'fieldName', key: 'fieldName' },
            { title: '类型', dataIndex: 'fieldType', key: 'fieldType' },
            { title: '行业', dataIndex: 'industryType', key: 'industryType', render: (v) => v || '全部' },
            { title: '必填', dataIndex: 'requiredFlag', key: 'requiredFlag', render: (v) => (v ? '是' : '否') },
            { title: '启用', dataIndex: 'status', key: 'status', render: (v) => (v ? '是' : '否') },
            {
              title: '操作',
              key: 'action',
              render: (_, row: CustomFieldDef) => (
                <Space>
                  <Button
                    type="link"
                    onClick={() => {
                      setEditingField(row)
                      fieldForm.setFieldsValue({
                        ...row,
                        requiredFlag: !!row.requiredFlag,
                        status: row.status !== 0,
                        optionsText: toFieldOptions(row.optionsJson).map((o: any) => o.value).join(',')
                      })
                      setFieldEditorOpen(true)
                    }}
                  >
                    编辑
                  </Button>
                  <Popconfirm title="确认删除字段？" onConfirm={async () => { await deleteUserCustomField(row.id); loadCustomFields(userForm.getFieldValue('industryType') || 'company') }}>
                    <Button type="link" danger>删除</Button>
                  </Popconfirm>
                </Space>
              )
            }
          ]}
        />
      </Modal>

      <Modal
        title={editingField ? '编辑字段' : '新增字段'}
        open={fieldEditorOpen}
        onCancel={() => setFieldEditorOpen(false)}
        onOk={submitField}
      >
        <Form form={fieldForm} layout="vertical">
          {!editingField && (
            <Form.Item name="fieldKey" label="字段编码" rules={[{ required: true, message: '请输入字段编码' }]}>
              <Input placeholder="如: professionalTitle" />
            </Form.Item>
          )}
          <Form.Item name="fieldName" label="字段名称" rules={[{ required: true, message: '请输入字段名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="fieldType" label="字段类型" rules={[{ required: true, message: '请选择字段类型' }]}>
            <Select
              options={[
                { label: '文本', value: 'TEXT' },
                { label: '数字', value: 'NUMBER' },
                { label: '日期', value: 'DATE' },
                { label: '下拉', value: 'SELECT' },
                { label: '布尔', value: 'BOOLEAN' }
              ]}
            />
          </Form.Item>
          <Form.Item noStyle shouldUpdate={(prev, curr) => prev.fieldType !== curr.fieldType}>
            {({ getFieldValue }) =>
              getFieldValue('fieldType') === 'SELECT' ? (
                <Form.Item name="optionsText" label="下拉选项（逗号分隔）" rules={[{ required: true, message: '请输入下拉选项' }]}>
                  <Input placeholder="主任医师,副主任医师,主治医师" />
                </Form.Item>
              ) : null
            }
          </Form.Item>
          <Form.Item name="placeholder" label="占位提示"><Input /></Form.Item>
          <Form.Item name="defaultValue" label="默认值"><Input /></Form.Item>
          <Form.Item name="industryType" label="行业范围">
            <Select allowClear options={[{ label: '企业', value: 'company' }, { label: '医院', value: 'hospital' }]} />
          </Form.Item>
          <Form.Item name="sortOrder" label="排序"><InputNumber style={{ width: '100%' }} min={0} /></Form.Item>
          <Form.Item name="requiredFlag" label="必填" valuePropName="checked"><Switch /></Form.Item>
          <Form.Item name="status" label="启用" valuePropName="checked"><Switch /></Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default UserManagement
