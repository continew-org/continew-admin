import axios from 'axios';
import qs from 'query-string';
import { DeptParams } from '@/api/system/dept';
import { TreeNodeData } from '@arco-design/web-vue';

export default function getDeptTree(params: DeptParams) {
  return axios.get<TreeNodeData[]>('/common/tree/dept', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}