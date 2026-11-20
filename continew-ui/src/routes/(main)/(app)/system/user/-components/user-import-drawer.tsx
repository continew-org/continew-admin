import * as React from 'react'

import { useQueryClient } from '@tanstack/react-query'

import { Download, FileSpreadsheet, RotateCcw, Upload } from 'lucide-react'

import { cn } from 'cn'

import { Button } from '@/components/ui/button'
import { DrawerFooter } from '@/components/ui/drawer'
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Spinner } from '@/components/ui/spinner'
import { toast } from '@/components/ui/toast'
import { EntityDrawer } from '@/components/entity-drawer'
import {
  downloadUserImportTemplate,
  importUsers,
  parseUserImport,
  userKeys,
  type UserImportParseResp,
  type UserImportPolicy,
  type UserStatus
} from '@/lib/api/user'
import { reportError } from '@/lib/http'

const POLICY_OPTIONS: { value: UserImportPolicy; label: string }[] = [
  { value: 1, label: '跳过该行' },
  { value: 2, label: '修改数据' },
  { value: 3, label: '停止导入' }
]

const STATUS_OPTIONS: { value: UserStatus; label: string }[] = [
  { value: 1, label: '启用' },
  { value: 2, label: '禁用' }
]

// The selects read the label from `items`; without it the trigger shows the raw numeric value.
const POLICY_ITEMS = POLICY_OPTIONS.map(option => ({ value: String(option.value), label: option.label }))
const STATUS_ITEMS = STATUS_OPTIONS.map(option => ({ value: String(option.value), label: option.label }))

/** The backend rejects an expired `importKey` with a "请重新上传" message. */
function isImportExpired(error: unknown) {
  return error instanceof Error && (error.message.includes('过期') || error.message.includes('重新上传'))
}

function Stat({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-md border px-2 py-1.5">
      <div className="text-xs text-muted-foreground">{label}</div>
      <div className="text-base font-semibold tabular-nums">{value}</div>
    </div>
  )
}

function PolicySelect({
  label,
  value,
  onChange,
  disabled
}: {
  label: string
  value: UserImportPolicy
  onChange: (value: UserImportPolicy) => void
  disabled: boolean
}) {
  return (
    <div className="flex items-center justify-between gap-3">
      <span className="text-sm text-muted-foreground">{label}</span>
      <Select
        items={POLICY_ITEMS}
        value={String(value)}
        onValueChange={next => onChange(Number(next) as UserImportPolicy)}
      >
        <SelectTrigger size="sm" className="w-32" disabled={disabled}>
          <SelectValue />
        </SelectTrigger>
        <SelectContent side="top">
          <SelectGroup>
            {POLICY_OPTIONS.map(option => (
              <SelectItem key={option.value} value={String(option.value)}>
                {option.label}
              </SelectItem>
            ))}
          </SelectGroup>
        </SelectContent>
      </Select>
    </div>
  )
}

interface UserImportDrawerProps {
  readonly open: boolean
  readonly onClose: () => void
}

