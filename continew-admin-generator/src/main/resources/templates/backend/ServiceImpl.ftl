package ${packageName}.${subPackageName};

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import top.continew.starter.extension.crud.service.impl.BaseServiceImpl;
import ${packageName}.mapper.${classNamePrefix}Mapper;
import ${packageName}.model.entity.${classNamePrefix}DO;
import ${packageName}.model.query.${classNamePrefix}Query;
import ${packageName}.model.req.${classNamePrefix}Req;
import ${packageName}.model.resp.${classNamePrefix}DetailResp;
import ${packageName}.model.resp.${classNamePrefix}Resp;
import ${packageName}.service.${classNamePrefix}Service;

/**
 * ${businessName}业务实现
 *
 * @author ${author}
 * @since ${date}
 */
@Service
@RequiredArgsConstructor
public class ${className} extends BaseServiceImpl<${classNamePrefix}Mapper, ${classNamePrefix}DO, ${classNamePrefix}Resp, ${classNamePrefix}DetailResp, ${classNamePrefix}Query, ${classNamePrefix}Req> implements ${classNamePrefix}Service {}