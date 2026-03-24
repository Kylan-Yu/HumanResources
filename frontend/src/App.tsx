import React from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import AppLayout from './components/common/AppLayout'
import Login from './pages/auth/Login'
import Dashboard from './pages/dashboard/Dashboard'

import UserManagement from './pages/system/user'
import RoleManagement from './pages/system/role'
import MenuManagement from './pages/system/menu'
import DictManagement from './pages/system/dict'
import TestPage from './pages/system/test'

import OrgTree from './pages/org/tree'
import DeptManagement from './pages/org/dept'
import PositionManagement from './pages/org/position'
import RankManagement from './pages/org/rank'

import EmployeeList from './pages/employee/list'
import EmployeeCreate from './pages/employee/create'
import EmployeeEdit from './pages/employee/edit'
import EmployeeDetail from './pages/employee/detail'
import EmployeeChangePage from './pages/employee/change'

import RecruitRequirementList from './pages/recruit/requirement/list'
import RecruitRequirementCreate from './pages/recruit/requirement/create'
import RecruitRequirementEdit from './pages/recruit/requirement/edit'
import RecruitRequirementDetail from './pages/recruit/requirement/detail'

import PayrollStandardList from './pages/payroll/standard/list'
import PayrollStandardCreate from './pages/payroll/standard/create'
import PayrollStandardEdit from './pages/payroll/standard/edit'
import PayrollStandardDetail from './pages/payroll/standard/detail'

import PerformancePage from './pages/performance'
import TrainingPage from './pages/training'

import ContractList from './pages/contract/list'
import ContractCreate from './pages/contract/create'
import ContractEdit from './pages/contract/edit'
import ContractDetail from './pages/contract/detail'
import ContractExpireWarning from './pages/contract/expireWarning'

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
              <Route path="/system/role" element={<RoleManagement />} />
              <Route path="/system/menu" element={<MenuManagement />} />
              <Route path="/system/dict" element={<DictManagement />} />
              <Route path="/system/test" element={<TestPage />} />

              <Route path="/org/tree" element={<OrgTree />} />
              <Route path="/org/dept" element={<DeptManagement />} />
              <Route path="/org/position" element={<PositionManagement />} />
              <Route path="/org/rank" element={<RankManagement />} />

              <Route path="/employee/list" element={<EmployeeList />} />
              <Route path="/employee/create" element={<EmployeeCreate />} />
              <Route path="/employee/edit/:id" element={<EmployeeEdit />} />
              <Route path="/employee/detail/:id" element={<EmployeeDetail />} />
              <Route path="/employee/change" element={<EmployeeChangePage />} />

              <Route path="/recruit" element={<Navigate to="/recruit/requirement/list" replace />} />
              <Route path="/recruit/requirement/list" element={<RecruitRequirementList />} />
              <Route path="/recruit/requirement/create" element={<RecruitRequirementCreate />} />
              <Route path="/recruit/requirement/edit/:id" element={<RecruitRequirementEdit />} />
              <Route path="/recruit/requirement/detail/:id" element={<RecruitRequirementDetail />} />

              <Route path="/payroll" element={<Navigate to="/payroll/standard/list" replace />} />
              <Route path="/payroll/standard/list" element={<PayrollStandardList />} />
              <Route path="/payroll/standard/create" element={<PayrollStandardCreate />} />
              <Route path="/payroll/standard/edit/:id" element={<PayrollStandardEdit />} />
              <Route path="/payroll/standard/detail/:id" element={<PayrollStandardDetail />} />

              <Route path="/performance" element={<PerformancePage />} />
              <Route path="/performance/*" element={<PerformancePage />} />
              <Route path="/training" element={<TrainingPage />} />
              <Route path="/training/*" element={<TrainingPage />} />

              <Route path="/contract/list" element={<ContractList />} />
              <Route path="/contract/create" element={<ContractCreate />} />
              <Route path="/contract/edit/:id" element={<ContractEdit />} />
              <Route path="/contract/detail/:id" element={<ContractDetail />} />
              <Route path="/contract/expire-warning" element={<ContractExpireWarning />} />

              <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Routes>
          </AppLayout>
        }
      />
    </Routes>
  )
}

export default App
