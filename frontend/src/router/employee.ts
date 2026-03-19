import React from 'react'
import { lazy } from 'react'

// 懒加载员工相关页面组件
const EmployeeList = lazy(() => import('../pages/employee/list'))
const EmployeeCreate = lazy(() => import('../pages/employee/create'))
const EmployeeEdit = lazy(() => import('../pages/employee/edit'))
const EmployeeDetail = lazy(() => import('../pages/employee/detail'))

// 员工管理路由配置
export const employeeRoutes = [
  {
    path: '/employee',
    name: '员工管理',
    icon: 'TeamOutlined',
    children: [
      {
        path: '/employee/list',
        name: '员工列表',
        component: EmployeeList,
        meta: {
          title: '员工列表',
          permission: 'employee:list'
        }
      },
      {
        path: '/employee/create',
        name: '新增员工',
        component: EmployeeCreate,
        meta: {
          title: '新增员工',
          permission: 'employee:add',
          hidden: true // 不在菜单中显示
        }
      },
      {
        path: '/employee/edit/:id',
        name: '编辑员工',
        component: EmployeeEdit,
        meta: {
          title: '编辑员工',
          permission: 'employee:edit',
          hidden: true // 不在菜单中显示
        }
      },
      {
        path: '/employee/detail/:id',
        name: '员工详情',
        component: EmployeeDetail,
        meta: {
          title: '员工详情',
          permission: 'employee:detail',
          hidden: true // 不在菜单中显示
        }
      }
    ]
  }
]

// 菜单配置
export const employeeMenus = [
  {
    key: 'employee',
    name: '员工管理',
    icon: 'TeamOutlined',
    path: '/employee',
    children: [
      {
        key: 'employee-list',
        name: '员工列表',
        path: '/employee/list',
        permission: 'employee:list'
      }
    ]
  }
]
