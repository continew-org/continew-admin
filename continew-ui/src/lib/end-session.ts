import type { QueryClient } from '@tanstack/react-query'
import type { useNavigate } from '@tanstack/react-router'

type Navigate = ReturnType<typeof useNavigate>

interface EndSessionOptions {
  clearAuthSession: () => void
  queryClient: QueryClient
  resetTabs: () => void
  navigate: Navigate
  redirect?: string
}

export async function endSession({ clearAuthSession, queryClient, resetTabs, navigate, redirect }: EndSessionOptions) {
  await queryClient.cancelQueries().catch(() => undefined)
  clearAuthSession()
  resetTabs()

  try {
    if (redirect) {
      await navigate({ to: '/auth/login', search: { redirect }, replace: true })
    } else {
      await navigate({ to: '/auth/login', replace: true })
    }
  } finally {
    queryClient.clear()
  }
}
