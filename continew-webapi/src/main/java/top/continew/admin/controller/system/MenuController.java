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

package top.continew.admin.controller.system;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.controller.BaseController;
import top.continew.admin.system.model.query.MenuQuery;
import top.continew.admin.system.model.req.MenuReq;
import top.continew.admin.system.model.resp.MenuResp;
import top.continew.admin.system.service.MenuService;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.URLUtils;
import top.continew.starter.core.validation.ValidationUtils;
import top.continew.starter.extension.crud.annotation.CrudApi;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

import java.lang.reflect.Method;

/**
 * 菜单管理 API
 *
 * @author Charles7c
 * @since 2023/2/15 20:35
 */
@Tag(name = "菜单管理 API")
@RestController
@CrudRequestMapping(value = "/system/menu", api = {Api.TREE, Api.DETAIL, Api.ADD, Api.UPDATE, Api.DELETE})
public class MenuController extends BaseController<MenuService, MenuResp, MenuResp, MenuQuery, MenuReq> {

    @Override
    public void preHandle(CrudApi crudApi, Object[] args, Method targetMethod, Class<?> targetClass) throws Exception {
        super.preHandle(crudApi, args, targetMethod, targetClass);
        Api api = crudApi.value();
        if (!(Api.ADD.equals(api) || Api.UPDATE.equals(api))) {
            return;
        }
        MenuReq req = (MenuReq)args[0];
        Boolean isExternal = ObjectUtil.defaultIfNull(req.getIsExternal(), false);
        String path = req.getPath();
        ValidationUtils.throwIf(Boolean.TRUE.equals(isExternal) && !URLUtils
            .isHttpUrl(path), "路由地址格式错误，请以 http:// 或 https:// 开头");
        // 非外链菜单参数修正
        if (Boolean.FALSE.equals(isExternal)) {
            ValidationUtils.throwIf(URLUtils.isHttpUrl(path), "路由地址格式错误");
            req.setPath(StrUtil.isBlank(path) ? path : StrUtil.prependIfMissing(path, StringConstants.SLASH));
            req.setName(StrUtil.removePrefix(req.getName(), StringConstants.SLASH));
            req.setComponent(StrUtil.removePrefix(req.getComponent(), StringConstants.SLASH));
        }
    }
}
