import axios from 'axios';
import qs from 'query-string';

export interface DeptRecord {
  deptId: string;
  deptName: string;
  parentId: number;
  deptSort: number;
  description: string;
  status: number;
  updateUserString: string;
  updateTime: string;
  children: Array<DeptRecord>,
}

export interface DeptParams {
  deptName?: string;
  status?: number;
}

export function getDeptList(params: DeptParams) {
  return axios.get<DeptRecord[]>('/system/dept', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export interface CreateDeptReq {
  parentId: number;
  deptName: string;
  deptSort: number;
  description: string;
}
export function createDept(req: CreateDeptReq) {
  return axios.post('/system/dept', req);
}