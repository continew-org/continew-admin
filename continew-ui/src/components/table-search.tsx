import * as React from 'react'

import { Search, X } from 'lucide-react'

import { cn } from 'cn'

import { InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput } from '@/components/ui/input-group'
import { SEARCH_DEBOUNCE_MS } from '@/hooks/use-debounced-value'

interface TableSearchProps {
  onSearch: (keyword: string) => void
  placeholder?: string
  delay?: number
  clearable?: boolean
  className?: string
  /** Controlled text. Omit it and the box keeps its own text, as most screens do. */
  value?: string
  onValueChange?: (value: string) => void
}

export function TableSearch({
  onSearch,
  placeholder = '搜索',
  delay = SEARCH_DEBOUNCE_MS,
  clearable = false,
  className,
  value: controlledValue,
  onValueChange
}: TableSearchProps) {
  const [uncontrolledValue, setUncontrolledValue] = React.useState('')
  const text = controlledValue ?? uncontrolledValue
  const reportedRef = React.useRef(text.trim())
  const onSearchRef = React.useRef(onSearch)

  React.useEffect(() => {
    onSearchRef.current = onSearch
  })

  const handleTextChange = (next: string) => {
    if (controlledValue === undefined) {
      setUncontrolledValue(next)
    }
    onValueChange?.(next)
  }

  React.useEffect(() => {
    const timer = setTimeout(() => {
      const next = text.trim()
      if (next === reportedRef.current) return
      reportedRef.current = next
      onSearchRef.current(next)
    }, delay)

    return () => clearTimeout(timer)
  }, [text, delay])

  const clear = () => {
    handleTextChange('')
    reportedRef.current = ''
    onSearchRef.current('')
  }

  return (
    <InputGroup className={cn('h-8 w-full md:w-64', className)}>
      <InputGroupAddon align="inline-start">
        <Search className="size-3.5" />
      </InputGroupAddon>
      <InputGroupInput
        className="h-full text-sm"
        placeholder={placeholder}
        value={text}
        onChange={event => handleTextChange(event.target.value)}
      />
      {clearable && text ? (
        <InputGroupAddon align="inline-end">
          <InputGroupButton size="icon-xs" onClick={clear}>
            <X />
            <span className="sr-only">清空搜索</span>
          </InputGroupButton>
        </InputGroupAddon>
      ) : null}
    </InputGroup>
  )
}
