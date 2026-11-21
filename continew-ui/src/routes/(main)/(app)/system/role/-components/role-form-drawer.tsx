import * as React from 'react'

import { revalidateLogic, useForm, useSelector } from '@tanstack/react-form'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { z } from 'zod'

import { UserShield } from 'lucide-react'

import { type CheckableTreeNode, CheckableTree } from '@/components/checkable-tree'
import { EntityDrawer } from '@/components/entity-drawer'
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
import { deptKeys, getDeptTree, type DeptResp } from '@/lib/api/dept'
import { createRole, DATA_SCOPE_OPTIONS, type DataScope, roleKeys, type RoleReq, updateRole } from '@/lib/api/role'

import { useRoleDetail } from './use-role-detail'

const NAME_PATTERN = /^[\u4e00-\u9fa5a-zA-Z0-9_-]{2,30}$/
const CODE_PATTERN = /^[a-zA-Z][a-zA-Z0-9_]{1,29}$/

const formSchema = z
  .object({
    name: z
      .string()
      .min(1, '角色名称不能为空')
      .regex(NAME_PATTERN, '角色名称长度为 2-30 个字符，支持中文、字母、数字、下划线、短横线'),
    code: z
      .string()
      .min(1, '角色编码不能为空')
      .regex(CODE_PATTERN, '角色编码长度为 2-30 个字符，支持大小写字母、数字、下划线，且以字母开头'),
    sort: z.number().int().min(1, '排序最小值为 1'),
    dataScope: z.union([z.literal(1), z.literal(2), z.literal(3), z.literal(4), z.literal(5)]),
    deptIds: z.array(z.union([z.string(), z.number()])),
    deptCheckStrictly: z.boolean(),
    description: z.string().max(200, '描述长度不能超过 200 个字符').optional().nullable()
  })
  .superRefine((data, ctx) => {
    if (data.dataScope === 5 && data.deptIds.length === 0) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['deptIds'],
        message: '自定义数据权限下必须至少选择一个部门'
      })
    }
  })

type FormValues = z.infer<typeof formSchema>

const EMPTY_FORM: FormValues = {
  name: '',
  code: '',
  sort: 1,
  dataScope: 1,
  deptIds: [],
  deptCheckStrictly: false,
  description: ''
}

const DATA_SCOPE_ITEMS = DATA_SCOPE_OPTIONS.map(opt => ({ value: String(opt.value), label: opt.label }))

function toCheckableDeptNodes(nodes: DeptResp[]): CheckableTreeNode[] {
  return nodes.map(node => ({
    id: String(node.id),
    parentId: node.parentId ? String(node.parentId) : null,
    label: node.name,
    children: node.children?.length ? toCheckableDeptNodes(node.children) : undefined
  }))
}

interface RoleFormDrawerProps {
  readonly roleId: string | number | null
  readonly open: boolean
  readonly onClose: () => void
}

