import * as React from 'react'

import { useQuery } from '@tanstack/react-query'

import { listDict, type LabelValueResp } from '@/lib/api/common'
import { dictKeys } from '@/lib/query-keys'

export interface NoticeDictOption {
  readonly value: string
  readonly label: string
}

export interface NoticeDicts {
  readonly typeOptions: NoticeDictOption[]
  readonly scopeOptions: NoticeDictOption[]
  readonly methodOptions: NoticeDictOption[]
  readonly statusOptions: NoticeDictOption[]
}

const FALLBACK_SCOPE: NoticeDictOption[] = [
  { value: '1', label: '所有人' },
  { value: '2', label: '指定用户' }
]

const FALLBACK_METHODS: NoticeDictOption[] = [
  { value: '1', label: '系统消息' },
  { value: '2', label: '登录弹窗' }
]

const FALLBACK_STATUS: NoticeDictOption[] = [
  { value: '1', label: '草稿' },
  { value: '2', label: '待发布' },
  { value: '3', label: '已发布' }
]

function normalize(options: LabelValueResp[]): NoticeDictOption[] {
  return options.map(item => ({ value: String(item.value), label: item.label }))
}

function orFallback(options: NoticeDictOption[], fallback: NoticeDictOption[]) {
  return options.length > 0 ? options : fallback
}

/** The four dictionaries the Vue notice screens use: one user dict and three enum dicts. */
export function useNoticeDicts(): NoticeDicts {
  const typeQuery = useQuery({
    queryKey: dictKeys.code('notice_type'),
    queryFn: ({ signal }) => listDict('notice_type', signal)
  })

  const scopeQuery = useQuery({
    queryKey: dictKeys.code('notice_scope_enum'),
    queryFn: ({ signal }) => listDict('notice_scope_enum', signal)
  })

  const methodQuery = useQuery({
    queryKey: dictKeys.code('notice_method_enum'),
    queryFn: ({ signal }) => listDict('notice_method_enum', signal)
  })

  const statusQuery = useQuery({
    queryKey: dictKeys.code('notice_status_enum'),
    queryFn: ({ signal }) => listDict('notice_status_enum', signal)
  })

  // A stable object keeps the memoized table columns from rebuilding on every render.
  return React.useMemo(
    () => ({
      typeOptions: normalize(typeQuery.data ?? []),
      scopeOptions: orFallback(normalize(scopeQuery.data ?? []), FALLBACK_SCOPE),
      methodOptions: orFallback(normalize(methodQuery.data ?? []), FALLBACK_METHODS),
      statusOptions: orFallback(normalize(statusQuery.data ?? []), FALLBACK_STATUS)
    }),
    [typeQuery.data, scopeQuery.data, methodQuery.data, statusQuery.data]
  )
}
