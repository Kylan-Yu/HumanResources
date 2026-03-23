import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { Layout } from 'antd'
import AppLayout from './components/common/AppLayout'
import Login from './pages/auth/Login'
import Dashboard from './pages/dashboard/Dashboard'
import UserManagement from './pages/system/user'
import TestPage from './pages/system/test'

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/*"
        element={
          <AppLayout>
            <Routes>
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/system/user" element={<UserManagement />} />
              <Route path="/system/test" element={<TestPage />} />
              {/* TODO: 添加其他系统管理路由 */}
              <Route path="/system/role" element={<div>角色管理 - 开发中</div>} />
              <Route path="/system/menu" element={<div>菜单管理 - 开发中</div>} />
              <Route path="/system/dict" element={<div>字典管理 - 开发中</div>} />
              {/* TODO: 添加其他模块路由 */}
              <Route path="/org/*" element={<div>组织管理 - 开发中</div>} />
              <Route path="/employee/*" element={<div>员工管理 - 开发中</div>} />
              <Route path="/recruit/*" element={<div>招聘管理 - 开发中</div>} />
              <Route path="/attendance/*" element={<div>考勤管理 - 开发中</div>} />
              <Route path="/payroll/*" element={<div>薪酬管理 - 开发中</div>} />
              <Route path="/performance/*" element={<div>绩效管理 - 开发中</div>} />
              <Route path="/contract/*" element={<div>合同管理 - 开发中</div>} />
              <Route path="/training/*" element={<div>培训管理 - 开发中</div>} />
            </Routes>
          </AppLayout>
        }
      />
    </Routes>
  )
}

export default App
