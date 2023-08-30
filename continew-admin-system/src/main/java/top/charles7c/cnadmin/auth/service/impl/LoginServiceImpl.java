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

package top.charles7c.cnadmin.auth.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;

import top.charles7c.cnadmin.auth.model.vo.MetaVO;
import top.charles7c.cnadmin.auth.model.vo.RouteVO;
import top.charles7c.cnadmin.auth.service.LoginService;
import top.charles7c.cnadmin.auth.service.PermissionService;
import top.charles7c.cnadmin.common.annotation.TreeField;
import top.charles7c.cnadmin.common.constant.SysConsts;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.enums.MenuTypeEnum;
import top.charles7c.cnadmin.common.model.dto.LoginUser;
import top.charles7c.cnadmin.common.util.SecureUtils;
import top.charles7c.cnadmin.common.util.TreeUtils;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.model.entity.UserDO;
import top.charles7c.cnadmin.system.model.query.MenuQuery;
import top.charles7c.cnadmin.system.model.vo.DeptDetailVO;
import top.charles7c.cnadmin.system.model.vo.MenuVO;
import top.charles7c.cnadmin.system.service.DeptService;
import top.charles7c.cnadmin.system.service.MenuService;
import top.charles7c.cnadmin.system.service.RoleService;
import top.charles7c.cnadmin.system.service.UserService;

/**
 * 登录业务实现
 *
 * @author Charles7c
 * @since 2022/12/21 21:49
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserService userService;
    private final DeptService deptService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final PermissionService permissionService;

    @Override
    public String login(String username, String password) {
        UserDO user = userService.getByUsername(username);
        CheckUtils.throwIfNull(user, "用户名或密码错误");
        Long userId = user.getId();
        CheckUtils.throwIfNotEqual(SecureUtils.md5Salt(password, userId.toString()), user.getPassword(), "用户名或密码错误");
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, user.getStatus(), "此账号已被禁用，如有疑问，请联系管理员");
        DeptDetailVO deptDetailVO = deptService.get(user.getDeptId());
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, deptDetailVO.getStatus(), "此账号部门已被禁用，如有疑问，请联系管理员");

        // 登录
        LoginUser loginUser = BeanUtil.copyProperties(user, LoginUser.class);
        loginUser.setPermissions(permissionService.listPermissionByUserId(userId));
        loginUser.setRoles(permissionService.listRoleCodeByUserId(userId));
        loginUser.setRoleSet(roleService.listByUserId(userId));
        LoginHelper.login(loginUser);

        // 返回令牌
        return StpUtil.getTokenValue();
    }

    @Override
    public List<RouteVO> buildRouteTree(Long userId) {
        Set<String> roleSet = permissionService.listRoleCodeByUserId(userId);
        if (CollUtil.isEmpty(roleSet)) {
            return new ArrayList<>(0);
        }

        // 查询菜单列表
        List<MenuVO> menuList;
        if (roleSet.contains(SysConsts.ADMIN_ROLE_CODE)) {
            MenuQuery menuQuery = new MenuQuery();
            menuQuery.setStatus(DisEnableStatusEnum.ENABLE.getValue());
            menuList = menuService.list(menuQuery, null);
        } else {
            menuList = menuService.listByUserId(userId);
        }
        menuList.removeIf(m -> MenuTypeEnum.BUTTON.equals(m.getType()));

        // 构建路由树
        TreeField treeField = MenuVO.class.getDeclaredAnnotation(TreeField.class);
        TreeNodeConfig treeNodeConfig = TreeUtils.genTreeNodeConfig(treeField);
        List<Tree<Long>> treeList = TreeUtils.build(menuList, treeNodeConfig, (m, tree) -> {
            tree.setId(m.getId());
            tree.setParentId(m.getParentId());
            tree.setName(m.getTitle());
            tree.setWeight(m.getSort());

            tree.putExtra("path", m.getPath());
            tree.putExtra("name", m.getName());
            tree.putExtra("component", m.getComponent());
            MetaVO metaVO = new MetaVO();
            metaVO.setLocale(m.getTitle());
            metaVO.setIcon(m.getIcon());
            metaVO.setIgnoreCache(!m.getIsCache());
            metaVO.setHideInMenu(m.getIsHidden());
            metaVO.setOrder(m.getSort());
            tree.putExtra("meta", metaVO);
        });
        return BeanUtil.copyToList(treeList, RouteVO.class);
    }
}
