import { createFileRoute } from '@tanstack/react-router'

import { FileManager } from './-components/file-manager'

export const Route = createFileRoute('/(main)/(app)/system/file')({
  component: FileManager
})
