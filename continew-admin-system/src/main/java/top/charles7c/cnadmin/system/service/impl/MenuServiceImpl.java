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

package top.charles7c.cnadmin.system.service.impl;

import java.util.*;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.bean.BeanUtil;

import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.common.constant.CacheConsts;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.mapper.MenuMapper;
import top.charles7c.cnadmin.system.model.entity.MenuDO;
import top.charles7c.cnadmin.system.model.query.MenuQuery;
import top.charles7c.cnadmin.system.model.request.MenuRequest;
import top.charles7c.cnadmin.system.model.vo.MenuVO;
import top.charles7c.cnadmin.system.service.MenuService;

/**
 * 菜单业务实现
 *
 * @author Charles7c
 * @since 2023/2/15 20:30
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = CacheConsts.MENU_KEY_PREFIX)
public class MenuServiceImpl extends BaseServiceImpl<MenuMapper, MenuDO, MenuVO, MenuVO, MenuQuery, MenuRequest>
    implements MenuService {

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Long add(MenuRequest request) {
        String title = request.getTitle();
        CheckUtils.throwIf(this.isNameExists(title, request.getParentId(), null), "新增失败，[{}] 已存在", title);
        request.setStatus(DisEnableStatusEnum.ENABLE);
        return super.add(request);
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void update(MenuRequest request, Long id) {
        String title = request.getTitle();
        CheckUtils.throwIf(this.isNameExists(title, request.getParentId(), id), "修改失败，[{}] 已存在", title);
        super.update(request, id);
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
    public List<MenuVO> listByRoleCode(String roleCode) {
        List<MenuDO> menuList = baseMapper.selectListByRoleCode(roleCode);
        List<MenuVO> list = BeanUtil.copyToList(menuList, MenuVO.class);
        list.forEach(this::fill);
        return list;
    }

    @Override
    @Cacheable(key = "'ALL'")
    public List<MenuVO> list() {
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
