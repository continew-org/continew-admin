import * as React from 'react'

import { revalidateLogic, useForm, useSelector } from '@tanstack/react-form'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { z } from 'zod'

import { Building2 } from 'lucide-react'

import { EntityDrawer } from '@/components/entity-drawer'
import { ParentTreeSelect } from '@/components/parent-tree-select'
import { QueryState } from '@/components/query-state'
import { RequiredMark } from '@/components/required-mark'
import { Button } from '@/components/ui/button'
import { DrawerFooter } from '@/components/ui/drawer'
import { Field, FieldContent, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Spinner } from '@/components/ui/spinner'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import { toast } from '@/components/ui/toast'
import { useFillFormOnce } from '@/hooks/use-fill-form-once'
import { createDept, deptKeys, getDept, type DeptReq, type DeptResp, updateDept } from '@/lib/api/dept'
import { toDeptNodeData } from '@/lib/dept-tree'

const formSchema = z.object({
  parentId: z
    .union([z.string(), z.number()])
    .refine(val => val !== null && val !== undefined && val !== '', '请选择上级部门'),
  name: z.string().trim().min(1, '部门名称不能为空').max(30, '部门名称长度不能超过 30 个字符'),
  sort: z.number().int().min(1, '排序最小值为 1'),
  status: z.union([z.literal(1), z.literal(2)]),
  description: z.string().max(200, '描述长度不能超过 200 个字符')
})

type FormValues = z.infer<typeof formSchema>

const EMPTY_FORM: FormValues = {
  parentId: '',
  name: '',
  sort: 999,
  status: 1,
  description: ''
}

interface DeptFormDrawerProps {
  readonly deptId: string | number | null
  readonly defaultParentId?: string | number | null
  readonly open: boolean
  readonly deptTree: DeptResp[]
  readonly onClose: () => void
}

