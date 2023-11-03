import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/message';

export interface DataRecord {
  id: number;
  title: string;
  content: string;
  type: number;
  createUserString?: string;
  createTime: string;
  isRead: boolean;
  readTime: string;
}

export interface ListParam {
  title?: string;
  type?: number;
  isRead?: boolean;
  page?: number;
  size?: number;
  sort?: Array<string>;
}

export interface ListRes {
  list: DataRecord[];
  total: number;
}

export function list(params: ListParam) {
  return axios.get<ListRes>(`${BASE_URL}`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function del(ids: number | Array<number>) {
  return axios.delete(`${BASE_URL}/${ids}`);
}

export function read(ids: Array<number>) {
  return axios.patch(`${BASE_URL}/read?ids=${ids}`);
}

export interface MessageTypeUnreadRes {
  type: number;
  count: number;
}

export interface MessageUnreadRes {
  total: number;
  details: MessageTypeUnreadRes[];
}

export function countUnread(detail: boolean) {
  return axios.get<MessageUnreadRes>(`${BASE_URL}/unread?detail=${detail}`);
}
