import React, { useMemo, useState } from 'react'
import { Avatar, Button, Dropdown, Layout, Menu, Result, Space } from 'antd'
import {
  BookOutlined,
  CalendarOutlined,
  DashboardOutlined,
  FileTextOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  NotificationOutlined,
  PayCircleOutlined,
  SettingOutlined,
  TeamOutlined,
  TrophyOutlined,
  UserOutlined,
  UsergroupAddOutlined,
  AuditOutlined,
  ApartmentOutlined,
  BarsOutlined,
  ReadOutlined,
  SolutionOutlined,
  ProfileOutlined,
  FormOutlined,
  ClockCircleOutlined
} from '@ant-design/icons'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth'
import type { MenuItem } from '@/api/menu'

const { Header, Sider, Content } = Layout

interface AppLayoutProps {
  children: React.ReactNode
}

const iconMap: Record<string, React.ReactNode> = {
  DashboardOutlined: <DashboardOutlined />,
  SettingOutlined: <SettingOutlined />,
  TeamOutlined: <TeamOutlined />,
  UsergroupAddOutlined: <UsergroupAddOutlined />,
  UserOutlined: <UserOutlined />,
  CalendarOutlined: <CalendarOutlined />,
  PayCircleOutlined: <PayCircleOutlined />,
  TrophyOutlined: <TrophyOutlined />,
  FileTextOutlined: <FileTextOutlined />,
  BookOutlined: <BookOutlined />,
  NotificationOutlined: <NotificationOutlined />,
  AuditOutlined: <AuditOutlined />,
  ApartmentOutlined: <ApartmentOutlined />,
  BarsOutlined: <BarsOutlined />,
  ReadOutlined: <ReadOutlined />,
  SolutionOutlined: <SolutionOutlined />,
  ProfileOutlined: <ProfileOutlined />,
  FormOutlined: <FormOutlined />,
  ClockCircleOutlined: <ClockCircleOutlined />
}

const fallbackMenus: MenuItem[] = [
  { id: 1, menuName: '工作台', menuType: 2, path: '/dashboard', icon: 'DashboardOutlined', children: [] },
  {
    id: 2,
    menuName: '系统管理',
    menuType: 1,
    path: '/system',
    icon: 'SettingOutlined',
    children: [
      { id: 21, menuName: '用户管理', menuType: 2, path: '/system/user', children: [] },
      { id: 22, menuName: '角色管理', menuType: 2, path: '/system/role', children: [] },
      { id: 23, menuName: '菜单管理', menuType: 2, path: '/system/menu', children: [] },
      { id: 24, menuName: '字典管理', menuType: 2, path: '/system/dict', children: [] }
    ]
  },
  {
    id: 3,
    menuName: '组织管理',
    menuType: 1,
    path: '/org',
    icon: 'TeamOutlined',
    children: [
      { id: 31, menuName: '组织架构', menuType: 2, path: '/org/tree', children: [] },
      { id: 32, menuName: '部门管理', menuType: 2, path: '/org/dept', children: [] },
      { id: 33, menuName: '岗位管理', menuType: 2, path: '/org/position', children: [] },
      { id: 34, menuName: '职级管理', menuType: 2, path: '/org/rank', children: [] }
    ]
  },
  { id: 4, menuName: '考勤管理', menuType: 2, path: '/attendance', icon: 'CalendarOutlined', children: [] }
]

const flattenAllowedPaths = (menus: MenuItem[]): string[] => {
  const paths: string[] = []
  const walk = (nodes: MenuItem[]) => {
    nodes.forEach((node) => {
      if (node.menuType !== 3 && node.path) {
        paths.push(node.path)
      }
      if (node.children?.length) {
        walk(node.children)
      }
    })
  }
  walk(menus)
  return Array.from(new Set(paths))
}

const toAntMenuItems = (menus: MenuItem[]): any[] => {
  return menus
    .filter((item) => item.visible !== 0 && item.status !== 0 && item.menuType !== 3)
    .map((item) => {
      const children = item.children?.length ? toAntMenuItems(item.children) : undefined
      return {
        key: item.path || `menu-${item.id}`,
        icon: item.icon ? iconMap[item.icon] : undefined,
        label: item.menuName,
        children
      }
    })
}

const AppLayout: React.FC<AppLayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()

  const user = useAuthStore((state) => state.user)
  const menuTree = useAuthStore((state) => state.menuTree)
  const logout = useAuthStore((state) => state.logout)

  const effectiveMenus = menuTree.length > 0 ? menuTree : fallbackMenus

  const menuItems = useMemo(() => toAntMenuItems(effectiveMenus), [effectiveMenus])
  const allowedPaths = useMemo(() => flattenAllowedPaths(effectiveMenus), [effectiveMenus])

  const hasRoutePermission = useMemo(() => {
    const path = location.pathname
    if (path === '/' || path === '/dashboard') {
      return true
    }

    if (menuTree.length === 0) {
      return true
    }

    if (allowedPaths.includes(path)) {
      return true
    }

    const root = '/' + (path.split('/')[1] || '')
    if (root !== '/' && allowedPaths.some((menuPath) => menuPath.startsWith(`${root}/`))) {
      return true
    }

    return allowedPaths.some((menuPath) => {
      if (!menuPath || menuPath === '/') {
        return false
      }
      return path.startsWith(`${menuPath}/`)
    })
  }, [location.pathname, allowedPaths, menuTree.length])

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
          onClick={({ key }) => {
            if (String(key).startsWith('/')) {
              navigate(String(key))
            }
          }}
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
              items: [
                { key: 'profile', icon: <UserOutlined />, label: '个人信息' },
                { key: 'logout', icon: <LogoutOutlined />, label: '退出登录' }
              ],
              onClick: async ({ key }) => {
                if (key === 'logout') {
                  await logout()
                  navigate('/login')
                }
              }
            }}
          >
            <Space style={{ cursor: 'pointer' }}>
              <Avatar size="small" icon={<UserOutlined />} />
              <span>{user?.realName || user?.username || '用户'}</span>
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
          {hasRoutePermission ? (
            children
          ) : (
            <Result
              status="403"
              title="无权限访问"
              subTitle="当前账号无此页面权限，请联系管理员配置菜单权限。"
              extra={<Button onClick={() => navigate('/dashboard')}>返回首页</Button>}
            />
          )}
        </Content>
      </Layout>
    </Layout>
  )
}

export default AppLayout
