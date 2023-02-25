import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/system/post';

export interface PostRecord {
  postId?: number;
  postName: string;
  postSort?: number;
  description?: string;
  status?: number;
  createUserString?: string;
  createTime?: string;
  updateUserString?: string;
  updateTime?: string;
}

export interface PostParam {
  postName?: string;
  status?: number;
  page?: number;
  size?: number;
  sort?: Array<string>;
}

export interface PostListRes {
  list: PostRecord[];
  total: number;
}

export function listPost(params: PostParam) {
  return axios.get<PostListRes>(`${BASE_URL}`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function getPost(id: number) {
  return axios.get<PostRecord>(`${BASE_URL}/${id}`);
}

export function addPost(req: PostRecord) {
  return axios.post(BASE_URL, req);
}

export function updatePost(req: PostRecord) {
  return axios.put(BASE_URL, req);
}

export function deletePost(ids: number | Array<number>) {
  return axios.delete(`${BASE_URL}/${ids}`);
}
