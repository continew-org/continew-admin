import { createFileRoute, Navigate, notFound, type RegisteredRouter } from '@tanstack/react-router'

import { Construction } from 'lucide-react'

import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { ensureMenuLoaded } from '@/hooks/use-menu'
import { findMenuByPath, toPathname } from '@/navigation/menu'

export const Route = createFileRoute('/(main)/(app)/$')({
  loader: async ({ location }) => {
    const routes = await ensureMenuLoaded()
    const entry = findMenuByPath(routes, location.pathname)

    if (!entry) {
      throw notFound()
    }

    return { entry, pathname: location.pathname }
  },
  component: PlaceholderPage
})

function PlaceholderPage() {
  const { entry, pathname } = Route.useLoaderData()

  if (entry.redirect && toPathname(entry.redirect) !== pathname) {
    return <Navigate<RegisteredRouter, string, string> href={entry.redirect} replace />
  }

  const component = entry.component && entry.component !== 'Layout' ? entry.component : undefined
  const pagePath = component ? `src/routes/(main)/(app)/${component.replace(/\/index$/, '')}/route.tsx` : undefined

  return (
    <div className="flex h-full items-center justify-center">
      <Card className="w-full max-w-md">
        <CardHeader>
          <Construction className="size-8 text-muted-foreground" />
          <CardTitle>{entry.title}</CardTitle>
          <CardDescription>
            {pagePath ? `该菜单入口还没有对应页面。可在 ${pagePath} 添加页面。` : '该菜单入口还没有对应页面。'}
          </CardDescription>
        </CardHeader>
      </Card>
    </div>
  )
}
