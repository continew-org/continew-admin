import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/monitor/online/user';

export interface DataRecord {
  token: string;
  username: string;
  nickname: string;
  ip: string;
  address: string;
  browser: string;
  os: string;
  loginTime: string;
}

export interface ListParam extends Partial<DataRecord> {
  page: number;
  size: number;
  sort: Array<string>;
}

export interface ListRes {
  list: DataRecord[];
  total: number;
}

export function list(params: ListParam) {
  return axios.get<ListRes>(BASE_URL, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function kickout(token: string) {
  return axios.delete(`${BASE_URL}/${token}`);
}
