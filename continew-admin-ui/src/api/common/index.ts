import axios from 'axios';
import qs from 'query-string';
import { DeptParam } from '@/api/system/dept';
import { TreeNodeData } from '@arco-design/web-vue';

export default function listDeptTree(params: DeptParam) {
  return axios.get<TreeNodeData[]>('/common/tree/dept', {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}