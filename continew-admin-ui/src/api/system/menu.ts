import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/menu';

export interface MenuRecord {
  id?: string;
  title: string;
  parentId?: string;
  type: number;
  path?: string;
  name?: string;
  component?: string;
  icon?: string;
  isExternal: boolean;
  isCache: boolean;
  isHidden: boolean;
  permission?: string;
  sort: number;
  status?: number;
  createUserString?: string;
  createTime?: string;
  updateUserString?: string;
  updateTime?: string;
  children?: Array<MenuRecord>;
  parentName?: string;
}

export interface MenuParam {
  name?: string;
  status?: number;
}

export function listMenu(params: MenuParam) {
  return axios.get<MenuRecord[]>(`${BASE_URL}/tree`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function getMenu(id: string) {
  return axios.get<MenuRecord>(`${BASE_URL}/${id}`);
}

export function addMenu(req: MenuRecord) {
  return axios.post(BASE_URL, req);
}

export function updateMenu(req: MenuRecord, id: string) {
  return axios.put(`${BASE_URL}/${id}`, req);
}

export function deleteMenu(ids: string | Array<string>) {
  return axios.delete(`${BASE_URL}/${ids}`);
}
