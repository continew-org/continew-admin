import { Field, FieldContent, FieldDescription, FieldError, FieldLabel } from '@/components/ui/field'
import { Switch } from '@/components/ui/switch'

export interface SwitchFieldApi {
  state: { value: number; meta: { errors: Array<{ message?: string } | undefined> } }
  handleChange: (value: number) => void
  handleBlur: () => void
}

interface SwitchFieldProps {
  field: SwitchFieldApi
  label?: string
  description?: string
  disabled?: boolean
}

/** An on/off option stored as 1 or 0, laid out like `TextField`. */
export function SwitchField({ field, label, description, disabled = false }: SwitchFieldProps) {
  return (
    <Field
      orientation="horizontal"
      className="items-center justify-between"
      data-invalid={field.state.meta.errors.length > 0}
    >
      <FieldContent>
        <FieldLabel>{label}</FieldLabel>
        <FieldDescription>{description}</FieldDescription>
        <FieldError errors={field.state.meta.errors} />
      </FieldContent>
      <Switch
        checked={field.state.value === 1}
        onCheckedChange={checked => field.handleChange(checked ? 1 : 0)}
        onBlur={field.handleBlur}
        disabled={disabled}
      />
    </Field>
  )
}
