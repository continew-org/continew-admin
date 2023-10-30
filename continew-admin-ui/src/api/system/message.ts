import axios from 'axios';
import qs from 'query-string';
import { DataRecord } from "@/api/system/dict";

const BASE_URL = '/system/message';

export interface MessageRecord {
  id?: number;
  type?: string;
  title?: string;
  subTitle?: string;
  avatar?: string;
  content?: string;
  createTime?: string;
  readStatus?: 0 | 1;
  messageType?: number;
}

export interface ChatRecord {
  id: number;
  username: string;
  content: string;
  time: string;
  isCollect: boolean;
}

export interface ListParam {
  title?: string;
  readStatus?: 0 | 1;
  type?: string;
  page?: number;
  size?: number;
  uid?:number
  sort?: Array<string>;
}

export interface PageRes {
  list: DataRecord[];
  total: number;
}

export type MessageListType = MessageRecord[];

export function page(params?: ListParam) {
  return axios.get<PageRes>(`${BASE_URL}`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function list(params?: ListParam) {
  return axios.get<MessageListType>(`${BASE_URL}/list`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function get(id: number) {
  return axios.get<MessageRecord>(`${BASE_URL}/${id}`);
}

export function del(ids: number | Array<number>) {
  return axios.delete(`${BASE_URL}/${ids}`);
}

export function read(data: Array<number>) {
  return axios.patch<MessageListType>(`${BASE_URL}/read?ids=${data}`);
}

export function queryChatList() {
  return axios.get<ChatRecord[]>('/api/chat/list');
}
