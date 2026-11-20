import { useQuery } from '@tanstack/react-query'
import { format } from 'date-fns'
import { CircleCheck } from 'lucide-react'

import { Skeleton } from '@/components/ui/skeleton'
import { usePermission } from '@/hooks/use-permission'
import { listLogs, logKeys } from '@/lib/api/log'
import { useAuthStore } from '@/stores/auth/auth-provider'

function formatDateTime(value?: string | null) {
  if (!value) {
    return '—'
  }

  const date = new Date(value.replace(' ', 'T'))
  return Number.isNaN(date.getTime()) ? value : format(date, 'yyyy-MM-dd HH:mm')
}

export function ProfileStatusSidebar() {
  const user = useAuthStore(state => state.user)
  const { hasPermission } = usePermission()
  const canViewLoginLog = hasPermission('monitor:log:list')

  const query = useQuery({
    queryKey: logKeys.list({
      page: 1,
      size: 1,
      module: '登录',
      createUserString: user?.username,
      sort: 't1.create_time,desc'
    }),
    queryFn: ({ signal }) =>
      listLogs(
        {
          page: 1,
          size: 1,
          module: '登录',
          createUserString: user?.username,
          sort: 't1.create_time,desc'
        },
        signal
      ),
    enabled: canViewLoginLog && Boolean(user?.username),
    staleTime: 60_000
  })

  const lastLoginTime = query.data?.list[0]?.createTime

  return (
    <aside>
      <h2 className="font-heading text-sm font-medium">账户记录</h2>
      <div className="mt-4 flex items-start gap-2">
        <CircleCheck aria-hidden="true" className="mt-0.5 size-4 text-muted-foreground" />
        <div className="flex flex-col gap-1">
          <p className="text-sm font-medium">账号状态正常</p>
          {query.isPending && canViewLoginLog ? (
            <Skeleton className="h-4 w-36" />
          ) : (
            <p className="text-xs text-muted-foreground">
              最后登录时间：{canViewLoginLog ? formatDateTime(lastLoginTime) : '—'}
            </p>
          )}
        </div>
      </div>
    </aside>
  )
}
