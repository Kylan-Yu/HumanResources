import React from 'react'
import { Card, Row, Col, Statistic, Progress, List, Avatar, Tag } from 'antd'
import {
  UserOutlined,
  TeamOutlined,
  CalendarOutlined,
  TrophyOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  PayCircleOutlined
} from '@ant-design/icons'

const Dashboard: React.FC = () => {
  const recentActivities = [
    { id: 1, user: '张三', action: '提交了请假申请', time: '10分钟前', type: 'leave' },
    { id: 2, user: '李四', action: '完成了绩效评估', time: '1小时前', type: 'performance' },
    { id: 3, user: '王五', action: '更新了个人信息', time: '2小时前', type: 'profile' },
    { id: 4, user: '赵六', action: '提交了报销申请', time: '3小时前', type: 'expense' }
  ]

  const upcomingEvents = [
    { id: 1, title: '月度绩效评估', date: '2024-03-20', type: 'performance' },
    { id: 2, title: '新员工培训', date: '2024-03-22', type: 'training' },
    { id: 3, title: '季度总结会议', date: '2024-03-25', type: 'meeting' }
  ]

  const getActivityColor = (type: string) => {
    const colors: Record<string, string> = {
      leave: 'blue',
      performance: 'green',
      profile: 'orange',
      expense: 'purple'
    }
    return colors[type] || 'default'
  }

  const getEventColor = (type: string) => {
    const colors: Record<string, string> = {
      performance: 'red',
      training: 'blue',
      meeting: 'green'
    }
    return colors[type] || 'default'
  }

  return (
    <div>
      <h2>工作台</h2>
      
      {/* 统计卡片 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="总员工数"
              value={1234}
              prefix={<UserOutlined />}
              suffix={<ArrowUpOutlined style={{ color: '#3f8600' }} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="本月入职"
              value={23}
              prefix={<TeamOutlined />}
              suffix={<ArrowUpOutlined style={{ color: '#3f8600' }} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="本月离职"
              value={5}
              prefix={<UserOutlined />}
              suffix={<ArrowDownOutlined style={{ color: '#cf1322' }} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="待处理事项"
              value={12}
              prefix={<CalendarOutlined />}
              suffix={<TrophyOutlined style={{ color: '#1890ff' }} />}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        {/* 部门考勤率 */}
        <Col xs={24} lg={12}>
          <Card title="部门考勤率" style={{ height: 400 }}>
            <div style={{ marginBottom: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                <span>技术部</span>
                <span>95%</span>
              </div>
              <Progress percent={95} status="active" />
            </div>
            <div style={{ marginBottom: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                <span>销售部</span>
                <span>88%</span>
              </div>
              <Progress percent={88} status="active" />
            </div>
            <div style={{ marginBottom: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                <span>人事部</span>
                <span>92%</span>
              </div>
              <Progress percent={92} status="active" />
            </div>
            <div style={{ marginBottom: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                <span>财务部</span>
                <span>96%</span>
              </div>
              <Progress percent={96} status="active" />
            </div>
          </Card>
        </Col>

        {/* 最近活动 */}
        <Col xs={24} lg={12}>
          <Card title="最近活动" style={{ height: 400 }}>
            <List
              dataSource={recentActivities}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={<Avatar icon={<UserOutlined />} />}
                    title={
                      <span>
                        {item.user}
                        <Tag color={getActivityColor(item.type)} style={{ marginLeft: 8 }}>
                          {item.action}
                        </Tag>
                      </span>
                    }
                    description={item.time}
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>

        {/* 即将到来的事件 */}
        <Col xs={24} lg={12}>
          <Card title="即将到来的事件" style={{ height: 400 }}>
            <List
              dataSource={upcomingEvents}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <span>
                        {item.title}
                        <Tag color={getEventColor(item.type)} style={{ marginLeft: 8 }}>
                          {item.type === 'performance' ? '绩效' : 
                           item.type === 'training' ? '培训' : '会议'}
                        </Tag>
                      </span>
                    }
                    description={item.date}
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>

        {/* 快速操作 */}
        <Col xs={24} lg={12}>
          <Card title="快速操作" style={{ height: 400 }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 16 }}>
              <Card size="small" hoverable>
                <div style={{ textAlign: 'center' }}>
                  <UserOutlined style={{ fontSize: 24, marginBottom: 8 }} />
                  <div>员工管理</div>
                </div>
              </Card>
              <Card size="small" hoverable>
                <div style={{ textAlign: 'center' }}>
                  <CalendarOutlined style={{ fontSize: 24, marginBottom: 8 }} />
                  <div>考勤管理</div>
                </div>
              </Card>
              <Card size="small" hoverable>
                <div style={{ textAlign: 'center' }}>
                  <PayCircleOutlined style={{ fontSize: 24, marginBottom: 8 }} />
                  <div>薪酬管理</div>
                </div>
              </Card>
              <Card size="small" hoverable>
                <div style={{ textAlign: 'center' }}>
                  <TrophyOutlined style={{ fontSize: 24, marginBottom: 8 }} />
                  <div>绩效管理</div>
                </div>
              </Card>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Dashboard
