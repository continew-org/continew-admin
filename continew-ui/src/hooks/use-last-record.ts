import * as React from 'react'

/** Keeps the last non-null record so a closing overlay does not lose its label. */
export function useLastRecord<T>(record: T | null): T | null {
  const [last, setLast] = React.useState<T | null>(null)

  React.useEffect(() => {
    if (record !== null) {
      setLast(record)
    }
  }, [record])

  return record ?? last
}
