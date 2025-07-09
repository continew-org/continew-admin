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

package top.continew.admin.tenant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.base.controller.BaseController;
import top.continew.admin.common.config.properties.TenantProperties;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.model.entity.MenuDO;
import top.continew.admin.system.model.query.MenuQuery;
import top.continew.admin.system.service.MenuService;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.admin.tenant.model.query.TenantPackageQuery;
import top.continew.admin.tenant.model.req.TenantPackageReq;
import top.continew.admin.tenant.model.resp.TenantPackageDetailResp;
import top.continew.admin.tenant.model.resp.TenantPackageResp;
import top.continew.admin.tenant.service.TenantPackageService;
import top.continew.admin.tenant.service.TenantService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;
import top.continew.starter.extension.tenant.TenantHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 租户套餐管理 API
 *
 * @author 小熊
 * @since 2024/11/26 11:25
 */
@Tag(name = "租户套餐管理 API")
@RestController
@AllArgsConstructor
@CrudRequestMapping(value = "/tenant/package", api = {Api.LIST, Api.PAGE, Api.GET, Api.CREATE, Api.UPDATE, Api.DELETE})
public class TenantPackageController extends BaseController<TenantPackageService, TenantPackageResp, TenantPackageDetailResp, TenantPackageQuery, TenantPackageReq> {

    private final MenuService menuService;
    private final TenantProperties tenantProperties;
    private final TenantService tenantService;

    @GetMapping("/menuTree")
    @SaCheckPermission("tenant:package:get")
    @Operation(summary = "获取租户套餐菜单", description = "获取租户套餐菜单")
    public List<Tree<Long>> menuTree() {
        MenuQuery query = new MenuQuery();
        //必须是启用状态的菜单
        query.setStatus(DisEnableStatusEnum.ENABLE);
        //过滤掉租户不能使用的菜单
        query.setExcludeMenuIdList(tenantProperties.getIgnoreMenus());
        return menuService.tree(query, null, true);
    }

    @Override
    @DSTransactional
    public void update(TenantPackageReq req, Long id) {
        //查询套餐对应的租户
        List<TenantDO> tenantDOList = tenantService.list(Wrappers.lambdaQuery(TenantDO.class)
            .eq(TenantDO::getPackageId, id));
        if (!tenantDOList.isEmpty()) {
            TenantPackageDetailResp detail = baseService.get(id);
            List<Long> oldMenuIds = detail.getMenuIds();
            List<Long> newMenuIds = Arrays.stream(req.getMenuIds()).toList();
            //删除的菜单
            List<Long> deleteMenuIds = new ArrayList<>(oldMenuIds);
            deleteMenuIds.removeAll(newMenuIds);
            //如果有删除的菜单则绑定了套餐的租户对应的菜单也会删除
            if (!deleteMenuIds.isEmpty()) {
                List<MenuDO> deleteMenus = menuService.listByIds(deleteMenuIds);
                tenantDOList.forEach(tenantDO -> SpringUtil.getBean(TenantHandler.class)
                    .execute(tenantDO.getId(), () -> menuService.deleteTenantMenus(deleteMenus)));
            }
            //新增的菜单
            List<Long> addMenuIds = new ArrayList<>(newMenuIds);
            addMenuIds.removeAll(oldMenuIds);
            //如果有新增的菜单则绑定了套餐的租户对应的菜单也会新增
            if (!addMenuIds.isEmpty()) {
                List<MenuDO> addMenus = menuService.listByIds(addMenuIds);
                for (MenuDO addMenu : addMenus) {
                    MenuDO pMenu = addMenu.getParentId() != 0 ? menuService.getById(addMenu.getParentId()) : null;
                    tenantDOList.forEach(tenantDO -> SpringUtil.getBean(TenantHandler.class)
                        .execute(tenantDO.getId(), () -> menuService.addTenantMenu(addMenu, pMenu)));
                }
                RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
            }
        }
        super.update(req, id);
    }
}