import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/tool/generator';

export interface TableRecord {
  tableName: string;
  comment?: string;
  engine: string;
  charset: string;
  createTime?: string;
}

export interface TableParam {
  tableName?: string;
}

export interface TableListRes {
  list: TableRecord[];
  total: number;
}

export function listTable(params: TableParam) {
  return axios.get<TableListRes>(`${BASE_URL}/table`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}
