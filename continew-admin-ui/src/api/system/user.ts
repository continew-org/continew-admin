import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/user';

export interface UserRecord {
  userId?: number;
  username: string;
  nickname: string;
  gender: number;
  email?: string;
  phone?: string;
  description?: string;
  roleIds?: Array<number>;
  deptId?: number;
  status?: number;
  createUserString?: string;
  createTime?: string;
  updateUserString?: string;
  updateTime?: string;
  deptName?: string;
  roleNames?: Array<string>;
  disabled?: boolean;
}

export interface UserParam {
  username?: string;
  status?: number;
  createTime?: Array<string>;
  page?: number;
  size?: number;
  sort?: Array<string>;
}

export interface UserListRes {
  list: UserRecord[];
  total: number;
}

export function listUser(params: UserParam) {
  return axios.get<UserListRes>(`${BASE_URL}`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function getUser(id: number) {
  return axios.get<UserRecord>(`${BASE_URL}/${id}`);
}

export function addUser(req: UserRecord) {
  return axios.post(BASE_URL, req);
}

export function updateUser(req: UserRecord) {
  return axios.put(BASE_URL, req);
}

export function deleteUser(ids: number | Array<number>) {
  return axios.delete(`${BASE_URL}/${ids}`);
}
