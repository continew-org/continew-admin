import { Link } from '@tanstack/react-router'

import { useShallow } from 'zustand/react/shallow'

import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem
} from '@/components/ui/sidebar'
import { useMenuGroups } from '@/hooks/use-menu'
import { useSiteOptions } from '@/hooks/use-site-options'
import type { MenuGroup } from '@/navigation/menu'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'

import { NavMain } from './nav-main'
import { SupportCarousel } from './support-carousel'

export function AppSidebar({
  showBrand = true,
  groups: groupsProp,
  ...props
}: React.ComponentProps<typeof Sidebar> & { showBrand?: boolean; groups?: readonly MenuGroup[] }) {
  const { sidebarVariant, sidebarCollapsible, isSynced } = usePreferencesStore(
    useShallow(s => ({
      sidebarVariant: s.values.sidebar_variant,
      sidebarCollapsible: s.values.sidebar_collapsible,
      isSynced: s.isSynced
    }))
  )
  const { groups: hookGroups } = useMenuGroups()
  const groups = groupsProp ?? hookGroups
  const { title, logo } = useSiteOptions()

  const variant = isSynced ? sidebarVariant : props.variant
  const collapsible = isSynced ? sidebarCollapsible : props.collapsible

  return (
    <Sidebar {...props} variant={variant} collapsible={collapsible}>
      {showBrand && (
        <SidebarHeader>
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton render={<Link to="/dashboard" />}>
                <img src={logo} alt="" className="size-5 shrink-0 object-contain" />
                <span className="text-base font-semibold">{title}</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarHeader>
      )}
      <SidebarContent className="scroll-fade-y">
        <NavMain items={groups} />
      </SidebarContent>
      <SidebarFooter>
        <SupportCarousel />
      </SidebarFooter>
    </Sidebar>
  )
}
