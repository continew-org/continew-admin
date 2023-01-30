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

export interface CreateDeptReq {
  parentId: number;
  deptName: string;
  deptSort: number;
  description: string;
}
export function createDept(req: CreateDeptReq) {
  return axios.post('/system/dept', req);
}

export interface UpdateDeptStatusReq {
  status: number;
}
export function updateDeptStatus(ids: Array<number>, req: UpdateDeptStatusReq) {
  return axios.patch(`/system/dept/${ids}`, req);
}

export function deleteDept(ids: Array<number>) {
  return axios.delete(`/system/dept/${ids}`);
}