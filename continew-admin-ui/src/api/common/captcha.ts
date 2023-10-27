import axios from 'axios';

const BASE_URL = '/common/captcha';

export interface ImageCaptchaRes {
  uuid: string;
  img: string;
}
export function getImageCaptcha() {
  return axios.get<ImageCaptchaRes>(`${BASE_URL}/img`);
}

export function getMailCaptcha(email: string) {
  return axios.get(`${BASE_URL}/mail?email=${email}`);
}

export function getSmsCaptcha(phone: string) {
  return axios.get(`${BASE_URL}/sms?phone=${phone}`);
}
