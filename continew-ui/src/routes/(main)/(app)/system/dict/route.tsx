import { createFileRoute } from '@tanstack/react-router'

import { DictManager } from './-components/dict-manager'

export const Route = createFileRoute('/(main)/(app)/system/dict')({
  component: DictManager
})
