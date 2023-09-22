import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/option';

export interface BasicConfigRecord {
  site_title?: string;
  site_copyright?: string;
  site_logo?: string;
  site_favicon?: string;
}

export interface DataRecord {
  name?: string;
  code: string;
  value: string;
  description?: string;
}

export interface ListParam {
  code?: Array<string>;
}

export function list(params: ListParam) {
  return axios.get<DataRecord[]>(`${BASE_URL}`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function save(req: DataRecord[]) {
  return axios.patch(`${BASE_URL}`, req);
}

export function resetValue(params: ListParam) {
  return axios.patch(`${BASE_URL}/value`, params);
}
