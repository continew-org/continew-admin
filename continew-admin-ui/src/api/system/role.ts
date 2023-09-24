import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/role';

export interface DataRecord {
  id?: number;
  name?: string;
  code?: string;
  sort?: number;
  description?: string;
  menuIds?: Array<number>;
  dataScope?: number;
  deptIds?: Array<number>;
  status?: number;
  isSystem?: boolean;
  createUserString?: string;
  createTime?: string;
  updateUserString?: string;
  updateTime?: string;
  disabled?: boolean;
}

export interface ListParam {
  name?: string;
  status?: number;
  page?: number;
  size?: number;
  sort?: Array<string>;
}

export interface ListRes {
  list: DataRecord[];
  total: number;
}

export function list(params: ListParam) {
  return axios.get<ListRes>(`${BASE_URL}`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function get(id: number) {
  return axios.get<DataRecord>(`${BASE_URL}/${id}`);
}

export function add(req: DataRecord) {
  return axios.post(BASE_URL, req);
}

export function update(req: DataRecord, id: number) {
  return axios.put(`${BASE_URL}/${id}`, req);
}

export function del(ids: number | Array<number>) {
  return axios.delete(`${BASE_URL}/${ids}`);
}
