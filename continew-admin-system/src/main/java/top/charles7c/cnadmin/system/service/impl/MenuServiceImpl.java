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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;

import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.util.TreeUtils;
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
        String menuName = request.getMenuName();
        boolean isExists = this.checkNameExists(menuName, request.getParentId(), request.getMenuId());
        CheckUtils.throwIf(() -> isExists, String.format("新增失败，'%s'已存在", menuName));

        // 保存信息
        request.setStatus(DisEnableStatusEnum.ENABLE);
        return super.add(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MenuRequest request) {
        String menuName = request.getMenuName();
        boolean isExists = this.checkNameExists(menuName, request.getParentId(), request.getMenuId());
        CheckUtils.throwIf(() -> isExists, String.format("修改失败，'%s'已存在", menuName));

        super.update(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        super.delete(ids);
        super.lambdaUpdate().in(MenuDO::getParentId, ids).remove();
    }

    @Override
    public List<MenuVO> buildListTree(List<MenuVO> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        // 去除重复子菜单列表
        List<MenuVO> deDuplicationList = deDuplication(list);
        return deDuplicationList.stream().map(m -> m.setChildren(this.getChildren(m, list)))
            .collect(Collectors.toList());
    }

    /**
     * 数据去重（去除重复子菜单列表）
     *
     * @param list
     *            菜单列表
     * @return 去重后菜单列表
     */
    private List<MenuVO> deDuplication(List<MenuVO> list) {
        List<MenuVO> deDuplicationList = new ArrayList<>();
        for (MenuVO outer : list) {
            boolean flag = true;
            for (MenuVO inner : list) {
                // 忽略重复子列表
                if (inner.getMenuId().equals(outer.getParentId())) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                deDuplicationList.add(outer);
            }
        }
        return deDuplicationList;
    }

    /**
     * 获取指定菜单的子菜单列表
     *
     * @param menuVO
     *            指定菜单
     * @param list
     *            菜单列表
     * @return 子菜单列表
     */
    private List<MenuVO> getChildren(MenuVO menuVO, List<MenuVO> list) {
        return list.stream().filter(m -> Objects.equals(m.getParentId(), menuVO.getMenuId()))
            .map(m -> m.setChildren(this.getChildren(m, list))).collect(Collectors.toList());
    }

    @Override
    public List<Tree<Long>> buildTree(List<MenuVO> list) {
        return TreeUtils.build(list, (m, tree) -> {
            tree.setId(m.getMenuId());
            tree.setName(m.getMenuName());
            tree.setParentId(m.getParentId());
            tree.setWeight(m.getMenuSort());
        });
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
        return super.lambdaQuery().eq(MenuDO::getMenuName, name).eq(MenuDO::getParentId, parentId)
            .ne(id != null, MenuDO::getMenuId, id).exists();
    }
}
