import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/menu';

export interface MenuRecord {
  menuId?: number;
  menuName: string;
  parentId?: number;
  menuType: number;
  path?: string;
  name?: string;
  component?: string;
  icon?: string;
  isExternal: boolean;
  isCache: boolean;
  isHidden: boolean;
  permission?: string;
  menuSort: number;
  status?: number;
  createUserString?: string;
  createTime?: string;
  updateUserString?: string;
  updateTime?: string;
  children?: Array<MenuRecord>;
  parentName?: string;
}

export interface MenuParam {
  menuName?: string;
  status?: number;
}

export function listMenu(params: MenuParam) {
  return axios.get<MenuRecord[]>(`${BASE_URL}/list`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function getMenu(id: number) {
  return axios.get<MenuRecord>(`${BASE_URL}/${id}`);
}

export function createMenu(req: MenuRecord) {
  return axios.post(BASE_URL, req);
}

export function updateMenu(req: MenuRecord) {
  return axios.put(BASE_URL, req);
}

export function deleteMenu(ids: number | Array<number>) {
  return axios.delete(`${BASE_URL}/${ids}`);
}
