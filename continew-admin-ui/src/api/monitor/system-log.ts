import axios from 'axios';
import qs from 'query-string';

export interface SystemLogRecord {
  logId: string;
  statusCode: number;
  requestMethod: string;
  requestUrl: string;
  elapsedTime: number;
  clientIp: string;
  location: string;
  browser: string;
  errorMsg: string;
  exceptionDetail?: string;
  createUserString: string;
  createTime: string;
}

export interface SystemLogParams extends Partial<SystemLogRecord> {
  page: number;
  size: number;
  sort: Array<string>;
}

export interface SystemLogListRes {
  list: SystemLogRecord[];
  total: number;
}

export function querySystemLogList(params: SystemLogParams) {
  return axios.get<SystemLogListRes>('/monitor/log/system', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export interface SystemLogDetailRecord {
  logId: string;
  description: string;
  requestUrl: string;
  requestMethod: string;
  requestHeaders: string;
  requestBody: string;
  statusCode: number;
  responseHeaders: string;
  responseBody: string;
  elapsedTime: number;
  clientIp: string;
  location: string;
  browser: string;
  createUserString: string;
  createTime: string;
}

export function querySystemLogDetail(logId: string) {
  return axios.get<SystemLogDetailRecord>(`/monitor/log/system/${logId}`);
}