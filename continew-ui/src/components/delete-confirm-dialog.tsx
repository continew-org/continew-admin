import * as React from 'react'

import { useMutation, useQueryClient } from '@tanstack/react-query'
import { TriangleAlert } from 'lucide-react'

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogMedia,
  AlertDialogTitle
} from '@/components/ui/alert-dialog'
import { Spinner } from '@/components/ui/spinner'
import { toast } from '@/components/ui/toast'
import { reportError } from '@/lib/http'

interface DeleteConfirmDialogProps {
  readonly open: boolean
  readonly onClose: () => void
  readonly title: React.ReactNode
  readonly description: React.ReactNode
  /** Defaults to the trash icon; screens that warn harder pass a triangle alert. */
  readonly icon?: React.ReactNode
  readonly confirmLabel?: string
  readonly pendingLabel?: string
  /** The delete call; the mutation only starts when the confirm button is pressed. */
  readonly action: () => Promise<unknown>
  /** Query keys invalidated after a successful delete. */
  readonly invalidateKeys?: ReadonlyArray<readonly unknown[]>
  /** `null` skips the success toast. Defaults to 「删除成功」. */
  readonly successMessage?: string | null
  readonly errorMessage?: string
  /** Runs after the delete succeeds, before the dialog closes (such as a page callback). */
  readonly onSuccess?: () => void
  readonly disabled?: boolean
  /** Hides the confirm button, for a record that cannot be deleted but still shows the warning. */
  readonly showConfirm?: boolean
}

/**
 * The destructive confirmation shared by every delete screen: an alert dialog with a stale-safe
 * message, a pending state that blocks closing, and one place for the toast and cache invalidation.
 * Screens keep their own copy, icon, mutation call and query keys.
 */
export function DeleteConfirmDialog({
  open,
  onClose,
  title,
  description,
  icon,
  confirmLabel = '删除',
  pendingLabel = '删除中',
  action,
  invalidateKeys,
  successMessage = '删除成功',
  errorMessage = '删除失败',
  onSuccess,
  disabled = false,
  showConfirm = true
}: DeleteConfirmDialogProps) {
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: () => action(),
    onSuccess: () => {
      if (invalidateKeys) {
        for (const key of invalidateKeys) {
          void queryClient.invalidateQueries({ queryKey: key })
        }
      }
      if (successMessage) {
        toast.add({ title: successMessage, type: 'success' })
      }
      onSuccess?.()
      onClose()
    },
    onError: error => reportError(error, errorMessage)
  })

  return (
    <AlertDialog
      open={open}
      onOpenChange={nextOpen => {
        // Closing mid-delete would leave the request in flight with no way to report it.
        if (!nextOpen && !mutation.isPending) {
          onClose()
        }
      }}
    >
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogMedia className="bg-destructive/10 text-destructive">
            {icon ?? <TriangleAlert />}
          </AlertDialogMedia>
          <AlertDialogTitle>{title}</AlertDialogTitle>
          <AlertDialogDescription>{description}</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={mutation.isPending}>取消</AlertDialogCancel>
          {showConfirm ? (
            <AlertDialogAction
              variant="destructive"
              disabled={mutation.isPending || disabled}
              onClick={() => mutation.mutate()}
            >
              {mutation.isPending ? (
                <>
                  <Spinner />
                  {pendingLabel}
                </>
              ) : (
                confirmLabel
              )}
            </AlertDialogAction>
          ) : null}
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
