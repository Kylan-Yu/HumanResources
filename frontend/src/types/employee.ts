// 员工相关类型定义
export interface Employee {
  id: number
  employeeNo: string
  name: string
  gender: number
  genderDesc: string
  birthday?: string
  age?: number
  idCardNo?: string
  mobile?: string
  email?: string
  maritalStatus?: number
  maritalStatusDesc?: string
  nationality?: string
  domicileAddress?: string
  currentAddress?: string
  employeeStatus: number
  employeeStatusDesc: string
  industryType: string
  industryTypeDesc: string
  extJson?: string
  remark?: string
  createdTime: string
  updatedTime: string
  mainJob?: EmployeeJob
  jobs?: EmployeeJob[]
  families?: EmployeeFamily[]
  educations?: EmployeeEducation[]
  workExperiences?: EmployeeWorkExperience[]
  attachments?: EmployeeAttachment[]
  changeRecords?: EmployeeChangeRecord[]
}

export interface EmployeeJob {
  id: number
  employeeId: number
  orgId: number
  orgName?: string
  deptId: number
  deptName?: string
  positionId: number
  positionName?: string
  rankId?: number
  rankName?: string
  leaderId?: number
  leaderName?: string
  employeeType: string
  employeeTypeDesc?: string
  employmentType: string
  employmentTypeDesc?: string
  entryDate: string
  regularDate?: string
  workLocation?: string
  isMainJob: number
  isMainJobDesc?: string
  status: number
  statusDesc?: string
  createdTime: string
  updatedTime: string
}

export interface EmployeeFamily {
  id: number
  employeeId: number
  name: string
  relationship: string
  relationshipDesc?: string
  gender?: number
  genderDesc?: string
  birthday?: string
  age?: number
  idCardNo?: string
  mobile?: string
  occupation?: string
  workUnit?: string
  remark?: string
  createdTime: string
  updatedTime: string
}

export interface EmployeeEducation {
  id: number
  employeeId: number
  schoolName: string
  educationLevel: string
  educationLevelDesc?: string
  major?: string
  startDate: string
  endDate?: string
  isHighest: number
  isHighestDesc?: string
  degreeType?: string
  degreeTypeDesc?: string
  graduationCertificate?: string
  remark?: string
  createdTime: string
  updatedTime: string
}

export interface EmployeeWorkExperience {
  id: number
  employeeId: number
  companyName: string
  position: string
  startDate: string
  endDate?: string
  jobDescription?: string
  resignReason?: string
  witness?: string
  witnessMobile?: string
  workMonths?: number
  remark?: string
  createdTime: string
  updatedTime: string
}

export interface EmployeeAttachment {
  id: number
  employeeId: number
  attachmentType: string
  attachmentTypeDesc?: string
  fileName: string
  filePath: string
  fileSize?: number
  fileSizeDesc?: string
  fileType?: string
  uploadTime: string
  remark?: string
  createdTime: string
  updatedTime: string
}

export interface EmployeeChangeRecord {
  id: number
  employeeId: number
  changeType: string
  changeTypeDesc?: string
  changeDate: string
  beforeValue?: string
  afterValue?: string
  changeReason?: string
  approverId?: number
  approverName?: string
  approveTime?: string
  remark?: string
  createdTime: string
  updatedTime: string
}

export interface EmployeeCreateParams {
  name: string
  gender: number
  birthday?: string
  idCardNo?: string
  mobile?: string
  email?: string
  maritalStatus?: number
  nationality?: string
  domicileAddress?: string
  currentAddress?: string
  industryType: string
  extJson?: string
  remark?: string
  jobInfo?: EmployeeJobCreateParams
}

export interface EmployeeJobCreateParams {
  orgId: number
  deptId: number
  positionId: number
  rankId?: number
  leaderId?: number
  employeeType: string
  employmentType: string
  entryDate: string
  regularDate?: string
  workLocation?: string
  isMainJob: number
}

export interface EmployeeUpdateParams {
  name: string
  gender: number
  birthday?: string
  idCardNo?: string
  mobile?: string
  email?: string
  maritalStatus?: number
  nationality?: string
  domicileAddress?: string
  currentAddress?: string
  employeeStatus?: number
  industryType: string
  extJson?: string
  remark?: string
}
