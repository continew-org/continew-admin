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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.auth.LoginHandler;
import top.continew.admin.auth.LoginHandlerFactory;
import top.continew.admin.auth.enums.AuthTypeEnum;
import top.continew.admin.auth.model.req.LoginReq;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.model.resp.RouteResp;
import top.continew.admin.auth.service.AuthService;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.common.context.RoleContext;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.enums.MenuTypeEnum;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.model.resp.MenuResp;
import top.continew.admin.system.service.ClientService;
import top.continew.admin.system.service.MenuService;
import top.continew.admin.system.service.RoleService;
import top.continew.starter.core.validation.ValidationUtils;
import top.continew.starter.extension.crud.annotation.TreeField;
import top.continew.starter.extension.crud.autoconfigure.CrudProperties;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 认证业务实现
 *
 * @author Charles7c
 * @since 2022/12/21 21:49
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
    public LoginResp login(LoginReq req, HttpServletRequest request) {
        AuthTypeEnum authType = req.getAuthType();
        // 校验终端
        ClientResp client = clientService.getByClientId(req.getClientId());
        ValidationUtils.throwIfNull(client, "终端不存在");
        ValidationUtils.throwIf(DisEnableStatusEnum.DISABLE.equals(client.getStatus()), "终端已禁用");
        ValidationUtils.throwIf(!client.getAuthType().contains(authType.getValue()), "该终端暂未授权 [{}] 认证", authType
            .getDescription());
        // 获取处理器
        LoginHandler<LoginReq> loginHandler = loginHandlerFactory.getHandler(authType);
        // 登录前置处理
        loginHandler.preLogin(req, client, request);
        // 登录
        LoginResp loginResp = loginHandler.login(req, client, request);
        // 登录后置处理
        loginHandler.postLogin(req, client, request);
        return loginResp;
    }

    @Override
    public List<RouteResp> buildRouteTree(Long userId) {
        Set<RoleContext> roleSet = roleService.listByUserId(userId);
        if (CollUtil.isEmpty(roleSet)) {
            return new ArrayList<>(0);
        }
        // 查询菜单列表
        Set<MenuResp> menuSet = new LinkedHashSet<>();
        if (roleSet.stream().anyMatch(r -> SysConstants.SUPER_ROLE_ID.equals(r.getId()))) {
            menuSet.addAll(menuService.listByRoleId(SysConstants.SUPER_ROLE_ID));
        } else {
            roleSet.forEach(r -> menuSet.addAll(menuService.listByRoleId(r.getId())));
        }
        List<MenuResp> menuList = menuSet.stream().filter(m -> !MenuTypeEnum.BUTTON.equals(m.getType())).toList();
        if (CollUtil.isEmpty(menuList)) {
            return new ArrayList<>(0);
        }
        // 构建路由树
        TreeField treeField = MenuResp.class.getDeclaredAnnotation(TreeField.class);
        TreeNodeConfig treeNodeConfig = crudProperties.getTree().genTreeNodeConfig(treeField);
        List<Tree<Long>> treeList = TreeUtil.build(menuList, treeField.rootId(), treeNodeConfig, (m, tree) -> {
            tree.setId(m.getId());
            tree.setParentId(m.getParentId());
            tree.setName(m.getTitle());
            tree.setWeight(m.getSort());
            tree.putExtra("type", m.getType().getValue());
            tree.putExtra("path", m.getPath());
            tree.putExtra("name", m.getName());
            tree.putExtra("component", m.getComponent());
            tree.putExtra("redirect", m.getRedirect());
            tree.putExtra("icon", m.getIcon());
            tree.putExtra("isExternal", m.getIsExternal());
            tree.putExtra("isCache", m.getIsCache());
            tree.putExtra("isHidden", m.getIsHidden());
            tree.putExtra("permission", m.getPermission());
        });
        return BeanUtil.copyToList(treeList, RouteResp.class);
    }
}
