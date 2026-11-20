import { Spinner } from '@/components/ui/spinner'

/** Full-screen pending state for the auth area, before the login card is ready. */
export function AuthPending() {
  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-background">
      <Spinner className="size-5" />
    </div>
  )
}
