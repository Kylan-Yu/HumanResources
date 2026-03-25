import React, { useEffect } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { Spin } from 'antd'
import AppLayout from './components/common/AppLayout'
import Login from './pages/auth/Login'
import Dashboard from './pages/dashboard/Dashboard'

import UserManagement from './pages/system/user'
import RoleManagement from './pages/system/role'
import MenuManagement from './pages/system/menu'
import DictManagement from './pages/system/dict'

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

import AttendancePage from './pages/attendance'
import PerformancePage from './pages/performance'
import TrainingPage from './pages/training'

import ContractList from './pages/contract/list'
import ContractCreate from './pages/contract/create'
import ContractEdit from './pages/contract/edit'
import ContractDetail from './pages/contract/detail'
import ContractExpireWarning from './pages/contract/expireWarning'
import { useAuthStore } from './store/auth'
import NoticeListPage from './pages/notice/list'
import NoticeManagePage from './pages/notice/manage'
import WorkflowTodoPage from './pages/workflow/todo'
import WorkflowTemplatePage from './pages/workflow/template'
import LeaveApplyPage from './pages/ess/leave/apply'
import LeaveMyPage from './pages/ess/leave/my'
import ProfilePage from './pages/ess/profile'
import AttendanceSelfPage from './pages/ess/attendance'
import PatchApplyPage from './pages/ess/patch'
import OvertimeApplyPage from './pages/ess/overtime'
import TeamAttendancePage from './pages/teamAttendance'
import TeamMemberPage from './pages/teamMember'
import DeptNoticePage from './pages/noticeDept'

const RequireAuth: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const initialized = useAuthStore((state) => state.initialized)
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const initialize = useAuthStore((state) => state.initialize)

  useEffect(() => {
    initialize()
  }, [initialize])

  if (!initialized) {
    return <Spin style={{ marginTop: 120, width: '100%' }} />
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/*"
        element={
          <RequireAuth>
            <AppLayout>
              <Routes>
                <Route path="/" element={<Navigate to="/dashboard" replace />} />
                <Route path="/dashboard" element={<Dashboard />} />

                <Route path="/system/user" element={<UserManagement />} />
                <Route path="/system/role" element={<RoleManagement />} />
                <Route path="/system/menu" element={<MenuManagement />} />
                <Route path="/system/dict" element={<DictManagement />} />

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

                <Route path="/attendance" element={<AttendancePage />} />
                <Route path="/performance" element={<PerformancePage />} />
                <Route path="/performance/*" element={<PerformancePage />} />
                <Route path="/training" element={<TrainingPage />} />
                <Route path="/training/*" element={<TrainingPage />} />

                <Route path="/contract/list" element={<ContractList />} />
                <Route path="/contract/create" element={<ContractCreate />} />
                <Route path="/contract/edit/:id" element={<ContractEdit />} />
                <Route path="/contract/detail/:id" element={<ContractDetail />} />
                <Route path="/contract/expire-warning" element={<ContractExpireWarning />} />

                <Route path="/notice/list" element={<NoticeListPage />} />
                <Route path="/notice/manage" element={<NoticeManagePage />} />

                <Route path="/workflow/todo" element={<WorkflowTodoPage />} />
                <Route path="/workflow/template" element={<WorkflowTemplatePage />} />

                <Route path="/ess/profile" element={<ProfilePage />} />
                <Route path="/ess/attendance" element={<AttendanceSelfPage />} />
                <Route path="/ess/leave/apply" element={<LeaveApplyPage />} />
                <Route path="/ess/leave/my" element={<LeaveMyPage />} />
                <Route path="/ess/patch/apply" element={<PatchApplyPage />} />
                <Route path="/ess/overtime/apply" element={<OvertimeApplyPage />} />

                <Route path="/attendance/team" element={<TeamAttendancePage />} />
                <Route path="/employee/team" element={<TeamMemberPage />} />
                <Route path="/notice/dept" element={<DeptNoticePage />} />

                <Route path="*" element={<Navigate to="/dashboard" replace />} />
              </Routes>
            </AppLayout>
          </RequireAuth>
        }
      />
    </Routes>
  )
}

export default App
