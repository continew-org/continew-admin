import axios from 'axios';
import type { RouteRecordNormalized } from 'vue-router';
import { UserState } from '@/store/modules/login/types';

const BASE_URL = '/auth';

export interface LoginReq {
  phone?: string;
  email?: string;
  username?: string;
  password?: string;
  captcha: string;
  uuid?: string;
}

export interface LoginRes {
  token: string;
}

export function login(req: LoginReq) {
  return axios.post<LoginRes>(`${BASE_URL}/login`, req);
}

export function logout() {
  return axios.post(`${BASE_URL}/logout`);
}

export function getUserInfo() {
  return axios.get<UserState>(`${BASE_URL}/user/info`);
}

export function listRoute() {
  return axios.get<RouteRecordNormalized[]>(`${BASE_URL}/route`);
}
