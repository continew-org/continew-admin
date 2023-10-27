import axios from 'axios';
import type { RouteRecordNormalized } from 'vue-router';
import { UserState } from '@/store/modules/user/types';

const BASE_URL = '/auth';

export interface AccountLoginReq {
  username?: string;
  password?: string;
  captcha: string;
  uuid?: string;
}

export interface LoginRes {
  token: string;
}

export function accountLogin(req: AccountLoginReq) {
  return axios.post<LoginRes>(`${BASE_URL}/account`, req);
}

export interface EmailLoginReq {
  email: string;
  captcha: string;
}

export function emailLogin(req: EmailLoginReq) {
  return axios.post<LoginRes>(`${BASE_URL}/email`, req);
}

export interface PhoneLoginReq {
  phone: string;
  captcha: string;
}

export function phoneLogin(req: PhoneLoginReq) {
  return axios.post<LoginRes>(`${BASE_URL}/phone`, req);
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

export function socialAuth(source: string) {
  return axios.get<string>(`/oauth/${source}`);
}

export function socialLogin(source: string, req: any) {
  return axios.post<LoginRes>(`/oauth/${source}`, req);
}
