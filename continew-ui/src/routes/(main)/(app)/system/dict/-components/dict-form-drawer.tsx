import { revalidateLogic, useForm } from '@tanstack/react-form'
import { useQueryClient } from '@tanstack/react-query'

import { Bookmark } from 'lucide-react'

import { z } from 'zod'

import { EntityDrawer } from '@/components/entity-drawer'
import { RequiredMark } from '@/components/required-mark'
import { Button } from '@/components/ui/button'
import { DrawerFooter } from '@/components/ui/drawer'
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
import { Textarea } from '@/components/ui/textarea'
import { toast } from '@/components/ui/toast'
import { useFillFormOnce } from '@/hooks/use-fill-form-once'
import { createDict, dictKeys, updateDict, type DictResp } from '@/lib/api/dict'

const NAME_PATTERN = /^[\u4e00-\u9fa5a-zA-Z0-9_-]{2,30}$/
const CODE_PATTERN = /^[a-zA-Z][a-zA-Z0-9_]{1,29}$/

const formSchema = z.object({
  name: z.string().trim().regex(NAME_PATTERN, '名称长度为 2-30 个字符，支持中文、字母、数字、下划线、短横线'),
  code: z.string().trim().regex(CODE_PATTERN, '编码长度为 2-30 个字符，以字母开头，支持字母、数字、下划线'),
  description: z.string().trim().max(200, '描述长度不能超过 200 个字符')
})

type DictFormValues = z.infer<typeof formSchema>

const EMPTY_FORM: DictFormValues = {
  name: '',
  code: '',
  description: ''
}

interface DictFormDrawerProps {
  /** The dictionary being edited, or `null` to create one. */
  readonly dict: DictResp | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Creates a dictionary or edits its name and description; the code cannot change after creation. */
export function DictFormDrawer({ dict, open, onClose }: DictFormDrawerProps) {
  const queryClient = useQueryClient()
  const isEdit = dict !== null
  const submitLabel = isEdit ? '保存' : '创建'

  const form = useForm({
    defaultValues: EMPTY_FORM,
    validators: { onDynamic: formSchema },
    validationLogic: revalidateLogic(),
    onSubmit: async ({ value }) => {
      const body = {
        name: value.name.trim(),
        code: value.code.trim(),
        description: value.description.trim()
      }

      try {
        if (dict) {
          await updateDict(dict.id, body)
        } else {
          await createDict(body)
        }

        toast.add({ title: dict ? '修改成功' : '新增成功', type: 'success' })
        void queryClient.invalidateQueries({ queryKey: dictKeys.lists() })
        onClose()
      } catch {
        // The HTTP layer already reported the failure.
      }
    }
  })

  useFillFormOnce({
    open,
    isEdit,
    entityKey: dict?.id,
    record: dict ?? undefined,
    buildValues: ({ isEdit: editing, record }) =>
      editing && record ? { name: record.name, code: record.code, description: record.description ?? '' } : EMPTY_FORM,
    apply: values => form.reset(values, { keepDefaultValues: true }),
    resetOnClose: false
  })

  return (
    <EntityDrawer
      open={open}
      onClose={onClose}
      title={isEdit ? '修改字典' : '新增字典'}
      icon={<Bookmark className="size-4" />}
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
                      maxLength={30}
                      autoComplete="off"
                      placeholder="请输入字典名称"
                      aria-invalid={invalid}
                      onChange={event => field.handleChange(event.target.value)}
                      onBlur={field.handleBlur}
                    />
                    <FieldError errors={field.state.meta.errors} />
                  </Field>
                )
              }}
            </form.Field>

            <form.Field name="code">
              {field => {
                const invalid = field.state.meta.errors.length > 0

                return (
                  <Field className="gap-1.5" data-invalid={invalid}>
                    <FieldLabel>
                      编码
                      <RequiredMark />
                    </FieldLabel>
                    <Input
                      name="code"
                      value={field.state.value}
                      maxLength={30}
                      autoComplete="off"
                      disabled={isEdit}
                      placeholder="请输入字典编码"
                      aria-invalid={invalid}
                      onChange={event => field.handleChange(event.target.value)}
                      onBlur={field.handleBlur}
                    />
                    {isEdit ? <FieldDescription>编码创建后不可修改</FieldDescription> : null}
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
