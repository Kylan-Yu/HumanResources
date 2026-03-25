import React, { useEffect, useState } from 'react'
import { Button, Card, Form, Input, message } from 'antd'
import { getUserProfile, updateUserProfile } from '@/api/user'

const ProfilePage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [form] = Form.useForm()

  const loadProfile = async () => {
    setLoading(true)
    try {
      const res = await getUserProfile()
      form.setFieldsValue(res.data || {})
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadProfile()
  }, [])

  const submit = async () => {
    const values = await form.validateFields()
    setSaving(true)
    try {
      await updateUserProfile(values)
      message.success('个人信息更新成功')
      loadProfile()
    } finally {
      setSaving(false)
    }
  }

  return (
    <Card title="我的信息" loading={loading}>
      <Form form={form} layout="vertical">
        <Form.Item name="username" label="用户名">
          <Input disabled />
        </Form.Item>
        <Form.Item name="realName" label="姓名">
          <Input disabled />
        </Form.Item>
        <Form.Item
          name="mobile"
          label="手机号"
          rules={[{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]}
        >
          <Input allowClear />
        </Form.Item>
        <Form.Item name="email" label="邮箱" rules={[{ type: 'email', message: '邮箱格式不正确' }]}>
          <Input allowClear />
        </Form.Item>
        <Form.Item name="address" label="地址">
          <Input.TextArea rows={2} allowClear />
        </Form.Item>
        <Form.Item name="emergencyContact" label="紧急联系人">
          <Input allowClear placeholder="姓名/电话" />
        </Form.Item>
        <Button type="primary" loading={saving} onClick={submit}>
          保存
        </Button>
      </Form>
    </Card>
  )
}

export default ProfilePage
