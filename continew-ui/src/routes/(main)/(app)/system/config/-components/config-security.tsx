import { z } from 'zod'

import { InputGroupText } from '@/components/ui/input-group'
import { usePermission } from '@/hooks/use-permission'

import { ConfigPanelHeader } from './config-panel-header'
import { ConfigPanelError, ConfigPanelSkeleton } from './config-panel-states'
import { ConfigResetDialog } from './config-reset-dialog'
import { SwitchField } from './switch-field'
import { TextField } from './text-field'
import { type OptionMap, useOptionForm } from './use-option-form'

const ranged = (label: string, min: number, max: number, unit: string) =>
  z.string().superRefine((value, ctx) => {
    const text = value.trim()
    if (!text) {
      ctx.addIssue({ code: 'custom', message: `请输入${label}` })
      return
    }
    if (!/^\d+$/.test(text)) {
      ctx.addIssue({ code: 'custom', message: `${label}只能包含数字` })
      return
    }
    const parsed = Number(text)
    if (parsed < min || parsed > max) {
      ctx.addIssue({ code: 'custom', message: `${label}应在 ${min}-${max} ${unit}之间` })
    }
  })

const fields = z.object({
  PASSWORD_ERROR_LOCK_COUNT: ranged('密码错误锁定阈值', 0, 10, '次'),
  PASSWORD_ERROR_LOCK_MINUTES: ranged('账号锁定时长', 1, 1440, '分钟'),
  PASSWORD_EXPIRATION_DAYS: ranged('密码有效期', 0, 999, '天'),
  PASSWORD_EXPIRATION_WARNING_DAYS: ranged('密码到期提醒', 0, 998, '天'),
  PASSWORD_REPETITION_TIMES: ranged('历史密码重复校验次数', 3, 32, '次'),
  PASSWORD_MIN_LENGTH: ranged('密码最小长度', 8, 32, '个字符'),
  PASSWORD_ALLOW_CONTAIN_USERNAME: z.number().int().min(0).max(1),
  PASSWORD_REQUIRE_SYMBOLS: z.number().int().min(0).max(1)
})

const schema = fields.refine(
  value => {
    const days = Number(value.PASSWORD_EXPIRATION_DAYS)
    const warning = Number(value.PASSWORD_EXPIRATION_WARNING_DAYS)
    return !days || warning < days
  },
  { message: '密码到期提醒时间应小于密码有效期', path: ['PASSWORD_EXPIRATION_WARNING_DAYS'] }
)

type SecurityFormState = z.infer<typeof fields>

type NumberFieldName =
  | 'PASSWORD_ERROR_LOCK_COUNT'
  | 'PASSWORD_ERROR_LOCK_MINUTES'
  | 'PASSWORD_EXPIRATION_DAYS'
  | 'PASSWORD_EXPIRATION_WARNING_DAYS'
  | 'PASSWORD_REPETITION_TIMES'
  | 'PASSWORD_MIN_LENGTH'

function toForm(optionMap: OptionMap): SecurityFormState {
  return {
    PASSWORD_ERROR_LOCK_COUNT: optionMap.PASSWORD_ERROR_LOCK_COUNT?.value || '5',
    PASSWORD_ERROR_LOCK_MINUTES: optionMap.PASSWORD_ERROR_LOCK_MINUTES?.value || '5',
    PASSWORD_EXPIRATION_DAYS: optionMap.PASSWORD_EXPIRATION_DAYS?.value || '0',
    PASSWORD_EXPIRATION_WARNING_DAYS: optionMap.PASSWORD_EXPIRATION_WARNING_DAYS?.value || '0',
    PASSWORD_REPETITION_TIMES: optionMap.PASSWORD_REPETITION_TIMES?.value || '3',
    PASSWORD_MIN_LENGTH: optionMap.PASSWORD_MIN_LENGTH?.value || '8',
    PASSWORD_ALLOW_CONTAIN_USERNAME: Number.parseInt(optionMap.PASSWORD_ALLOW_CONTAIN_USERNAME?.value || '1', 10),
    PASSWORD_REQUIRE_SYMBOLS: Number.parseInt(optionMap.PASSWORD_REQUIRE_SYMBOLS?.value || '0', 10)
  }
}

