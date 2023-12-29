import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/file';

export interface FileItem {
  id: string;
  name: string;
  size: number;
  url: string;
  extension: string;
  mimeType?: string;
  type?: string;
  storageId?: string;
  createUser?: string;
  createTime?: string;
  updateUser?: string;
  updateTime: string;
  createUserString?: string;
  updateUserString?: string;
}

export interface ListParam {
  name?: string;
  type?: string;
  updateTime?: string;
  page?: number;
  size?: number;
  sort?: Array<string>;
}

export interface PageRes<T> {
  total: number;
  list: T;
}

export function list(params: ListParam) {
  return axios.get<PageRes<FileItem[]>>(`${BASE_URL}`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function get(id: string) {
  return axios.get<FileItem>(`${BASE_URL}/${id}`);
}

export interface FileItemUpdate {
  name: string;
}

export function update(req: FileItemUpdate, id: string) {
  return axios.put(`${BASE_URL}/${id}`, req);
}

export function del(ids: string | Array<string>) {
  return axios.delete(`${BASE_URL}/${ids}`);
}
