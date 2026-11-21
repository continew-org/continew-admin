import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteClients, type ClientResp } from '@/lib/api/client'
import { clientKeys } from '@/lib/query-keys'

interface ClientDeleteDialogProps {
  readonly client: ClientResp | null
  readonly open: boolean
  readonly onClose: () => void
}

export function ClientDeleteDialog({ client, open, onClose }: ClientDeleteDialogProps) {
  // Keep the client named after the dialog starts closing and `client` is already null.
  const shownClient = useLastRecord(client)

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="确认删除客户端配置"
      description={`确认删除客户端「${shownClient?.clientId}」吗？删除后相关客户端将无法从此通道换取 Token 或调用系统接口。`}
      disabled={!client}
      action={async () => {
        if (client) await deleteClients([client.id])
      }}
      invalidateKeys={[clientKeys.all]}
      successMessage="删除客户端成功"
    />
  )
}
