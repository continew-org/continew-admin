import type { ChangeEvent, ReactNode } from 'react'

import { RequiredMark } from '@/components/required-mark'
import { Field, FieldContent, FieldDescription, FieldError, FieldLabel } from '@/components/ui/field'
import { InputGroup, InputGroupAddon, InputGroupInput } from '@/components/ui/input-group'
import { Textarea } from '@/components/ui/textarea'

export interface TextFieldApi {
  state: { value: string; meta: { errors: Array<{ message?: string } | undefined> } }
  handleChange: (value: string) => void
  handleBlur: () => void
}

interface TextFieldProps {
  field: TextFieldApi
  label?: string
  description?: string
  required?: boolean
  placeholder?: string
  type?: string
  min?: number
  max?: number
  maxLength?: number
  multiline?: boolean
  rows?: number
  disabled?: boolean
  addonEnd?: ReactNode
}

export function TextField({
  field,
  label,
  description,
  required = false,
  placeholder,
  type,
  min,
  max,
  maxLength,
  multiline = false,
  rows,
  disabled = false,
  addonEnd
}: TextFieldProps) {
  const invalid = field.state.meta.errors.length > 0
  const controlProps = {
    value: field.state.value,
    onChange: (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => field.handleChange(event.target.value),
    onBlur: field.handleBlur,
    placeholder,
    maxLength,
    disabled,
    required,
    'aria-invalid': invalid
  }

  return (
    <Field data-invalid={invalid}>
      <FieldContent>
        {label && (
          <FieldLabel className="text-left">
            {label}
            {required && <RequiredMark />}
          </FieldLabel>
        )}
        {description && <FieldDescription>{description}</FieldDescription>}
      </FieldContent>
      {multiline ? (
        <Textarea {...controlProps} rows={rows} className="resize-none" />
      ) : (
        <InputGroup>
          <InputGroupInput {...controlProps} type={type} min={min} max={max} />
          {addonEnd && <InputGroupAddon align="inline-end">{addonEnd}</InputGroupAddon>}
        </InputGroup>
      )}
      <FieldError errors={field.state.meta.errors} />
    </Field>
  )
}
