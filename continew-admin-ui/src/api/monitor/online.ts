import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/monitor/online/user';

export interface OnlineUserRecord {
  token: string;
  username: string;
  nickname: string;
  clientIp: string;
  location: string;
  browser: string;
  loginTime: string;
}

export interface OnlineUserParam extends Partial<OnlineUserRecord> {
  page: number;
  size: number;
  sort: Array<string>;
}

export interface OnlineUserListRes {
  list: OnlineUserRecord[];
  total: number;
}

export function listOnlineUser(params: OnlineUserParam) {
  return axios.get<OnlineUserListRes>(BASE_URL, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    }
  });
}

export function kickout(token: string) {
  return axios.delete(`${BASE_URL}/${token}`);
}