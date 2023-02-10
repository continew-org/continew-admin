import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/dept';

export interface DeptRecord {
  deptId?: number;
  deptName: string;
  parentId?: number;
  deptSort: number;
  description?: string;
  status?: number;
  createUserString?: string;
  createTime?: string;
  updateUserString?: string;
  updateTime?: string;
  children?: Array<DeptRecord>;
  parentName?: string;
}

export interface DeptParam {
  deptName?: string;
  status?: number;
}

export function listDept(params: DeptParam) {
  return axios.get<DeptRecord[]>(`${BASE_URL}/all`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function getDept(id: number) {
  return axios.get<DeptRecord>(`${BASE_URL}/${id}`);
}

export function createDept(req: DeptRecord) {
  return axios.post(BASE_URL, req);
}

export function updateDept(req: DeptRecord) {
  return axios.put(BASE_URL, req);
}

export function deleteDept(ids: number | Array<number>) {
  return axios.delete(`${BASE_URL}/${ids}`);
}

export function exportDept(params: DeptParam) {
  return axios.get(`${BASE_URL}/export`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
    responseType: 'blob',
  });
}