/** User import: download the template, upload to parse (drag or click), then commit with policies. */
export function UserImportDrawer({ open, onClose }: UserImportDrawerProps) {
  const queryClient = useQueryClient()
  const fileInputRef = React.useRef<HTMLInputElement>(null)

  const [file, setFile] = React.useState<File | null>(null)
  const [isDragging, setIsDragging] = React.useState(false)
  const [parsing, setParsing] = React.useState(false)
  const [parseResult, setParseResult] = React.useState<UserImportParseResp | null>(null)
  const [duplicateUser, setDuplicateUser] = React.useState<UserImportPolicy>(1)
  const [duplicateEmail, setDuplicateEmail] = React.useState<UserImportPolicy>(1)
  const [duplicatePhone, setDuplicatePhone] = React.useState<UserImportPolicy>(1)
  const [defaultStatus, setDefaultStatus] = React.useState<UserStatus>(1)
  const [importing, setImporting] = React.useState(false)

  // Reset the wizard every time the drawer opens so a previous import never leaks in.
  React.useEffect(() => {
    if (!open) return
    setFile(null)
    setIsDragging(false)
    setParsing(false)
    setParseResult(null)
    setDuplicateUser(1)
    setDuplicateEmail(1)
    setDuplicatePhone(1)
    setDefaultStatus(1)
    setImporting(false)
  }, [open])

  const resetUpload = () => {
    setFile(null)
    setParseResult(null)
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  const handleDownloadTemplate = async () => {
    try {
      await downloadUserImportTemplate()
    } catch (error) {
      reportError(error, '模板下载失败')
    }
  }

  const handleFile = async (nextFile: File) => {
    const name = nextFile.name.toLowerCase()
    if (!name.endsWith('.xlsx') && !name.endsWith('.xls')) {
      toast.add({ title: '请选择 Excel 文件（.xlsx / .xls）', type: 'error' })
      return
    }

    setFile(nextFile)
    setParseResult(null)
    setParsing(true)
    try {
      const parsed = await parseUserImport(nextFile)
      setParseResult(parsed)
    } catch {
      // The HTTP layer already reports the failure; let the user pick another file.
      setFile(null)
      if (fileInputRef.current) fileInputRef.current.value = ''
    } finally {
      setParsing(false)
    }
  }

  const handleImport = async () => {
    if (!parseResult) return
    setImporting(true)
    try {
      const imported = await importUsers({
        importKey: parseResult.importKey,
        duplicateUser,
        duplicateEmail,
        duplicatePhone,
        defaultStatus
      })
      toast.add({
        title: `导入完成：新增 ${imported.insertRows} 条，修改 ${imported.updateRows} 条`,
        type: 'success'
      })
      await queryClient.invalidateQueries({ queryKey: userKeys.lists() })
      onClose()
    } catch (error) {
      reportError(error, '导入失败，请重试')
      // An expired import session cannot be retried with the same key, so send the user back
      // to the upload step instead of leaving an invalid "开始导入" button.
      if (isImportExpired(error)) {
        resetUpload()
      }
    } finally {
      setImporting(false)
    }
  }

  const busy = parsing || importing

  return (
    <EntityDrawer
      open={open}
      onClose={onClose}
      title="导入用户"
      icon={<Upload className="size-4" />}
      width={36}
      closeDisabled={busy}
      canClose={() => !busy}
    >
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-4">
        <div className="flex flex-col gap-4">
          <div className="flex flex-col gap-3 rounded-lg border bg-muted/30 p-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex flex-col gap-1">
              <span className="text-sm font-medium">下载导入模板</span>
              <span className="text-xs text-muted-foreground">
                请按照模板要求填写数据，填写完毕后，请先上传并进行解析。
              </span>
            </div>
            <Button
              type="button"
              variant="outline"
              className="shrink-0"
              onClick={() => void handleDownloadTemplate()}
            >
              <Download />
              下载模板
            </Button>
          </div>

          <input
            ref={fileInputRef}
            type="file"
            accept=".xlsx,.xls"
            className="hidden"
            onChange={event => {
              const nextFile = event.target.files?.[0]
              if (nextFile) void handleFile(nextFile)
              event.target.value = ''
            }}
          />
          <div className="relative">
            <button
              type="button"
              disabled={busy}
              aria-label="上传并解析 Excel 文件"
              onClick={() => fileInputRef.current?.click()}
              onDragEnter={event => {
                event.preventDefault()
                if (!busy) setIsDragging(true)
              }}
              onDragOver={event => {
                event.preventDefault()
                if (!busy) setIsDragging(true)
              }}
              onDragLeave={event => {
                event.preventDefault()
                setIsDragging(false)
              }}
              onDrop={event => {
                event.preventDefault()
                setIsDragging(false)
                if (busy) return
                const dropped = event.dataTransfer.files?.[0]
                if (dropped) void handleFile(dropped)
              }}
              className={cn(
                'flex min-h-[280px] w-full flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed px-6 py-10 text-center transition-colors',
                isDragging ? 'border-primary bg-primary/5' : 'border-input hover:border-primary/60 hover:bg-muted/40',
                busy && 'cursor-not-allowed opacity-60'
              )}
            >
              {parsing ? (
                <Spinner className="size-7 text-muted-foreground" />
              ) : (
                <FileSpreadsheet className="size-8 text-muted-foreground" />
              )}
              <span className="text-sm font-medium">{parsing ? '解析中' : '拖拽文件到此处，或点击上传'}</span>
              <span className="text-xs text-muted-foreground">仅支持 .xlsx / .xls</span>
              {file ? (
                <span className="max-w-full truncate text-xs text-muted-foreground">已选择：{file.name}</span>
              ) : null}
            </button>

            {parseResult ? (
              <div className="absolute inset-0 flex flex-col justify-center gap-3 overflow-auto rounded-xl border-2 border-primary/40 bg-background/60 p-4 backdrop-blur-sm">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-sm font-medium">解析结果</span>
                  {file ? (
                    <span className="max-w-full truncate text-xs text-muted-foreground">{file.name}</span>
                  ) : null}
                </div>
                <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
                  <Stat label="总计" value={parseResult.totalRows} />
                  <Stat label="有效" value={parseResult.validRows} />
                  <Stat label="重复用户" value={parseResult.duplicateUserRows} />
                  <Stat label="重复邮箱" value={parseResult.duplicateEmailRows} />
                  <Stat label="重复手机" value={parseResult.duplicatePhoneRows} />
                </div>
              </div>
            ) : null}
          </div>

          {parseResult ? (
            <>
              <div className="flex flex-col gap-3 rounded-lg border p-4">
                <span className="text-sm font-medium">确认导入</span>
                <PolicySelect label="重复用户" value={duplicateUser} onChange={setDuplicateUser} disabled={importing} />
                <PolicySelect
                  label="重复邮箱"
                  value={duplicateEmail}
                  onChange={setDuplicateEmail}
                  disabled={importing}
                />
                <PolicySelect
                  label="重复手机"
                  value={duplicatePhone}
                  onChange={setDuplicatePhone}
                  disabled={importing}
                />
                <div className="flex items-center justify-between gap-3">
                  <span className="text-sm text-muted-foreground">默认状态</span>
                  <Select
                    items={STATUS_ITEMS}
                    value={String(defaultStatus)}
                    onValueChange={next => setDefaultStatus(Number(next) as UserStatus)}
                  >
                    <SelectTrigger size="sm" className="w-32" disabled={importing}>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent side="top">
                      <SelectGroup>
                        {STATUS_OPTIONS.map(option => (
                          <SelectItem key={option.value} value={String(option.value)}>
                            {option.label}
                          </SelectItem>
                        ))}
                      </SelectGroup>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </>
          ) : null}
        </div>
      </div>

      <DrawerFooter>
        <Button type="button" variant="outline" disabled={busy} onClick={onClose}>
          取消
        </Button>
        {parseResult ? (
          <Button type="button" variant="outline" disabled={busy} onClick={resetUpload}>
            <RotateCcw />
            重新上传
          </Button>
        ) : null}
        <Button
          type="button"
          disabled={!parseResult || parseResult.validRows === 0 || busy}
          onClick={() => void handleImport()}
        >
          {importing ? (
            <>
              <Spinner />
              导入中
            </>
          ) : (
            '开始导入'
          )}
        </Button>
      </DrawerFooter>
    </EntityDrawer>
  )
}
