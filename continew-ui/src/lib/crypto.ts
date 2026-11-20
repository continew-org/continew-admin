import { JSEncrypt } from 'jsencrypt'

/**
 * Encrypts a payload the backend decrypts with its RSA private key.
 *
 * The backend uses PKCS#1 v1.5 padding, which WebCrypto does not support (it only handles OAEP),
 * so JSEncrypt does the encryption.
 */
export function encryptByRsa(plainText: string): string {
  const publicKey = import.meta.env.VITE_APP_RSA_PUBLIC_KEY

  if (!publicKey || publicKey.startsWith('REPLACE_WITH')) {
    throw new Error('VITE_APP_RSA_PUBLIC_KEY is not configured.')
  }

  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey)

  const encrypted = encryptor.encrypt(plainText)

  if (!encrypted) {
    throw new Error('Failed to encrypt the payload with the configured RSA public key.')
  }

  return encrypted
}
