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

package top.charles7c.continew.admin.system.service.impl;

import java.util.*;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.bean.BeanUtil;

import top.charles7c.continew.admin.common.constant.CacheConstants;
import top.charles7c.continew.admin.common.enums.DisEnableStatusEnum;
import top.charles7c.continew.admin.system.mapper.MenuMapper;
import top.charles7c.continew.admin.system.model.entity.MenuDO;
import top.charles7c.continew.admin.system.model.query.MenuQuery;
import top.charles7c.continew.admin.system.model.req.MenuReq;
import top.charles7c.continew.admin.system.model.resp.MenuResp;
import top.charles7c.continew.admin.system.service.MenuService;
import top.charles7c.continew.starter.core.util.validate.CheckUtils;
import top.charles7c.continew.starter.extension.crud.base.BaseServiceImpl;

/**
 * 菜单业务实现
 *
 * @author Charles7c
 * @since 2023/2/15 20:30
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = CacheConstants.MENU_KEY_PREFIX)
public class MenuServiceImpl extends BaseServiceImpl<MenuMapper, MenuDO, MenuResp, MenuResp, MenuQuery, MenuReq>
    implements MenuService {

    @Override
    @CacheEvict(allEntries = true)
    public Long add(MenuReq req) {
        String title = req.getTitle();
        CheckUtils.throwIf(this.isNameExists(title, req.getParentId(), null), "新增失败，[{}] 已存在", title);
        req.setStatus(DisEnableStatusEnum.ENABLE);
        return super.add(req);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void update(MenuReq req, Long id) {
        String title = req.getTitle();
        CheckUtils.throwIf(this.isNameExists(title, req.getParentId(), id), "修改失败，[{}] 已存在", title);
        super.update(req, id);
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.lambdaUpdate().in(MenuDO::getParentId, ids).remove();
        super.delete(ids);
    }

    @Override
    public Set<String> listPermissionByUserId(Long userId) {
        return baseMapper.selectPermissionByUserId(userId);
    }

    @Override
    @Cacheable(key = "#roleCode")
    public List<MenuResp> listByRoleCode(String roleCode) {
        List<MenuDO> menuList = baseMapper.selectListByRoleCode(roleCode);
        List<MenuResp> list = BeanUtil.copyToList(menuList, MenuResp.class);
        list.forEach(this::fill);
        return list;
    }

    @Override
    @Cacheable(key = "'ALL'")
    public List<MenuResp> list() {
        MenuQuery menuQuery = new MenuQuery();
        menuQuery.setStatus(DisEnableStatusEnum.ENABLE.getValue());
        return super.list(menuQuery, null);
    }

    /**
     * 名称是否存在
     *
     * @param name
     *            名称
     * @param parentId
     *            上级 ID
     * @param id
     *            ID
     * @return 是否存在
     */
    private boolean isNameExists(String name, Long parentId, Long id) {
        return baseMapper.lambdaQuery().eq(MenuDO::getTitle, name).eq(MenuDO::getParentId, parentId)
            .ne(null != id, MenuDO::getId, id).exists();
    }
}
