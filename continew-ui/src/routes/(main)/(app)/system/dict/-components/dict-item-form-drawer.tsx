import { revalidateLogic, useForm } from '@tanstack/react-form'
import { useQueryClient } from '@tanstack/react-query'

import { ListChecks } from 'lucide-react'

import { z } from 'zod'

import { cn } from 'cn'

import { EntityDrawer } from '@/components/entity-drawer'
import { RequiredMark } from '@/components/required-mark'
import { Button } from '@/components/ui/button'
import { DrawerFooter } from '@/components/ui/drawer'
import { Field, FieldContent, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Spinner } from '@/components/ui/spinner'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import { toast } from '@/components/ui/toast'
import { useFillFormOnce } from '@/hooks/use-fill-form-once'
import { createDictItem, dictKeys, updateDictItem, type DictItemReq, type DictItemResp } from '@/lib/api/dict'

import { DICT_COLOR_DOT_CLASSES, DICT_COLOR_OPTIONS } from './dict-display'

const formSchema = z.object({
  label: z.string().trim().min(1, '请输入标签').max(30, '标签长度不能超过 30 个字符'),
  value: z.string().trim().min(1, '请输入值').max(30, '值长度不能超过 30 个字符'),
  color: z.string(),
  sort: z
    .string()
    .trim()
    .regex(/^[1-9]\d*$/, '排序必须是不小于 1 的整数'),
  description: z.string().trim().max(200, '描述长度不能超过 200 个字符'),
  status: z.union([z.literal(1), z.literal(2)])
})

type DictItemFormValues = z.infer<typeof formSchema>

const EMPTY_FORM: DictItemFormValues = {
  label: '',
  value: '',
  color: 'none',
  sort: '999',
  description: '',
  status: 1
}

interface DictItemFormDrawerProps {
  /** The dictionary item being edited, or `null` to create one. */
  readonly item: DictItemResp | null
  readonly dictId: string | number
  readonly open: boolean
  readonly onClose: () => void
}

