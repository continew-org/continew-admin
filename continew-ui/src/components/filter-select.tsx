import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'

interface FilterSelectOption {
  readonly value: string
  readonly label: string
}

interface FilterSelectProps {
  readonly label: string
  readonly options: readonly FilterSelectOption[]
  readonly value: string
  readonly onValueChange: (value: string | null) => void
  readonly className?: string
}

/** A labelled select used in list filter rows; long option labels truncate with a full-width title. */
export function FilterSelect({ label, options, value, onValueChange, className }: FilterSelectProps) {
  const selectedLabel = options.find(option => option.value === value)?.label

  return (
    <div className={className}>
      <Select items={options} value={value} onValueChange={onValueChange}>
        <SelectTrigger className="w-full">
          <span className="shrink-0 text-muted-foreground">{label}</span>
          <span className="block min-w-0 flex-1 overflow-hidden text-left" title={selectedLabel}>
            <SelectValue className="block truncate" />
          </span>
        </SelectTrigger>
        <SelectContent className="w-auto max-w-md min-w-(--anchor-width)" align="start" alignItemWithTrigger={false}>
          <SelectGroup>
            {options.map(option => (
              <SelectItem key={option.value} value={option.value} title={option.label}>
                {option.label}
              </SelectItem>
            ))}
          </SelectGroup>
        </SelectContent>
      </Select>
    </div>
  )
}
