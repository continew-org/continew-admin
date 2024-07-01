package ${packageName}.${subPackageName};

import top.continew.starter.extension.crud.service.BaseService;
import ${packageName}.model.query.${classNamePrefix}Query;
import ${packageName}.model.req.${classNamePrefix}Req;
import ${packageName}.model.resp.${classNamePrefix}DetailResp;
import ${packageName}.model.resp.${classNamePrefix}Resp;

/**
 * ${businessName}业务接口
 *
 * @author ${author}
 * @since ${date}
 */
public interface ${className} extends BaseService<${classNamePrefix}Resp, ${classNamePrefix}DetailResp, ${classNamePrefix}Query, ${classNamePrefix}Req> {}