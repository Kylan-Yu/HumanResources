import { useEffect, useState } from 'react'
import { getWorkflowRoleList, getWorkflowUserPage } from '@/api/leaveWorkflow'
import { get } from '@/utils/request'

interface OptionItem {
  label: string
  value: string
}

interface ActorOptionState {
  roleOptions: OptionItem[]
  userOptions: OptionItem[]
  positionOptions: OptionItem[]
  deptOptions: OptionItem[]
  loading: boolean
}

let cache: Omit<ActorOptionState, 'loading'> | null = null

export const useWorkflowActorOptions = (): ActorOptionState => {
  const [state, setState] = useState<ActorOptionState>(() => ({
    roleOptions: cache?.roleOptions || [],
    userOptions: cache?.userOptions || [],
    positionOptions: cache?.positionOptions || [],
    deptOptions: cache?.deptOptions || [],
    loading: !cache
  }))

  useEffect(() => {
    if (cache) {
      return
    }
    let active = true

    const load = async () => {
      try {
        const [rolesRes, usersRes, positionsRes, deptsRes] = await Promise.all([
          getWorkflowRoleList(),
          getWorkflowUserPage({ pageNum: 1, pageSize: 200 }),
          get<any[]>('/position/list'),
          get<any[]>('/dept/list')
        ])

        const roleOptions = Array.isArray(rolesRes.data)
          ? rolesRes.data
              .filter((item) => item?.roleCode)
              .map((item) => ({
                label: item.roleName || item.roleCode,
                value: String(item.roleCode)
              }))
          : []

        const users = Array.isArray(usersRes.data?.list) ? usersRes.data.list : []
        const userOptions = users
          .filter((item) => item?.id)
          .map((item) => ({
            label: item.realName || item.username || String(item.id),
            value: String(item.id)
          }))

        const positionOptions = Array.isArray(positionsRes.data)
          ? positionsRes.data
              .filter((item) => item?.id)
              .map((item) => ({
                label: item.positionName || item.positionCode || String(item.id),
                value: String(item.id)
              }))
          : []

        const deptOptions = Array.isArray(deptsRes.data)
          ? deptsRes.data
              .filter((item) => item?.id)
              .map((item) => ({
                label: item.deptName || item.deptCode || String(item.id),
                value: String(item.id)
              }))
          : []

        cache = { roleOptions, userOptions, positionOptions, deptOptions }
        if (active) {
          setState({
            roleOptions,
            userOptions,
            positionOptions,
            deptOptions,
            loading: false
          })
        }
      } catch (_error) {
        if (active) {
          setState((prev) => ({ ...prev, loading: false }))
        }
      }
    }

    void load()

    return () => {
      active = false
    }
  }, [])

  return state
}
