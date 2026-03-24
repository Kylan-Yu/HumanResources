import React, { useState } from 'react'
import { Avatar, Button, Dropdown, Layout, Menu, Space } from 'antd'
import {
  BookOutlined,
  CalendarOutlined,
  DashboardOutlined,
  FileTextOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  PayCircleOutlined,
  SettingOutlined,
  TeamOutlined,
  TrophyOutlined,
  UserOutlined,
  UsergroupAddOutlined
} from '@ant-design/icons'
import { useLocation, useNavigate } from 'react-router-dom'

const { Header, Sider, Content } = Layout

interface AppLayoutProps {
  children: React.ReactNode
}

const AppLayout: React.FC<AppLayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()

  const menuItems = [
    { key: '/dashboard', icon: <DashboardOutlined />, label: '工作台' },
    {
      key: '/system',
      icon: <SettingOutlined />,
      label: '系统管理',
      children: [
        { key: '/system/user', label: '用户管理' },
        { key: '/system/role', label: '角色管理' },
        { key: '/system/menu', label: '菜单管理' },
        { key: '/system/dict', label: '字典管理' },
        { key: '/system/test', label: '接口测试' }
      ]
    },
    {
      key: '/org',
      icon: <TeamOutlined />,
      label: '组织管理',
      children: [
        { key: '/org/tree', label: '组织架构' },
        { key: '/org/dept', label: '部门管理' },
        { key: '/org/position', label: '岗位管理' },
        { key: '/org/rank', label: '职级管理' }
      ]
    },
    {
      key: '/employee',
      icon: <UsergroupAddOutlined />,
      label: '员工管理',
      children: [
        { key: '/employee/list', label: '员工档案' },
        { key: '/employee/change', label: '员工异动' }
      ]
    },
    {
      key: '/recruit',
      icon: <UserOutlined />,
      label: '招聘管理',
      children: [{ key: '/recruit/requirement/list', label: '招聘需求' }]
    },
    {
      key: '/attendance',
      icon: <CalendarOutlined />,
      label: '考勤管理'
    },
    {
      key: '/payroll',
      icon: <PayCircleOutlined />,
      label: '薪酬管理',
      children: [{ key: '/payroll/standard/list', label: '薪资标准' }]
    },
    {
      key: '/performance',
      icon: <TrophyOutlined />,
      label: '绩效管理',
      children: [{ key: '/performance', label: '绩效管理' }]
    },
    {
      key: '/contract',
      icon: <FileTextOutlined />,
      label: '合同管理',
      children: [
        { key: '/contract/list', label: '合同列表' },
        { key: '/contract/expire-warning', label: '到期预警' }
      ]
    },
    {
      key: '/training',
      icon: <BookOutlined />,
      label: '培训管理',
      children: [{ key: '/training', label: '培训管理' }]
    }
  ]

  const userMenuItems = [
    { key: 'profile', icon: <UserOutlined />, label: '个人信息' },
    { key: 'logout', icon: <LogoutOutlined />, label: '退出登录' }
  ]

  const getSelectedKeys = () => [location.pathname]

  const getOpenKeys = () => {
    const path = location.pathname
    const first = '/' + path.split('/')[1]
    return first === '/' ? [] : [first]
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed}>
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontSize: collapsed ? 16 : 20,
            fontWeight: 'bold'
          }}
        >
          {collapsed ? 'HR' : 'HRMS'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={getSelectedKeys()}
          defaultOpenKeys={getOpenKeys()}
          items={menuItems as any}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            padding: '0 16px',
            background: '#fff',
            borderBottom: '1px solid #f0f0f0',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}
        >
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
          />
          <Dropdown
            menu={{
              items: userMenuItems,
              onClick: ({ key }) => {
                if (key === 'logout') {
                  navigate('/login')
                }
              }
            }}
          >
            <Space style={{ cursor: 'pointer' }}>
              <Avatar size="small" icon={<UserOutlined />} />
              <span>管理员</span>
            </Space>
          </Dropdown>
        </Header>
        <Content
          style={{
            margin: '16px',
            padding: '16px',
            background: '#fff',
            borderRadius: 8,
            minHeight: 280
          }}
        >
          {children}
        </Content>
      </Layout>
    </Layout>
  )
}

export default AppLayout

