import axios from 'axios';
import type { RouteRecordNormalized } from 'vue-router';
import { UserState } from '@/store/modules/login/types';

export interface LoginReq {
  username: string;
  password: string;
  captcha: string;
  uuid: string;
}
export interface LoginRes {
  token: string;
}
export function login(req: LoginReq) {
  return axios.post<LoginRes>('/auth/login', req);
}

export function logout() {
  return axios.post('/auth/logout');
}

export function getUserInfo() {
  return axios.get<UserState>('/auth/user/info');
}

export function getMenuList() {
  return axios.get<RouteRecordNormalized[]>('/api/user/menu');
}
