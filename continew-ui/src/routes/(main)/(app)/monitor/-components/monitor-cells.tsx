import { TextCell } from '@/components/text-cell'

type DisplayValue = string | null | undefined

export function UserCell({ nickname, username }: { nickname: DisplayValue; username: DisplayValue }) {
  const label = nickname && username && nickname !== username ? `${nickname} (${username})` : nickname || username
  return <TextCell value={label} />
}

export function DeviceCell({ browser, os }: { browser: DisplayValue; os: DisplayValue }) {
  return <TextCell value={[browser, os].filter(Boolean).join(' / ')} />
}
