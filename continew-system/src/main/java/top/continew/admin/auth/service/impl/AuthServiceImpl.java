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

package top.continew.admin.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.auth.LoginHandler;
import top.continew.admin.auth.LoginHandlerFactory;
import top.continew.admin.auth.enums.AuthTypeEnum;
import top.continew.admin.auth.model.req.LoginReq;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.model.resp.RouteResp;
import top.continew.admin.auth.service.AuthService;
import top.continew.admin.common.context.RoleContext;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.constant.SystemConstants;
import top.continew.admin.system.enums.MenuTypeEnum;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.model.resp.MenuResp;
import top.continew.admin.system.service.ClientService;
import top.continew.admin.system.service.MenuService;
import top.continew.admin.system.service.RoleService;
import top.continew.starter.core.exception.BadRequestException;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.extension.crud.annotation.TreeField;
import top.continew.starter.extension.crud.autoconfigure.CrudProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 认证业务实现
 *
 * @author Charles7c
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final LoginHandlerFactory loginHandlerFactory;
    private final ClientService clientService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final CrudProperties crudProperties;

    @Override
    public LoginResp login(LoginReq req, HttpServletRequest request,
        HttpServletResponse response) {
        AuthTypeEnum authType = req.getAuthType();
        // 校验客户端
        ClientResp client = clientService.getByClientId(req.getClientId());
        if (client == null) {
            throw new BadRequestException("客户端不存在");
        }
        ValidationUtils.throwIf(DisEnableStatusEnum.DISABLE.equals(client.getStatus()), "客户端已禁用");
        ValidationUtils.throwIf(!client.getAuthType().contains(authType.getValue()),
            "该客户端暂未授权 [{}] 认证", authType
                .getDescription());
        // 获取处理器
        LoginHandler<LoginReq> loginHandler = loginHandlerFactory.getHandler(authType);
        if (loginHandler == null) {
            throw new BadRequestException("不支持的认证类型 [" + authType.getDescription() + "]");
        }
        // 登录前置处理
        loginHandler.preLogin(req, client, request);
        // 登录
        LoginResp resp = loginHandler.login(req, client, request, response);
        // 登录后置处理
        loginHandler.postLogin(req, client, request);
        return resp;
    }

    @Override
    public List<RouteResp> buildRouteTree(Long userId) {
        Set<RoleContext> roles = roleService.listByUserId(userId);
        if (CollUtil.isEmpty(roles)) {
            return new ArrayList<>(0);
        }
        // 查询菜单列表
        Set<MenuResp> menus = new LinkedHashSet<>();
        if (roles.stream()
            .anyMatch(role -> SystemConstants.SUPER_ADMIN_ROLE_ID.equals(role.getId()))) {
            menus.addAll(menuService.listByRoleId(SystemConstants.SUPER_ADMIN_ROLE_ID));
        } else {
            roles.forEach(role -> menus.addAll(menuService.listByRoleId(role.getId())));
        }
        List<MenuResp> routeMenus =
            menus.stream().filter(menu -> !MenuTypeEnum.BUTTON.equals(menu.getType()))
                .toList();
        if (CollUtil.isEmpty(routeMenus)) {
            return new ArrayList<>(0);
        }
        // 构建路由树
        TreeField treeField = MenuResp.class.getDeclaredAnnotation(TreeField.class);
        TreeNodeConfig treeNodeConfig =
            crudProperties.getTreeDictModel().genTreeNodeConfig(treeField);
        List<Tree<Long>> routeTrees =
            TreeUtil.build(routeMenus, treeField.rootId(), treeNodeConfig, (menu, tree) -> {
                tree.setId(menu.getId());
                tree.setParentId(menu.getParentId());
                tree.setName(menu.getTitle());
                tree.setWeight(menu.getSort());
                tree.putExtra("type", menu.getType().getValue());
                tree.putExtra("path", menu.getPath());
                tree.putExtra("name", menu.getName());
                tree.putExtra("component", menu.getComponent());
                tree.putExtra("redirect", menu.getRedirect());
                tree.putExtra("icon", menu.getIcon());
                tree.putExtra("isExternal", menu.getIsExternal());
                tree.putExtra("isCache", menu.getIsCache());
                tree.putExtra("isHidden", menu.getIsHidden());
                tree.putExtra("permission", menu.getPermission());
            });
        return BeanUtil.copyToList(routeTrees, RouteResp.class);
    }
}
