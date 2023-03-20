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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.bean.BeanUtil;

import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.mapper.MenuMapper;
import top.charles7c.cnadmin.system.model.entity.MenuDO;
import top.charles7c.cnadmin.system.model.query.MenuQuery;
import top.charles7c.cnadmin.system.model.request.MenuRequest;
import top.charles7c.cnadmin.system.model.vo.MenuVO;
import top.charles7c.cnadmin.system.service.MenuService;

/**
 * 菜单业务实现类
 *
 * @author Charles7c
 * @since 2023/2/15 20:30
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends BaseServiceImpl<MenuMapper, MenuDO, MenuVO, MenuVO, MenuQuery, MenuRequest>
    implements MenuService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(MenuRequest request) {
        String title = request.getTitle();
        boolean isExists = this.checkNameExists(title, request.getParentId(), null);
        CheckUtils.throwIf(isExists, "新增失败，[{}] 已存在", title);

        request.setStatus(DisEnableStatusEnum.ENABLE);
        return super.add(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MenuRequest request, Long id) {
        String title = request.getTitle();
        boolean isExists = this.checkNameExists(title, request.getParentId(), id);
        CheckUtils.throwIf(isExists, "修改失败，[{}] 已存在", title);

        super.update(request, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        super.delete(ids);
        baseMapper.lambdaUpdate().in(MenuDO::getParentId, ids).remove();
    }

    @Override
    public Set<String> listPermissionByUserId(Long userId) {
        return baseMapper.selectPermissionByUserId(userId);
    }

    @Override
    public List<MenuVO> listByUserId(Long userId) {
        List<MenuDO> menuList = baseMapper.selectListByUserId(userId);
        List<MenuVO> list = BeanUtil.copyToList(menuList, MenuVO.class);
        list.forEach(this::fill);
        return list;
    }

    /**
     * 检查名称是否存在
     *
     * @param name
     *            名称
     * @param parentId
     *            上级 ID
     * @param id
     *            ID
     * @return 是否存在
     */
    private boolean checkNameExists(String name, Long parentId, Long id) {
        return baseMapper.lambdaQuery().eq(MenuDO::getTitle, name).eq(MenuDO::getParentId, parentId)
            .ne(id != null, MenuDO::getId, id).exists();
    }
}
