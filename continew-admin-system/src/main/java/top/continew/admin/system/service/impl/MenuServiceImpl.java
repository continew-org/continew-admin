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
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.enums.MenuTypeEnum;
import top.continew.admin.system.mapper.MenuMapper;
import top.continew.admin.system.model.entity.MenuDO;
import top.continew.admin.system.model.query.MenuQuery;
import top.continew.admin.system.model.req.MenuReq;
import top.continew.admin.system.model.resp.MenuResp;
import top.continew.admin.system.service.MenuService;
import top.continew.starter.core.util.validate.CheckUtils;
import top.continew.starter.extension.crud.service.impl.BaseServiceImpl;

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

    @Override
    @CacheInvalidate(key = "'ALL'", name = CacheConstants.MENU_KEY_PREFIX)
    public Long add(MenuReq req) {
        String title = req.getTitle();
        CheckUtils.throwIf(this.isNameExists(title, req.getParentId(), null), "新增失败，[{}] 已存在", title);
        // 目录类型菜单，默认为 Layout
        if (MenuTypeEnum.DIR.equals(req.getType())) {
            req.setComponent(StrUtil.blankToDefault(req.getComponent(), "Layout"));
        }
        return super.add(req);
    }

    @Override
    @CacheInvalidate(key = "'ALL'", name = CacheConstants.MENU_KEY_PREFIX)
    public void update(MenuReq req, Long id) {
        String title = req.getTitle();
        CheckUtils.throwIf(this.isNameExists(title, req.getParentId(), id), "修改失败，[{}] 已存在", title);
        MenuDO oldMenu = super.getById(id);
        CheckUtils.throwIfNotEqual(req.getType(), oldMenu.getType(), "不允许修改菜单类型");
        super.update(req, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheInvalidate(key = "'ALL'", name = CacheConstants.MENU_KEY_PREFIX)
    public void delete(List<Long> ids) {
        baseMapper.lambdaUpdate().in(MenuDO::getParentId, ids).remove();
        super.delete(ids);
    }

    @Override
    @Cached(key = "'ALL'", name = CacheConstants.MENU_KEY_PREFIX, cacheType = CacheType.BOTH, syncLocal = true)
    public List<MenuResp> listAll() {
        return super.list(new MenuQuery(DisEnableStatusEnum.ENABLE.getValue()), null);
    }

    @Override
    public Set<String> listPermissionByUserId(Long userId) {
        return baseMapper.selectPermissionByUserId(userId);
    }

    @Override
    @Cached(key = "#roleCode", name = CacheConstants.MENU_KEY_PREFIX)
    public List<MenuResp> listByRoleCode(String roleCode) {
        List<MenuDO> menuList = baseMapper.selectListByRoleCode(roleCode);
        List<MenuResp> list = BeanUtil.copyToList(menuList, MenuResp.class);
        list.forEach(this::fill);
        return list;
    }

    /**
     * 名称是否存在
     *
     * @param name     名称
     * @param parentId 上级 ID
     * @param id       ID
     * @return 是否存在
     */
    private boolean isNameExists(String name, Long parentId, Long id) {
        return baseMapper.lambdaQuery()
            .eq(MenuDO::getTitle, name)
            .eq(MenuDO::getParentId, parentId)
            .ne(null != id, MenuDO::getId, id)
            .exists();
    }
}
