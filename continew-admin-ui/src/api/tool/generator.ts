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
  tableName: string;
  columnName: string;
  columnType: string;
  fieldName: string;
  fieldType: string;
  comment: string;
  isRequired: boolean;
  showInList: boolean;
  showInForm: boolean;
  showInQuery: boolean;
  formType: string;
  queryType: string;
  createTime: string;
  updateTime: string;
}

export function listColumnMapping(tableName: string) {
  return axios.get<ColumnMappingRecord[]>(`${BASE_URL}/column/${tableName}`);
}

export interface GenConfigRecord {
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

export function getGenConfig(tableName: string) {
  return axios.get<GenConfigRecord>(`${BASE_URL}/table/${tableName}`);
}

export interface GeneratorConfigRecord {
  genConfig: GenConfigRecord;
  columnMappings: ColumnMappingRecord[];
}

export function saveConfig(tableName: string, req: GeneratorConfigRecord) {
  return axios.post(`${BASE_URL}/table/${tableName}`, req);
}