export function DeptFormDrawer({ deptId, defaultParentId, open, deptTree, onClose }: DeptFormDrawerProps) {
  const isEdit = deptId !== null
  const queryClient = useQueryClient()
  const formRef = React.useRef<HTMLFormElement>(null)

  const detailQuery = useQuery({
    queryKey: deptKeys.detail(deptId),
    queryFn: ({ signal }) => getDept(deptId!, signal),
    enabled: open && isEdit
  })

  const form = useForm({
    defaultValues: EMPTY_FORM,
    validationLogic: revalidateLogic(),
    validators: { onDynamic: formSchema },
    onSubmit: async ({ value }) => {
      const payload: DeptReq = {
        parentId: value.parentId,
        name: value.name,
        sort: value.sort,
        status: value.status,
        description: value.description || undefined
      }

      try {
        if (isEdit && deptId !== null) {
          await updateDept(deptId, payload)
        } else {
          await createDept(payload)
        }
      } catch {
        // The HTTP layer already reported the failure.
        return
      }

      onClose()
      void queryClient.invalidateQueries({ queryKey: deptKeys.all })
      toast.add({ title: isEdit ? '修改部门成功' : '新增部门成功', type: 'success' })
    }
  })

  // `form.state.isSubmitting` is a render-time snapshot, so the close guard, the close button and the
  // footer all read the subscribed value instead. It covers the whole request, unlike a `mutate()`
  // call that returns before the response lands.
  const isSubmitting = useSelector(form.store, state => state.isSubmitting)

  const isRootDept = isEdit && (detailQuery.data?.parentId === 0 || detailQuery.data?.parentId === null)

  // A create waits until it has a parent to default to, then fills once: a tree that arrives after
  // the drawer opened must not overwrite what the user already typed.
  const createParentId = defaultParentId ?? (deptTree.length > 0 ? deptTree[0].id : null)
  useFillFormOnce({
    open,
    isEdit,
    entityKey: deptId,
    record: detailQuery.data,
    version: detailQuery.dataUpdatedAt,
    ready: isEdit || createParentId !== null,
    buildValues: ({ isEdit: editing, record }) =>
      editing && record
        ? {
            parentId: record.parentId === null || record.parentId === undefined ? 0 : record.parentId,
            name: record.name ?? '',
            sort: record.sort ?? 999,
            status: record.status ?? 1,
            description: record.description ?? ''
          }
        : { ...EMPTY_FORM, parentId: createParentId ?? '' },
    apply: values => form.reset(values, { keepDefaultValues: true })
  })

  /**
   * Validates the whole object before submitting: `handleSubmit` returns as soon as the field
   * validators report an invalid form, so a rule that only sits on the form would stay hidden until
   * the other errors were fixed.
   */
  const submit = async () => {
    await form.validate('submit')
    await form.handleSubmit()

    // TanStack Form does not move focus to the first invalid control, so the drawer does it here.
    requestAnimationFrame(() => {
      formRef.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
    })
  }

  const isLoadingDetail = isEdit && detailQuery.isLoading
  const isDetailError = isEdit && detailQuery.isError

  return (
    <EntityDrawer
      open={open}
      onClose={onClose}
      title={isEdit ? '修改部门' : '新增部门'}
      icon={<Building2 className="size-4" />}
      width={30}
      closeDisabled={isSubmitting}
    >
      {isLoadingDetail ? (
        <div className="flex min-h-0 flex-1 flex-col gap-4 p-4">
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-20 w-full" />
        </div>
      ) : isDetailError ? (
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
              {!isRootDept ? (
                <form.Field name="parentId" validators={{ onDynamic: formSchema.shape.parentId }}>
                  {field => {
                    const invalid = field.state.meta.errors.length > 0

                    return (
                      <Field data-invalid={invalid}>
                        <FieldLabel>
                          上级部门
                          <RequiredMark />
                        </FieldLabel>
                        <ParentTreeSelect
                          value={field.state.value}
                          onChange={next => field.handleChange(next ?? '')}
                          data={deptTree}
                          currentId={deptId}
                          toNodeData={toDeptNodeData}
                          disabled={isSubmitting}
                          invalid={invalid}
                          placeholder="选择上级部门"
                          searchPlaceholder="搜索上级部门"
                          emptyText="未找到相关部门"
                        />
                        <FieldError errors={field.state.meta.errors} />
                      </Field>
                    )
                  }}
                </form.Field>
              ) : (
                <Field>
                  <FieldLabel>上级部门</FieldLabel>
                  <Input value="顶级部门（根机构）" disabled readOnly />
                  <FieldDescription>根机构部门不支持变更上级部门</FieldDescription>
                </Field>
              )}

              <form.Field name="name" validators={{ onDynamic: formSchema.shape.name }}>
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field data-invalid={invalid}>
                      <FieldLabel htmlFor="dept-name">
                        部门名称
                        <RequiredMark />
                      </FieldLabel>
                      <Input
                        id="dept-name"
                        name="name"
                        value={field.state.value}
                        onChange={event => field.handleChange(event.target.value)}
                        onBlur={field.handleBlur}
                        placeholder="请输入部门名称，例如：研发中心"
                        disabled={isSubmitting}
                        aria-invalid={invalid}
                      />
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
              </form.Field>

              <form.Field name="sort" validators={{ onDynamic: formSchema.shape.sort }}>
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field data-invalid={invalid}>
                      <FieldLabel htmlFor="dept-sort">
                        排序
                        <RequiredMark />
                      </FieldLabel>
                      <Input
                        id="dept-sort"
                        name="sort"
                        type="number"
                        min={1}
                        value={field.state.value}
                        onChange={event => field.handleChange(event.target.valueAsNumber || 1)}
                        onBlur={field.handleBlur}
                        placeholder="请输入排序值（越小越靠前）"
                        disabled={isSubmitting}
                        aria-invalid={invalid}
                      />
                      <FieldError errors={field.state.meta.errors} />
                    </Field>
                  )
                }}
              </form.Field>

              <form.Field name="status" validators={{ onDynamic: formSchema.shape.status }}>
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field orientation="horizontal" className="items-center justify-between" data-invalid={invalid}>
                      <FieldContent>
                        <FieldLabel>部门状态</FieldLabel>
                        <FieldDescription>
                          {field.state.value === 1
                            ? '启用状态，允许部门下属人员正常访问'
                            : '禁用状态，禁用后该部门直属用户会话将失效'}
                        </FieldDescription>
                      </FieldContent>
                      <Switch
                        checked={field.state.value === 1}
                        onCheckedChange={checked => field.handleChange(checked ? 1 : 2)}
                        disabled={isSubmitting}
                        aria-invalid={invalid}
                      />
                    </Field>
                  )
                }}
              </form.Field>

              <form.Field name="description" validators={{ onDynamic: formSchema.shape.description }}>
                {field => {
                  const invalid = field.state.meta.errors.length > 0

                  return (
                    <Field data-invalid={invalid}>
                      <FieldLabel htmlFor="dept-description">部门描述</FieldLabel>
                      <Textarea
                        id="dept-description"
                        name="description"
                        value={field.state.value}
                        onChange={event => field.handleChange(event.target.value)}
                        onBlur={field.handleBlur}
                        placeholder="请输入部门描述信息（选填，最大 200 字）"
                        rows={3}
                        disabled={isSubmitting}
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
            <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
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
