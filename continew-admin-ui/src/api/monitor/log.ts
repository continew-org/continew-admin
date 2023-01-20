import axios from 'axios';
import qs from 'query-string';

export interface LogRecord {
  logId: string;
  clientIp: string;
  location: string;
  browser: string;
  createTime: string;
}

export interface LoginLogRecord extends LogRecord {
  description: string;
  status: number;
  errorMsg: string;
  createUserString: string;
}

export interface OperationLogRecord extends LogRecord {
  description: string;
  status: number;
  errorMsg: string;
  createUserString: string;
}

export interface SystemLogRecord extends LogRecord {
  statusCode: number;
  requestMethod: string;
  requestUrl: string;
  elapsedTime: number;
  exceptionDetail?: string;
}

export interface SystemLogDetailRecord extends SystemLogRecord {
  requestHeaders: string;
  requestBody: string;
  responseHeaders: string;
  responseBody: string;
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

export interface OperationLogParams extends Partial<OperationLogRecord> {
  page: number;
  size: number;
  sort: Array<string>;
  uid?: string;
}
export interface OperationLogListRes {
  list: OperationLogRecord[];
  total: number;
}
export function queryOperationLogList(params: OperationLogParams) {
  return axios.get<OperationLogListRes>('/monitor/log/operation', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
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

export function querySystemLogDetail(logId: string) {
  return axios.get<SystemLogDetailRecord>(`/monitor/log/system/${logId}`);
}