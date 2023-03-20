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

package top.charles7c.cnadmin.webapi.controller.system;

import static top.charles7c.cnadmin.common.annotation.CrudRequestMapping.Api;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;

import top.charles7c.cnadmin.common.annotation.CrudRequestMapping;
import top.charles7c.cnadmin.common.base.BaseController;
import top.charles7c.cnadmin.common.base.BaseRequest;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.common.util.URLUtils;
import top.charles7c.cnadmin.common.util.validate.ValidationUtils;
import top.charles7c.cnadmin.system.model.query.MenuQuery;
import top.charles7c.cnadmin.system.model.request.MenuRequest;
import top.charles7c.cnadmin.system.model.vo.MenuVO;
import top.charles7c.cnadmin.system.service.MenuService;

/**
 * 菜单管理 API
 *
 * @author Charles7c
 * @since 2023/2/15 20:35
 */
@Tag(name = "菜单管理 API")
@RestController
@CrudRequestMapping(value = "/system/menu", api = {Api.TREE, Api.GET, Api.ADD, Api.UPDATE, Api.DELETE, Api.EXPORT})
public class MenuController extends BaseController<MenuService, MenuVO, MenuVO, MenuQuery, MenuRequest> {

    @Override
    @SaCheckPermission("system:menu:add")
    protected R<Long> add(@Validated(BaseRequest.Add.class) @RequestBody MenuRequest request) {
        this.checkPath(request);
        return super.add(request);
    }

    @Override
    @SaCheckPermission("system:menu:update")
    protected R update(@Validated(BaseRequest.Update.class) @RequestBody MenuRequest request, @PathVariable Long id) {
        this.checkPath(request);
        return super.update(request, id);
    }

    /**
     * 检查路由地址格式
     * 
     * @param request
     *            创建或修改信息
     */
    private void checkPath(MenuRequest request) {
        Boolean isExternal = ObjectUtil.defaultIfNull(request.getIsExternal(), false);
        String path = request.getPath();
        ValidationUtils.throwIf(isExternal && !URLUtils.isHttpUrl(path), "路由地址格式错误，请以 http:// 或 https:// 开头");
        ValidationUtils.throwIf(!isExternal && URLUtils.isHttpUrl(path), "路由地址格式错误");
    }
}
