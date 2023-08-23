import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/dept';

export interface DataRecord {
  id?: string;
  name: string;
  parentId?: string;
  description?: string;
  sort: number;
  status?: number;
  type?: number;
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

export function get(id: string) {
  return axios.get<DataRecord>(`${BASE_URL}/${id}`);
}

export function add(req: DataRecord) {
  return axios.post(BASE_URL, req);
}

export function update(req: DataRecord, id: string) {
  return axios.put(`${BASE_URL}/${id}`, req);
}

export function del(ids: string | Array<string>) {
  return axios.delete(`${BASE_URL}/${ids}`);
}
