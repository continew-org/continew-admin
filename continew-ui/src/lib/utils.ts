export function getInitials(str: string): string {
  const trimmed = str.trim()

  if (!trimmed) {
    return '?'
  }

  return trimmed
    .split(/\s+/)
    .map(word => word[0])
    .join('')
    .toUpperCase()
}

interface CurrencyFormatOptions {
  currency?: string
  locale?: string
  minimumFractionDigits?: number
  maximumFractionDigits?: number
  noDecimals?: boolean
}

export function formatCurrency(amount: number, options: CurrencyFormatOptions = {}) {
  const { currency = 'USD', locale = 'en-US', minimumFractionDigits, maximumFractionDigits, noDecimals } = options

  const formatOptions: Intl.NumberFormatOptions = {
    style: 'currency',
    currency,
    minimumFractionDigits: noDecimals ? 0 : minimumFractionDigits,
    maximumFractionDigits: noDecimals ? 0 : maximumFractionDigits
  }

  return new Intl.NumberFormat(locale, formatOptions).format(amount)
}
