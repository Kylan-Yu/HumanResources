// 薪资相关类型定义
export interface PayrollStandard {
  id: number
  standardName: string
  orgId: number
  orgName: string
  deptId: number
  deptName: string
  positionId: number
  positionName: string
  gradeLevel: string
  baseSalary: number
  performanceSalary: number
  positionAllowance: number
  mealAllowance: number
  transportAllowance: number
  communicationAllowance: number
  housingAllowance: number
  otherAllowance: number
  totalSalary: number
  status: string
  statusDesc: string
  industryType: string
  industryTypeDesc: string
  extJson?: string
  remark?: string
  createdTime: string
  updatedTime: string
}

export interface PayrollRecord {
  id: number
  payrollNo: string
  employeeId: number
  employeeName: string
  employeeNo: string
  payrollPeriod: string
  periodStartDate: string
  periodEndDate: string
  payDate: string
  baseSalary: number
  performanceSalary: number
  positionAllowance: number
  mealAllowance: number
  transportAllowance: number
  communicationAllowance: number
  housingAllowance: number
  otherAllowance: number
  grossSalary: number
  socialPersonal: number
  fundPersonal: number
  incomeTax: number
  otherDeduction: number
  totalDeduction: number
  netSalary: number
  status: string
  statusDesc: string
  industryType: string
  industryTypeDesc: string
  extJson?: string
  remark?: string
  createdTime: string
  updatedTime: string
}

export interface PayrollStandardCreateParams {
  standardName: string
  orgId: number
  deptId?: number
  positionId?: number
  gradeLevel?: string
  baseSalary: number
  performanceSalary?: number
  positionAllowance?: number
  mealAllowance?: number
  transportAllowance?: number
  communicationAllowance?: number
  housingAllowance?: number
  otherAllowance?: number
  status: string
  industryType: string
  extJson?: string
  remark?: string
}

export interface PayrollStandardUpdateParams {
  standardName?: string
  orgId?: number
  deptId?: number
  positionId?: number
  gradeLevel?: string
  baseSalary?: number
  performanceSalary?: number
  positionAllowance?: number
  mealAllowance?: number
  transportAllowance?: number
  communicationAllowance?: number
  housingAllowance?: number
  otherAllowance?: number
  status?: string
  industryType?: string
  extJson?: string
  remark?: string
}

export interface PayrollStandardQueryParams {
  pageNum?: number
  pageSize?: number
  standardName?: string
  orgId?: number
  deptId?: number
  positionId?: number
  gradeLevel?: string
  status?: string
  industryType?: string
}

export interface PayrollCalculationParams {
  employeeIds: number[]
  payrollPeriod: string
  periodStartDate: string
  periodEndDate: string
  payDate: string
}
