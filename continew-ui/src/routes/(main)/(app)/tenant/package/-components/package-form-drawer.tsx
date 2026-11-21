import * as React from 'react'

import { revalidateLogic, useForm } from '@tanstack/react-form'
import { useQueryClient } from '@tanstack/react-query'
import { z } from 'zod'

import { FolderTree } from 'lucide-react'

import { CheckableTree } from '@/components/checkable-tree'
import { EntityDrawer } from '@/components/entity-drawer'
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
import { buildTreeIndex } from '@/components/ui/tree'
import { useFillFormOnce } from '@/hooks/use-fill-form-once'
import {
  createPackage,
  packageKeys,
  updatePackage,
  type PackageDetailResp,
  type PackageMenuNode,
  type PackageReq
} from '@/lib/api/package'
import { tenantKeys } from '@/lib/api/tenant'

import { toMenuTreeNodes, usePackageMenuTree } from './use-package-menu-tree'
import { usePackageDetail } from './use-package-detail'

const formSchema = z.object({
  name: z.string().trim().min(1, '请输入名称').max(30, '名称长度不能超过 30 个字符'),
  sort: z.string().refine(value => /^\d+$/.test(value) && Number(value) >= 1, '请输入大于 0 的整数'),
  description: z.string().max(200, '描述长度不能超过 200 个字符'),
  status: z.union([z.literal(1), z.literal(2)]),
  menuCheckStrictly: z.boolean()
})

type FormValues = z.infer<typeof formSchema>

const EMPTY_FORM: FormValues = {
  name: '',
  sort: '999',
  description: '',
  status: 1,
  menuCheckStrictly: true
}

/** `sort` becomes a string so it can drive a text input. */
function toFormValues(pkg: PackageDetailResp): FormValues {
  return {
    name: pkg.name ?? '',
    sort: pkg.sort === null || pkg.sort === undefined ? '999' : String(pkg.sort),
    description: pkg.description ?? '',
    status: pkg.status ?? 1,
    menuCheckStrictly: pkg.menuCheckStrictly ?? true
  }
}

/** Keeps the ids the backend sent, so the request does not turn every number into a string. */
function collectRawMenuIds(nodes: PackageMenuNode[], ids = new Map<string, string | number>()) {
  for (const node of nodes) {
    ids.set(String(node.key), node.key)
    if (node.children?.length) {
      collectRawMenuIds(node.children, ids)
    }
  }
  return ids
}

function PackageFormSkeleton() {
  return (
    <div className="flex min-h-0 flex-1 flex-col">
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-4">
        <div className="flex flex-col gap-4">
          {Array.from({ length: 4 }, (_, index) => (
            <div key={index} className="flex flex-col gap-1.5">
              <Skeleton className="h-4 w-16" />
              <Skeleton className="h-9 w-full" />
            </div>
          ))}
          <Skeleton className="h-48 w-full" />
        </div>
      </div>
    </div>
  )
}

