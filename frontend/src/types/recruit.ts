// 招聘相关类型定义
export interface RecruitRequirement {
  id: number
  requirementNo: string
  title: string
  orgId: number
  orgName: string
  deptId: number
  deptName: string
  positionId: number
  positionName: string
  headcount: number
  urgencyLevel: string
  urgencyLevelDesc: string
  requirementStatus: string
  requirementStatusDesc: string
  expectedEntryDate: string
  reason: string
  industryType: string
  industryTypeDesc: string
  extJson?: string
  remark?: string
  createdTime: string
  updatedTime: string
  positions?: RecruitPosition[]
}

export interface RecruitPosition {
  id: number
  requirementId: number
  requirementNo: string
  positionName: string
  jobDescription: string
  jobRequirements: string
  salaryMin: number
  salaryMax: number
  salaryRange: string
  city: string
  employmentType: string
  employmentTypeDesc: string
  publishStatus: string
  publishStatusDesc: string
  industryType: string
  industryTypeDesc: string
  extJson?: string
  remark?: string
  createdTime: string
  updatedTime: string
}

export interface Candidate {
  id: number
  candidateNo: string
  name: string
  gender: string
  genderDesc: string
  mobile: string
  email: string
  resumeUrl?: string
  sourceChannel: string
  sourceChannelDesc: string
  applyPositionId: number
  applyPositionName: string
  candidateStatus: string
  candidateStatusDesc: string
  currentCompany?: string
  currentPosition?: string
  expectedSalary?: number
  industryType: string
  industryTypeDesc: string
  extJson?: string
  remark?: string
  createdTime: string
  updatedTime: string
  interviews?: CandidateInterview[]
  offer?: CandidateOffer
}

export interface CandidateInterview {
  id: number
  candidateId: number
  candidateName: string
  interviewRound: number
  interviewRoundDesc: string
  interviewerId: number
  interviewerName: string
  interviewTime: string
  interviewType: string
  interviewTypeDesc: string
  score?: number
  result?: string
  resultDesc?: string
  feedback?: string
  createdTime: string
  updatedTime: string
}

export interface CandidateOffer {
  id: number
  candidateId: number
  candidateName: string
  offerNo: string
  positionId: number
  positionName: string
  salaryAmount: number
  entryDate: string
  offerStatus: string
  offerStatusDesc: string
  remark?: string
  createdTime: string
  updatedTime: string
}

export interface RecruitRequirementCreateParams {
  title: string
  orgId: number
  deptId: number
  positionId: number
  headcount: number
  urgencyLevel: string
  expectedEntryDate: string
  reason: string
  industryType: string
  extJson?: string
  remark?: string
}

export interface RecruitRequirementUpdateParams {
  title?: string
  orgId?: number
  deptId?: number
  positionId?: number
  headcount?: number
  urgencyLevel?: string
  expectedEntryDate?: string
  reason?: string
  industryType?: string
  extJson?: string
  remark?: string
}

export interface RecruitRequirementQueryParams {
  pageNum?: number
  pageSize?: number
  title?: string
  orgId?: number
  deptId?: number
  positionId?: number
  requirementStatus?: string
  urgencyLevel?: string
  industryType?: string
  expectedEntryDateBegin?: string
  expectedEntryDateEnd?: string
  dateRange?: [string, string] | []
}
