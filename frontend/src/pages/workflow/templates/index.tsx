import React, { useEffect, useMemo, useState } from 'react'
import { Button, Card, Form, Input, Popconfirm, Select, Space, Table, Tag, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { useNavigate } from 'react-router-dom'
import {
  deleteWorkflowTemplate,
  duplicateWorkflowTemplate,
  getWorkflowTemplateCategories,
  getWorkflowTemplatePage,
  publishWorkflowTemplate
} from '../designer/mockApi'
import type { WorkflowTemplateModel, WorkflowTemplateStatus } from '../designer/types'

interface QueryState {
  keyword: string
  category: string
  status: WorkflowTemplateStatus | 'all'
  pageNum: number
  pageSize: number
}

const initialQuery: QueryState = {
  keyword: '',
  category: 'all',
  status: 'all',
  pageNum: 1,
  pageSize: 10
}

const statusTagMap: Record<WorkflowTemplateStatus, { text: string; color: string }> = {
  draft: { text: '草稿', color: 'default' },
  published: { text: '已发布', color: 'success' },
  disabled: { text: '停用', color: 'warning' }
}

const WorkflowTemplateListPage: React.FC = () => {
  const navigate = useNavigate()
  const [query, setQuery] = useState<QueryState>(initialQuery)
  const [tableData, setTableData] = useState<{ list: WorkflowTemplateModel[]; total: number }>({
    list: [],
    total: 0
  })
  const [loading, setLoading] = useState(false)
  const [categories, setCategories] = useState<string[]>([])
  const [form] = Form.useForm()

  const categoryOptions = useMemo(
    () => [{ label: '全部分类', value: 'all' }, ...categories.map((item) => ({ label: item, value: item }))],
    [categories]
  )

  const statusOptions = [
    { label: '全部状态', value: 'all' },
    { label: '草稿', value: 'draft' },
    { label: '已发布', value: 'published' },
    { label: '停用', value: 'disabled' }
  ]

  const loadData = async (nextQuery?: Partial<QueryState>) => {
    const merged = { ...query, ...(nextQuery || {}) }
    setLoading(true)

    try {
      const [page, categoryList] = await Promise.all([
        getWorkflowTemplatePage(merged),
        getWorkflowTemplateCategories()
      ])

      setTableData({
        list: page.list,
        total: page.total
      })
      setCategories(categoryList)
      setQuery(merged)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadData()
  }, [])

  const onSearch = (values: { keyword?: string; category?: string; status?: WorkflowTemplateStatus | 'all' }) => {
    void loadData({
      keyword: values.keyword?.trim() || '',
      category: values.category || 'all',
      status: values.status || 'all',
      pageNum: 1
    })
  }

  const onReset = () => {
    form.resetFields()
    void loadData({ ...initialQuery })
  }

  const columns: ColumnsType<WorkflowTemplateModel> = [
    {
      title: '模板名称',
      dataIndex: 'templateName',
      width: 220
    },
    {
      title: '模板编码',
      dataIndex: 'templateCode',
      width: 220
    },
    {
      title: '流程分类',
      dataIndex: 'category',
      width: 130
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 110,
      render: (value: WorkflowTemplateStatus) => (
        <Tag color={statusTagMap[value].color}>{statusTagMap[value].text}</Tag>
      )
    },
    {
      title: '当前版本',
      dataIndex: 'version',
      width: 100,
      render: (value: number) => `v${value}`
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      width: 170
    },
    {
      title: '操作',
      width: 380,
      fixed: 'right',
      render: (_, row) => (
        <Space size={4} wrap>
          <Button type="link" onClick={() => navigate(`/workflow/templates/${row.templateId}/design`)}>
            编辑设计
          </Button>
          <Button
            type="link"
            disabled={row.status === 'published'}
            onClick={async () => {
              try {
                const result = await publishWorkflowTemplate(row.templateId)
                message.success(`发布成功：${result.publishTime}`)
                void loadData()
              } catch (error) {
                message.error((error as Error).message || '发布失败')
              }
            }}
          >
            发布
          </Button>
          <Button
            type="link"
            onClick={async () => {
              try {
                const duplicated = await duplicateWorkflowTemplate(row.templateId)
                message.success(`复制成功：${duplicated.templateName}`)
                void loadData({ pageNum: 1 })
              } catch (error) {
                message.error((error as Error).message || '复制失败')
              }
            }}
          >
            复制
          </Button>
          <Popconfirm
            title="确认删除该模板吗？"
            onConfirm={async () => {
              try {
                await deleteWorkflowTemplate(row.templateId)
                message.success('删除成功')
                void loadData()
              } catch (error) {
                message.error((error as Error).message || '删除失败')
              }
            }}
          >
            <Button type="link" danger>
              删除
            </Button>
          </Popconfirm>
          <Button type="link" onClick={() => navigate(`/workflow/templates/${row.templateId}/history`)}>
            历史
          </Button>
        </Space>
      )
    }
  ]

  return (
    <Card title="流程模板" extra={<Button onClick={() => navigate('/workflow/templates/new')}>新建模板</Button>}>
      <Form
        form={form}
        layout="inline"
        style={{ marginBottom: 16 }}
        initialValues={{ keyword: '', category: 'all', status: 'all' }}
        onFinish={onSearch}
      >
        <Form.Item name="keyword" label="关键词">
          <Input allowClear placeholder="模板名称/编码" style={{ width: 220 }} />
        </Form.Item>
        <Form.Item name="category" label="流程分类">
          <Select options={categoryOptions} style={{ width: 160 }} />
        </Form.Item>
        <Form.Item name="status" label="状态">
          <Select options={statusOptions} style={{ width: 140 }} />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={loading}>
              搜索
            </Button>
            <Button onClick={onReset}>重置</Button>
          </Space>
        </Form.Item>
      </Form>

      <Table
        rowKey="templateId"
        columns={columns}
        loading={loading}
        dataSource={tableData.list}
        scroll={{ x: 1400 }}
        pagination={{
          current: query.pageNum,
          pageSize: query.pageSize,
          total: tableData.total,
          showSizeChanger: true,
          onChange: (pageNum, pageSize) => {
            void loadData({ pageNum, pageSize })
          }
        }}
      />
    </Card>
  )
}

export default WorkflowTemplateListPage
