import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/menu';

export interface DataRecord {
  id?: number;
  title?: string;
  parentId?: number;
  type?: number;
  path?: string;
  name?: string;
  component?: string;
  icon?: string;
  isExternal?: boolean;
  isCache?: boolean;
  isHidden?: boolean;
  permission?: string;
  sort?: number;
  status?: number;
  createUserString?: string;
  createTime?: string;
  updateUserString?: string;
  updateTime?: string;
  children?: Array<DataRecord>;
  parentName?: string;
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
