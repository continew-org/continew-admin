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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.mapper.DeptMapper;
import top.continew.admin.system.model.entity.DeptDO;
import top.continew.admin.system.model.query.DeptQuery;
import top.continew.admin.system.model.req.DeptReq;
import top.continew.admin.system.model.resp.DeptResp;
import top.continew.admin.system.service.DeptService;
import top.continew.admin.system.service.RoleDeptService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.util.validate.CheckUtils;
import top.continew.starter.data.core.enums.DatabaseType;
import top.continew.starter.data.core.util.MetaUtils;
import top.continew.starter.extension.crud.service.impl.BaseServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 部门业务实现
 *
 * @author Charles7c
 * @since 2023/1/22 17:55
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends BaseServiceImpl<DeptMapper, DeptDO, DeptResp, DeptResp, DeptQuery, DeptReq> implements DeptService {

    @Resource
    private UserService userService;
    private final RoleDeptService roleDeptService;

    @Override
    public List<DeptDO> listChildren(Long id) {
        DatabaseType databaseType = MetaUtils.getDatabaseTypeOrDefault(SpringUtil
            .getBean(DynamicRoutingDataSource.class), DatabaseType.MYSQL);
        return baseMapper.lambdaQuery().apply(databaseType.findInSet(id, "ancestors")).list();
    }

    @Override
    protected void beforeAdd(DeptReq req) {
        String name = req.getName();
        boolean isExists = this.isNameExists(name, req.getParentId(), null);
        CheckUtils.throwIf(isExists, "新增失败，[{}] 已存在", name);
        req.setAncestors(this.getAncestors(req.getParentId()));
    }

    @Override
    protected void beforeUpdate(DeptReq req, Long id) {
        String name = req.getName();
        boolean isExists = this.isNameExists(name, req.getParentId(), id);
        CheckUtils.throwIf(isExists, "修改失败，[{}] 已存在", name);
        DeptDO oldDept = super.getById(id);
        String oldName = oldDept.getName();
        DisEnableStatusEnum newStatus = req.getStatus();
        Long oldParentId = oldDept.getParentId();
        if (Boolean.TRUE.equals(oldDept.getIsSystem())) {
            CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, newStatus, "[{}] 是系统内置部门，不允许禁用", oldName);
            CheckUtils.throwIfNotEqual(req.getParentId(), oldParentId, "[{}] 是系统内置部门，不允许变更上级部门", oldName);
        }
        // 启用/禁用部门
        if (ObjectUtil.notEqual(newStatus, oldDept.getStatus())) {
            List<DeptDO> children = this.listChildren(id);
            long enabledChildrenCount = children.stream()
                .filter(d -> DisEnableStatusEnum.ENABLE.equals(d.getStatus()))
                .count();
            CheckUtils.throwIf(DisEnableStatusEnum.DISABLE
                .equals(newStatus) && enabledChildrenCount > 0, "禁用 [{}] 前，请先禁用其所有下级部门", oldName);
            DeptDO oldParentDept = this.getByParentId(oldParentId);
            CheckUtils.throwIf(DisEnableStatusEnum.ENABLE.equals(newStatus) && DisEnableStatusEnum.DISABLE
                .equals(oldParentDept.getStatus()), "启用 [{}] 前，请先启用其所有上级部门", oldName);
        }
        // 变更上级部门
        if (ObjectUtil.notEqual(req.getParentId(), oldParentId)) {
            // 更新祖级列表
            String newAncestors = this.getAncestors(req.getParentId());
            req.setAncestors(newAncestors);
            // 更新子级的祖级列表
            this.updateChildrenAncestors(newAncestors, oldDept.getAncestors(), id);
        }
    }

    @Override
    protected void beforeDelete(List<Long> ids) {
        List<DeptDO> list = baseMapper.lambdaQuery()
            .select(DeptDO::getName, DeptDO::getIsSystem)
            .in(DeptDO::getId, ids)
            .list();
        Optional<DeptDO> isSystemData = list.stream().filter(DeptDO::getIsSystem).findFirst();
        CheckUtils.throwIf(isSystemData::isPresent, "所选部门 [{}] 是系统内置部门，不允许删除", isSystemData.orElseGet(DeptDO::new)
            .getName());
        CheckUtils.throwIf(this.countChildren(ids) > 0, "所选部门存在下级部门，不允许删除");
        CheckUtils.throwIf(userService.countByDeptIds(ids) > 0, "所选部门存在用户关联，请解除关联后重试");
        // 删除角色和部门关联
        roleDeptService.deleteByDeptIds(ids);
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
            .eq(DeptDO::getName, name)
            .eq(DeptDO::getParentId, parentId)
            .ne(null != id, DeptDO::getId, id)
            .exists();
    }

    /**
     * 获取祖级列表
     *
     * @param parentId 上级部门
     * @return 祖级列表
     */
    private String getAncestors(Long parentId) {
        DeptDO parentDept = this.getByParentId(parentId);
        return "%s,%s".formatted(parentDept.getAncestors(), parentId);
    }

    /**
     * 根据上级部门 ID 查询
     *
     * @param parentId 上级部门 ID
     * @return 上级部门信息
     */
    private DeptDO getByParentId(Long parentId) {
        DeptDO parentDept = baseMapper.selectById(parentId);
        CheckUtils.throwIfNull(parentDept, "上级部门不存在");
        return parentDept;
    }

    /**
     * 查询子部门数量
     *
     * @param ids ID 列表
     * @return 子部门数量
     */
    private Long countChildren(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0L;
        }
        DatabaseType databaseType = MetaUtils.getDatabaseTypeOrDefault(SpringUtil
            .getBean(DynamicRoutingDataSource.class), DatabaseType.MYSQL);
        return ids.stream()
            .mapToLong(id -> baseMapper.lambdaQuery().apply(databaseType.findInSet(id, "ancestors")).count())
            .sum();
    }

    /**
     * 更新子部门祖级列表
     *
     * @param newAncestors 新祖级列表
     * @param oldAncestors 原祖级列表
     * @param id           ID
     */
    private void updateChildrenAncestors(String newAncestors, String oldAncestors, Long id) {
        List<DeptDO> children = this.listChildren(id);
        if (CollUtil.isEmpty(children)) {
            return;
        }
        List<DeptDO> list = new ArrayList<>(children.size());
        for (DeptDO child : children) {
            DeptDO dept = new DeptDO();
            dept.setId(child.getId());
            dept.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
            list.add(dept);
        }
        baseMapper.updateBatchById(list);
    }
}
