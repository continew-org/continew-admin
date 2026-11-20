import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { appKeys, deleteApp, type AppResp } from '@/lib/api/app'

import { appLabel } from './app-display'

interface AppDeleteDialogProps {
  /** The app being deleted, or `null` before one is selected. */
  readonly app: AppResp | null
  readonly open: boolean
  readonly onClose: () => void
}

export function AppDeleteDialog({ app, open, onClose }: AppDeleteDialogProps) {
  const shownApp = useLastRecord(app)
  const shownLabel = appLabel(shownApp)

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="删除应用"
      description={
        shownApp ? `是否确定删除应用「${shownLabel}」？删除后不可恢复` : '是否确定删除该应用？删除后不可恢复'
      }
      disabled={!app}
      action={async () => {
        if (app) await deleteApp(app.id)
      }}
      invalidateKeys={[appKeys.all]}
    />
  )
}
