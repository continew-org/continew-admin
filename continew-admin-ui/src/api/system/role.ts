import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/role';

export interface RoleRecord {
  roleId?: number;
  roleName: string;
  roleCode?: string;
  roleSort?: number;
  description?: string;
  menuIds?: Array<number>;
  dataScope: number;
  deptIds?: Array<number>;
  status?: number;
  createUserString?: string;
  createTime?: string;
  updateUserString?: string;
  updateTime?: string;
  disabled?: boolean;
}

export interface RoleParam {
  roleName?: string;
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

export function getRole(id: number) {
  return axios.get<RoleRecord>(`${BASE_URL}/${id}`);
}

export function addRole(req: RoleRecord) {
  return axios.post(BASE_URL, req);
}

export function updateRole(req: RoleRecord) {
  return axios.put(BASE_URL, req);
}

export function deleteRole(ids: number | Array<number>) {
  return axios.delete(`${BASE_URL}/${ids}`);
}
