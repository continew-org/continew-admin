import { z } from 'zod'

import { usePermission } from '@/hooks/use-permission'

import { ConfigPanelHeader } from './config-panel-header'
import { ConfigPanelError, ConfigPanelSkeleton } from './config-panel-states'
import { ConfigResetDialog } from './config-reset-dialog'
import { ImageUploadField } from './image-upload-field'
import { SitePreview } from './site-preview'
import { TextField } from './text-field'
import { type OptionMap, useOptionForm } from './use-option-form'

const nonBlank = (message: string) => z.string().trim().min(1, message)

const schema = z.object({
  SITE_LOGO: z.string(),
  SITE_FAVICON: z.string(),
  SITE_TITLE: nonBlank('请输入系统名称').max(18, '系统名称最多 18 个字符'),
  SITE_DESCRIPTION: nonBlank('请输入系统描述'),
  SITE_COPYRIGHT: nonBlank('请输入版权声明'),
  SITE_BEIAN: z.string().max(30, '备案号最多 30 个字符')
})

type SiteFormState = z.infer<typeof schema>

function toForm(optionMap: OptionMap): SiteFormState {
  return {
    SITE_LOGO: optionMap.SITE_LOGO?.value || '',
    SITE_FAVICON: optionMap.SITE_FAVICON?.value || '',
    SITE_TITLE: optionMap.SITE_TITLE?.value || '',
    SITE_DESCRIPTION: optionMap.SITE_DESCRIPTION?.value || '',
    SITE_COPYRIGHT: optionMap.SITE_COPYRIGHT?.value || '',
    SITE_BEIAN: optionMap.SITE_BEIAN?.value || ''
  }
}

const isRequired = (field: keyof SiteFormState) => !schema.shape[field].safeParse('').success

export function ConfigSite() {
  const { hasPermission } = usePermission()
  const canUpdate = hasPermission('system:siteConfig:update')

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
  } = useOptionForm({ category: 'SITE', toForm, schema })

  if (isLoading) {
    return <ConfigPanelSkeleton variant="card" />
  }

  if (isError) {
    return <ConfigPanelError onRetry={() => void refetch()} />
  }

  return (
    <>
      <form {...formProps} className="grid grid-cols-1 gap-5 xl:grid-cols-2">
        <div className="flex flex-col gap-4 rounded-xl border bg-card p-4 sm:p-5">
          <ConfigPanelHeader
            title="网站基础配置"
            description="设置系统的名称、Logo、图标与版权等基础信息"
            canUpdate={canUpdate}
            isEdit={isEdit}
            isSubmitting={isSubmitting}
            onEdit={() => setIsEdit(true)}
            onResetDefault={() => setResetDialogOpen(true)}
            onResetForm={resetForm}
            onCancel={cancelEdit}
            variant="card"
          />

          <div className="flex flex-col gap-3.5">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <form.Field name="SITE_LOGO" validators={{ onSubmit: schema.shape.SITE_LOGO }}>
                {field => (
                  <ImageUploadField
                    label={optionMap.SITE_LOGO?.name}
                    description={optionMap.SITE_LOGO?.description}
                    value={field.state.value}
                    onChange={field.handleChange}
                    disabled={!isEdit}
                  />
                )}
              </form.Field>

              <form.Field name="SITE_FAVICON" validators={{ onSubmit: schema.shape.SITE_FAVICON }}>
                {field => (
                  <ImageUploadField
                    label={optionMap.SITE_FAVICON?.name}
                    description={optionMap.SITE_FAVICON?.description}
                    accept="image/*,.ico"
                    previewClassName="size-7 object-contain"
                    invalidFileMessage="请选择有效的图标文件"
                    value={field.state.value}
                    onChange={field.handleChange}
                    disabled={!isEdit}
                  />
                )}
              </form.Field>
            </div>

            <div className="grid grid-cols-1 items-start gap-4 sm:grid-cols-2">
              <form.Field name="SITE_TITLE" validators={{ onSubmit: schema.shape.SITE_TITLE }}>
                {field => (
                  <TextField
                    field={field}
                    label={optionMap.SITE_TITLE?.name}
                    description={optionMap.SITE_TITLE?.description}
                    placeholder="请输入系统名称"
                    maxLength={18}
                    required={isRequired('SITE_TITLE')}
                    disabled={!isEdit}
                  />
                )}
              </form.Field>

              <form.Field name="SITE_BEIAN" validators={{ onSubmit: schema.shape.SITE_BEIAN }}>
                {field => (
                  <TextField
                    field={field}
                    label={optionMap.SITE_BEIAN?.name}
                    description={optionMap.SITE_BEIAN?.description}
                    placeholder="请输入备案号"
                    maxLength={30}
                    required={isRequired('SITE_BEIAN')}
                    disabled={!isEdit}
                  />
                )}
              </form.Field>
            </div>

            <form.Field name="SITE_COPYRIGHT" validators={{ onSubmit: schema.shape.SITE_COPYRIGHT }}>
              {field => (
                <TextField
                  field={field}
                  label={optionMap.SITE_COPYRIGHT?.name}
                  description={optionMap.SITE_COPYRIGHT?.description}
                  placeholder="请输入版权声明"
                  required={isRequired('SITE_COPYRIGHT')}
                  disabled={!isEdit}
                />
              )}
            </form.Field>

            <form.Field name="SITE_DESCRIPTION" validators={{ onSubmit: schema.shape.SITE_DESCRIPTION }}>
              {field => (
                <TextField
                  field={field}
                  label={optionMap.SITE_DESCRIPTION?.name}
                  description={optionMap.SITE_DESCRIPTION?.description}
                  placeholder="请输入系统描述"
                  multiline
                  rows={2}
                  required={isRequired('SITE_DESCRIPTION')}
                  disabled={!isEdit}
                />
              )}
            </form.Field>
          </div>
        </div>

        <SitePreview store={form.store} />
      </form>

      <ConfigResetDialog
        open={resetDialogOpen}
        onOpenChange={setResetDialogOpen}
        onConfirm={resetToDefault}
        isResetting={isResetting}
        description="确认恢复基础配置为系统默认值吗？该操作不可撤销。"
      />
    </>
  )
}
