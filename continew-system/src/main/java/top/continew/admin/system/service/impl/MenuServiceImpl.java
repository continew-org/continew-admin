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

package top.continew.admin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.enums.MenuTypeEnum;
import top.continew.admin.system.mapper.MenuMapper;
import top.continew.admin.system.mapper.RoleMapper;
import top.continew.admin.system.model.entity.MenuDO;
import top.continew.admin.system.model.entity.RoleDO;
import top.continew.admin.system.model.entity.RoleMenuDO;
import top.continew.admin.system.model.query.MenuQuery;
import top.continew.admin.system.model.req.MenuReq;
import top.continew.admin.system.model.resp.MenuResp;
import top.continew.admin.system.service.MenuService;
import top.continew.admin.system.service.RoleMenuService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.validation.CheckUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 菜单业务实现
 *
 * @author Charles7c
 * @since 2023/2/15 20:30
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends BaseServiceImpl<MenuMapper, MenuDO, MenuResp, MenuResp, MenuQuery, MenuReq> implements MenuService {

    private final RoleMenuService roleMenuService;
    private final RoleMapper roleMapper;

    @Override
    public Long create(MenuReq req) {
        String title = req.getTitle();
        CheckUtils.throwIf(this.isTitleExists(title, req.getParentId(), null), "新增失败，标题 [{}] 已存在", title);
        // 目录和菜单的组件名称不能重复
        if (!MenuTypeEnum.BUTTON.equals(req.getType())) {
            String name = req.getName();
            CheckUtils.throwIf(this.isNameExists(name, null), "新增失败，组件名称 [{}] 已存在", name);
        }
        // 目录类型菜单，默认为 Layout
        if (MenuTypeEnum.DIR.equals(req.getType())) {
            req.setComponent(StrUtil.blankToDefault(req.getComponent(), "Layout"));
        }
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
        return super.create(req);
    }

    @Override
    public void update(MenuReq req, Long id) {
        String title = req.getTitle();
        CheckUtils.throwIf(this.isTitleExists(title, req.getParentId(), id), "修改失败，标题 [{}] 已存在", title);
        // 目录和菜单的组件名称不能重复
        if (!MenuTypeEnum.BUTTON.equals(req.getType())) {
            String name = req.getName();
            CheckUtils.throwIf(this.isNameExists(name, id), "修改失败，组件名称 [{}] 已存在", name);
        }
        MenuDO oldMenu = super.getById(id);
        CheckUtils.throwIfNotEqual(req.getType(), oldMenu.getType(), "不允许修改菜单类型");
        super.update(req, id);
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.lambdaUpdate().in(MenuDO::getParentId, ids).remove();
        super.delete(ids);
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
    }

    @Override
    @Cached(key = "'ALL' + #tenantId", name = CacheConstants.ROLE_MENU_KEY_PREFIX)
    public List<MenuResp> listAll(Long tenantId) {
        return super.list(new MenuQuery(DisEnableStatusEnum.ENABLE), null);
    }

    @Override
    public Set<String> listPermissionByUserId(Long userId) {
        return baseMapper.selectPermissionByUserId(userId);
    }

    @Cached(key = "#roleId", name = CacheConstants.ROLE_MENU_KEY_PREFIX)
    public List<MenuResp> listByRoleId(Long roleId) {
        if (SysConstants.SUPER_ROLE_ID.equals(roleId)) {
            return super.list(new MenuQuery(DisEnableStatusEnum.ENABLE), null);
        }
        List<MenuDO> menuList = baseMapper.selectListByRoleId(roleId);
        List<MenuResp> list = BeanUtil.copyToList(menuList, MenuResp.class);
        list.forEach(super::fill);
        return list;
    }

    @Override
    public void menuInit(List<MenuDO> menuList, Long oldParentId, Long newParentId) {
        List<MenuDO> children = menuList.stream().filter(menuDO -> menuDO.getParentId().equals(oldParentId)).toList();
        for (MenuDO menuDO : children) {
            Long oldId = menuDO.getId();
            menuDO.setId(null);
            menuDO.setParentId(newParentId);
            save(menuDO);
            menuInit(menuList, oldId, menuDO.getId());
        }
    }

    @Override
    public void deleteTenantMenus(List<MenuDO> menuList) {
        if (!menuList.isEmpty()) {
            List<Long> delIds = new ArrayList<>();
            for (MenuDO menuDO : menuList) {
                MenuDO tMenu = getOne(Wrappers.query(MenuDO.class)
                    .eq(menuDO.getType().equals(MenuTypeEnum.BUTTON.getValue()), "CONCAT(title,permission)", menuDO
                        .getTitle() + menuDO.getPermission())
                    .eq(!menuDO.getType().equals(MenuTypeEnum.BUTTON.getValue()), "name", menuDO.getName()));
                if (tMenu != null) {
                    delIds.add(tMenu.getId());
                }
            }
            if (!delIds.isEmpty()) {
                //菜单删除
                delete(delIds);
                //绑定关系删除
                roleMenuService.remove(Wrappers.lambdaQuery(RoleMenuDO.class).in(RoleMenuDO::getMenuId, delIds));
            }
        }
    }

    @Override
    public void addTenantMenu(MenuDO menu, MenuDO pMenu) {
        Long pId = 0l;
        if (pMenu != null) {
            MenuDO tPMenu = getOne(Wrappers.query(MenuDO.class)
                .eq(pMenu.getType().equals(MenuTypeEnum.BUTTON.getValue()), "CONCAT(title,permission)", pMenu
                    .getTitle() + pMenu.getPermission())
                .eq(!pMenu.getType().equals(MenuTypeEnum.BUTTON.getValue()), "name", pMenu.getName()));
            pId = tPMenu.getId();
        }
        menu.setId(null);
        menu.setParentId(pId);
        //菜单新增
        save(menu);
        //管理员绑定菜单
        RoleDO roleDO = roleMapper.selectOne(Wrappers.lambdaQuery(RoleDO.class)
            .eq(RoleDO::getCode, SysConstants.TENANT_ADMIN_CODE));
        RoleMenuDO roleMenuDO = new RoleMenuDO();
        roleMenuDO.setRoleId(roleDO.getId());
        roleMenuDO.setMenuId(menu.getId());
        roleMenuService.save(roleMenuDO);
    }

    /**
     * 标题是否存在
     *
     * @param title    标题
     * @param parentId 上级 ID
     * @param id       ID
     * @return true：存在；false：不存在
     */
    private boolean isTitleExists(String title, Long parentId, Long id) {
        return baseMapper.lambdaQuery()
            .eq(MenuDO::getTitle, title)
            .eq(MenuDO::getParentId, parentId)
            .ne(id != null, MenuDO::getId, id)
            .exists();
    }

    /**
     * 名称是否存在
     *
     * @param name 标题
     * @param id   ID
     * @return true：存在；false：不存在
     */
    private boolean isNameExists(String name, Long id) {
        return baseMapper.lambdaQuery()
            .eq(MenuDO::getName, name)
            .ne(MenuDO::getType, MenuTypeEnum.BUTTON)
            .ne(id != null, MenuDO::getId, id)
            .exists();
    }
}
