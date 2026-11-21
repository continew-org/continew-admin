import * as React from 'react'

import { useQuery, useQueryClient } from '@tanstack/react-query'

import { KeyRound } from 'lucide-react'

import { type CheckableTreeNode, CheckableTree } from '@/components/checkable-tree'
import { EntityDrawer } from '@/components/entity-drawer'
import { QueryState } from '@/components/query-state'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { DrawerFooter } from '@/components/ui/drawer'
import { Field, FieldContent, FieldDescription, FieldLabel } from '@/components/ui/field'
import { Skeleton } from '@/components/ui/skeleton'
import { Spinner } from '@/components/ui/spinner'
import { Switch } from '@/components/ui/switch'
import { toast } from '@/components/ui/toast'
import { useFillFormOnce } from '@/hooks/use-fill-form-once'
import { useLastRecord } from '@/hooks/use-last-record'
import {
  listRolePermissionTree,
  roleKeys,
  type RolePermissionResp,
  type RoleResp,
  updateRolePermission
} from '@/lib/api/role'

import { useRoleDetail } from './use-role-detail'

interface RolePermissionDrawerProps {
  readonly role: RoleResp | null
  readonly open: boolean
  readonly onClose: () => void
}

const PERMISSION_TYPE_LABELS: Readonly<Record<number, string | undefined>> = {
  1: '目录',
  2: '菜单',
  3: '按钮'
}

function toCheckablePermissionNodes(nodes: RolePermissionResp[]): CheckableTreeNode[] {
  return nodes.map(node => {
    const typeLabel = PERMISSION_TYPE_LABELS[Number(node.type)]

    return {
      id: String(node.id),
      parentId: node.parentId ? String(node.parentId) : null,
      label: node.title,
      searchTerms: node.permission ?? '',
      tag: typeLabel ? <Badge variant="secondary">{typeLabel}</Badge> : null,
      subtitle: node.permission ? <Badge variant="secondary">{node.permission}</Badge> : null,
      children: node.children?.length ? toCheckablePermissionNodes(node.children) : undefined
    }
  })
}

export function RolePermissionDrawer({ role, open, onClose }: RolePermissionDrawerProps) {
  const queryClient = useQueryClient()
  const shownRole = useLastRecord(role)
  const roleId = shownRole?.id ?? null

  const detail = useRoleDetail(roleId, { enabled: open })

  const permTree = useQuery({
    queryKey: roleKeys.permissions(),
    queryFn: ({ signal }) => listRolePermissionTree(signal),
    enabled: open
  })

  const [selectedMenuIds, setSelectedMenuIds] = React.useState<Array<string | number>>([])
  const [menuCheckStrictly, setMenuCheckStrictly] = React.useState(false)
  const [isSubmitting, setIsSubmitting] = React.useState(false)

  useFillFormOnce({
    open,
    isEdit: true,
    entityKey: roleId,
    record: detail.data,
    version: detail.dataUpdatedAt,
    buildValues: ({ record }) =>
      record
        ? { menuIds: record.menuIds.map(String), menuCheckStrictly: record.menuCheckStrictly }
        : { menuIds: [] as string[], menuCheckStrictly: false },
    apply: values => {
      setSelectedMenuIds(values.menuIds)
      setMenuCheckStrictly(values.menuCheckStrictly)
    },
    resetOnClose: false
  })

  const treeNodes = React.useMemo(() => toCheckablePermissionNodes(permTree.data ?? []), [permTree.data])

  const handleSave = async () => {
    if (roleId === null) return

    setIsSubmitting(true)
    try {
      await updateRolePermission(roleId, {
        roleId,
        menuIds: selectedMenuIds,
        menuCheckStrictly
      })

      toast.add({ title: '修改功能权限成功', type: 'success' })
      onClose()
      void queryClient.invalidateQueries({ queryKey: roleKeys.all })
    } catch {
      // The HTTP layer already reported the failure.
    } finally {
      setIsSubmitting(false)
    }
  }

  const isLoading = detail.isPending || detail.fetchStatus !== 'idle' || permTree.isPending
  const isLoadError = !isLoading && (detail.isError || permTree.isError)

  return (
    <EntityDrawer
      open={open}
      onClose={onClose}
      title="分配功能权限"
      icon={<KeyRound className="size-4" />}
      badge={shownRole ? <Badge variant="secondary">{shownRole.name}</Badge> : undefined}
      width={36}
      closeDisabled={isSubmitting}
    >
      {isLoading ? (
        <div className="flex flex-col gap-4 p-4">
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-64 w-full" />
        </div>
      ) : isLoadError ? (
        <QueryState
          state="error"
          className="p-6"
          onRetry={() => {
            void detail.refetch()
            void permTree.refetch()
          }}
        />
      ) : (
        <div className="flex min-h-0 flex-1 flex-col">
          <div className="flex min-h-0 flex-1 flex-col gap-3 p-4">
            <Field orientation="horizontal">
              <FieldContent>
                <FieldLabel>父子联动</FieldLabel>
                <FieldDescription>
                  {menuCheckStrictly ? '已开启独立选择' : '勾选上级菜单时自动全选子菜单与操作按钮'}
                </FieldDescription>
              </FieldContent>
              <Switch checked={!menuCheckStrictly} onCheckedChange={checked => setMenuCheckStrictly(!checked)} />
            </Field>

            <CheckableTree
              data={treeNodes}
              checkedKeys={selectedMenuIds}
              onCheckedChange={setSelectedMenuIds}
              checkStrictly={menuCheckStrictly}
              searchPlaceholder="搜索菜单或权限名"
              fillHeight
            />
          </div>

          <DrawerFooter>
            <Button type="button" variant="outline" disabled={isSubmitting} onClick={onClose}>
              取消
            </Button>
            <Button type="button" disabled={isSubmitting} onClick={handleSave}>
              {isSubmitting ? (
                <>
                  <Spinner />
                  保存中
                </>
              ) : (
                '保存'
              )}
            </Button>
          </DrawerFooter>
        </div>
      )}
    </EntityDrawer>
  )
}
