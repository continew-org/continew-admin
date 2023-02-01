import axios from 'axios';
import qs from 'query-string';

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
  children?: Array<DeptRecord>,
}

export interface DeptParams {
  deptName?: string;
  status?: number;
}

export function listDept(params: DeptParams) {
  return axios.get<DeptRecord[]>('/system/dept/all', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function getDept(id: number) {
  return axios.get<DeptRecord>(`/system/dept/${id}`);
}

export function createDept(req: DeptRecord) {
  return axios.post('/system/dept', req);
}

export function updateDept(req: DeptRecord) {
  return axios.put(`/system/dept`, req);
}

export function deleteDept(ids: number | Array<number>) {
  return axios.delete(`/system/dept/${ids}`);
}