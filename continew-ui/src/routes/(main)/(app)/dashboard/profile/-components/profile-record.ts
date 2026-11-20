import { differenceInDays, differenceInMonths, format } from 'date-fns'

import type { UserInfoResp } from '@/lib/api/types'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'
import { getInitials } from '@/lib/utils'

import type { ProfileRecord } from './profile-data'

/** The backend sends `2026-10-13` and `2026-10-13 06:07:16`. */
function parseBackendDate(value?: string | null): Date | undefined {
  if (!value) {
    return undefined
  }

  const date = new Date(value.replace(' ', 'T'))
  return Number.isNaN(date.getTime()) ? undefined : date
}

function formatBackendDate(value?: string | null): string | undefined {
  const date = parseBackendDate(value)
  return date ? format(date, 'yyyy-MM-dd') : undefined
}

function formatGender(gender?: number): string {
  if (gender === 1) {
    return '男'
  }

  if (gender === 2) {
    return '女'
  }

  return '未知'
}

function formatActiveDuration(createdAt?: string | null): string | undefined {
  const start = parseBackendDate(createdAt)

  if (!start) {
    return undefined
  }

  const days = Math.max(0, differenceInDays(new Date(), start))

  if (days < 1) {
    return '不到 1 天'
  }

  if (days < 30) {
    return `${days} 天`
  }

  const months = Math.max(1, differenceInMonths(new Date(), start))

  if (months < 12) {
    return `${months} 个月`
  }

  const years = Math.floor(months / 12)
  const rest = months % 12
  return rest === 0 ? `${years} 年` : `${years} 年 ${rest} 个月`
}

/** The record shown before the signed-in user resolves; the page never renders it in practice. */
function emptyProfile(): ProfileRecord {
  return {
    name: EMPTY_PLACEHOLDER,
    preferredName: EMPTY_PLACEHOLDER,
    legalName: EMPTY_PLACEHOLDER,
    pronouns: EMPTY_PLACEHOLDER,
    initials: '?',
    avatar: '',
    department: EMPTY_PLACEHOLDER,
    workEmail: EMPTY_PLACEHOLDER,
    personalEmail: EMPTY_PLACEHOLDER,
    workPhone: EMPTY_PLACEHOLDER,
    contractorId: EMPTY_PLACEHOLDER,
    startDate: EMPTY_PLACEHOLDER,
    activeDuration: EMPTY_PLACEHOLDER,
    bio: EMPTY_PLACEHOLDER,
    roleNames: []
  }
}

/** Builds the profile record from the signed-in user; every field has a real source. */
export function toProfileRecord(user: UserInfoResp | null): ProfileRecord {
  if (!user) {
    return emptyProfile()
  }

  const name = user.nickname || user.username

  return {
    name,
    preferredName: user.username || EMPTY_PLACEHOLDER,
    legalName: name || EMPTY_PLACEHOLDER,
    pronouns: formatGender(user.gender),
    initials: getInitials(name),
    avatar: user.avatar ?? '',
    department: user.deptName || EMPTY_PLACEHOLDER,
    workEmail: user.email || EMPTY_PLACEHOLDER,
    personalEmail: user.email || EMPTY_PLACEHOLDER,
    workPhone: user.phone || EMPTY_PLACEHOLDER,
    contractorId: String(user.id),
    startDate: formatBackendDate(user.registrationDate) ?? EMPTY_PLACEHOLDER,
    activeDuration: formatActiveDuration(user.registrationDate) ?? EMPTY_PLACEHOLDER,
    bio: user.description || EMPTY_PLACEHOLDER,
    roleNames: user.roleNames ?? []
  }
}
