import React, { useState } from 'react'
import { Layout, Menu, Avatar, Dropdown, Space, Button } from 'antd'
import {
  UserOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  TeamOutlined,
  UsergroupAddOutlined,
  CalendarOutlined,
  PayCircleOutlined,
  TrophyOutlined,
  FileTextOutlined,
  BookOutlined,
  SettingOutlined
} from '@ant-design/icons'
import { useNavigate, useLocation } from 'react-router-dom'

const { Header, Sider, Content } = Layout

interface AppLayoutProps {
  children: React.ReactNode
}

const AppLayout: React.FC<AppLayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()

  const menuItems = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: '工作台'
    },
    {
      key: '/system',
      icon: <SettingOutlined />,
      label: '系统管理',
      children: [
        { key: '/system/user', label: '用户管理' },
        { key: '/system/test', label: 'API测试' },
        { key: '/system/role', label: '角色管理' },
        { key: '/system/menu', label: '菜单管理' },
        { key: '/system/dict', label: '字典管理' }
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
      label: '招聘管理'
    },
    {
      key: '/attendance',
      icon: <CalendarOutlined />,
      label: '考勤管理'
    },
    {
      key: '/payroll',
      icon: <PayCircleOutlined />,
      label: '薪酬管理'
    },
    {
      key: '/performance',
      icon: <TrophyOutlined />,
      label: '绩效管理'
    },
    {
      key: '/contract',
      icon: <FileTextOutlined />,
      label: '合同管理'
    },
    {
      key: '/training',
      icon: <BookOutlined />,
      label: '培训管理'
    }
  ]

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人信息'
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录'
    }
  ]

  const handleMenuClick = (key: string) => {
    navigate(key)
  }

  const handleUserMenuClick = ({ key }: { key: string }) => {
    if (key === 'logout') {
      // TODO: 实现退出登录逻辑
      navigate('/login')
    }
  }

  const getSelectedKeys = () => {
    return [location.pathname]
  }

  const getOpenKeys = () => {
    const pathSegments = location.pathname.split('/')
    return pathSegments.slice(0, 2).map((_, index) => `/${pathSegments.slice(1, index + 1).join('/')}`)
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed}>
        <div style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          fontSize: collapsed ? 16 : 20,
          fontWeight: 'bold',
          borderBottom: '1px solid #f0f0f0'
        }}>
          {collapsed ? 'HR' : 'HRMS'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={getSelectedKeys()}
          defaultOpenKeys={getOpenKeys()}
          items={menuItems}
          onClick={({ key }) => handleMenuClick(key)}
        />
      </Sider>
      <Layout>
        <Header style={{
          padding: '0 16px',
          background: '#fff',
          borderBottom: '1px solid #f0f0f0',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ fontSize: 16 }}
          />
          <Dropdown
            menu={{
              items: userMenuItems,
              onClick: handleUserMenuClick
            }}
            placement="bottomRight"
          >
            <Space style={{ cursor: 'pointer' }}>
              <Avatar size="small" icon={<UserOutlined />} />
              <span>管理员</span>
            </Space>
          </Dropdown>
        </Header>
        <Content style={{
          margin: '16px',
          padding: '16px',
          background: '#fff',
          borderRadius: '8px',
          minHeight: 280
        }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  )
}

export default AppLayout
