import axios from 'axios';
import qs from 'query-string';

export interface LoginLogRecord {
  logId: string;
  status: number;
  clientIp: string;
  location: string;
  browser: string;
  errorMsg: string;
  createUserString: string;
  createTime: string;
}

export interface LoginLogParams extends Partial<LoginLogRecord> {
  page: number;
  size: number;
  sort: Array<string>;
}

export interface LoginLogListRes {
  list: LoginLogRecord[];
  total: number;
}

export function queryLoginLogList(params: LoginLogParams) {
  return axios.get<LoginLogListRes>('/monitor/log/login', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}