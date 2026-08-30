/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.continew.admin.common.base.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.sign.template.SaSignTemplate;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.extra.spring.SpringUtil;
import top.continew.admin.common.base.service.BaseService;
import top.continew.admin.common.config.crud.CrudApiPermissionPrefixCache;
import top.continew.starter.auth.satoken.autoconfigure.SaTokenExtensionProperties;
import top.continew.starter.core.util.ServletUtils;
import top.continew.starter.core.util.SpringUtils;
import top.continew.starter.extension.crud.annotation.CrudApi;
import top.continew.starter.extension.crud.controller.AbstractCrudController;
import top.continew.starter.extension.crud.enums.Api;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * 控制器基类
 *
 * <p>
 * 根据实际项目需要，自行重写 CRUD 接口或增加自定义通用业务接口
 * </p>
 *
 * @param <S> 业务接口
 * @param <L> 列表类型
 * @param <D> 详情类型
 * @param <Q> 查询条件类型
 * @param <C> 创建或修改请求参数类型
 * @author Charles7c
 * @since 2024/12/6 20:30
 */
public class BaseController<S extends BaseService<L, D, Q, C>, L, D, Q, C>
    extends AbstractCrudController<S, L, D, Q, C> {

    @Override
    public void preHandle(CrudApi crudApi, Object[] args, Method targetMethod, Class<?> targetClass)
        throws Exception {
        // 忽略带 sign 请求权限校验
        SaRequest saRequest = SaHolder.getRequest();
        Collection<String> paramNames = saRequest.getParamNames();
        if (paramNames.stream().anyMatch(SaSignTemplate.sign::equals)) {
            return;
        }
        // 忽略接口类或接口方法上带 @SaIgnore 注解的权限校验
        if (AnnotationUtil.hasAnnotation(targetMethod, SaIgnore.class) || AnnotationUtil
            .hasAnnotation(targetClass, SaIgnore.class)) {
            return;
        }
        // 忽略排除（放行）路径
        SaTokenExtensionProperties saTokenExtensionProperties =
            SpringUtil.getBean(SaTokenExtensionProperties.class);
        if (saTokenExtensionProperties.isEnabled()) {
            String[] excludePatterns = saTokenExtensionProperties.getSecurity().getExcludes();
            if (SpringUtils.isMatch(ServletUtils.getRequest().getServletPath(), excludePatterns)) {
                return;
            }
        }
        // 不需要校验 DICT、DICT_TREE 接口权限
        if (Api.DICT.equals(crudApi.value()) || Api.TREE_DICT.equals(crudApi.value())) {
            return;
        }
        // 校验权限，例如：创建用户接口（POST /system/user） => 校验 system:user:create 权限
        String permissionPrefix = CrudApiPermissionPrefixCache.get(targetClass);
        String apiName = getApiName(crudApi.value());
        StpUtil.checkPermission("%s:%s".formatted(permissionPrefix, apiName.toLowerCase()));
    }

    /**
     * 获取 API 名称
     *
     * @param api API
     * @return API 名称
     */
    public static String getApiName(Api api) {
        return switch (api) {
            case PAGE, TREE, LIST -> Api.LIST.name();
            case DELETE, BATCH_DELETE -> Api.DELETE.name();
            default -> api.name();
        };
    }
}
