import axios from 'axios';

import qs from 'query-string';

export interface DeptRecord {
  deptId: string;
  deptName: string;
  parentId: string;
  deptSort: number;
  description: string;
  status: number;
  updateUserString: string;
  updateTime: string;
  children: Array<DeptRecord>,
}

export interface DeptParams extends Partial<DeptRecord> {
  page: number;
  size: number;
  sort: Array<string>;
}

export function getDeptList(params: DeptParams) {
  return axios.get<DeptRecord[]>('/system/dept', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}