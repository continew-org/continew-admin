import { revalidateLogic, useForm } from '@tanstack/react-form'
import { useQueryClient } from '@tanstack/react-query'

import { Boxes } from 'lucide-react'

import { z } from 'zod'

import { EntityDrawer } from '@/components/entity-drawer'
import { QueryState } from '@/components/query-state'
import { RequiredMark } from '@/components/required-mark'
import { Button } from '@/components/ui/button'
import { DrawerFooter } from '@/components/ui/drawer'
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Spinner } from '@/components/ui/spinner'
import { Textarea } from '@/components/ui/textarea'
import { toast } from '@/components/ui/toast'
import { useDetailQuery } from '@/hooks/use-detail-query'
import { useFillFormOnce } from '@/hooks/use-fill-form-once'
import { createDemo, demoKeys, getDemo, updateDemo, type DemoDetailResp, type DemoReq } from '@/lib/api/demo'

function normalizeOnlineTime(value: string) {
  // datetime-local stops at minute precision, while the API expects yyyy-MM-dd HH:mm:ss
  const withSeconds = value.length === 16 ? `${value}:00` : value
  return withSeconds.replace('T', ' ')
}

function toDatetimeLocal(value: string | null) {
  return value ? value.replace(' ', 'T') : null
}

const formSchema = z
  .object({
    name: z.string().trim().min(1, '请输入名称').max(64, '名称长度不能超过 64 个字符'),
    quantity: z.string().regex(/^\d+$/, '数量必须是大于等于 0 的整数'),
    onlineTime: z.string().nullable(),
    extConfig: z.string().nullable()
  })
  .superRefine((value, ctx) => {
    if (!value.extConfig) return

    try {
      const parsed: unknown = JSON.parse(value.extConfig)
      if (parsed === null || typeof parsed !== 'object' || Array.isArray(parsed)) {
        ctx.addIssue({ code: 'custom', path: ['extConfig'], message: '扩展配置必须是 JSON 对象' })
      }
    } catch {
      ctx.addIssue({ code: 'custom', path: ['extConfig'], message: '扩展配置必须是合法 JSON' })
    }
  })

type DemoFormValues = z.infer<typeof formSchema>

const EMPTY_FORM: DemoFormValues = {
  name: '',
  quantity: '0',
  onlineTime: null,
  extConfig: null
}

function toFormValues(detail: DemoDetailResp): DemoFormValues {
  return {
    name: detail.name,
    quantity: String(detail.quantity),
    onlineTime: toDatetimeLocal(detail.onlineTime),
    extConfig: detail.extConfig ? JSON.stringify(detail.extConfig, null, 2) : null
  }
}

