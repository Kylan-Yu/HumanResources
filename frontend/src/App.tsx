import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { Layout } from 'antd'
import AppLayout from './components/common/AppLayout'
import Login from './pages/auth/Login'
import Dashboard from './pages/dashboard/Dashboard'

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
            </Routes>
          </AppLayout>
        }
      />
    </Routes>
  )
}

export default App
