import { ChevronDown } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Label } from '@/components/ui/label'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'

interface ClientAuthTypeSelectProps {
  readonly value: string[]
  readonly onChange: (nextValue: string[]) => void
  readonly options: { label: string; value: string }[]
  readonly disabled?: boolean
  readonly invalid?: boolean
}

/**
 * Multi-select popover for client auth types, designed consistently with UserRoleSelect.
 */
export function ClientAuthTypeSelect({
  value = [],
  onChange,
  options = [],
  disabled = false,
  invalid = false
}: ClientAuthTypeSelectProps) {
  const selectedLabels = options.filter(opt => value.includes(opt.value)).map(opt => opt.label)

  return (
    <Popover>
      <PopoverTrigger
        render={
          <Button
            type="button"
            variant="outline"
            className="w-full justify-between font-normal"
            aria-invalid={invalid}
            disabled={disabled}
          />
        }
      >
        <span className={selectedLabels.length ? 'truncate' : 'truncate text-muted-foreground'}>
          {selectedLabels.length ? selectedLabels.join('、') : '请选择认证类型'}
        </span>
        <ChevronDown className="size-4 opacity-60" />
      </PopoverTrigger>

      <PopoverContent align="start" className="max-h-60 w-64 gap-0 overflow-y-auto overscroll-contain p-1">
        {options.length === 0 ? (
          <p className="px-2 py-1.5 text-sm text-muted-foreground">暂无可选项</p>
        ) : (
          options.map(opt => {
            const checked = value.includes(opt.value)
            return (
              <div key={opt.value} className="flex items-center gap-2 rounded-md px-2 py-1.5 hover:bg-accent">
                <Checkbox
                  id={`auth-type-${opt.value}`}
                  checked={checked}
                  disabled={disabled}
                  onCheckedChange={next =>
                    onChange(next ? [...value, opt.value] : value.filter(item => item !== opt.value))
                  }
                />
                <Label
                  htmlFor={`auth-type-${opt.value}`}
                  className="flex-1 cursor-pointer text-sm font-normal whitespace-nowrap"
                >
                  {opt.label}
                </Label>
              </div>
            )
          })
        )}
      </PopoverContent>
    </Popover>
  )
}
