import Base64 from 'crypto-js/enc-base64';
import UTF8 from 'crypto-js/enc-utf8';
import { JSEncrypt } from 'jsencrypt';
import md5 from 'crypto-js/md5';

export function encodeByBase64(txt: string) {
  return UTF8.parse(txt).toString(Base64);
}

export function decodeByBase64(txt: string) {
  return Base64.parse(txt).toString(UTF8);
}

export function encryptByMd5(txt: string) {
  return md5(txt).toString();
}

const publicKey =
  'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAM51dgYtMyF+tTQt80sfFOpSV27a7t9u' +
  'aUVeFrdGiVxscuizE7H8SMntYqfn9lp8a5GH5P1/GGehVjUD2gF/4kcCAwEAAQ==';

export function encryptByRsa(txt: string) {
  const encryptor = new JSEncrypt();
  encryptor.setPublicKey(publicKey); // 设置公钥
  return encryptor.encrypt(txt); // 对数据进行加密
}
