import { type ErrorComponentProps, Link } from '@tanstack/react-router'

import { TriangleAlert } from 'lucide-react'

import { Button } from '@/components/ui/button'

import { StatusPage } from './status-page'

export function RootError({ error, reset }: ErrorComponentProps) {
  return (
    <StatusPage
      icon={TriangleAlert}
      iconClassName="text-destructive"
      title="页面出错了"
      description="加载页面时发生意外错误，请重试或返回首页。"
      actions={
        <>
          <Button onClick={reset}>重试</Button>
          <Button nativeButton={false} variant="outline" render={<Link to="/dashboard" replace />}>
            返回首页
          </Button>
        </>
      }
    >
      {import.meta.env.DEV ? (
        <p className="text-sm wrap-break-word text-destructive">
          {error instanceof Error ? error.message : String(error)}
        </p>
      ) : null}
    </StatusPage>
  )
}
