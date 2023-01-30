import axios from 'axios';
import qs from 'query-string';

export interface DeptRecord {
  deptId?: number;
  deptName: string;
  parentId?: number;
  deptSort: number;
  description: string;
  status?: number;
  createUserString: string;
  createTime: string;
  children: Array<DeptRecord>,
}

export interface DeptParams {
  deptName?: string;
  status?: number;
}

export function getDeptList(params: DeptParams) {
  return axios.get<DeptRecord[]>('/system/dept/all', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export interface DeptReq {
  parentId: number;
  deptName: string;
  deptSort: number;
  description: string;
}
export function createDept(req: DeptReq) {
  return axios.post('/system/dept', req);
}

export interface UpdateDeptReq extends Partial<DeptReq> {
  deptId: number;
  status?: number;
}
export function updateDept(req: UpdateDeptReq) {
  return axios.put(`/system/dept`, req);
}

export function deleteDept(ids: Array<number>) {
  return axios.delete(`/system/dept/${ids}`);
}