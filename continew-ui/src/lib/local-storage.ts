function warn(message: string, error: unknown) {
  if (import.meta.env.DEV) {
    console.error(message, error)
  }
}

export function getLocalStorageValue(key: string): string | null {
  try {
    return window.localStorage.getItem(key)
  } catch {
    return null
  }
}

export function setLocalStorageValue(key: string, value: string) {
  try {
    window.localStorage.setItem(key, value)
  } catch (error) {
    warn('[local-storage] Failed to write value:', error)
  }
}

export function removeLocalStorageValue(key: string) {
  try {
    window.localStorage.removeItem(key)
  } catch (error) {
    warn('[local-storage] Failed to remove value:', error)
  }
}

export function getLocalStorageJson<T>(key: string): T | null {
  const raw = getLocalStorageValue(key)

  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

export function setLocalStorageJson(key: string, value: unknown) {
  setLocalStorageValue(key, JSON.stringify(value))
}
