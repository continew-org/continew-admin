import * as React from 'react'

import { revalidateLogic, useForm, useSelector } from '@tanstack/react-form'
import { useNavigate } from '@tanstack/react-router'
import { z } from 'zod'

import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Field, FieldContent, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
import { toast } from '@/components/ui/toast'
import { useCaptcha } from '@/hooks/use-captcha'
import { useAuthStore } from '@/stores/auth/auth-provider'

import { CaptchaImage } from './captcha-image'

const DEFAULT_REDIRECT = '/dashboard'

const formSchema = z.object({
  tenantCode: z.string().optional(),
  username: z.string().min(1, { message: '请输入用户名' }),
  password: z.string().min(1, { message: '请输入密码' }),
  captcha: z.string().optional(),
  remember: z.boolean().optional()
})

type LoginFormValues = z.infer<typeof formSchema>

// FOR DEMO PURPOSES ONLY: Pre-fill the form with default values to make it easier to log in.
const DEFAULT_FORM: LoginFormValues = {
  tenantCode: '',
  username: 'admin',
  password: 'admin123',
  captcha: '',
  remember: true
}

const requiredCaptchaRule = z.string().min(1, '请输入验证码')

export function LoginForm({ redirectTo }: { readonly redirectTo?: string }) {
  const navigate = useNavigate()
  const login = useAuthStore(state => state.login)
  const {
    captcha,
    isEnabled: isCaptchaEnabled,
    isExpired: isCaptchaExpired,
    isLoading: isCaptchaLoading,
    refresh: refreshCaptcha
  } = useCaptcha()
  const formRef = React.useRef<HTMLFormElement>(null)

  const form = useForm({
    defaultValues: DEFAULT_FORM,
    validationLogic: revalidateLogic(),
    validators: { onDynamic: formSchema },
    onSubmit: async ({ value }) => {
      try {
        await login({
          username: value.username,
          password: value.password,
          captcha: value.captcha || undefined,
          uuid: captcha?.uuid,
          persist: value.remember,
          tenantCode: value.tenantCode?.trim() || undefined
        })
      } catch (error) {
        // `login` is a silent request, so this form is the only place that reports the failure.
        // A captcha is single-use, so clear the typed one and fetch a fresh image as well.
        toast.add({
          title: error instanceof Error ? error.message : '登录失败，请重试',
          type: 'error'
        })
        form.setFieldValue('captcha', '')
        void refreshCaptcha()
        return
      }

      toast.add({ title: '登录成功', type: 'success' })

      // `href` keeps any query string that was part of the requested URL.
      await navigate({ href: redirectTo?.startsWith('/') ? redirectTo : DEFAULT_REDIRECT, replace: true })
    }
  })

  const isSubmitting = useSelector(form.store, state => state.isSubmitting)

  const submit = async () => {
    await form.validate('submit')
    await form.handleSubmit()

    // TanStack Form does not move focus to the first invalid control, so the form does it here.
    requestAnimationFrame(() => {
      formRef.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
    })
  }

  return (
    <form
      ref={formRef}
      noValidate
      className="flex flex-col gap-4"
      onSubmit={event => {
        event.preventDefault()
        void submit()
      }}
    >
      <FieldGroup className="gap-4">
        <form.Field name="tenantCode">
          {field => (
            <Field>
              <Input
                name="tenantCode"
                type="text"
                value={field.state.value ?? ''}
                onChange={event => field.handleChange(event.target.value)}
                onBlur={field.handleBlur}
                placeholder="请输入租户编码（留空为默认租户）"
                autoComplete="off"
                disabled={isSubmitting}
              />
            </Field>
          )}
        </form.Field>

        <form.Field name="username" validators={{ onDynamic: formSchema.shape.username }}>
          {field => {
            const invalid = field.state.meta.errors.length > 0

            return (
              <Field data-invalid={invalid}>
                <Input
                  name="username"
                  type="text"
                  value={field.state.value}
                  onChange={event => field.handleChange(event.target.value)}
                  onBlur={field.handleBlur}
                  placeholder="请输入用户名"
                  autoComplete="username"
                  disabled={isSubmitting}
                  aria-invalid={invalid}
                />
                <FieldError errors={field.state.meta.errors} />
              </Field>
            )
          }}
        </form.Field>

        <form.Field name="password" validators={{ onDynamic: formSchema.shape.password }}>
          {field => {
            const invalid = field.state.meta.errors.length > 0

            return (
              <Field data-invalid={invalid}>
                <Input
                  name="password"
                  type="password"
                  value={field.state.value}
                  onChange={event => field.handleChange(event.target.value)}
                  onBlur={field.handleBlur}
                  placeholder="请输入密码"
                  autoComplete="current-password"
                  disabled={isSubmitting}
                  aria-invalid={invalid}
                />
                <FieldError errors={field.state.meta.errors} />
              </Field>
            )
          }}
        </form.Field>

        {isCaptchaEnabled && (
          <form.Field name="captcha" validators={{ onDynamic: requiredCaptchaRule }}>
            {field => {
              const invalid = field.state.meta.errors.length > 0

              return (
                <Field data-invalid={invalid}>
                  <div className="flex gap-2">
                    <Input
                      name="captcha"
                      type="text"
                      value={field.state.value}
                      onChange={event => field.handleChange(event.target.value)}
                      onBlur={field.handleBlur}
                      placeholder="请输入验证码"
                      autoComplete="off"
                      className="flex-1"
                      disabled={isSubmitting}
                      aria-invalid={invalid}
                    />
                    <CaptchaImage
                      img={captcha?.img}
                      isExpired={isCaptchaExpired}
                      isLoading={isCaptchaLoading}
                      onRefresh={refreshCaptcha}
                    />
                  </div>
                  <FieldError errors={field.state.meta.errors} />
                </Field>
              )
            }}
          </form.Field>
        )}

        <form.Field name="remember">
          {field => (
            <Field orientation="horizontal">
              <Checkbox
                id="login-remember"
                name="remember"
                checked={field.state.value}
                onCheckedChange={checked => field.handleChange(Boolean(checked))}
                disabled={isSubmitting}
              />
              <FieldContent>
                <FieldLabel htmlFor="login-remember" className="font-normal">
                  记住我
                </FieldLabel>
              </FieldContent>
            </Field>
          )}
        </form.Field>
      </FieldGroup>

      <Button className="w-full" type="submit" disabled={isSubmitting}>
        {isSubmitting ? (
          <>
            <Spinner />
            登录中
          </>
        ) : (
          '登录'
        )}
      </Button>
    </form>
  )
}
