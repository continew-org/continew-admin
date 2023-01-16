import axios from 'axios';
import qs from 'query-string';

export interface OperationLogRecord {
  logId: string;
  description: string;
  status: number,
  clientIp: string,
  location: string,
  browser: string,
  createUserString: string;
  createTime: string;
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