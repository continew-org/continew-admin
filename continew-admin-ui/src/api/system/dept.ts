import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/dept';

export interface DataRecord {
  id?: number;
  name?: string;
  parentId?: number;
  description?: string;
  sort?: number;
  status?: number;
  isSystem?: boolean;
  createUserString?: string;
  createTime?: string;
  updateUserString?: string;
  updateTime?: string;
  children?: Array<DataRecord>;
  parentName?: string;
  disabled?: boolean;
}

export interface ListParam {
  name?: string;
  status?: number;
}

export function list(params: ListParam) {
  return axios.get<DataRecord[]>(`${BASE_URL}/tree`, {
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
