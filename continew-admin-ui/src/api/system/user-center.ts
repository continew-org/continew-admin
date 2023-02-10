import axios from 'axios';

const BASE_URL = '/system/user/center';

export interface BasicInfoModel {
  username: string;
  nickname: string;
  gender: number;
}

export interface AvatarRes {
  avatar: string;
}

export function uploadAvatar(data: FormData) {
  return axios.post<AvatarRes>(`${BASE_URL}/avatar`, data);
}

export interface UpdateBasicInfoReq {
  nickname: string;
  gender: number;
}

export function updateBasicInfo(req: UpdateBasicInfoReq) {
  return axios.patch(`${BASE_URL}/basic/info`, req);
}

export interface UpdatePasswordReq {
  oldPassword: string;
  newPassword: string;
}

export function updatePassword(req: UpdatePasswordReq) {
  return axios.patch(`${BASE_URL}/password`, req);
}

export interface UpdateEmailReq {
  newEmail: string;
  captcha: string;
  currentPassword: string;
}

export function updateEmail(req: UpdateEmailReq) {
  return axios.patch(`${BASE_URL}/email`, req);
}
