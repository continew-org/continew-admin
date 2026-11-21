import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteDemo, demoKeys, type DemoResp } from '@/lib/api/demo'

interface DemoDeleteDialogProps {
  /** The demo being deleted, or `null` before the first one is picked. */
  readonly demo: DemoResp | null
  readonly open: boolean
  readonly onClose: () => void
}

export function DemoDeleteDialog({ demo, open, onClose }: DemoDeleteDialogProps) {
  const shownDemo = useLastRecord(demo)

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="删除示例"
      description={
        shownDemo ? `是否确定删除示例「${shownDemo.name}」？删除后不可恢复` : '是否确定删除该示例？删除后不可恢复'
      }
      disabled={!demo}
      action={async () => {
        if (demo) await deleteDemo(demo.id)
      }}
      invalidateKeys={[demoKeys.all]}
    />
  )
}
