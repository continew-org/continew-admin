import axios from 'axios';

const BASE_URL = '/system/user';

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

export interface UserSocialBindRecord {
  source: string;
  description: string;
}

export function listSocial() {
  return axios.get<UserSocialBindRecord[]>(`${BASE_URL}/social`);
}

export function bindSocial(source: string, req: any) {
  return axios.post(`${BASE_URL}/social/${source}`, req);
}

export function unbindSocial(source: string) {
  return axios.delete(`${BASE_URL}/social/${source}`);
}
