import * as React from 'react'

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { type StandardSchemaV1, useForm, useSelector } from '@tanstack/react-form'

import { toast } from '@/components/ui/toast'
import { listOption, type OptionReq, type OptionResp, resetOptionValue, updateOption } from '@/lib/api/option'
import { reportError } from '@/lib/http'
import { optionKeys, siteOptionKeys } from '@/lib/query-keys'

export type OptionMap = Record<string, OptionResp>

const EMPTY_OPTIONS: OptionResp[] = []

/**
 * Codes missing from `optionMap` are skipped: the backend resolves every entry by `id` and rejects
 * the batch otherwise.
 */
function buildPayload(values: object, optionMap: OptionMap): OptionReq[] {
  const payload: OptionReq[] = []
  for (const [code, value] of Object.entries(values)) {
    const option = optionMap[code]
    if (!option) continue
    payload.push({ id: option.id, code, value: String(value) })
  }
  return payload
}

interface UseOptionFormOptions<TForm extends object> {
  category: string
  toForm: (optionMap: OptionMap) => TForm
  /**
   * The whole-object schema, validated on submit.
   *
   * Every `<form.Field>` must also carry `validators={{ onSubmit: schema.shape.X }}`: `handleSubmit`
   * runs the field validators first and returns as soon as they report an invalid form, so a rule
   * that only sits on the form never runs again and fields that turn invalid later show no error.
   */
  schema: StandardSchemaV1<TForm>
}

export function useOptionForm<TForm extends object>({ category, toForm, schema }: UseOptionFormOptions<TForm>) {
  const queryClient = useQueryClient()

  // Site options (title/logo/copyright/beian) are read by the shell from a separate public endpoint
  // and cached in localStorage, so saving the SITE category must also refresh that query; otherwise
  // the sidebar and auth screens keep showing the old values until a reload.
  const refreshSiteOptions = () => {
    if (category === 'SITE') {
      void queryClient.invalidateQueries({ queryKey: siteOptionKeys.all })
    }
  }

  const [isEdit, setIsEdit] = React.useState(false)
  const [resetDialogOpen, setResetDialogOpen] = React.useState(false)

  const query = useQuery({
    queryKey: optionKeys.list(category),
    queryFn: ({ signal }) => listOption({ category }, signal)
  })
  const options = query.data ?? EMPTY_OPTIONS

  const optionMap = React.useMemo(() => {
    const map: OptionMap = {}
    for (const option of options) {
      map[option.code] = option
    }
    return map
  }, [options])

  const form = useForm({
    defaultValues: toForm({}),
    validators: { onSubmit: schema },
    onSubmit: async ({ value }) => {
      try {
        await updateOption(buildPayload(value, optionMap))
        toast.add({ title: '保存成功', type: 'success' })
        setIsEdit(false)
        void queryClient.invalidateQueries({ queryKey: optionKeys.list(category) })
        refreshSiteOptions()
      } catch (err) {
        reportError(err, '保存失败')
      }
    }
  })

  // `keepDefaultValues` is load-bearing: `reset(values)` otherwise rewrites the form's own
  // defaultValues, and the next render's `update(opts)` reads that as "defaults changed" and puts the
  // `toForm({})` fallbacks back into the form, which the panel would then save to the backend.
  React.useEffect(() => {
    if (!isEdit && options.length > 0) {
      form.reset(toForm(optionMap), { keepDefaultValues: true })
    }
  }, [form, isEdit, optionMap, options.length, toForm])

  const resetMutation = useMutation({
    mutationFn: () => resetOptionValue({ category }),
    onSuccess: () => {
      toast.add({ title: '恢复成功', type: 'success' })
      setResetDialogOpen(false)
      setIsEdit(false)
      void queryClient.invalidateQueries({ queryKey: optionKeys.list(category) })
      refreshSiteOptions()
    },
    onError: err => reportError(err, '恢复失败')
  })

  const resetForm = () => form.reset(toForm(optionMap), { keepDefaultValues: true })

  const cancelEdit = () => {
    resetForm()
    setIsEdit(false)
  }

  const resetToDefault = () => resetMutation.mutate()

  /**
   * Validates the whole object before submitting: `handleSubmit` returns as soon as the field
   * validators report an invalid form, so a form-level rule (a cross-field one, say) would otherwise
   * stay hidden until the other errors were fixed.
   */
  const submit = async () => {
    await form.validate('submit')
    await form.handleSubmit()
  }

  /** The panel owns the form's layout; the hook owns the submit wiring. */
  const formProps = {
    noValidate: true,
    onSubmit: (event: React.FormEvent<HTMLFormElement>) => {
      event.preventDefault()
      void submit()
    }
  }

  return {
    form,
    formProps,
    optionMap,
    isLoading: query.isLoading,
    isError: query.isError,
    refetch: query.refetch,
    isSubmitting: useSelector(form.store, state => state.isSubmitting),
    isEdit,
    setIsEdit,
    cancelEdit,
    resetForm,
    submit,
    resetToDefault,
    isResetting: resetMutation.isPending,
    resetDialogOpen,
    setResetDialogOpen
  }
}
