import { useCallback, useEffect, useState } from 'react'

import { getImageCaptcha } from '@/lib/api/captcha'

export type CaptchaState = {
  uuid: string
  img: string
  isEnabled: boolean
  expireTime?: number
}

export function useCaptcha() {
  const [captcha, setCaptcha] = useState<CaptchaState | null>(null)
  const [isExpired, setIsExpired] = useState(false)
  const [isLoading, setIsLoading] = useState(false)

  const refresh = useCallback(async () => {
    setIsLoading(true)

    try {
      const payload = await getImageCaptcha()
      setCaptcha({
        uuid: payload.uuid,
        img: payload.img,
        isEnabled: payload.isEnabled,
        expireTime: payload.expireTime
      })
      setIsExpired(false)
    } catch {
      // The captcha is optional: a failed request must not block the login form.
      setCaptcha(null)
      setIsExpired(false)
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    void refresh()
  }, [refresh])

  useEffect(() => {
    const expireTime = captcha?.expireTime

    if (!expireTime) {
      return
    }

    const remaining = expireTime - Date.now()
    if (remaining <= 0) {
      setIsExpired(true)
      return
    }

    const timer = setTimeout(() => setIsExpired(true), remaining)
    return () => clearTimeout(timer)
  }, [captcha?.expireTime])

  return {
    captcha,
    /** Whether the backend requires a captcha; hidden until the first response. */
    isEnabled: captcha?.isEnabled ?? false,
    /** True once the captcha TTL has elapsed; the image can no longer be used. */
    isExpired: captcha !== null && isExpired,
    isLoading,
    refresh
  }
}
