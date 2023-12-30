import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/storage';

export interface DataRecord {
  id?: number;
  name?: string;
  code?: string;
  type?: number;
  accessKey?: string;
  secretKey?: string;
  endpoint?: string;
  bucketName?: string;
  domain?: string;
  description?: string;
  isDefault?: boolean;
  sort?: number;
  status?: number;
  createUser?: string;
  createTime?: string;
  updateUser?: string;
  updateTime?: string;
  createUserString?: string;
  updateUserString?: string;
}

export interface ListParam {
  name?: string;
  status?: string;
  page?: number;
  size?: number;
  sort?: Array<string>;
}

export interface PageRes<T> {
  total: number;
  list: T;
}

export function list(params: ListParam) {
  return axios.get<PageRes<DataRecord[]>>(`${BASE_URL}`, {
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
