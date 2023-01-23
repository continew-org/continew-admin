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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;

import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.util.ExceptionUtils;
import top.charles7c.cnadmin.common.util.TreeUtils;
import top.charles7c.cnadmin.common.util.helper.QueryHelper;
import top.charles7c.cnadmin.system.mapper.DeptMapper;
import top.charles7c.cnadmin.system.model.entity.SysDept;
import top.charles7c.cnadmin.system.model.query.DeptQuery;
import top.charles7c.cnadmin.system.model.request.CreateDeptRequest;
import top.charles7c.cnadmin.system.model.vo.DeptVO;
import top.charles7c.cnadmin.system.service.DeptService;
import top.charles7c.cnadmin.system.service.UserService;

/**
 * 部门业务实现类
 *
 * @author Charles7c
 * @since 2023/1/22 17:55
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;
    private final UserService userService;

    @Override
    public List<DeptVO> list(DeptQuery query) {
        QueryWrapper<SysDept> queryWrapper = QueryHelper.build(query);
        queryWrapper.lambda().orderByAsc(SysDept::getParentId).orderByAsc(SysDept::getDeptSort)
            .orderByDesc(SysDept::getUpdateTime);
        List<SysDept> list = deptMapper.selectList(queryWrapper);
        List<DeptVO> voList = BeanUtil.copyToList(list, DeptVO.class);
        voList.forEach(this::fill);
        return voList;
    }

    @Override
    public List<DeptVO> buildListTree(List<DeptVO> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        // 去重
        List<DeptVO> deDuplicationDeptList = deDuplication(list);
        return deDuplicationDeptList.stream().map(d -> d.setChildren(this.getChildren(d, list)))
            .collect(Collectors.toList());
    }

    /**
     * 数据去重（去除重复子部门列表）
     *
     * @param list
     *            部门列表
     * @return 去重后部门列表
     */
    private List<DeptVO> deDuplication(List<DeptVO> list) {
        List<DeptVO> deptList = new ArrayList<>();
        for (DeptVO outerDept : list) {
            boolean flag = true;
            for (DeptVO innerDept : list) {
                // 忽略重复子列表
                if (innerDept.getDeptId().equals(outerDept.getParentId())) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                deptList.add(outerDept);
            }
        }
        return deptList;
    }

    /**
     * 获取指定部门的子部门列表
     *
     * @param dept
     *            指定部门
     * @param list
     *            部门列表
     * @return 子部门列表
     */
    private List<DeptVO> getChildren(DeptVO dept, List<DeptVO> list) {
        return list.stream().filter(d -> Objects.equals(d.getParentId(), dept.getDeptId()))
            .map(d -> d.setChildren(this.getChildren(d, list))).collect(Collectors.toList());
    }

    @Override
    public List<Tree<Long>> buildTree(List<DeptVO> list) {
        return TreeUtils.build(list, (dept, tree) -> {
            tree.setId(dept.getDeptId());
            tree.setName(dept.getDeptName());
            tree.setParentId(dept.getParentId());
            tree.setWeight(dept.getDeptSort());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CreateDeptRequest request) {
        SysDept sysDept = BeanUtil.copyProperties(request, SysDept.class);
        sysDept.setStatus(DisEnableStatusEnum.ENABLE);
        deptMapper.insert(sysDept);
        return sysDept.getDeptId();
    }

    @Override
    public boolean checkDeptNameExist(String deptName, Long parentId, Long deptId) {
        return deptMapper.exists(Wrappers.<SysDept>lambdaQuery().eq(SysDept::getDeptName, deptName)
            .eq(SysDept::getParentId, parentId).ne(deptId != null, SysDept::getDeptId, deptId));
    }

    /**
     * 填充数据
     *
     * @param vo
     *            VO
     */
    private void fill(DeptVO vo) {
        Long updateUser = vo.getUpdateUser();
        if (updateUser == null) {
            return;
        }
        vo.setUpdateUserString(ExceptionUtils.exToNull(() -> userService.getById(vo.getUpdateUser())).getNickname());
    }
}
