import { createFileRoute, redirect } from '@tanstack/react-router'

export const Route = createFileRoute('/(external)/')({
  beforeLoad: () => redirect({ to: '/dashboard', replace: true })
})
