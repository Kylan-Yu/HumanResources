// 合同相关类型定义
export interface Contract {
  id: number
  employeeId: number
  employeeName: string
  employeeNo: string
  contractNo: string
  contractType: string
  contractTypeDesc: string
  contractSubject: string
  startDate: string
  endDate: string
  signDate: string
  contractStatus: string
  contractStatusDesc: string
  renewCount: number
  industryType: string
  industryTypeDesc: string
  extJson?: string
  remark?: string
  createdTime: string
  updatedTime: string
  records?: ContractRecord[]
}

export interface ContractRecord {
  id: number
  contractId: number
  recordType: string
  recordTypeDesc: string
  oldValue?: string
  newValue?: string
  changeReason?: string
  operatorId: number
  operatorName: string
  createdTime: string
}

export interface ContractCreateParams {
  employeeId: number
  contractType: string
  contractSubject: string
  startDate: string
  endDate: string
  signDate: string
  industryType: string
  extJson?: string
  remark?: string
}

export interface ContractUpdateParams {
  contractType?: string
  contractSubject?: string
  startDate?: string
  endDate?: string
  signDate?: string
  industryType?: string
  extJson?: string
  remark?: string
}

export interface ContractRenewParams {
  newEndDate: string
  newSignDate: string
  renewReason: string
  remark?: string
}

export interface ContractQueryParams {
  pageNum?: number
  pageSize?: number
  employeeId?: number
  employeeNo?: string
  employeeName?: string
  contractNo?: string
  contractType?: string
  contractStatus?: string
  startDateBegin?: string
  startDateEnd?: string
  endDateBegin?: string
  endDateEnd?: string
  industryType?: string
  dateRange?: [string, string] | []
}
