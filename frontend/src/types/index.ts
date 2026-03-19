import React from 'react'

// 通用API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 分页请求参数
export interface PageRequest {
  pageNum: number
  pageSize: number
  orderBy?: string
  orderDirection?: string
  keyword?: string
}

// 分页响应数据
export interface PageResult<T = any> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
  hasNext: boolean
  hasPrevious: boolean
}

// 用户相关类型
export interface User {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  avatar?: string
  status: number
  industryType: string
  createTime: string
  updateTime: string
}

// 角色相关类型
export interface Role {
  id: number
  roleCode: string
  roleName: string
  description: string
  status: number
  sortOrder: number
  createTime: string
  updateTime: string
}

// 菜单相关类型
export interface Menu {
  id: number
  parentId: number
  menuName: string
  menuType: number
  path: string
  component: string
  permission: string
  icon: string
  sortOrder: number
  visible: number
  status: number
  createTime: string
  updateTime: string
}

// 组织相关类型
export interface Organization {
  id: number
  orgCode: string
  orgName: string
  orgType: string
  parentId: number
  industryType: string
  status: number
  sortOrder: number
  extJson?: Record<string, any>
  createTime: string
  updateTime: string
}

// 部门相关类型
export interface Department {
  id: number
  deptCode: string
  deptName: string
  deptType: string
  orgId: number
  parentId: number
  leaderId?: number
  phone?: string
  email?: string
  status: number
  sortOrder: number
  extJson?: Record<string, any>
  createTime: string
  updateTime: string
}

// 岗位相关类型
export interface Position {
  id: number
  positionCode: string
  positionName: string
  positionCategory: string
  orgId: number
  deptId: number
  description?: string
  requirements?: string
  status: number
  sortOrder: number
  extJson?: Record<string, any>
  createTime: string
  updateTime: string
}

// 员工相关类型
export interface Employee {
  id: number
  employeeCode: string
  userId?: number
  realName: string
  gender?: number
  birthDate?: string
  phone: string
  email?: string
  employeeStatus: string
  industryType: string
  hireDate?: string
  probationEndDate?: string
  resignDate?: string
  workYears: number
  extJson?: Record<string, any>
  createTime: string
  updateTime: string
}

// 字典类型
export interface DictType {
  id: number
  dictName: string
  dictType: string
  status: number
  remark?: string
  createTime: string
  updateTime: string
}

// 字典数据
export interface DictData {
  id: number
  dictSort: number
  dictLabel: string
  dictValue: string
  dictType: string
  cssClass?: string
  listClass?: string
  isDefault: string
  status: number
  remark?: string
  createTime: string
  updateTime: string
}

// 表格列配置
export interface TableColumn {
  title: string
  dataIndex: string
  key?: string
  width?: number
  fixed?: 'left' | 'right'
  sorter?: boolean
  render?: (value: any, record: any, index: number) => React.ReactNode
}

// 表单字段配置
export interface FormField {
  name: string
  label: string
  type: 'input' | 'select' | 'textarea' | 'date' | 'number' | 'radio' | 'checkbox'
  required?: boolean
  placeholder?: string
  options?: Array<{ label: string; value: any }>
  rules?: any[]
}

// 按钮配置
export interface ButtonConfig {
  type?: 'primary' | 'default' | 'danger' | 'link'
  icon?: React.ReactNode
  text: string
  onClick?: () => void
  disabled?: boolean
  loading?: boolean
}
