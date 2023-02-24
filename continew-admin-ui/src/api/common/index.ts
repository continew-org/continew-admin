import axios from 'axios';
import qs from 'query-string';
import { DeptParam } from '@/api/system/dept';
import { MenuParam } from '@/api/system/menu';
import { RoleParam } from '@/api/system/role';
import { TreeNodeData } from '@arco-design/web-vue';

export function listDeptTree(params: DeptParam) {
  return axios.get<TreeNodeData[]>('/common/tree/dept', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function listMenuTree(params: MenuParam) {
  return axios.get<TreeNodeData[]>('/common/tree/menu', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function listRoleDict(params: RoleParam) {
  return axios.get<TreeNodeData[]>('/common/dict/role', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}
