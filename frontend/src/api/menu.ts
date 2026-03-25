import { get } from '@/utils/request'

export interface MenuItem {
  id: number
  parentId?: number
  menuName: string
  menuType: number
  path?: string
  component?: string
  permission?: string
  icon?: string
  sortOrder?: number
  visible?: number
  status?: number
  children?: MenuItem[]
}

export const getCurrentMenuTree = () => get<MenuItem[]>('/menus/current/tree')
