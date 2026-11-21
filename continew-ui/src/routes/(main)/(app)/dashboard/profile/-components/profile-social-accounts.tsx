import { useQuery } from '@tanstack/react-query'

import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { listSocialBindings, profileKeys } from '@/lib/api/profile'

export function ProfileSocialAccounts() {
  const query = useQuery({
    queryKey: profileKeys.social(),
    queryFn: ({ signal }) => listSocialBindings(signal)
  })

  return (
    <div className="flex flex-col gap-2">
      <h2 className="font-heading text-base font-medium">第三方登录信息</h2>
      {query.isPending ? (
        <Skeleton className="h-5 w-32" />
      ) : query.isError ? (
        <p className="text-sm text-muted-foreground">第三方账号加载失败</p>
      ) : !query.data?.length ? (
        <p className="text-sm text-muted-foreground">暂未绑定第三方账号</p>
      ) : (
        <div className="flex flex-wrap gap-2">
          {query.data.map(item => (
            <Badge key={item.source} className="rounded-sm" variant="secondary">
              {item.description || item.source}
            </Badge>
          ))}
        </div>
      )}
    </div>
  )
}
