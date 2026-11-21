import { Link } from '@tanstack/react-router'

import { FileQuestionMark } from 'lucide-react'

import { Button } from '@/components/ui/button'

import { StatusPage } from './status-page'

export function NotFound() {
  return (
    <StatusPage
      icon={FileQuestionMark}
      title="页面不存在"
      description="您访问的页面不存在或已被移动。"
      actions={
        <Button nativeButton={false} render={<Link to="/dashboard" replace />}>
          返回首页
        </Button>
      }
    />
  )
}
