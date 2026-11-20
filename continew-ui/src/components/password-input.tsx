import * as React from 'react'
import { Eye, EyeOff } from 'lucide-react'

import { InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput } from '@/components/ui/input-group'

export interface PasswordInputProps extends Omit<React.ComponentProps<'input'>, 'type'> {
  readonly className?: string
}

export function PasswordInput({ className, disabled, ...props }: PasswordInputProps) {
  const [visible, setVisible] = React.useState(false)

  return (
    <InputGroup className={className}>
      <InputGroupInput type={visible ? 'text' : 'password'} disabled={disabled} {...props} />
      <InputGroupAddon align="inline-end">
        <InputGroupButton
          type="button"
          size="icon-xs"
          disabled={disabled}
          onClick={() => setVisible(v => !v)}
          aria-label={visible ? '隐藏密码' : '显示密码'}
          tabIndex={-1}
        >
          {visible ? <EyeOff className="size-3.5" /> : <Eye className="size-3.5" />}
        </InputGroupButton>
      </InputGroupAddon>
    </InputGroup>
  )
}