/** Creates or edits an item of the selected dictionary. */
export function DictItemFormDrawer({ item, dictId, open, onClose }: DictItemFormDrawerProps) {
  const queryClient = useQueryClient()
  const isEdit = item !== null
  const submitLabel = isEdit ? '保存' : '创建'

  const form = useForm({
    defaultValues: EMPTY_FORM,
    validators: { onDynamic: formSchema },
    validationLogic: revalidateLogic(),
    onSubmit: async ({ value }) => {
      const body: DictItemReq = {
        label: value.label.trim(),
        value: value.value.trim(),
        color: value.color === 'none' ? undefined : value.color,
        sort: Number(value.sort),
        description: value.description.trim(),
        status: value.status,
        dictId: item?.dictId ?? dictId
      }

      try {
        if (item) {
          await updateDictItem(item.id, body)
        } else {
          await createDictItem(body)
        }

        toast.add({ title: item ? '修改成功' : '新增成功', type: 'success' })
        void queryClient.invalidateQueries({ queryKey: dictKeys.items() })
        onClose()
      } catch {
        // The HTTP layer already reported the failure.
      }
    }
  })

  useFillFormOnce({
    open,
    isEdit,
    entityKey: item?.id,
    createKey: dictId,
    record: item ?? undefined,
    buildValues: ({ isEdit: editing, record }) =>
      editing && record
        ? {
            label: record.label,
            value: record.value,
            color: record.color ?? 'none',
            sort: String(record.sort),
            description: record.description ?? '',
            status: record.status
          }
        : EMPTY_FORM,
    apply: values => form.reset(values, { keepDefaultValues: true }),
    resetOnClose: false
  })

  return (
    <EntityDrawer
      open={open}
      onClose={onClose}
      title={isEdit ? '修改字典项' : '新增字典项'}
      icon={<ListChecks className="size-4" />}
      width={32}
      canClose={() => !form.state.isSubmitting}
    >
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
            <form.Field name="label">
              {field => {
                const invalid = field.state.meta.errors.length > 0

                return (
                  <Field className="gap-1.5" data-invalid={invalid}>
                    <FieldLabel>
                      标签
                      <RequiredMark />
                    </FieldLabel>
                    <Input
                      name="label"
                      value={field.state.value}
                      maxLength={30}
                      autoComplete="off"
                      placeholder="请输入标签"
                      aria-invalid={invalid}
                      onChange={event => field.handleChange(event.target.value)}
                      onBlur={field.handleBlur}
                    />
                    <FieldError errors={field.state.meta.errors} />
                  </Field>
                )
              }}
            </form.Field>

            <form.Field name="value">
              {field => {
                const invalid = field.state.meta.errors.length > 0

                return (
                  <Field className="gap-1.5" data-invalid={invalid}>
                    <FieldLabel>
                      值
                      <RequiredMark />
                    </FieldLabel>
                    <Input
                      name="value"
                      value={field.state.value}
                      maxLength={30}
                      autoComplete="off"
                      placeholder="请输入值"
                      aria-invalid={invalid}
                      onChange={event => field.handleChange(event.target.value)}
                      onBlur={field.handleBlur}
                    />
                    <FieldError errors={field.state.meta.errors} />
                  </Field>
                )
              }}
            </form.Field>

            <form.Field name="color">
              {field => (
                <Field className="gap-1.5">
                  <FieldLabel>颜色</FieldLabel>
                  <Select
                    items={DICT_COLOR_OPTIONS}
                    value={field.state.value}
                    onValueChange={value => field.handleChange(value ?? 'none')}
                  >
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="请选择颜色" />
                    </SelectTrigger>
                    <SelectContent alignItemWithTrigger={false}>
                      <SelectGroup>
                        {DICT_COLOR_OPTIONS.map(option => (
                          <SelectItem key={option.value} value={option.value}>
                            {option.value === 'none' ? (
                              option.label
                            ) : (
                              <span className="flex items-center gap-2">
                                <span className={cn('size-2 rounded-full', DICT_COLOR_DOT_CLASSES[option.value])} />
                                {option.label}
                              </span>
                            )}
                          </SelectItem>
                        ))}
                      </SelectGroup>
                    </SelectContent>
                  </Select>
                  <FieldDescription>仅影响列表里标签的展示颜色</FieldDescription>
                </Field>
              )}
            </form.Field>

            <form.Field name="sort">
              {field => {
                const invalid = field.state.meta.errors.length > 0

                return (
                  <Field className="gap-1.5" data-invalid={invalid}>
                    <FieldLabel>
                      排序
                      <RequiredMark />
                    </FieldLabel>
                    <Input
                      name="sort"
                      type="number"
                      min={1}
                      step={1}
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

            <form.Field name="description">
              {field => {
                const invalid = field.state.meta.errors.length > 0

                return (
                  <Field className="gap-1.5 sm:col-span-2" data-invalid={invalid}>
                    <FieldLabel>描述</FieldLabel>
                    <Textarea
                      name="description"
                      value={field.state.value}
                      rows={3}
                      maxLength={200}
                      placeholder="请输入描述"
                      aria-invalid={invalid}
                      onChange={event => field.handleChange(event.target.value)}
                      onBlur={field.handleBlur}
                    />
                    <FieldError errors={field.state.meta.errors} />
                  </Field>
                )
              }}
            </form.Field>

            <form.Field name="status">
              {field => {
                const invalid = field.state.meta.errors.length > 0

                return (
                  <Field className="gap-1.5 sm:col-span-2" orientation="horizontal" data-invalid={invalid}>
                    <FieldContent>
                      <FieldLabel>状态</FieldLabel>
                      <FieldDescription>
                        {field.state.value === 1 ? '启用后该字典项会返回给前端' : '禁用后该字典项不会返回给前端'}
                      </FieldDescription>
                    </FieldContent>
                    <Switch
                      checked={field.state.value === 1}
                      onCheckedChange={checked => field.handleChange(checked ? 1 : 2)}
                      aria-invalid={invalid}
                    />
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
                      {submitLabel}中
                    </>
                  ) : (
                    submitLabel
                  )}
                </Button>
              </>
            )}
          </form.Subscribe>
        </DrawerFooter>
      </form>
    </EntityDrawer>
  )
}
