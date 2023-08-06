import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/tool/generator';

export interface TableRecord {
  tableName: string;
  comment?: string;
  engine: string;
  charset: string;
  createTime?: string;
}

export interface TableParam {
  tableName?: string;
}

export interface TableListRes {
  list: TableRecord[];
  total: number;
}

export function listTable(params: TableParam) {
  return axios.get<TableListRes>(`${BASE_URL}/table`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export interface ColumnMappingRecord {
  id: string;
  tableName: string;
  columnName: string;
  columnType: string;
  fieldName: string;
  fieldType: string;
  comment: string;
  sort: number;
  isRequired: boolean;
  showInList: boolean;
  showInAdd: boolean;
  showInUpdate: boolean;
  showInQuery: boolean;
  formType: string;
  queryType: string;
  createTime: string;
  updateTime: string;
}

export interface GenConfigRecord {
  id: string;
  tableName: string;
  moduleName: string;
  packageName: string;
  frontendPath: string;
  businessName: string;
  author: string;
  tablePrefix: string;
  isOverride: boolean;
  createTime: string;
  updateTime: string;
}

export function listColumnMapping(tableName: string) {
  return axios.get<ColumnMappingRecord[]>(`${BASE_URL}/column/${tableName}`);
}