interface DemoFormDrawerProps {
  /** The demo being edited, or `null` to create one. */
  readonly demoId: string | number | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Creates or edits a demo; every action is a drawer, matching the notice management screen. */
export function DemoFormDrawer({ demoId, open, onClose }: DemoFormDrawerProps) {
  const queryClient = useQueryClient()

  const isEdit = demoId !== null
  const detailQuery = useDetailQuery(demoKeys.detail(demoId), getDemo, demoId, { enabled: open })
  const detail = detailQuery.data

  const finish = (message: string) => {
    onClose()
    void queryClient.invalidateQueries({ queryKey: demoKeys.all })
    toast.add({ title: message, type: 'success' })
  }

  const form = useForm({
    defaultValues: EMPTY_FORM,
    validationLogic: revalidateLogic(),
    validators: { onDynamic: formSchema },
    onSubmit: async ({ value }) => {
      const body: DemoReq = {
        name: value.name.trim(),
        quantity: Number(value.quantity),
        onlineTime: value.onlineTime ? normalizeOnlineTime(value.onlineTime) : null,
        extConfig: value.extConfig ? (JSON.parse(value.extConfig) as Record<string, unknown>) : null
      }

      try {
        if (demoId === null) {
          await createDemo(body)
          finish('新增成功')
          return
        }

        await updateDemo(demoId, body)
        finish('修改成功')
      } catch {
        // The HTTP layer already reported the failure.
      }
    }
  })

  useFillFormOnce({
    open,
    isEdit: demoId !== null,
    entityKey: demoId,
    record: detail,
    version: detailQuery.dataUpdatedAt,
    buildValues: ({ isEdit: editing, record }) => (editing && record ? toFormValues(record) : EMPTY_FORM),
    apply: values => form.reset(values, { keepDefaultValues: true })
  })

  return (
    <EntityDrawer
      open={open}
      onClose={onClose}
      title={isEdit ? '修改示例' : '新增示例'}
      icon={<Boxes className="size-4" />}
      width={48}
      canClose={() => !form.state.isSubmitting}
    >
      {!isEdit || (detail && !detailQuery.isError && detailQuery.fetchStatus === 'idle') ? (
        <form
          noValidate
          className="flex min-h-0 flex-1 flex-col"
          onSubmit={event => {
            event.preventDefault()
            void form.handleSubmit()
          }}
        >
          <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-4">
            <FieldGroup className="grid gap-4 sm:grid-cols-2">
              <form.Field name="name">
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field className="gap-1.5" data-invalid={invalid}>
                      <FieldLabel>
                        名称
                        <RequiredMark />
                      </FieldLabel>
                      <Input
                        name="name"
                        value={field.state.value}
                        maxLength={64}
                        placeholder="请输入名称"
                        aria-invalid={invalid}
                        onChange={event => field.handleChange(event.target.value)}
                        onBlur={field.handleBlur}
                      />
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
              </form.Field>

              <form.Field name="quantity">
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field className="gap-1.5" data-invalid={invalid}>
                      <FieldLabel>
                        数量
                        <RequiredMark />
                      </FieldLabel>
                      <Input
                        name="quantity"
                        type="number"
                        min={0}
                        value={field.state.value}
                        aria-invalid={invalid}
                        onChange={event => field.handleChange(event.target.value)}
                        onBlur={field.handleBlur}
                      />
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
              </form.Field>

              <form.Field name="onlineTime">
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field className="gap-1.5" data-invalid={invalid}>
                      <FieldLabel>上线时间</FieldLabel>
                      <Input
                        name="onlineTime"
                        type="datetime-local"
                        value={field.state.value ?? ''}
                        aria-invalid={invalid}
                        onChange={event => field.handleChange(event.target.value || null)}
                        onBlur={field.handleBlur}
                      />
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
              </form.Field>

              <form.Field name="extConfig">
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field className="gap-1.5 sm:col-span-2" data-invalid={invalid}>
                      <FieldLabel>扩展配置</FieldLabel>
                      <Textarea
                        name="extConfig"
                        rows={6}
                        className="font-mono"
                        placeholder={'{"key": "value"}'}
                        value={field.state.value ?? ''}
                        aria-invalid={invalid}
                        onChange={event => field.handleChange(event.target.value || null)}
                        onBlur={field.handleBlur}
                      />
                      <FieldDescription>JSON 对象，保存该示例的自定义扩展属性</FieldDescription>
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
              </form.Field>
            </FieldGroup>
          </div>

          <DrawerFooter>
            <form.Subscribe selector={state => state.isSubmitting}>
              {isSubmitting => (
                <>
                  <Button type="button" variant="outline" disabled={isSubmitting} onClick={onClose}>
                    取消
                  </Button>
                  <Button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? (
                      <>
                        <Spinner />
                        保存中
                      </>
                    ) : (
                      '保存'
                    )}
                  </Button>
                </>
              )}
            </form.Subscribe>
          </DrawerFooter>
        </form>
      ) : detailQuery.isError ? (
        <div className="flex min-h-0 flex-1 flex-col">
          <QueryState state="error" className="p-6" onRetry={() => void detailQuery.refetch()} />
          <DrawerFooter>
            <Button type="button" variant="outline" onClick={onClose}>
              取消
            </Button>
          </DrawerFooter>
        </div>
      ) : (
        <DemoFormSkeleton onClose={onClose} />
      )}
    </EntityDrawer>
  )
}

function DemoFormSkeleton({ onClose }: { onClose: () => void }) {
  return (
    <div className="flex min-h-0 flex-1 flex-col">
      <div className="min-h-0 flex-1 overflow-y-auto p-4">
        <div className="grid gap-4 sm:grid-cols-2">
          {Array.from({ length: 4 }, (_, index) => (
            <div
              key={index}
              className={index === 3 ? 'flex flex-col gap-1.5 sm:col-span-2' : 'flex flex-col gap-1.5'}
            >
              <Skeleton className="h-3.5 w-16" />
              <Skeleton className="h-9 w-full" />
            </div>
          ))}
        </div>
      </div>
      <DrawerFooter>
        <Button type="button" variant="outline" onClick={onClose}>
          取消
        </Button>
      </DrawerFooter>
    </div>
  )
}
