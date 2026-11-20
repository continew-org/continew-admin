import { createFileRoute } from '@tanstack/react-router'

import { PackageList } from './-components/package-list'

export const Route = createFileRoute('/(main)/(app)/tenant/package')({
  component: PackageList
})
