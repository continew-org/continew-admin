import type { ColumnDef } from '@tanstack/react-table'

import { TextCell } from '@/components/text-cell'
import { Button } from '@/components/ui/button'
import type { LogResp } from '@/lib/api/log'
import type { DataTableFeatures } from '@/lib/data-table-features'

import { LogStatusCell, TimeTakenCell } from './log-display'

type LogColumn = ColumnDef<DataTableFeatures, LogResp>
type TextColumnKey = 'createTime' | 'createUserString' | 'description' | 'ip' | 'address' | 'browser' | 'os'

/** Labels for the columns whose header is a node, which the hide-columns menu cannot read. */
export const LOG_COLUMN_LABELS: Record<string, string> = {
  status: '状态',
  module: '所属模块',
  timeTaken: '耗时'
}

interface OperationColumnOptions {
  /** The index of the first row on the current page, so 序号 stays continuous. */
  readonly rowOffset: number
  readonly canViewDetail: boolean
  readonly onDetail: (log: LogResp) => void
}

function textColumn(key: TextColumnKey, header: string): LogColumn {
  return {
    accessorKey: key,
    header,
    cell: ({ row }) => <TextCell value={row.original[key]} />
  }
}

function indexColumn(rowOffset: number): LogColumn {
  return {
    id: 'index',
    header: '序号',
    enableHiding: false,
    cell: ({ row }) => <div>{rowOffset + row.index + 1}</div>
  }
}

function statusColumn(): LogColumn {
  return {
    id: 'status',
    header: () => <div>状态</div>,
    cell: ({ row }) => <LogStatusCell status={row.original.status} errorMsg={row.original.errorMsg} />
  }
}

export function createLoginLogColumns(rowOffset: number): LogColumn[] {
  return [
    indexColumn(rowOffset),
    textColumn('createTime', '登录时间'),
    textColumn('createUserString', '用户昵称'),
    textColumn('description', '登录行为'),
    statusColumn(),
    textColumn('ip', '登录 IP'),
    textColumn('address', '登录地点'),
    textColumn('browser', '浏览器'),
    textColumn('os', '终端系统')
  ]
}

export function createOperationLogColumns({ rowOffset, canViewDetail, onDetail }: OperationColumnOptions): LogColumn[] {
  return [
    indexColumn(rowOffset),
    {
      accessorKey: 'createTime',
      header: '操作时间',
      cell: ({ row }) =>
        canViewDetail ? (
          <Button
            variant="link"
            className="h-auto border-0 p-0 text-sm font-normal"
            onClick={() => onDetail(row.original)}
          >
            {row.original.createTime}
          </Button>
        ) : (
          <TextCell value={row.original.createTime} />
        )
    },
    textColumn('createUserString', '操作人'),
    textColumn('description', '操作内容'),
    {
      accessorKey: 'module',
      header: () => <div>所属模块</div>,
      cell: ({ row }) => (
        <div>
          <TextCell value={row.original.module} />
        </div>
      )
    },
    statusColumn(),
    textColumn('ip', '操作 IP'),
    textColumn('address', '操作地点'),
    {
      accessorKey: 'timeTaken',
      header: () => <div>耗时</div>,
      cell: ({ row }) => <TimeTakenCell timeTaken={row.original.timeTaken} />
    },
    textColumn('browser', '浏览器'),
    textColumn('os', '终端系统')
  ]
}
