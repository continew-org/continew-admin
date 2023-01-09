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