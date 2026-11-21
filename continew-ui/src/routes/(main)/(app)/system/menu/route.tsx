import { createFileRoute } from '@tanstack/react-router'

import { MenuList } from './-components/menu-list'

export const Route = createFileRoute('/(main)/(app)/system/menu')({
  component: MenuList
})
