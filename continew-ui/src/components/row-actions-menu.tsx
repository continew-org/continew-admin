import type { ComponentProps, ReactNode } from 'react'
import { MoreHorizontal } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'

type RowActionsMenuProps = {
  /** Optional first-level action rendered before the ellipsis, such as a high-frequency detail button. */
  primary?: ReactNode
  /** Whether to render the ellipsis menu. Defaults to true; set false when a row has no secondary actions. */
  showMenu?: boolean
  /** The menu body: DropdownMenuGroup / DropdownMenuItem / DropdownMenuSeparator. */
  children: ReactNode
  align?: ComponentProps<typeof DropdownMenuContent>['align']
  contentClassName?: string
  triggerVariant?: ComponentProps<typeof Button>['variant']
  triggerSize?: ComponentProps<typeof Button>['size']
}

/**
 * The ellipsis trigger and popup shell shared by the table row action menus.
 * The items stay at the call site because they depend on row state and permissions.
 */
export function RowActionsMenu({
  primary,
  showMenu = true,
  children,
  align = 'end',
  contentClassName = 'w-36',
  triggerVariant = 'ghost',
  triggerSize = 'icon-sm'
}: RowActionsMenuProps) {
  const menu = (
    <DropdownMenu>
      <DropdownMenuTrigger render={<Button variant={triggerVariant} size={triggerSize} />}>
        <MoreHorizontal className="size-4" />
      </DropdownMenuTrigger>
      <DropdownMenuContent align={align} className={contentClassName}>
        {children}
      </DropdownMenuContent>
    </DropdownMenu>
  )

  if (!primary) {
    return menu
  }

  return (
    <div className="inline-flex items-center gap-1">
      {primary}
      {showMenu ? menu : null}
    </div>
  )
}
