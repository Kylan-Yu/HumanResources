import React, { useState, useEffect } from 'react'
import { Card, Table, Button, message } from 'antd'
import { getUserPage } from '@/api/user'

const TestPage: React.FC = () => {
  const [data, setData] = useState<any[]>([])
  const [loading, setLoading] = useState(false)

  const loadData = async () => {
    setLoading(true)
    try {
      const response = await getUserPage({ pageNum: 1, pageSize: 10 })
      console.log('API Response:', response)
      setData(response.data.records || [])
      message.success('数据加载成功')
    } catch (error) {
      console.error('API Error:', error)
      message.error('数据加载失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  return (
    <Card title="API测试页面">
      <Button onClick={loadData} loading={loading} style={{ marginBottom: 16 }}>
        重新加载数据
      </Button>
      <Table
        dataSource={data}
        loading={loading}
        rowKey="id"
        columns={[
          { title: 'ID', dataIndex: 'id' },
          { title: '用户名', dataIndex: 'username' },
          { title: '真实姓名', dataIndex: 'realName' },
          { title: '邮箱', dataIndex: 'email' },
          { title: '手机', dataIndex: 'mobile' },
          { title: '状态', dataIndex: 'status' }
        ]}
      />
    </Card>
  )
}

export default TestPage
