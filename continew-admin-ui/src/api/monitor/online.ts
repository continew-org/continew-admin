import axios from 'axios';
import qs from 'query-string';

export interface OnlineUserRecord {
  token: string;
  username: string;
  nickname: string;
  clientIp: string;
  location: string;
  browser: string;
  loginTime: string;
}

export interface OnlineUserParams extends Partial<OnlineUserRecord> {
  page: number;
  size: number;
  sort: Array<string>;
}
export interface OnlineUserListRes {
  list: OnlineUserRecord[];
  total: number;
}

export function queryOnlineUserList(params: OnlineUserParams) {
  return axios.get<OnlineUserListRes>('/monitor/online/user', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function kickout(token: string) {
  return axios.delete(`/monitor/online/user/${token}`);
}