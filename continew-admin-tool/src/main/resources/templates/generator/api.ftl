import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/${apiModuleName}/${apiName}';

export interface ${classNamePrefix}Record {
<#if fieldConfigs??>
<#list fieldConfigs as fieldConfig>
  <#if fieldConfig.showInList>
  ${fieldConfig.fieldName}: string;
  </#if>
</#list>
</#if>
}

export interface ${classNamePrefix}Param {
<#if fieldConfigs??>
<#list fieldConfigs as fieldConfig>
  <#if fieldConfig.showInQuery>
  ${fieldConfig.fieldName}?: string;
  </#if>
</#list>
</#if>
  page?: number;
  size?: number;
  sort?: Array<string>;
}

export interface ${classNamePrefix}ListRes {
  list: ${classNamePrefix}Record[];
  total: number;
}

export function list${classNamePrefix}(params: ${classNamePrefix}Param) {
  return axios.get<${classNamePrefix}ListRes>(`${BASE_URL}`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}

export function get${classNamePrefix}(id: string) {
  return axios.get<${classNamePrefix}Record>(`${BASE_URL}/${id}`);
}

export function add${classNamePrefix}(req: ${classNamePrefix}Record) {
  return axios.post(BASE_URL, req);
}

export function update${classNamePrefix}(req: ${classNamePrefix}Record, id: string) {
  return axios.put(`${BASE_URL}/${id}`, req);
}

export function delete${classNamePrefix}(ids: string | Array<string>) {
  return axios.delete(`${BASE_URL}/${ids}`);
}