interface PackageFormDrawerProps {
  /** The package being edited, or `null` to create one. */
  readonly packageId: string | number | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Create and edit form for one package, including the menus it grants. */
export function PackageFormDrawer({ packageId, open, onClose }: PackageFormDrawerProps) {
  const queryClient = useQueryClient()
  const formRef = React.useRef<HTMLFormElement>(null)
  const isCreate = packageId === null

  const detail = usePackageDetail(packageId, { enabled: open && !isCreate })

  const menuTree = usePackageMenuTree(open)

  const treeNodes = React.useMemo(() => toMenuTreeNodes(menuTree.data ?? []), [menuTree.data])
  const treeIndex = React.useMemo(() => buildTreeIndex(treeNodes), [treeNodes])
  const rawMenuIds = React.useMemo(() => collectRawMenuIds(menuTree.data ?? []), [menuTree.data])

  const [checkedKeys, setCheckedKeys] = React.useState<Array<string | number>>([])
  const [menuError, setMenuError] = React.useState<string | null>(null)

  /** The checked ids plus every ancestor they need, in the ids the backend sent. */
  const selectedMenuIds = React.useCallback(() => {
    const keys = new Set(checkedKeys.map(String))

    for (const key of keys) {
      for (const ancestor of treeIndex.ancestorsMap.get(key) ?? []) {
        keys.add(String(ancestor))
      }
    }

    return [...keys].map(key => rawMenuIds.get(key) ?? key)
  }, [checkedKeys, treeIndex, rawMenuIds])

  const form = useForm({
    defaultValues: EMPTY_FORM,
    validationLogic: revalidateLogic(),
    validators: { onDynamic: formSchema },
    onSubmit: async ({ value }) => {
      const body: PackageReq = {
        name: value.name.trim(),
        sort: Number(value.sort),
        menuCheckStrictly: value.menuCheckStrictly,
        description: value.description.trim() || undefined,
        status: value.status,
        menuIds: selectedMenuIds()
      }

      try {
        if (packageId === null) {
          await createPackage(body)
        } else {
          await updatePackage(packageId, body)
        }

        onClose()
        void queryClient.invalidateQueries({ queryKey: packageKeys.all })
        // The tenant list shows the package name, so it goes stale with the package.
        void queryClient.invalidateQueries({ queryKey: tenantKeys.all })
        toast.add({ title: packageId === null ? '新增成功' : '修改成功', type: 'success' })
      } catch {
        // The HTTP layer already reported the failure.
      }
    }
  })

  useFillFormOnce({
    open,
    isEdit: !isCreate,
    entityKey: packageId,
    record: detail.data,
    version: detail.dataUpdatedAt,
    buildValues: ({ isEdit: editing, record }) => (editing && record ? toFormValues(record) : EMPTY_FORM),
    apply: values => form.reset(values, { keepDefaultValues: true }),
    onFilled: ({ isEdit: editing, record }) => {
      setCheckedKeys(editing && record ? (record.menuIds ?? []).map(String) : [])
      setMenuError(null)
    },
    resetOnClose: false
  })

  const handleCheckedChange = (keys: Array<string | number>) => {
    setCheckedKeys(keys)
    setMenuError(null)
  }

  const submit = async () => {
    const menuInvalid = checkedKeys.length === 0
    setMenuError(menuInvalid ? '请选择关联菜单' : null)

    await form.validate('submit')
    if (menuInvalid) {
      return
    }

    await form.handleSubmit()

    requestAnimationFrame(() => {
      formRef.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
    })
  }

  const renderTextField = (
    name: 'name' | 'sort',
    label: string,
    input: { type?: 'number'; min?: number; maxLength?: number; placeholder: string; required?: boolean }
  ) => (
    <form.Field name={name} validators={{ onDynamic: formSchema.shape[name] }}>
      {field => {
        const invalid = field.state.meta.errors.length > 0

        return (
          <Field className="gap-1.5" data-invalid={invalid}>
            <FieldLabel>
              {label}
              {input.required && <RequiredMark />}
            </FieldLabel>
            <Input
              type={input.type}
              min={input.min}
              name={name}
              value={field.state.value}
              onChange={event => field.handleChange(event.target.value)}
              onBlur={field.handleBlur}
              maxLength={input.maxLength}
              placeholder={input.placeholder}
              aria-invalid={invalid}
            />
            <FieldError errors={field.state.meta.errors} />
          </Field>
        )
      }}
    </form.Field>
  )

  return (
    <EntityDrawer
      open={open}
      onClose={onClose}
      title={isCreate ? '新增套餐' : '修改套餐'}
      icon={<FolderTree className="size-4" />}
      width={36}
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
              {renderTextField('name', '名称', { maxLength: 30, placeholder: '请输入名称', required: true })}

              {renderTextField('sort', '排序', { type: 'number', min: 1, placeholder: '请输入排序' })}

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
                        {field.state.value === 1 ? '启用后租户可以选择该套餐' : '禁用后租户不能再选择该套餐'}
                      </FieldDescription>
                    </FieldContent>
                    <Switch
                      checked={field.state.value === 1}
                      onCheckedChange={checked => field.handleChange(checked ? 1 : 2)}
                    />
                  </Field>
                )}
              </form.Field>

              <form.Field name="menuCheckStrictly">
                {field => (
                  <Field className="gap-2" data-invalid={Boolean(menuError)}>
                    <div className="flex items-center justify-between gap-2">
                      <FieldLabel>
                        关联菜单
                        <RequiredMark />
                      </FieldLabel>
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-muted-foreground">父子联动</span>
                        <Switch checked={field.state.value} onCheckedChange={field.handleChange} />
                      </div>
                    </div>

                    <CheckableTree
                      data={treeNodes}
                      checkedKeys={checkedKeys}
                      onCheckedChange={handleCheckedChange}
                      checkStrictly={!field.state.value}
                      loading={menuTree.isPending}
                      searchPlaceholder="搜索菜单或权限名"
                      emptyText="暂无菜单"
                    />

                    {menuError ? (
                      <FieldError>{menuError}</FieldError>
                    ) : (
                      <FieldDescription>勾选的菜单会授权给该套餐下的所有租户</FieldDescription>
                    )}
                  </Field>
                )}
              </form.Field>
            </FieldGroup>
          </div>

          <DrawerFooter>
            <Button type="button" variant="outline" disabled={form.state.isSubmitting} onClick={onClose}>
              取消
            </Button>
            <form.Subscribe selector={state => state.isSubmitting}>
              {isSubmitting => (
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
              )}
            </form.Subscribe>
          </DrawerFooter>
        </form>
      ) : detail.isError ? (
        <QueryState state="error" className="p-6" onRetry={() => void detail.refetch()} />
      ) : (
        <PackageFormSkeleton />
      )}
    </EntityDrawer>
  )
}
