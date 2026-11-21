import { z } from 'zod'

import { usePermission } from '@/hooks/use-permission'

import { ConfigPanelError, ConfigPanelSkeleton } from './config-panel-states'
import { ConfigPanelHeader } from './config-panel-header'
import { ConfigResetDialog } from './config-reset-dialog'
import { SwitchField } from './switch-field'
import { type OptionMap, useOptionForm } from './use-option-form'

const schema = z.object({
  LOGIN_CAPTCHA_ENABLED: z.number().int().min(0).max(1)
})

type LoginFormState = z.infer<typeof schema>

function toForm(optionMap: OptionMap): LoginFormState {
  return {
    LOGIN_CAPTCHA_ENABLED: Number.parseInt(optionMap.LOGIN_CAPTCHA_ENABLED?.value || '1', 10)
  }
}

export function ConfigLogin() {
  const { hasPermission } = usePermission()
  const canUpdate = hasPermission('system:loginConfig:update')

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
  } = useOptionForm({ category: 'LOGIN', toForm, schema })

  if (isLoading) {
    return <ConfigPanelSkeleton fields={1} />
  }

  if (isError) {
    return <ConfigPanelError onRetry={() => void refetch()} />
  }

  return (
    <form {...formProps} className="flex flex-col gap-6">
      <ConfigPanelHeader
        title="登录配置"
        description="配置图形验证码与登录行为策略"
        canUpdate={canUpdate}
        isEdit={isEdit}
        isSubmitting={isSubmitting}
        onEdit={() => setIsEdit(true)}
        onResetDefault={() => setResetDialogOpen(true)}
        onResetForm={resetForm}
        onCancel={cancelEdit}
      />

      <div className="max-w-xl">
        <form.Field name="LOGIN_CAPTCHA_ENABLED" validators={{ onSubmit: schema.shape.LOGIN_CAPTCHA_ENABLED }}>
          {field => (
            <SwitchField
              field={field}
              label={optionMap.LOGIN_CAPTCHA_ENABLED?.name}
              description="开启后，用户在后台登录时需要输入图形验证码以防止暴力破解"
              disabled={!isEdit}
            />
          )}
        </form.Field>
      </div>

      <ConfigResetDialog
        open={resetDialogOpen}
        onOpenChange={setResetDialogOpen}
        onConfirm={resetToDefault}
        isResetting={isResetting}
        description="确认恢复登录配置为系统默认值吗？该操作不可撤销。"
      />
    </form>
  )
}
