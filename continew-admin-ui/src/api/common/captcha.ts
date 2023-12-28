import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/captcha';

export interface ImageCaptchaRes {
  uuid: string;
  img: string;
}

export interface BehaviorCaptchaRes {
  originalImageBase64: string;
  point: {
    x: number;
    y: number;
  };
  jigsawImageBase64: string;
  token: string;
  secretKey: string;
}

export interface BehaviorCaptchaReq {
  captchaType?: string;
  captchaVerification?: string;
  clientUid?: string;
}

export interface CheckBehaviorCaptchaRes {
  repCode: string;
  repMsg: string;
}

export function getImageCaptcha() {
  return axios.get<ImageCaptchaRes>(`${BASE_URL}/img`);
}

export function getMailCaptcha(email: string) {
  return axios.get(`${BASE_URL}/mail?email=${email}`);
}

export function getSmsCaptcha(
  phone: string,
  behaviorCaptcha: BehaviorCaptchaReq,
) {
  return axios.get(
    `${BASE_URL}/sms?phone=${phone}&captchaVerification=${encodeURIComponent(
      behaviorCaptcha.captchaVerification || '',
    )}`,
  );
}

export function getBehaviorCaptcha(params: any) {
  return axios.get<BehaviorCaptchaRes>(`${BASE_URL}/behavior`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function checkBehaviorCaptcha(params: any) {
  return axios.post<CheckBehaviorCaptchaRes>(`${BASE_URL}/behavior`, params);
}