const isRequired = (field: keyof SecurityFormState) => !fields.shape[field].safeParse('').success

export function ConfigSecurity() {
  const { hasPermission } = usePermission()
  const canUpdate = hasPermission('system:securityConfig:update')

  const {
    form,
    formProps,
    optionMap,
    isLoading,
    isError,
    refetch,
    isSubmitting,
    isEdit,
    setIsEdit,
    cancelEdit,
    resetForm,
    resetToDefault,
    isResetting,
    resetDialogOpen,
    setResetDialogOpen
  } = useOptionForm({ category: 'PASSWORD', toForm, schema })

  /** The numeric policies differ only in their label, range and unit, which the schema already owns. */
  const numberField = (name: NumberFieldName, min: number, max: number, unit?: string) => (
    <form.Field name={name} validators={{ onSubmit: fields.shape[name] }}>
      {field => (
        <TextField
          field={field}
          label={optionMap[name]?.name}
          description={optionMap[name]?.description}
          type="number"
          min={min}
          max={max}
          required={isRequired(name)}
          disabled={!isEdit}
          addonEnd={unit && <InputGroupText className="text-xs font-normal">{unit}</InputGroupText>}
        />
      )}
    </form.Field>
  )

  if (isLoading) {
    return <ConfigPanelSkeleton fields={6} />
  }

  if (isError) {
    return <ConfigPanelError onRetry={() => void refetch()} />
  }

  return (
    <form {...formProps} className="flex flex-col gap-6">
      <ConfigPanelHeader
        title="安全策略配置"
        description="配置密码复杂度、重试锁定与密码过期提醒策略"
        canUpdate={canUpdate}
        isEdit={isEdit}
        isSubmitting={isSubmitting}
        onEdit={() => setIsEdit(true)}
        onResetDefault={() => setResetDialogOpen(true)}
        onResetForm={resetForm}
        onCancel={cancelEdit}
      />

      <div className="flex flex-col gap-6">
        <div className="grid grid-cols-1 items-start gap-6 sm:grid-cols-2">
          {numberField('PASSWORD_ERROR_LOCK_COUNT', 0, 10, '次')}
          {numberField('PASSWORD_ERROR_LOCK_MINUTES', 1, 1440, '分钟')}
          {numberField('PASSWORD_EXPIRATION_DAYS', 0, 999, '天')}
          {numberField('PASSWORD_EXPIRATION_WARNING_DAYS', 0, 998, '天')}
          {numberField('PASSWORD_REPETITION_TIMES', 3, 32, '次')}
          {numberField('PASSWORD_MIN_LENGTH', 8, 32)}
        </div>

        <div className="grid grid-cols-1 gap-6 pt-2 sm:grid-cols-2">
          <form.Field
            name="PASSWORD_ALLOW_CONTAIN_USERNAME"
            validators={{ onSubmit: fields.shape.PASSWORD_ALLOW_CONTAIN_USERNAME }}
          >
            {field => (
              <SwitchField
                field={field}
                label={optionMap.PASSWORD_ALLOW_CONTAIN_USERNAME?.name}
                description={optionMap.PASSWORD_ALLOW_CONTAIN_USERNAME?.description}
                disabled={!isEdit}
              />
            )}
          </form.Field>

          <form.Field name="PASSWORD_REQUIRE_SYMBOLS" validators={{ onSubmit: fields.shape.PASSWORD_REQUIRE_SYMBOLS }}>
            {field => (
              <SwitchField
                field={field}
                label={optionMap.PASSWORD_REQUIRE_SYMBOLS?.name}
                description={optionMap.PASSWORD_REQUIRE_SYMBOLS?.description}
                disabled={!isEdit}
              />
            )}
          </form.Field>
        </div>
      </div>

      <ConfigResetDialog
        open={resetDialogOpen}
        onOpenChange={setResetDialogOpen}
        onConfirm={resetToDefault}
        isResetting={isResetting}
        description="确认恢复安全配置为系统默认值吗？该操作不可撤销。"
      />
    </form>
  )
}
