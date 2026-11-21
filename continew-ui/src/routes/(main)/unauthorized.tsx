import { createFileRoute, Link } from '@tanstack/react-router'

import { Lock } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { StatusPage } from '@/routes/-components/status-page'

/** Shown when an API call is rejected with 403; the HTTP layer navigates here. */
export const Route = createFileRoute('/(main)/unauthorized')({
  component: UnauthorizedPage
})

function UnauthorizedPage() {
  return (
    <StatusPage
      icon={Lock}
      title="无访问权限"
      description="您没有权限访问该页面。如认为这是错误，请联系系统管理员。"
      actions={
        <Button nativeButton={false} render={<Link to="/dashboard" />}>
          返回首页
        </Button>
      }
    />
  )
}
