import * as React from 'react'

import { useNavigate } from '@tanstack/react-router'

import { useScrollFade } from '@animate-ui/react/primitives/effects/scroll-fade'
import { Search } from 'lucide-react'

import { Button } from '@/components/ui/button'
import {
  Command,
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator
} from '@/components/ui/command'
import { Kbd, KbdGroup } from '@/components/ui/kbd'
import { useMenuGroups } from '@/hooks/use-menu'
import type { MenuGroup, MenuItem } from '@/navigation/menu'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'
import { useShellStore } from '@/stores/shell'

interface SearchItem {
  id: string
  group: string
  label: string
  url: string
  icon?: MenuItem['icon']
  external?: boolean
}

/** macOS shows ⌘; every other platform uses Ctrl, matching the global shortcut handler. */
const MODIFIER_KEY = /Mac|iPhone|iPad|iPod/.test(navigator.platform) ? '⌘' : 'Ctrl'

function toSearchItems(groups: readonly MenuGroup[]): SearchItem[] {
  return groups.flatMap(group =>
    group.items.flatMap(item => {
      if (item.subItems?.length) {
        return item.subItems.map(subItem => ({
          id: subItem.id,
          group: item.title,
          label: subItem.title,
          url: subItem.url,
          icon: subItem.icon ?? item.icon,
          external: subItem.external
        }))
      }

      return [
        {
          id: item.id,
          group: group.label ?? '固定',
          label: item.title,
          url: item.url,
          icon: item.icon,
          external: item.external
        }
      ]
    })
  )
}

function groupBy(items: SearchItem[]) {
  const groups = [...new Set(items.map(item => item.group))]
  return groups.map(group => ({
    group,
    items: items.filter(item => item.group === group)
  }))
}

/**
 * The result list is the scroll container, so the fade is attached to it. A component of its own
 * keeps the hook mounting together with the dialog: while the palette is closed the list is not in
 * the DOM and the fade would never attach.
 */
function FadingCommandList({ children }: { children: React.ReactNode }) {
  const listRef = React.useRef<HTMLDivElement>(null)

  useScrollFade(listRef, { blur: 4 })

  return <CommandList ref={listRef}>{children}</CommandList>
}

/** Header trigger; the palette itself is mounted once by the shell and opened through the store. */
export function SearchTrigger() {
  const setOpen = useShellStore(state => state.setSearchOpen)
  const shortcut = usePreferencesStore(state => state.values.shortcut_enable && state.values.shortcut_search)

  return (
    <Button
      onClick={() => setOpen(true)}
      variant="link"
      className="px-0! font-normal text-muted-foreground hover:no-underline"
    >
      <Search data-icon="inline-start" />
      搜索
      {shortcut && (
        <KbdGroup>
          <Kbd>{MODIFIER_KEY}</Kbd>
          <Kbd>J</Kbd>
        </KbdGroup>
      )}
    </Button>
  )
}

export function SearchDialog() {
  const open = useShellStore(state => state.searchOpen)
  const setOpen = useShellStore(state => state.setSearchOpen)
  const [query, setQuery] = React.useState('')
  const navigate = useNavigate()
  const { groups } = useMenuGroups()
  const groupedItems = React.useMemo(() => groupBy(toSearchItems(groups)), [groups])

  const handleOpenChange = (value: boolean) => {
    setOpen(value)
    if (!value) setQuery('')
  }

  const handleSelect = (item: SearchItem) => {
    handleOpenChange(false)

    if (item.external) {
      window.open(item.url, '_blank', 'noopener,noreferrer')
      return
    }

    // `href` accepts the backend path verbatim, including any query string.
    void navigate({ href: item.url })
  }

  const renderGroups = () =>
    groupedItems.map(({ group, items }, index) => (
      <React.Fragment key={group}>
        {index > 0 && <CommandSeparator />}
        <CommandGroup heading={group}>
          {items.map(item => (
            <CommandItem
              key={`${group}-${item.id}`}
              value={`${item.group} ${item.label}`}
              onSelect={() => handleSelect(item)}
            >
              <span className="flex min-w-0 items-center gap-2">
                {item.icon && <item.icon />}
                <span className="truncate">{item.label}</span>
              </span>
            </CommandItem>
          ))}
        </CommandGroup>
      </React.Fragment>
    ))

  return (
    <CommandDialog open={open} onOpenChange={handleOpenChange} title="搜索" description="搜索目录或页面">
      <Command>
        <CommandInput placeholder="搜索目录或页面" value={query} onValueChange={setQuery} />
        <FadingCommandList>
          <CommandEmpty>没有找到结果</CommandEmpty>
          {renderGroups()}
        </FadingCommandList>
      </Command>
    </CommandDialog>
  )
}
