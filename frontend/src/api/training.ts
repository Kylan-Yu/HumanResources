import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types'

export interface TrainingCourse {
  id: number
  courseCode: string
  courseName: string
  courseType: string
  lecturer?: string
  durationHours?: number
  status: string
}

export interface TrainingSession {
  id: number
  courseId: number
  courseName?: string
  sessionName: string
  startTime?: string
  endTime?: string
  location?: string
  capacity?: number
  status: string
}

export interface TrainingEnrollment {
  id: number
  sessionId: number
  sessionName?: string
  employeeId: number
  employeeNo?: string
  employeeName?: string
  attendanceStatus?: string
  score?: number
}

export const getTrainingCoursePage = (params: any) =>
  get<PageResult<TrainingCourse>>('/training/courses/page', params)

export const createTrainingCourse = (data: any) =>
  post<number>('/training/courses', data)

export const updateTrainingCourse = (id: number, data: any) =>
  put<boolean>(`/training/courses/${id}`, data)

export const deleteTrainingCourse = (id: number) =>
  del<boolean>(`/training/courses/${id}`)

export const getTrainingSessionPage = (params: any) =>
  get<PageResult<TrainingSession>>('/training/sessions/page', params)

export const createTrainingSession = (data: any) =>
  post<number>('/training/sessions', data)

export const updateTrainingSession = (id: number, data: any) =>
  put<boolean>(`/training/sessions/${id}`, data)

export const deleteTrainingSession = (id: number) =>
  del<boolean>(`/training/sessions/${id}`)

export const getTrainingEnrollmentPage = (params: any) =>
  get<PageResult<TrainingEnrollment>>('/training/enrollments/page', params)

export const createTrainingEnrollment = (data: any) =>
  post<number>('/training/enrollments', data)

export const updateTrainingEnrollment = (id: number, data: any) =>
  put<boolean>(`/training/enrollments/${id}`, data)

export const deleteTrainingEnrollment = (id: number) =>
  del<boolean>(`/training/enrollments/${id}`)

