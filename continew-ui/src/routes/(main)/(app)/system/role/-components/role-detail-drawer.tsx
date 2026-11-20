import { useQuery } from '@tanstack/react-query'

import { UserShield } from 'lucide-react'

import { DetailField, DetailGrid } from '@/components/detail-field'
import { DetailGate } from '@/components/detail-gate'
import { EntityDrawer } from '@/components/entity-drawer'
import { Skeleton } from '@/components/ui/skeleton'
import { useLastRecord } from '@/hooks/use-last-record'
import { deptKeys, getDeptTree, type DeptResp } from '@/lib/api/dept'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

import { DataScopeBadge } from './role-columns'
import { useRoleDetail } from './use-role-detail'

const EMPTY_DEPTS: DeptResp[] = []

/** Flattens the department tree into an id -> name map so custom-scope ids render readable names. */
function collectDeptNames(nodes: DeptResp[], acc: Map<string, string>) {
  for (const node of nodes) {
    acc.set(String(node.id), node.name)
    if (node.children?.length) {
      collectDeptNames(node.children, acc)
    }
  }
  return acc
}

function RoleDetailSkeleton() {
  return (
    <DetailGrid>
      {Array.from({ length: 11 }, (_, index) => (
        <div key={index} className="flex flex-col gap-1">
          <Skeleton className="h-3.5 w-14" />
          <Skeleton className="h-5 w-28" />
        </div>
      ))}
    </DetailGrid>
  )
}

interface RoleDetailDrawerProps {
  /** The role to show, or `null` before the first one is picked. */
  readonly roleId: string | number | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Read-only detail of one role, showing the same fields as the create/edit form. */
export function RoleDetailDrawer({ roleId, open, onClose }: RoleDetailDrawerProps) {
  const query = useRoleDetail(roleId, { enabled: open })
  // Keep the last detail during the close animation; the query key becomes null first.
  const lastDetail = useLastRecord(query.data ?? null)
  const detail = query.data ?? (open ? null : lastDetail)

  // A custom data scope shows department names, so the tree is only needed for that scope.
  const depts = useQuery({
    queryKey: deptKeys.trees(),
    queryFn: ({ signal }) => getDeptTree(undefined, signal),
    enabled: open && query.data?.dataScope === 5
  })

  return (
    <EntityDrawer open={open} onClose={onClose} title="角色详情" icon={<UserShield className="size-4" />} width={32}>
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-4">
        <DetailGate
          query={query}
          data={detail}
          requireFresh={false}
          errorTitle="角色加载失败"
          skeleton={<RoleDetailSkeleton />}
        >
          {record => {
            const deptNameMap = collectDeptNames(depts.data ?? EMPTY_DEPTS, new Map<string, string>())
            const scopeDeptNames = (record.deptIds ?? []).map(id => deptNameMap.get(String(id)) ?? String(id))

            return (
              <DetailGrid>
                <DetailField label="ID" className="sm:col-span-2">
                  {record.id}
                </DetailField>
                <DetailField label="角色名称">{record.name}</DetailField>
                <DetailField label="角色编码">{record.code || EMPTY_PLACEHOLDER}</DetailField>
                <DetailField label="排序">{record.sort}</DetailField>
                <DetailField label="数据权限">
                  <DataScopeBadge scope={record.dataScope} />
                </DetailField>
                {record.dataScope === 5 ? (
                  <>
                    <DetailField label="权限部门" className="sm:col-span-2">
                      {scopeDeptNames.length ? scopeDeptNames.join('、') : EMPTY_PLACEHOLDER}
                    </DetailField>
                    <DetailField label="父子联动">
                      {record.deptCheckStrictly ? '独立选择（不联动）' : '勾选父部门时自动勾选全部子部门'}
                    </DetailField>
                  </>
                ) : null}
                <DetailField label="描述" className="sm:col-span-2">
                  {record.description || EMPTY_PLACEHOLDER}
                </DetailField>
                <DetailField label="创建人">{record.createUserString || EMPTY_PLACEHOLDER}</DetailField>
                <DetailField label="修改人">{record.updateUserString || EMPTY_PLACEHOLDER}</DetailField>
                <DetailField label="创建时间">{record.createTime || EMPTY_PLACEHOLDER}</DetailField>
                <DetailField label="修改时间">{record.updateTime || EMPTY_PLACEHOLDER}</DetailField>
              </DetailGrid>
            )
          }}
        </DetailGate>
      </div>
    </EntityDrawer>
  )
}
