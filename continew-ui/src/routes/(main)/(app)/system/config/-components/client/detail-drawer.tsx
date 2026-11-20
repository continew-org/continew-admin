import { Smartphone } from 'lucide-react'

import { CopyableText } from '@/components/copyable-text'
import { DetailField, DetailGrid } from '@/components/detail-field'
import { EntityDrawer } from '@/components/entity-drawer'
import { StatusCell } from '@/components/status-cell'
import { Badge } from '@/components/ui/badge'
import { useLastRecord } from '@/hooks/use-last-record'
import type { ClientResp } from '@/lib/api/client'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

import { REFRESH_TOKEN_MODE_MAP } from './columns'

interface ClientDetailDrawerProps {
  readonly client: ClientResp | null
  readonly open: boolean
  readonly clientTypeMap: Record<string, string>
  readonly authTypeMap: Record<string, string>
  readonly replacedRangeMap: Record<string, string>
  readonly logoutModeMap: Record<string, string>
  readonly onClose: () => void
}

export function ClientDetailDrawer({
  client,
  open,
  clientTypeMap,
  authTypeMap,
  replacedRangeMap,
  logoutModeMap,
  onClose
}: ClientDetailDrawerProps) {
  // Keep the last client during the close animation, where `client` is already null.
  const detail = useLastRecord(client)

  return (
    // Keep the Drawer mounted while closed so the first open still plays its enter transition; guard the body instead.
    <EntityDrawer open={open} onClose={onClose} title="客户端详情" icon={<Smartphone className="size-4" />} width={32}>
      {detail ? (
        <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-4">
          <DetailGrid>
            <DetailField label="ID">{detail.id}</DetailField>
            <DetailField label="客户端 ID" className="sm:col-span-2">
              <CopyableText
                value={detail.clientId}
                label="复制客户端 ID"
                successMessage="已复制客户端 ID"
                className="break-all"
              />
            </DetailField>
            <DetailField label="客户端类型" className="sm:col-span-2">
              <Badge variant="outline">{clientTypeMap[detail.clientType] || detail.clientType}</Badge>
            </DetailField>
            <DetailField label="认证类型" className="sm:col-span-2">
              <div className="flex items-center gap-1 overflow-x-auto whitespace-nowrap">
                {detail.authType?.map(authType => (
                  <Badge key={authType} variant="secondary" className="shrink-0 font-normal whitespace-nowrap">
                    {authTypeMap[authType] || authType}
                  </Badge>
                ))}
              </div>
            </DetailField>
            <DetailField label="Token 最低活跃频率">
              <span>{detail.activeTimeout === -1 ? '不限制' : `${detail.activeTimeout} 秒`}</span>
            </DetailField>
            <DetailField label="Token 有效期">
              <span>{detail.timeout === -1 ? '永不过期' : `${detail.timeout} 秒`}</span>
            </DetailField>
            <DetailField label="Refresh Token 有效期">
              <span>{detail.refreshTokenTimeout ? `${detail.refreshTokenTimeout} 秒` : EMPTY_PLACEHOLDER}</span>
            </DetailField>
            <DetailField label="Refresh Token 传输模式">
              <span>
                {detail.refreshTokenMode
                  ? REFRESH_TOKEN_MODE_MAP[detail.refreshTokenMode] || detail.refreshTokenMode
                  : EMPTY_PLACEHOLDER}
              </span>
            </DetailField>
            <DetailField label="状态" className="sm:col-span-2">
              <StatusCell status={detail.status} />
            </DetailField>
            <DetailField label="多地登录">
              <Badge variant={detail.isConcurrent ? 'default' : 'secondary'} className="font-normal">
                {detail.isConcurrent ? '允许' : '互斥'}
              </Badge>
            </DetailField>
            <DetailField label="下线范围">
              <span>
                {detail.replacedRange
                  ? replacedRangeMap[detail.replacedRange] || detail.replacedRange
                  : EMPTY_PLACEHOLDER}
              </span>
            </DetailField>
            <DetailField label="登录数量">
              <span>
                {detail.maxLoginCount === -1
                  ? '不限制'
                  : detail.maxLoginCount !== undefined && detail.maxLoginCount !== null
                    ? `${detail.maxLoginCount} 个`
                    : EMPTY_PLACEHOLDER}
              </span>
            </DetailField>
            <DetailField label="溢出处理">
              <span>
                {detail.overflowLogoutMode
                  ? logoutModeMap[detail.overflowLogoutMode] || detail.overflowLogoutMode
                  : EMPTY_PLACEHOLDER}
              </span>
            </DetailField>
            <DetailField label="创建人">{detail.createUserString || EMPTY_PLACEHOLDER}</DetailField>
            <DetailField label="创建时间">{detail.createTime || EMPTY_PLACEHOLDER}</DetailField>
            <DetailField label="修改人">{detail.updateUserString || EMPTY_PLACEHOLDER}</DetailField>
            <DetailField label="修改时间">{detail.updateTime || EMPTY_PLACEHOLDER}</DetailField>
          </DetailGrid>
        </div>
      ) : null}
    </EntityDrawer>
  )
}
