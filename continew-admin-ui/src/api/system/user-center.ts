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

export function saveUserInfo() {
  return axios.get('/api/user/save-info');
}