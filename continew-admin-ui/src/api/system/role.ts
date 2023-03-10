import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/role';

export interface RoleRecord {
  id?: string;
  name: string;
  code?: string;
  sort?: number;
  description?: string;
  menuIds?: Array<string>;
  dataScope: number;
  deptIds?: Array<string>;
  status?: number;
  createUserString?: string;
  createTime?: string;
  updateUserString?: string;
  updateTime?: string;
  disabled?: boolean;
}

export interface RoleParam {
  name?: string;
  status?: number;
  page?: number;
  size?: number;
  sort?: Array<string>;
}

export interface RoleListRes {
  list: RoleRecord[];
  total: number;
}

export function listRole(params: RoleParam) {
  return axios.get<RoleListRes>(`${BASE_URL}`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function getRole(id: string) {
  return axios.get<RoleRecord>(`${BASE_URL}/${id}`);
}

export function addRole(req: RoleRecord) {
  return axios.post(BASE_URL, req);
}

export function updateRole(req: RoleRecord, id: string) {
  return axios.put(`${BASE_URL}/${id}`, req);
}

export function deleteRole(ids: string | Array<string>) {
  return axios.delete(`${BASE_URL}/${ids}`);
}