export function RoleFormDrawer({ roleId, open, onClose }: RoleFormDrawerProps) {
  const queryClient = useQueryClient()
  const isCreate = roleId === null
  const formRef = React.useRef<HTMLFormElement>(null)

  const detail = useRoleDetail(roleId, { enabled: open })

  const depts = useQuery({
    queryKey: deptKeys.trees(),
    queryFn: ({ signal }) => getDeptTree(undefined, signal),
    enabled: open
  })

  const deptTreeData = React.useMemo(() => toCheckableDeptNodes(depts.data ?? []), [depts.data])

  const form = useForm({
    defaultValues: EMPTY_FORM,
    validationLogic: revalidateLogic(),
    validators: { onDynamic: formSchema },
    onSubmit: async ({ value }) => {
      const payload: RoleReq = {
        name: value.name.trim(),
        code: value.code.trim(),
        sort: value.sort,
        dataScope: value.dataScope,
        deptIds: value.dataScope === 5 ? value.deptIds : [],
        deptCheckStrictly: value.dataScope === 5 ? value.deptCheckStrictly : false,
        description: value.description ? value.description.trim() : null
      }

      try {
        if (roleId === null) {
          await createRole(payload)
          toast.add({ title: '新增角色成功', type: 'success' })
        } else {
          await updateRole(roleId, payload)
          toast.add({ title: '修改角色成功', type: 'success' })
        }
      } catch {
        // The HTTP layer already reported the failure.
        return
      }

      onClose()
      void queryClient.invalidateQueries({ queryKey: roleKeys.all })
    }
  })

  // `form.state` is a render-time snapshot, so anything that has to re-render on a form change goes
  // through a subscription: the two watched branches, and the "saving" state that gates closing.
  const dataScope = useSelector(form.store, state => state.values.dataScope)
  const deptCheckStrictly = useSelector(form.store, state => state.values.deptCheckStrictly)
  const isSubmitting = useSelector(form.store, state => state.isSubmitting)

  useFillFormOnce({
    open,
    isEdit: !isCreate,
    entityKey: roleId,
    record: detail.data,
    version: detail.dataUpdatedAt,
    buildValues: ({ isEdit: editing, record }) =>
      editing && record
        ? {
            name: record.name,
            code: record.code,
            sort: record.sort,
            dataScope: record.dataScope,
            deptIds: (record.deptIds ?? []).map(String),
            deptCheckStrictly: record.deptCheckStrictly,
            description: record.description ?? ''
          }
        : { ...EMPTY_FORM },
    apply: values => form.reset(values, { keepDefaultValues: true })
  })

  /**
   * Validates the whole object before submitting: `handleSubmit` returns as soon as the field
   * validators report an invalid form, so the cross-field `deptIds` rule would otherwise stay hidden
   * until the other errors were fixed.
   */
  const submit = async () => {
    await form.validate('submit')
    await form.handleSubmit()

    // TanStack Form does not move focus to the first invalid control, so the drawer does it here.
    requestAnimationFrame(() => {
      formRef.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
    })
  }

  const renderTextField = (
    name: 'name' | 'code',
    label: string,
    input: { placeholder: string; disabled?: boolean }
  ) => (
    <form.Field name={name} validators={{ onDynamic: formSchema.shape[name] }}>
      {field => {
        const invalid = field.state.meta.errors.length > 0

        return (
          <Field data-invalid={invalid}>
            <FieldLabel>
              {label}
              <RequiredMark />
            </FieldLabel>
            <Input
              name={name}
              value={field.state.value}
              onChange={event => field.handleChange(event.target.value)}
              onBlur={field.handleBlur}
              maxLength={30}
              disabled={isSubmitting || input.disabled}
              placeholder={input.placeholder}
              aria-invalid={invalid}
            />
            <FieldError errors={field.state.meta.errors} />
          </Field>
        )
      }}
    </form.Field>
  )

  const isLoading = !isCreate && (detail.isPending || detail.fetchStatus !== 'idle')
  const isDetailError = !isCreate && !isLoading && detail.isError

  return (
    <EntityDrawer
      open={open}
      onClose={onClose}
      title={isCreate ? '新增角色' : '修改角色'}
      icon={<UserShield className="size-4" />}
      width={32}
      closeDisabled={isSubmitting}
    >
      {isLoading ? (
        <div className="flex min-h-0 flex-1 flex-col gap-4 p-4">
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-20 w-full" />
        </div>
      ) : isDetailError ? (
        <QueryState state="error" className="p-6" onRetry={() => void detail.refetch()} />
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
            <FieldGroup>
              {renderTextField('name', '角色名称', { placeholder: '请输入角色名称' })}

              {renderTextField('code', '角色编码', {
                placeholder: '请输入角色编码',
                disabled: !isCreate && Boolean(detail.data?.isSystem)
              })}

              <form.Field name="sort" validators={{ onDynamic: formSchema.shape.sort }}>
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field data-invalid={invalid}>
                      <FieldLabel>排序</FieldLabel>
                      <Input
                        name="sort"
                        type="number"
                        min={1}
                        value={field.state.value}
                        onChange={event => field.handleChange(event.target.valueAsNumber || 1)}
                        onBlur={field.handleBlur}
                        disabled={isSubmitting}
                        placeholder="数值越小越靠前"
                        aria-invalid={invalid}
                      />
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
              </form.Field>

              <form.Field name="dataScope" validators={{ onDynamic: formSchema.shape.dataScope }}>
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field data-invalid={invalid}>
                      <FieldLabel>数据权限</FieldLabel>
                      <Select
                        items={DATA_SCOPE_ITEMS}
                        value={String(field.state.value)}
                        onValueChange={val => field.handleChange(Number(val) as DataScope)}
                      >
                        <SelectTrigger aria-invalid={invalid} disabled={isSubmitting}>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent align="start" alignItemWithTrigger={false}>
                          <SelectGroup>
                            {DATA_SCOPE_ITEMS.map(item => (
                              <SelectItem key={item.value} value={item.value}>
                                {item.label}
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

              {/* Conditional Department Tree for Custom Data Scope */}
              {dataScope === 5 && (
                <div className="flex flex-col gap-3 rounded-lg border bg-muted/20 p-3">
                  <form.Field name="deptCheckStrictly">
                    {field => (
                      <Field orientation="horizontal" className="items-center justify-between">
                        <FieldContent>
                          <FieldLabel>父子联动</FieldLabel>
                          <FieldDescription>
                            {field.state.value ? '已开启独立选择（不联动）' : '勾选父部门时自动勾选全部子部门'}
                          </FieldDescription>
                        </FieldContent>
                        <Switch
                          checked={!field.state.value}
                          onCheckedChange={checked => field.handleChange(!checked)}
                          disabled={isSubmitting}
                        />
                      </Field>
                    )}
                  </form.Field>

                  <form.Field name="deptIds" validators={{ onDynamic: formSchema.shape.deptIds }}>
                    {field => {
                      const invalid = field.state.meta.errors.length > 0

                      return (
                        <Field data-invalid={invalid}>
                          <FieldLabel>
                            选择权限部门
                            <RequiredMark />
                          </FieldLabel>
                          {/* The tree has no `disabled` prop of its own, so `inert` blocks both the
                                pointer and the keyboard while a save is in flight. */}
                          <div
                            inert={isSubmitting}
                            className={isSubmitting ? 'pointer-events-none opacity-50' : undefined}
                          >
                            <CheckableTree
                              data={deptTreeData}
                              checkedKeys={field.state.value}
                              onCheckedChange={field.handleChange}
                              checkStrictly={deptCheckStrictly}
                              loading={depts.isPending}
                              searchPlaceholder="搜索部门"
                              emptyText="未找到相关部门"
                            />
                          </div>
                          <FieldError errors={field.state.meta.errors} />
                        </Field>
                      )
                    }}
                  </form.Field>
                </div>
              )}

              <form.Field name="description" validators={{ onDynamic: formSchema.shape.description }}>
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field data-invalid={invalid}>
                      <FieldLabel>描述</FieldLabel>
                      <Textarea
                        name="description"
                        value={field.state.value ?? ''}
                        onChange={event => field.handleChange(event.target.value)}
                        onBlur={field.handleBlur}
                        disabled={isSubmitting}
                        maxLength={200}
                        rows={3}
                        placeholder="请输入角色描述信息（选填）"
                        aria-invalid={invalid}
                      />
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
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
