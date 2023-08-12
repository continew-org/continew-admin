import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/tool/generator';

export interface TableRecord {
  tableName: string;
  comment?: string;
  engine: string;
  charset: string;
  createTime?: string;
  isConfiged: boolean;
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

export interface FieldConfigRecord {
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
  createTime?: string;
}

export function listFieldConfig(tableName: string, requireSync: boolean) {
  return axios.get<FieldConfigRecord[]>(`${BASE_URL}/field/${tableName}?requireSync=${requireSync}`);
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
  createTime?: string;
  updateTime?: string;
}

export function getGenConfig(tableName: string) {
  return axios.get<GenConfigRecord>(`${BASE_URL}/config/${tableName}`);
}

export interface GeneratorConfigRecord {
  genConfig: GenConfigRecord;
  fieldConfigs: FieldConfigRecord[];
}

export function saveConfig(tableName: string, req: GeneratorConfigRecord) {
  return axios.post(`${BASE_URL}/config/${tableName}`, req);
}

export function generate(tableName: string) {
  return axios.post(`${BASE_URL}/${tableName}`);
}
