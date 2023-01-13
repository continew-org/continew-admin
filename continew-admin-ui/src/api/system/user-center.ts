import axios from 'axios';

export interface BasicInfoModel {
  username: string;
  nickname: string;
  gender: number;
}

export interface AvatarRes {
  avatar: string;
}
export function uploadAvatar(data: FormData) {
  return axios.post<AvatarRes>('/system/user/center/avatar', data);
}

export interface UpdateBasicInfoReq {
  nickname: string;
  gender: number;
}
export function updateBasicInfo(req: UpdateBasicInfoReq) {
  return axios.patch('/system/user/center/basic/info', req);
}

export interface UpdatePasswordReq {
  oldPassword: string;
  newPassword: string;
}
export function updatePassword(req: UpdatePasswordReq) {
  return axios.patch('/system/user/center/password', req);
}

export interface UpdateEmailReq {
  newEmail: string;
  captcha: string;
  currentPassword: string;
}
export function updateEmail(req: UpdateEmailReq) {
  return axios.patch('/system/user/center/email', req);
}