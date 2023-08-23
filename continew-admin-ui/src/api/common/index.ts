import axios from 'axios';
import qs from 'query-string';
import { ListParam as DeptParam } from '@/api/system/dept';
import { ListParam as MenuParam } from '@/api/system/menu';
import { ListParam as RoleParam } from '@/api/system/role';
import { TreeNodeData } from '@arco-design/web-vue';
import { LabelValueState } from '@/store/modules/dict/types';

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
  return axios.get<LabelValueState[]>('/common/dict/role', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function listEnumDict(enumTypeName: string) {
  return axios.get<LabelValueState[]>(`/common/dict/enum/${enumTypeName}`);
}
