import * as React from 'react'

import { revalidateLogic, useForm } from '@tanstack/react-form'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import { z } from 'zod'

import { Building2, PackageOpen } from 'lucide-react'

import { EntityDrawer } from '@/components/entity-drawer'
import { ExpiryPicker } from '@/components/expiry-picker'
import { PasswordInput } from '@/components/password-input'
import { QueryState } from '@/components/query-state'
import { RequiredMark } from '@/components/required-mark'
import { Button } from '@/components/ui/button'
import { DrawerFooter } from '@/components/ui/drawer'
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from '@/components/ui/empty'
import { Field, FieldContent, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { InputGroup, InputGroupAddon, InputGroupInput } from '@/components/ui/input-group'
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'
import { Spinner } from '@/components/ui/spinner'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import { toast } from '@/components/ui/toast'
import { useFillFormOnce } from '@/hooks/use-fill-form-once'
import { listPackageDict, packageKeys } from '@/lib/api/package'
import { createTenant, tenantKeys, updateTenant, type TenantDetailResp, type TenantReq } from '@/lib/api/tenant'
import { encryptByRsa } from '@/lib/crypto'
import { PASSWORD_HINT, PASSWORD_PATTERN } from '@/lib/password'

import { useTenantDetail } from './use-tenant-detail'

/** Mirrors `RegexConstants.USERNAME`: 4-64 chars, letters, digits, underscore, letter first. */
const USERNAME_PATTERN = /^[a-zA-Z][a-zA-Z0-9_]{3,64}$/

const USERNAME_HINT = '管理员用户名长度为 4-64 个字符，支持字母、数字、下划线，且以字母开头'

const formSchema = z.object({
  name: z.string().trim().min(1, '请输入租户名称').max(30, '名称长度不能超过 30 个字符'),
  domain: z
    .string()
    .max(255, '域名长度不能超过 255 个字符')
    .refine(value => !value || (/^[^\s/?#]+$/.test(value) && !value.includes('://')), '请输入不含协议的域名或 IP'),
  expireTime: z.string().refine(value => {
    if (!value) {
      return true
    }

    const date = new Date(value.replace(/-/g, '/'))
    return !Number.isNaN(date.getTime()) && date.getTime() > Date.now()
  }, '过期时间必须是未来时间'),
  adminUsername: z.string().max(64, '管理员用户名长度不能超过 64 个字符'),
  adminPassword: z.string().max(32, '密码长度不能超过 32 个字符'),
  packageId: z.string(),
  description: z.string().max(200, '描述长度不能超过 200 个字符'),
  status: z.union([z.literal(1), z.literal(2)])
})

type FormValues = z.infer<typeof formSchema>

const EMPTY_FORM: FormValues = {
  name: '',
  domain: '',
  expireTime: '',
  adminUsername: '',
  adminPassword: '',
  packageId: '',
  description: '',
  status: 1
}

/** `packageId` becomes a string so it can drive the select. */
function toFormValues(tenant: TenantDetailResp): FormValues {
  return {
    name: tenant.name ?? '',
    domain: tenant.domain ?? '',
    expireTime: tenant.expireTime ?? '',
    adminUsername: tenant.adminUsername ?? '',
    // The backend never returns a stored password, so it stays empty and never reaches the body.
    adminPassword: '',
    packageId: tenant.packageId === null || tenant.packageId === undefined ? '' : String(tenant.packageId),
    description: tenant.description ?? '',
    status: tenant.status ?? 1
  }
}

function TenantFormSkeleton() {
  return (
    <div className="flex min-h-0 flex-1 flex-col">
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-4">
        <div className="flex flex-col gap-4">
          {Array.from({ length: 6 }, (_, index) => (
            <div key={index} className="flex flex-col gap-1.5">
              <Skeleton className="h-4 w-16" />
              <Skeleton className="h-9 w-full" />
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

interface TenantFormDrawerProps {
  /** The tenant being edited, or `null` to create one. */
  readonly tenantId: string | number | null
  readonly open: boolean
  readonly onClose: () => void
}

/**
 * Create and edit form for one tenant.
 *
 * The field set and its order follow the Vue client's `AddModal.vue`: the administrator
 * fields and the package are only offered on create, while the package already attached
 * to an existing tenant is kept in the form so the required `packageId` still travels.
 */
export function TenantFormDrawer({ tenantId, open, onClose }: TenantFormDrawerProps) {
  const queryClient = useQueryClient()
  const formRef = React.useRef<HTMLFormElement>(null)
  const isCreate = tenantId === null

  const detail = useTenantDetail(tenantId, { enabled: open })

  const packages = useQuery({
    queryKey: packageKeys.dicts(),
    queryFn: ({ signal }) => listPackageDict(signal),
    enabled: open
  })

  const packageOptions = React.useMemo(() => packages.data ?? [], [packages.data])
  const packageItems = React.useMemo(
    () => packageOptions.map(option => ({ value: String(option.value), label: option.label })),
    [packageOptions]
  )
  const packageIdByValue = React.useMemo(
    () => new Map(packageOptions.map(option => [String(option.value), option.value])),
    [packageOptions]
  )

  /** The body both modes send; only a create adds the administrator credentials. */
  const toTenantReq = (values: FormValues): TenantReq => ({
    name: values.name.trim(),
    domain: values.domain.trim() || undefined,
    expireTime: values.expireTime || null,
    description: values.description.trim() || undefined,
    status: values.status,
    packageId: packageIdByValue.get(values.packageId) ?? values.packageId
  })

  const form = useForm({
    defaultValues: EMPTY_FORM,
    validationLogic: revalidateLogic(),
    validators: { onDynamic: formSchema },
    onSubmit: async ({ value }) => {
      if (tenantId === null) {
        let password: string
        try {
          password = encryptByRsa(value.adminPassword)
        } catch {
          // Encryption happens locally, so the HTTP layer is not there to report it.
          toast.add({ title: '密码加密失败，请刷新页面后重试', type: 'error' })
          return
        }

        try {
          await createTenant({
            ...toTenantReq(value),
            adminUsername: value.adminUsername.trim(),
            adminPassword: password
          })

          onClose()
          void queryClient.invalidateQueries({ queryKey: tenantKeys.all })
          toast.add({ title: '新增成功', type: 'success' })
        } catch {
          // The HTTP layer already reported the failure.
        }

        return
      }

      try {
        await updateTenant(tenantId, toTenantReq(value))

        onClose()
        // One prefix covers both the list and the cached detail.
        void queryClient.invalidateQueries({ queryKey: tenantKeys.all })
        toast.add({ title: '修改成功', type: 'success' })
      } catch {
        // The HTTP layer already reported the failure.
      }
    }
  })

  useFillFormOnce({
    open,
    isEdit: !isCreate,
    entityKey: tenantId,
    record: detail.data,
    version: detail.dataUpdatedAt,
    buildValues: ({ isEdit: editing, record }) => (editing && record ? toFormValues(record) : EMPTY_FORM),
    apply: values => form.reset(values, { keepDefaultValues: true }),
    resetOnClose: false
  })

  /**
   * Validates the whole object before submitting: `handleSubmit` returns as soon as the field
   * validators report an invalid form, so a rule that only sits on the form would stay hidden
   * until the other errors were fixed.
   */
  const submit = async () => {
    await form.validate('submit')
    await form.handleSubmit()

    requestAnimationFrame(() => {
      formRef.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
    })
  }

  return (
    <EntityDrawer
      open={open}
      onClose={onClose}
      title={isCreate ? '新增租户' : '修改租户'}
      icon={<Building2 className="size-4" />}
      width={32}
      canClose={() => !form.state.isSubmitting}
    >
      {isCreate || (!detail.isError && detail.data && detail.fetchStatus === 'idle') ? (
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
              <form.Field name="name" validators={{ onDynamic: formSchema.shape.name }}>
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
                        onChange={event => field.handleChange(event.target.value)}
                        onBlur={field.handleBlur}
                        maxLength={30}
                        placeholder="请输入租户名称"
                        aria-invalid={invalid}
                      />
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
              </form.Field>

              <form.Field name="domain" validators={{ onDynamic: formSchema.shape.domain }}>
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field className="gap-1.5" data-invalid={invalid}>
                      <FieldLabel>域名</FieldLabel>
                      <InputGroup>
                        <InputGroupAddon align="inline-start">
                          <span className="text-xs text-muted-foreground">http(s)://</span>
                        </InputGroupAddon>
                        <InputGroupInput
                          name="domain"
                          value={field.state.value}
                          onChange={event => field.handleChange(event.target.value)}
                          onBlur={field.handleBlur}
                          maxLength={255}
                          placeholder="请输入需要绑定的域名"
                          aria-invalid={invalid}
                        />
                      </InputGroup>
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
              </form.Field>

              <form.Field name="expireTime" validators={{ onDynamic: formSchema.shape.expireTime }}>
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field className="gap-1.5" data-invalid={invalid}>
                      <FieldLabel>过期时间</FieldLabel>
                      <ExpiryPicker
                        value={field.state.value}
                        onChange={field.handleChange}
                        onBlur={field.handleBlur}
                        invalid={invalid}
                      />
                      {invalid ? (
                        <FieldError errors={field.state.meta.errors} />
                      ) : (
                        <FieldDescription>留空表示永不过期；若设置则必须是未来时间</FieldDescription>
                      )}
                    </Field>
                  )
                }}
              </form.Field>

              {isCreate ? (
                <>
                  <form.Field
                    name="adminUsername"
                    validators={{
                      onBlur: ({ value }) => (USERNAME_PATTERN.test(value) ? undefined : { message: USERNAME_HINT }),
                      onSubmit: ({ value }) => (USERNAME_PATTERN.test(value) ? undefined : { message: USERNAME_HINT })
                    }}
                  >
                    {field => {
                      const invalid = field.state.meta.errors.length > 0

                      return (
                        <Field className="gap-1.5" data-invalid={invalid}>
                          <FieldLabel>
                            管理员用户
                            <RequiredMark />
                          </FieldLabel>
                          <Input
                            name="adminUsername"
                            value={field.state.value}
                            onChange={event => field.handleChange(event.target.value)}
                            onBlur={field.handleBlur}
                            maxLength={64}
                            placeholder="请输入管理员用户"
                            autoComplete="off"
                            aria-invalid={invalid}
                          />
                          <FieldError errors={field.state.meta.errors} />
                        </Field>
                      )
                    }}
                  </form.Field>

                  <form.Field
                    name="adminPassword"
                    validators={{
                      onBlur: ({ value }) => (PASSWORD_PATTERN.test(value) ? undefined : { message: PASSWORD_HINT }),
                      onSubmit: ({ value }) => (PASSWORD_PATTERN.test(value) ? undefined : { message: PASSWORD_HINT })
                    }}
                  >
                    {field => {
                      const invalid = field.state.meta.errors.length > 0

                      return (
                        <Field className="gap-1.5" data-invalid={invalid}>
                          <FieldLabel>
                            管理员密码
                            <RequiredMark />
                          </FieldLabel>
                          <PasswordInput
                            name="adminPassword"
                            value={field.state.value}
                            onChange={event => field.handleChange(event.target.value)}
                            onBlur={field.handleBlur}
                            maxLength={32}
                            placeholder="请输入管理员密码"
                            autoComplete="new-password"
                            aria-invalid={invalid}
                          />
                          {invalid ? (
                            <FieldError errors={field.state.meta.errors} />
                          ) : (
                            <FieldDescription>{PASSWORD_HINT}</FieldDescription>
                          )}
                        </Field>
                      )
                    }}
                  </form.Field>

                  <form.Field
                    name="packageId"
                    validators={{
                      onSubmit: ({ value }) => (value ? undefined : { message: '请选择套餐' })
                    }}
                  >
                    {field => {
                      const invalid = field.state.meta.errors.length > 0

                      return (
                        <Field className="gap-1.5" data-invalid={invalid}>
                          <FieldLabel>
                            套餐
                            <RequiredMark />
                          </FieldLabel>

                          {packages.isPending ? (
                            <Skeleton className="h-8 w-full" />
                          ) : packageOptions.length > 0 ? (
                            <Select
                              items={packageItems}
                              value={field.state.value}
                              onValueChange={value => field.handleChange(value ?? '')}
                            >
                              <SelectTrigger className="w-full" aria-invalid={invalid}>
                                <SelectValue placeholder="请选择套餐" />
                              </SelectTrigger>
                              <SelectContent alignItemWithTrigger={false}>
                                <SelectGroup>
                                  {packageItems.map(option => (
                                    <SelectItem key={option.value} value={option.value}>
                                      {option.label}
                                    </SelectItem>
                                  ))}
                                </SelectGroup>
                              </SelectContent>
                            </Select>
                          ) : (
                            <Empty className="min-h-28 border border-dashed p-4">
                              <EmptyHeader>
                                <EmptyMedia>
                                  <PackageOpen />
                                </EmptyMedia>
                                <EmptyTitle>{packages.isError ? '套餐加载失败' : '暂无可用套餐'}</EmptyTitle>
                                <EmptyDescription>
                                  {packages.isError ? (
                                    '请关闭抽屉后重新打开重试'
                                  ) : (
                                    <>
                                      {'请先在「'}
                                      <Button
                                        nativeButton={false}
                                        variant="link"
                                        className="h-auto p-0 align-baseline text-sm font-normal"
                                        render={<Link to="/tenant/package" />}
                                      >
                                        套餐管理
                                      </Button>
                                      {'」中创建并启用套餐'}
                                    </>
                                  )}
                                </EmptyDescription>
                              </EmptyHeader>
                            </Empty>
                          )}

                          {packageOptions.length > 0 ? <FieldError errors={field.state.meta.errors} /> : null}
                        </Field>
                      )
                    }}
                  </form.Field>
                </>
              ) : null}

              <form.Field name="description" validators={{ onDynamic: formSchema.shape.description }}>
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field className="gap-1.5" data-invalid={invalid}>
                      <FieldLabel>描述</FieldLabel>
                      <Textarea
                        name="description"
                        value={field.state.value}
                        onChange={event => field.handleChange(event.target.value)}
                        onBlur={field.handleBlur}
                        maxLength={200}
                        rows={3}
                        placeholder="请输入描述"
                        aria-invalid={invalid}
                      />
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
              </form.Field>

              <form.Field name="status">
                {field => (
                  <Field orientation="horizontal" className="items-center justify-between">
                    <FieldContent>
                      <FieldLabel>状态</FieldLabel>
                      <FieldDescription>
                        {field.state.value === 1 ? '启用后该租户可以正常访问' : '禁用后该租户无法访问'}
                      </FieldDescription>
                    </FieldContent>
                    <Switch
                      checked={field.state.value === 1}
                      onCheckedChange={checked => field.handleChange(checked ? 1 : 2)}
                    />
                  </Field>
                )}
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
      ) : detail.isError ? (
        <QueryState state="error" className="p-6" onRetry={() => void detail.refetch()} />
      ) : (
        <TenantFormSkeleton />
      )}
    </EntityDrawer>
  )
}
