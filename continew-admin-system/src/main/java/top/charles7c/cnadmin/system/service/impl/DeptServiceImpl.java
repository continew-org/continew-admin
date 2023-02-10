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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;

import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.common.base.BaseVO;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.util.ExcelUtils;
import top.charles7c.cnadmin.common.util.ExceptionUtils;
import top.charles7c.cnadmin.common.util.TreeUtils;
import top.charles7c.cnadmin.common.util.helper.QueryHelper;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.mapper.DeptMapper;
import top.charles7c.cnadmin.system.model.entity.DeptDO;
import top.charles7c.cnadmin.system.model.query.DeptQuery;
import top.charles7c.cnadmin.system.model.request.DeptRequest;
import top.charles7c.cnadmin.system.model.vo.DeptDetailVO;
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
public class DeptServiceImpl extends BaseServiceImpl<DeptMapper, DeptDO, DeptVO, DeptDetailVO, DeptQuery, DeptRequest>
    implements DeptService {

    private final UserService userService;

    @Override
    public List<DeptVO> list(DeptQuery query) {
        List<DeptDO> deptList = this.listDept(query);
        List<DeptVO> list = BeanUtil.copyToList(deptList, DeptVO.class);
        list.forEach(this::fill);
        return list;
    }

    /**
     * 查询列表
     *
     * @param query
     *            查询条件
     * @return 列表信息
     */
    private List<DeptDO> listDept(DeptQuery query) {
        QueryWrapper<DeptDO> queryWrapper = QueryHelper.build(query);
        queryWrapper.lambda().orderByAsc(DeptDO::getParentId).orderByAsc(DeptDO::getDeptSort)
            .orderByDesc(DeptDO::getCreateTime);
        List<DeptDO> deptList = baseMapper.selectList(queryWrapper);
        return CollUtil.isNotEmpty(deptList) ? deptList : Collections.emptyList();
    }

    @Override
    public DeptDetailVO get(Long id) {
        DeptDetailVO deptDetailVO = super.get(id);
        this.fillDetail(deptDetailVO);
        return deptDetailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DeptRequest request) {
        String deptName = request.getDeptName();
        boolean isExist = this.checkNameExist(deptName, request.getParentId(), request.getDeptId());
        CheckUtils.throwIf(() -> isExist, String.format("新增失败，'%s'已存在", deptName));

        // 保存部门信息
        DeptDO deptDO = BeanUtil.copyProperties(request, DeptDO.class);
        deptDO.setStatus(DisEnableStatusEnum.ENABLE);
        baseMapper.insert(deptDO);
        return deptDO.getDeptId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DeptRequest request) {
        String deptName = request.getDeptName();
        boolean isExist = this.checkNameExist(deptName, request.getParentId(), request.getDeptId());
        CheckUtils.throwIf(() -> isExist, String.format("新增失败，'%s'已存在", deptName));

        super.update(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        super.delete(ids);
        baseMapper.delete(Wrappers.<DeptDO>lambdaQuery().in(DeptDO::getParentId, ids));
    }

    @Override
    public List<DeptVO> buildListTree(List<DeptVO> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        // 去除重复子部门列表
        List<DeptVO> deDuplicationList = deDuplication(list);
        return deDuplicationList.stream().map(d -> d.setChildren(this.getChildren(d, list)))
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
        List<DeptVO> deDuplicationList = new ArrayList<>();
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
                deDuplicationList.add(outerDept);
            }
        }
        return deDuplicationList;
    }

    /**
     * 获取指定部门的子部门列表
     *
     * @param deptVO
     *            指定部门
     * @param list
     *            部门列表
     * @return 子部门列表
     */
    private List<DeptVO> getChildren(DeptVO deptVO, List<DeptVO> list) {
        return list.stream().filter(d -> Objects.equals(d.getParentId(), deptVO.getDeptId()))
            .map(d -> d.setChildren(this.getChildren(d, list))).collect(Collectors.toList());
    }

    @Override
    public List<Tree<Long>> buildTree(List<DeptVO> list) {
        return TreeUtils.build(list, (d, tree) -> {
            tree.setId(d.getDeptId());
            tree.setName(d.getDeptName());
            tree.setParentId(d.getParentId());
            tree.setWeight(d.getDeptSort());
        });
    }

    @Override
    public void export(DeptQuery query, HttpServletResponse response) {
        List<DeptDO> deptList = this.listDept(query);
        List<DeptDetailVO> list = BeanUtil.copyToList(deptList, DeptDetailVO.class);
        list.forEach(this::fillDetail);
        ExcelUtils.export(list, "部门数据", DeptDetailVO.class, response);
    }

    /**
     * 检查名称是否存在
     *
     * @param deptName
     *            部门名称
     * @param parentId
     *            上级部门 ID
     * @param deptId
     *            部门 ID
     * @return 是否存在
     */
    private boolean checkNameExist(String deptName, Long parentId, Long deptId) {
        return baseMapper.exists(Wrappers.<DeptDO>lambdaQuery().eq(DeptDO::getDeptName, deptName)
            .eq(DeptDO::getParentId, parentId).ne(deptId != null, DeptDO::getDeptId, deptId));
    }

    /**
     * 填充数据
     *
     * @param baseVO
     *            待填充列表信息
     */
    private void fill(BaseVO baseVO) {
        Long createUser = baseVO.getCreateUser();
        if (createUser == null) {
            return;
        }
        baseVO.setCreateUserString(ExceptionUtils.exToNull(() -> userService.getById(createUser)).getNickname());
    }

    /**
     * 填充详情数据
     *
     * @param detailVO
     *            待填充详情信息
     */
    private void fillDetail(DeptDetailVO detailVO) {
        this.fill(detailVO);

        Long updateUser = detailVO.getUpdateUser();
        if (updateUser == null) {
            return;
        }
        detailVO.setUpdateUserString(ExceptionUtils.exToNull(() -> userService.getById(updateUser)).getNickname());
        detailVO.setParentName(ExceptionUtils.exToNull(() -> this.get(detailVO.getParentId()).getDeptName()));
    }
}
