import axios from 'axios';

const BASE_URL = '/system/file';

export interface FileItem {
  id: string
  type: string
  name: string
  extendName: string
  src: string | null
  updateTime: string
  isDir: boolean
  filePath: string
  [propName: string]: any // 一个 interface 中任意属性只能有一个
}

interface PageRes<T> {
  records: T;
  total: number;
}

export function getFileList() {
  return axios.get<PageRes<FileItem[]>>(`${BASE_URL}/file`);
}
