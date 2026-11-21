import * as React from 'react'

import { revalidateLogic, useForm, useSelector } from '@tanstack/react-form'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { z } from 'zod'

import { MessageSquare } from 'lucide-react'

import { EntityDrawer } from '@/components/entity-drawer'
import { PasswordInput } from '@/components/password-input'
import { QueryState } from '@/components/query-state'
import { RequiredMark } from '@/components/required-mark'
import { Button } from '@/components/ui/button'
import { DrawerFooter } from '@/components/ui/drawer'
import { Field, FieldContent, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'
import { Spinner } from '@/components/ui/spinner'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import { toast } from '@/components/ui/toast'
import { useFillFormOnce } from '@/hooks/use-fill-form-once'
import { createSmsConfig, getSmsConfig, type SmsConfigReq, updateSmsConfig } from '@/lib/api/sms'
import { smsKeys } from '@/lib/query-keys'

const smsFormSchema = z.object({
  name: z.string().trim().min(1, '请输入配置名称').max(100, '配置名称不能超过 100 个字符'),
  supplier: z.string().min(1, '请选择服务厂商'),
  accessKey: z.string().trim().min(1, '请输入 Access Key').max(255, 'Access Key 不能超过 255 个字符'),
  secretKey: z.string().max(255, 'Secret Key 不能超过 255 个字符'),
  signature: z.string().max(100, '短信签名不能超过 100 个字符'),
  templateId: z.string().trim().min(1, '请输入模板 ID').max(50, '模板 ID 不能超过 50 个字符'),
  weight: z.number().int('权重必须为整数').min(1, '权重最小为 1').max(100, '权重最大为 100'),
  retryInterval: z.number().int('重试间隔必须为整数').min(1, '重试间隔最小为 1 秒'),
  maxRetries: z.number().int('重试次数必须为整数').min(0, '重试次数最小为 0'),
  maximum: z.number().int('发送上限必须为整数').min(1, '发送上限最小为 1'),
  supplierConfig: z.string().refine(val => {
    if (!val || !val.trim()) return true
    try {
      JSON.parse(val)
      return true
    } catch {
      return false
    }
  }, '扩展配置必须为合法的 JSON 格式'),
  status: z.number().int().min(1).max(2)
})

type FormValues = z.infer<typeof smsFormSchema>

const DEFAULT_FORM: FormValues = {
  name: '',
  supplier: 'alibaba',
  accessKey: '',
  secretKey: '',
  signature: '',
  templateId: '',
  weight: 1,
  retryInterval: 5,
  maxRetries: 0,
  maximum: 100,
  supplierConfig: '',
  status: 1
}

/**
 * A create has to carry a secret, but an edit leaves the field empty to mean "keep the stored one".
 * The object schema cannot express that, so the rule only sits on the field for a create.
 */
const createSecretKeyRule = z.string().trim().min(1, '请输入 Secret Key')

function SmsFormSkeleton({ onClose }: { onClose: () => void }) {
  return (
    <div className="flex min-h-0 flex-1 flex-col">
      <div className="flex min-h-0 flex-1 flex-col gap-4 p-4">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div className="flex flex-col gap-1.5">
            <Skeleton className="h-4 w-16" />
            <Skeleton className="h-9 w-full" />
          </div>
          <div className="flex flex-col gap-1.5">
            <Skeleton className="h-4 w-16" />
            <Skeleton className="h-9 w-full" />
          </div>
        </div>
        <div className="flex flex-col gap-1.5">
          <Skeleton className="h-4 w-20" />
          <Skeleton className="h-9 w-full" />
        </div>
        <div className="flex flex-col gap-1.5">
          <Skeleton className="h-4 w-20" />
          <Skeleton className="h-9 w-full" />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div className="flex flex-col gap-1.5">
            <Skeleton className="h-4 w-16" />
            <Skeleton className="h-9 w-full" />
          </div>
          <div className="flex flex-col gap-1.5">
            <Skeleton className="h-4 w-16" />
            <Skeleton className="h-9 w-full" />
          </div>
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div className="flex flex-col gap-1.5">
            <Skeleton className="h-4 w-20" />
            <Skeleton className="h-9 w-full" />
          </div>
          <div className="flex flex-col gap-1.5">
            <Skeleton className="h-4 w-20" />
            <Skeleton className="h-9 w-full" />
          </div>
        </div>
        <div className="flex flex-col gap-1.5">
          <Skeleton className="h-4 w-32" />
          <Skeleton className="h-16 w-full" />
        </div>
      </div>
      <DrawerFooter>
        <Button type="button" variant="outline" onClick={onClose}>
          取消
        </Button>
        <Button disabled>保存</Button>
      </DrawerFooter>
    </div>
  )
}

interface SmsFormDrawerProps {
  readonly smsId: string | number | null
  readonly open: boolean
  readonly supplierOptions: Array<{ label: string; value: string }>
  readonly onClose: () => void
}

export function SmsFormDrawer({ smsId, open, supplierOptions, onClose }: SmsFormDrawerProps) {
  const queryClient = useQueryClient()
  const isEdit = smsId !== null
  const formRef = React.useRef<HTMLFormElement>(null)

  const detailQuery = useQuery({
    queryKey: smsKeys.detail(smsId ?? ''),
    queryFn: ({ signal }) => getSmsConfig(smsId!, signal),
    enabled: open && isEdit
  })

  const form = useForm({
    defaultValues: DEFAULT_FORM,
    validationLogic: revalidateLogic(),
    validators: { onDynamic: smsFormSchema },
    onSubmit: async ({ value }) => {
      const payload: SmsConfigReq = {
        name: value.name.trim(),
        supplier: value.supplier,
        accessKey: value.accessKey.trim(),
        secretKey: value.secretKey.trim() || undefined!,
        signature: value.signature.trim() || undefined,
        templateId: value.templateId.trim(),
        weight: value.weight,
        retryInterval: value.retryInterval,
        maxRetries: value.maxRetries,
        maximum: value.maximum,
        supplierConfig: value.supplierConfig.trim() || undefined,
        status: value.status as 1 | 2
      }

      try {
        if (smsId === null) {
          await createSmsConfig(payload)
          toast.add({ title: '新增短信配置成功', type: 'success' })
        } else {
          await updateSmsConfig(smsId, payload)
          toast.add({ title: '修改短信配置成功', type: 'success' })
        }
      } catch {
        // The HTTP layer already reported the failure.
        return
      }

      void queryClient.invalidateQueries({ queryKey: smsKeys.all })
      onClose()
    }
  })

  // `form.state` is a render-time snapshot, so the "saving" flag that gates closing and disables the
  // fields comes from a subscription, not from a `mutate()` call that returns before the response.
  const isSubmitting = useSelector(form.store, state => state.isSubmitting)

  useFillFormOnce({
    open,
    isEdit,
    entityKey: smsId,
    record: detailQuery.data,
    version: detailQuery.dataUpdatedAt,
    buildValues: ({ isEdit: editing, record }) =>
      editing && record
        ? {
            name: record.name ?? '',
            supplier: record.supplier ?? (supplierOptions[0]?.value || 'alibaba'),
            accessKey: record.accessKey ?? '',
            // The backend never returns a stored secret, so it stays empty and never reaches the body.
            secretKey: '',
            signature: record.signature ?? '',
            templateId: record.templateId ?? '',
            weight: record.weight ?? 1,
            retryInterval: record.retryInterval ?? 5,
            maxRetries: record.maxRetries ?? 0,
            maximum: record.maximum ?? 100,
            supplierConfig: record.supplierConfig ?? '',
            status: record.status ?? 1
          }
        : { ...DEFAULT_FORM, supplier: supplierOptions[0]?.value || 'alibaba' },
    apply: values => form.reset(values, { keepDefaultValues: true })
  })

  /**
   * Validates the whole object before submitting: `handleSubmit` returns as soon as the field
   * validators report an invalid form, so the JSON rule above would otherwise stay hidden until the
   * other errors were fixed.
   */
  const submit = async () => {
    await form.validate('submit')
    await form.handleSubmit()

    // TanStack Form does not move focus to the first invalid control, so the drawer does it here.
    requestAnimationFrame(() => {
      formRef.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
    })
  }

  const isLoading = isEdit && detailQuery.isLoading

  return (
    <EntityDrawer
      open={open}
      onClose={onClose}
      title={isEdit ? '修改短信配置' : '新增短信配置'}
      icon={<MessageSquare className="size-4" />}
      width={32}
      closeDisabled={isSubmitting}
      canClose={() => !isSubmitting}
    >
      {isLoading ? (
        <SmsFormSkeleton onClose={onClose} />
      ) : isEdit && detailQuery.isError ? (
        // Without the record the form would post its defaults over an existing config.
        <QueryState state="error" className="p-6" onRetry={() => void detailQuery.refetch()} />
      ) : (
        <form
          ref={formRef}
          noValidate
          className="flex min-h-0 flex-1 flex-col"
          onSubmit={event => {
            event.preventDefault()
            void submit()
          }}
        >
          <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-4">
            <FieldGroup className="gap-4">
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <form.Field name="name" validators={{ onDynamic: smsFormSchema.shape.name }}>
                  {field => {
                    const invalid = field.state.meta.errors.length > 0

                    return (
                      <Field className="gap-1.5" data-invalid={invalid}>
                        <FieldLabel>
                          配置名称
                          <RequiredMark />
                        </FieldLabel>
                        <Input
                          name="name"
                          value={field.state.value}
                          onChange={event => field.handleChange(event.target.value)}
                          onBlur={field.handleBlur}
                          maxLength={100}
                          placeholder="请输入配置名称"
                          disabled={isSubmitting}
                          aria-invalid={invalid}
                        />
                        <FieldError errors={field.state.meta.errors} />
                      </Field>
                    )
                  }}
                </form.Field>

                <form.Field name="supplier" validators={{ onDynamic: smsFormSchema.shape.supplier }}>
                  {field => {
                    const invalid = field.state.meta.errors.length > 0

                    return (
                      <Field className="gap-1.5" data-invalid={invalid}>
                        <FieldLabel>
                          服务厂商
                          <RequiredMark />
                        </FieldLabel>
                        <Select
                          items={supplierOptions}
                          value={field.state.value}
                          disabled={isSubmitting}
                          onValueChange={val => {
                            if (!val) return
                            field.handleChange(val)
                          }}
                        >
                          <SelectTrigger aria-invalid={invalid}>
                            <SelectValue placeholder="请选择服务厂商" />
                          </SelectTrigger>
                          <SelectContent align="start" alignItemWithTrigger={false}>
                            <SelectGroup>
                              {supplierOptions.map(s => (
                                <SelectItem key={s.value} value={s.value}>
                                  {s.label}
                                </SelectItem>
                              ))}
                            </SelectGroup>
                          </SelectContent>
                        </Select>
                        <FieldError errors={field.state.meta.errors} />
                      </Field>
                    )
                  }}
                </form.Field>
              </div>

              <form.Field name="accessKey" validators={{ onDynamic: smsFormSchema.shape.accessKey }}>
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field className="gap-1.5" data-invalid={invalid}>
                      <FieldLabel>
                        Access Key
                        <RequiredMark />
                      </FieldLabel>
                      <Input
                        name="accessKey"
                        value={field.state.value}
                        onChange={event => field.handleChange(event.target.value)}
                        onBlur={field.handleBlur}
                        maxLength={255}
                        placeholder="请输入 Access Key"
                        disabled={isSubmitting}
                        aria-invalid={invalid}
                      />
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
              </form.Field>

              <form.Field name="secretKey" validators={isEdit ? undefined : { onDynamic: createSecretKeyRule }}>
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field data-invalid={invalid}>
                      <FieldLabel>
                        Secret Key
                        {!isEdit && <RequiredMark />}
                      </FieldLabel>
                      <PasswordInput
                        name="secretKey"
                        value={field.state.value}
                        onChange={event => field.handleChange(event.target.value)}
                        onBlur={field.handleBlur}
                        maxLength={255}
                        placeholder={isEdit ? '留空表示不修改' : '请输入 Secret Key'}
                        disabled={isSubmitting}
                        aria-invalid={invalid}
                      />
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
              </form.Field>

              <div className="grid grid-cols-2 gap-4">
                <form.Field name="signature" validators={{ onDynamic: smsFormSchema.shape.signature }}>
                  {field => {
                    const invalid = field.state.meta.errors.length > 0

                    return (
                      <Field data-invalid={invalid}>
                        <FieldLabel>短信签名</FieldLabel>
                        <Input
                          name="signature"
                          value={field.state.value}
                          onChange={event => field.handleChange(event.target.value)}
                          onBlur={field.handleBlur}
                          maxLength={100}
                          placeholder="请输入短信签名"
                          disabled={isSubmitting}
                          aria-invalid={invalid}
                        />
                        <FieldError errors={field.state.meta.errors} />
                      </Field>
                    )
                  }}
                </form.Field>

                <form.Field name="templateId" validators={{ onDynamic: smsFormSchema.shape.templateId }}>
                  {field => {
                    const invalid = field.state.meta.errors.length > 0

                    return (
                      <Field data-invalid={invalid}>
                        <FieldLabel>
                          模板 ID
                          <RequiredMark />
                        </FieldLabel>
                        <Input
                          name="templateId"
                          value={field.state.value}
                          onChange={event => field.handleChange(event.target.value)}
                          onBlur={field.handleBlur}
                          maxLength={50}
                          placeholder="请输入模板 ID"
                          disabled={isSubmitting}
                          aria-invalid={invalid}
                        />
                        <FieldError errors={field.state.meta.errors} />
                      </Field>
                    )
                  }}
                </form.Field>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <form.Field name="weight" validators={{ onDynamic: smsFormSchema.shape.weight }}>
                  {field => {
                    const invalid = field.state.meta.errors.length > 0

                    return (
                      <Field data-invalid={invalid}>
                        <FieldLabel>负载均衡权重</FieldLabel>
                        <Input
                          name="weight"
                          type="number"
                          min={1}
                          max={100}
                          value={field.state.value}
                          onChange={event =>
                            field.handleChange(
                              Number.isNaN(event.target.valueAsNumber) ? 1 : event.target.valueAsNumber
                            )
                          }
                          onBlur={field.handleBlur}
                          disabled={isSubmitting}
                          aria-invalid={invalid}
                        />
                        <FieldError errors={field.state.meta.errors} />
                      </Field>
                    )
                  }}
                </form.Field>

                <form.Field name="retryInterval" validators={{ onDynamic: smsFormSchema.shape.retryInterval }}>
                  {field => {
                    const invalid = field.state.meta.errors.length > 0

                    return (
                      <Field data-invalid={invalid}>
                        <FieldLabel>重试间隔</FieldLabel>
                        <div className="relative">
                          <Input
                            name="retryInterval"
                            type="number"
                            min={1}
                            value={field.state.value}
                            onChange={event =>
                              field.handleChange(
                                Number.isNaN(event.target.valueAsNumber) ? 1 : event.target.valueAsNumber
                              )
                            }
                            onBlur={field.handleBlur}
                            disabled={isSubmitting}
                            className="pr-10"
                            aria-invalid={invalid}
                          />
                          <span className="pointer-events-none absolute inset-y-0 right-3 flex items-center text-xs text-muted-foreground">
                            秒
                          </span>
                        </div>
                        <FieldError errors={field.state.meta.errors} />
                      </Field>
                    )
                  }}
                </form.Field>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <form.Field name="maxRetries" validators={{ onDynamic: smsFormSchema.shape.maxRetries }}>
                  {field => {
                    const invalid = field.state.meta.errors.length > 0

                    return (
                      <Field className="gap-1.5" data-invalid={invalid}>
                        <FieldLabel>重试次数</FieldLabel>
                        <Input
                          name="maxRetries"
                          type="number"
                          min={0}
                          value={field.state.value}
                          onChange={event =>
                            field.handleChange(
                              Number.isNaN(event.target.valueAsNumber) ? 0 : event.target.valueAsNumber
                            )
                          }
                          onBlur={field.handleBlur}
                          disabled={isSubmitting}
                          aria-invalid={invalid}
                        />
                        <FieldError errors={field.state.meta.errors} />
                      </Field>
                    )
                  }}
                </form.Field>

                <form.Field name="maximum" validators={{ onDynamic: smsFormSchema.shape.maximum }}>
                  {field => {
                    const invalid = field.state.meta.errors.length > 0

                    return (
                      <Field className="gap-1.5" data-invalid={invalid}>
                        <FieldLabel>发送上限</FieldLabel>
                        <Input
                          name="maximum"
                          type="number"
                          min={1}
                          value={field.state.value}
                          onChange={event =>
                            field.handleChange(
                              Number.isNaN(event.target.valueAsNumber) ? 1 : event.target.valueAsNumber
                            )
                          }
                          onBlur={field.handleBlur}
                          disabled={isSubmitting}
                          aria-invalid={invalid}
                        />
                        <FieldError errors={field.state.meta.errors} />
                      </Field>
                    )
                  }}
                </form.Field>
              </div>

              <form.Field name="supplierConfig" validators={{ onDynamic: smsFormSchema.shape.supplierConfig }}>
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field className="gap-1.5" data-invalid={invalid}>
                      <FieldContent>
                        <FieldLabel>厂商独立扩展配置</FieldLabel>
                        <FieldDescription>针对特定供应商（如腾讯云 sdkAppId）的专有扩展参数</FieldDescription>
                      </FieldContent>
                      <Textarea
                        name="supplierConfig"
                        value={field.state.value}
                        onChange={event => field.handleChange(event.target.value)}
                        onBlur={field.handleBlur}
                        rows={2}
                        maxLength={500}
                        placeholder="请输入厂商的（JSON 格式）扩展配置"
                        disabled={isSubmitting}
                        aria-invalid={invalid}
                      />
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
              </form.Field>

              <form.Field name="status">
                {field => (
                  <Field
                    orientation="horizontal"
                    className="items-center justify-between py-1"
                    data-invalid={field.state.meta.errors.length > 0}
                  >
                    <FieldContent>
                      <FieldLabel className="cursor-pointer">状态</FieldLabel>
                      <FieldDescription>禁用后该通道将不再参与短信发送</FieldDescription>
                    </FieldContent>
                    <Switch
                      checked={field.state.value === 1}
                      onCheckedChange={checked => field.handleChange(checked ? 1 : 2)}
                      disabled={isSubmitting}
                      aria-invalid={field.state.meta.errors.length > 0}
                    />
                  </Field>
                )}
              </form.Field>
            </FieldGroup>
          </div>

          <DrawerFooter>
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
          </DrawerFooter>
        </form>
      )}
    </EntityDrawer>
  )
}
