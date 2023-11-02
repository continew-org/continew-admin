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

export interface cropperOptions {
  autoCrop: boolean; // 是否默认生成截图框
  autoCropWidth: number; // 默认生成截图框宽度
  autoCropHeight: number; // 默认生成截图框高度
  canMove: boolean; // 上传图片是否可以移动  (默认:true)
  centerBox: boolean; // 截图框是否被限制在图片里面  (默认:false)
  full: boolean; // 是否输出原图比例的截图 选true生成的图片会非常大  (默认:false)
  fixed: boolean; // 是否开启截图框宽高固定比例  (默认:false)
  fixedBox: boolean; // 固定截图框大小 不允许改变
  img: string | ArrayBuffer | null; // 裁剪图片的地址
  outputSize: number; // 裁剪生成图片的质量  (默认:1)
  outputType: string; // 默认生成截图为PNG格式
}

export function uploadAvatar(data: FormData) {
  return axios.post<AvatarRes>(`${BASE_URL}/avatar`, data);
}

export interface UserBasicInfoUpdateReq {
  nickname: string;
  gender: number;
}

export function updateBasicInfo(req: UserBasicInfoUpdateReq) {
  return axios.patch(`${BASE_URL}/basic/info`, req);
}

export interface UserPasswordUpdateReq {
  oldPassword: string;
  newPassword: string;
}

export function updatePassword(req: UserPasswordUpdateReq) {
  return axios.patch(`${BASE_URL}/password`, req);
}

export interface UserPhoneUpdateReq {
  newPhone: string;
  captcha: string;
  currentPassword: string;
}

export function updatePhone(req: UserPhoneUpdateReq) {
  return axios.patch(`${BASE_URL}/phone`, req);
}

export interface UserEmailUpdateReq {
  newEmail: string;
  captcha: string;
  currentPassword: string;
}

export function updateEmail(req: UserEmailUpdateReq) {
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
