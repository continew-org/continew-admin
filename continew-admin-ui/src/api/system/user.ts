import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/user';

export interface DataRecord {
  id?: number;
  username: string;
  nickname: string;
  gender: number;
  email?: string;
  phone?: string;
  description?: string;
  status?: number;
  isSystem?: boolean;
  pwdResetTime?: string;
  createUserString?: string;
  createTime?: string;
  updateUserString?: string;
  updateTime?: string;
  deptId?: number;
  deptName?: string;
  roleIds?: Array<number>;
  roleNames?: Array<string>;
  disabled?: boolean;
}

export interface ListParam {
  username?: string;
  status?: number;
  createTime?: Array<string>;
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

export function resetPassword(id: number) {
  return axios.patch(`${BASE_URL}/${id}/password`);
}

export interface UpdateUserRoleReq {
  roleIds?: Array<number>;
}

export function updateUserRole(req: UpdateUserRoleReq, id: number) {
  return axios.patch(`${BASE_URL}/${id}/role`, req);
}
