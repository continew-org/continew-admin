import http from '@/utils/http'

const BASE_URL = '/${apiModuleName}/${apiName}'

export interface ${classNamePrefix}Resp {
<#if fieldConfigs??>
<#list fieldConfigs as fieldConfig>
  <#if fieldConfig.showInList>
  ${fieldConfig.fieldName}: string
  </#if>
</#list>
  createUserString: string
  updateUserString: string
</#if>
}
export interface ${classNamePrefix}DetailResp {
<#if fieldConfigs??>
<#list fieldConfigs as fieldConfig>
  ${fieldConfig.fieldName}: string
</#list>
  createUserString: string
  updateUserString: string
</#if>
}
export interface ${classNamePrefix}Query {
<#if fieldConfigs??>
<#list fieldConfigs as fieldConfig>
  <#if fieldConfig.showInQuery>
  ${fieldConfig.fieldName}: string
  </#if>
</#list>
</#if>
  sort: Array<string>
}
export interface ${classNamePrefix}PageQuery extends ${classNamePrefix}Query, PageQuery {}

/** @desc 查询${businessName}列表 */
export function list${classNamePrefix}(query: ${classNamePrefix}PageQuery) {
  return http.get<PageRes<${classNamePrefix}Resp[]>>(`${'$'}{BASE_URL}`, query)
}

/** @desc 查询${businessName}详情 */
export function get${classNamePrefix}(id: string) {
  return http.get<${classNamePrefix}DetailResp>(`${'$'}{BASE_URL}/${'$'}{id}`)
}

/** @desc 新增${businessName} */
export function add${classNamePrefix}(data: any) {
  return http.post(`${'$'}{BASE_URL}`, data)
}

/** @desc 修改${businessName} */
export function update${classNamePrefix}(data: any, id: string) {
  return http.put(`${'$'}{BASE_URL}/${'$'}{id}`, data)
}

/** @desc 删除${businessName} */
export function delete${classNamePrefix}(id: string) {
  return http.del(`${'$'}{BASE_URL}/${'$'}{id}`)
}

/** @desc 导出${businessName} */
export function export${classNamePrefix}(query: ${classNamePrefix}Query) {
  return http.download<any>(`${'$'}{BASE_URL}/export`, query)
}
