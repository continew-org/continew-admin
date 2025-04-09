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

package top.continew.admin.common.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.sign.SaSignTemplate;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.text.CharSequenceUtil;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.extension.crud.annotation.CrudApi;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.controller.AbstractBaseController;
import top.continew.starter.extension.crud.enums.Api;
import top.continew.starter.extension.crud.service.BaseService;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * 控制器基类
 *
 * @param <S> 业务接口
 * @param <L> 列表类型
 * @param <D> 详情类型
 * @param <Q> 查询条件
 * @param <C> 创建或修改参数类型
 * @author Charles7c
 * @since 2024/12/6 20:30
 */
public class BaseController<S extends BaseService<L, D, Q, C>, L, D, Q, C> extends AbstractBaseController<S, L, D, Q, C> {

    @Override
    public void preHandle(CrudApi crudApi, Object[] args, Method targetMethod, Class<?> targetClass) throws Exception {
        SaRequest saRequest = SaHolder.getRequest();
        Collection<String> paramNames = saRequest.getParamNames();
        if (paramNames.stream().anyMatch(SaSignTemplate.sign::equals)) {
            return;
        }
        if (AnnotationUtil.hasAnnotation(targetMethod, SaIgnore.class) || AnnotationUtil
            .hasAnnotation(targetClass, SaIgnore.class)) {
            return;
        }
        CrudRequestMapping crudRequestMapping = targetClass.getDeclaredAnnotation(CrudRequestMapping.class);
        String path = crudRequestMapping.value();
        String prefix = String.join(StringConstants.COLON, CharSequenceUtil.splitTrim(path, StringConstants.SLASH));
        Api api = crudApi.value();
        String apiName = Api.PAGE.equals(api) || Api.TREE.equals(api) ? Api.LIST.name() : api.name();
        StpUtil.checkPermission("%s:%s".formatted(prefix, apiName.toLowerCase()));
    }
}
