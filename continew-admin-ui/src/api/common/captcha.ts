import axios from 'axios';
import qs from 'query-string';

export interface ImageCaptchaRes {
  uuid: string;
  img: string;
}
export function getImageCaptcha() {
  return axios.get<ImageCaptchaRes>('/common/captcha/img');
}

export interface MailCaptchaReq {
  email: string;
}
export function getMailCaptcha(params: MailCaptchaReq) {
  return axios.get('/common/captcha/mail', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}
