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
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.validation.ValidationUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.result.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.mapper.DeptMapper;
import top.continew.admin.system.model.entity.DeptDO;
import top.continew.admin.system.model.query.DeptQuery;
import top.continew.admin.system.model.req.dept.DeptImportReq;
import top.continew.admin.system.model.req.dept.DeptImportRowReq;
import top.continew.admin.system.model.req.dept.DeptReq;
import top.continew.admin.system.model.resp.DeptResp;
import top.continew.admin.system.model.resp.dept.DeptImportParseResp;
import top.continew.admin.system.model.resp.dept.DeptImportResp;
import top.continew.admin.system.service.DeptService;
import top.continew.admin.system.service.RoleDeptService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.core.validation.CheckUtils;
import top.continew.starter.data.core.enums.DatabaseType;
import top.continew.starter.data.core.util.MetaUtils;
import top.continew.starter.extension.crud.service.BaseServiceImpl;
import top.continew.starter.web.util.FileUploadUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static top.continew.admin.system.enums.ImportPolicyEnum.EXIT;

/**
 * 部门业务实现
 *
 * @author Charles7c
 * @since 2023/1/22 17:55
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeptServiceImpl extends BaseServiceImpl<DeptMapper, DeptDO, DeptResp, DeptResp, DeptQuery, DeptReq> implements DeptService {

    private final RoleDeptService roleDeptService;
    @Resource
    private UserService userService;
    @Resource
    private DataSource dataSource;

    @Override
    public void beforeCreate(DeptReq req) {
        String name = req.getName();
        boolean isExists = this.isNameExists(name, req.getParentId(), null);
        CheckUtils.throwIf(isExists, "新增失败，[{}] 已存在", name);
        req.setAncestors(this.getAncestors(req.getParentId()));
    }

    @Override
    public void beforeUpdate(DeptReq req, Long id) {
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
    public void beforeDelete(List<Long> ids) {
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

    @Override
    public List<DeptDO> listChildren(Long id) {
        DatabaseType databaseType = MetaUtils.getDatabaseTypeOrDefault(dataSource, DatabaseType.MYSQL);
        return baseMapper.lambdaQuery().apply(databaseType.findInSet(id, "ancestors")).list();
    }

    @Override
    public List<DeptDO> listByNames(List<String> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return this.list(Wrappers.<DeptDO>lambdaQuery().in(DeptDO::getName, list));
    }

    @Override
    public int countByNames(List<String> deptNames) {
        if (CollUtil.isEmpty(deptNames)) {
            return 0;
        }
        return (int)this.count(Wrappers.<DeptDO>lambdaQuery().in(DeptDO::getName, deptNames));
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        try {
            FileUploadUtils.download(response, ResourceUtil.getStream("templates/import/dept.xlsx"), "部门导入模板.xlsx");
        } catch (Exception e) {
            log.error("下载用户导入模板失败：{}", e.getMessage(), e);
            response.setCharacterEncoding(CharsetUtil.UTF_8);
            response.setContentType(ContentType.JSON.toString());
            response.getWriter().write(JSONUtil.toJsonStr(R.fail("下载部门导入模板失败")));
        }
    }



    @Override
    public DeptImportParseResp parseImport(MultipartFile file) {
        DeptImportParseResp deptImportResp = new DeptImportParseResp();
        List<DeptImportRowReq> importRowList = Collections.emptyList();
        try {
            importRowList = EasyExcel.read(file.getInputStream())
                    .head(DeptImportRowReq.class)
                    .sheet()
                    .headRowNumber(1)
                    .doReadSync();
        } catch (Exception e) {
            log.error("导入异常:", e);
            throw new BusinessException("导入已过期，请重新上传");
        }
        // 总计行数
        deptImportResp.setTotalRows(importRowList.size());
        CheckUtils.throwIfEmpty(importRowList, "数据文件格式不正确");
        List<DeptImportRowReq> validRowList = this.filterImportData(importRowList);
        // 有效行数：过滤无效数据
        deptImportResp.setValidRows(validRowList.size());
        CheckUtils.throwIfEmpty(validRowList, "数据文件格式不正确");

        // 检测表格内数据是否合法
        Set<String> set = new HashSet<>();
        boolean hasDuplicateDept = importRowList.stream()
                .map(o -> o.getParentDeptName() + o.getDeptName())
                .anyMatch(o -> !set.add(o));
        CheckUtils.throwIf(hasDuplicateDept, "存在重复部门，请检测数据");

        // 查询不存在的上级部门
        deptImportResp
                .setDeficiencyParentDeptRows(this.countExistParentList(validRowList));
        // 查询重复部门
        deptImportResp
                .setDuplicateDeptRows(this.countExistChildrenList(validRowList));

        String importKey = UUID.fastUUID().toString(true);
        RedisUtils.set(CacheConstants.DATA_IMPORT_KEY + importKey, JSONUtil.toJsonStr(validRowList), Duration.ofMinutes(10));
        deptImportResp.setImportKey(importKey);
        return deptImportResp;
    }

    @Override
    public DeptImportResp importDept(DeptImportReq req) {
        List<DeptImportRowReq> importDeptList;
        try {
            String data = RedisUtils.get(CacheConstants.DATA_IMPORT_KEY + req.getImportKey());
            importDeptList = JSONUtil.toList(data, DeptImportRowReq.class);
            CheckUtils.throwIfEmpty(importDeptList, "导入已过期，请重新上传");
        } catch (Exception e) {
            log.error("导入异常:", e);
            throw new BusinessException("导入已过期，请重新上传");
        }

        CheckUtils
                .throwIf(isExitImportDept(req, importDeptList), "数据不符合导入策略，已退出导入");

        List<DeptDO> addList = new ArrayList<>();

        a:
        for (DeptImportRowReq row : importDeptList) {
            String[] parentDeptArr = row.getParentDeptName().split(StringConstants.DASHED);
            DeptDO parentDept = null;
            int i = 0;
            // 循环找到最后一个上级部门
            while (i < parentDeptArr.length) {
                if (i == 0) {
                    parentDept = baseMapper.selectOne(new LambdaQueryWrapper<>(DeptDO.class).eq(DeptDO::getName, parentDeptArr[i]));
                } else {
                    List<DeptDO> existedParentList = baseMapper.selectList(new LambdaQueryWrapper<>(DeptDO.class).eq(DeptDO::getParentId, parentDept.getId()));
                    int finalI = i;
                    parentDept = existedParentList.stream().filter(en -> en.getName().equals(parentDeptArr[finalI])).findFirst().orElse(null);
                }
                i++;
                if (parentDept == null) {
                    break;
                }
            }
            // 查看需要新增的子部门是否存在
            if (parentDept != null) {
                List<DeptDO> existedChildrenList = baseMapper.selectList(new LambdaQueryWrapper<>(DeptDO.class).eq(DeptDO::getParentId, parentDept.getId()));
                DeptDO childrenDept = existedChildrenList.stream().filter(en -> en.getName().equals(row.getDeptName())).findFirst().orElse(null);
                if (childrenDept == null) {
                    DeptDO deptDO = new DeptDO();
                    deptDO.setParentId(parentDept.getId());
                    deptDO.setName(row.getDeptName());
                    deptDO.setDescription(row.getDescription());
                    deptDO.setSort(row.getSort());
                    deptDO.setStatus(req.getDefaultStatus());
                    addList.add(deptDO);
                }
            }
        }
        if (CollUtil.isNotEmpty(addList)) {
            baseMapper.insertBatch(addList);
        }
        return DeptImportResp.builder()
                .insertRows(addList.size())
                .totalRows(addList.size())
                .updateRows(0)
                .build();
    }

    private boolean isExitImportDept(DeptImportReq req, List<DeptImportRowReq> importDeptList) {
        return (req.getDeficiencyParentDeptRows() == EXIT && countExistParentList(importDeptList) > 0)
                || (req.getDuplicateDeptRows() == EXIT && countExistChildrenList(importDeptList) > 0);
    }

    /**
     * 按指定数据集获取数据库已存在的数量
     *
     * @param deptRowList 导入的数据源
     * @return 存在的数量
     */
    private int countExistChildrenList(List<DeptImportRowReq> deptRowList) {
        int count = 0;
        if (CollUtil.isEmpty(deptRowList)) {
            return count;
        }
        for (DeptImportRowReq o : deptRowList) {
            String[] deptNameArr = o.getParentDeptName().split(StringConstants.DASHED);
            DeptDO parentDept = new DeptDO();
            for (int i = 0; i < deptNameArr.length; i++) {
                if (i == 0) {
                    parentDept = baseMapper.selectOne(new LambdaQueryWrapper<>(DeptDO.class).eq(DeptDO::getName, deptNameArr[i]));
                } else {
                    List<DeptDO> existedParentList = baseMapper.selectList(new LambdaQueryWrapper<>(DeptDO.class).eq(DeptDO::getParentId, parentDept.getId()));
                    int finalI = i;
                    parentDept = existedParentList.stream().filter(en -> en.getName().equals(deptNameArr[finalI])).findFirst().orElse(null);
                }
                if (parentDept != null && i == deptNameArr.length - 1) {
                    List<DeptDO> existedChildrenList = baseMapper.selectList(new LambdaQueryWrapper<>(DeptDO.class).eq(DeptDO::getParentId, parentDept.getId()));
                    if (CollUtil.anyMatch(existedChildrenList, dept -> StringUtils.equals(dept.getName(), o.getDeptName()))) {
                        log.warn("部门重复: {} -> {} :", o.getParentDeptName(), o.getDeptName());
                        count++;
                    }
                }
            }
        }
        return count;
    }


    /**
     * 过滤无效的导入部门数据（批量导入不严格校验数据）
     *
     * @param importRowList 导入数据
     */
    private List<DeptImportRowReq> filterImportData(List<DeptImportRowReq> importRowList) {
        // 校验过滤
        List<DeptImportRowReq> list = importRowList.stream()
                .filter(row -> ValidationUtil.validate(row).isEmpty())
                .toList();
        return list;
    }

    private int countExistParentList(List<DeptImportRowReq> importRowList) {
        int count = 0;
        if (CollUtil.isEmpty(importRowList)) {
            return count;
        }
        for (DeptImportRowReq o : importRowList) {
            String[] deptNameArr = o.getParentDeptName().split(StringConstants.DASHED);
            for (int i = 0; i < deptNameArr.length; i++) {
                DeptDO deptDO;
                if (i == 0) {
                    deptDO = baseMapper.selectOne(new LambdaQueryWrapper<>(DeptDO.class).eq(DeptDO::getName, deptNameArr[i]));
                } else {
                    deptDO = baseMapper.selectOne(new LambdaQueryWrapper<>(DeptDO.class).eq(DeptDO::getName, deptNameArr[i - 1]));
                    List<DeptDO> existedDeptList = baseMapper.selectList(new LambdaQueryWrapper<>(DeptDO.class).eq(DeptDO::getParentId, deptDO.getId()));
                    int finalI = i;
                    deptDO = existedDeptList.stream().filter(en -> en.getName().equals(deptNameArr[finalI])).findFirst().orElse(null);
                }
                if (deptDO == null) {
                    log.warn("部门不存在: {}", o.getParentDeptName());
                    count++;
                }
            }
        }
        return count;
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
            .ne(id != null, DeptDO::getId, id)
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
        DatabaseType databaseType = MetaUtils.getDatabaseTypeOrDefault(dataSource, DatabaseType.MYSQL);
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
        baseMapper.updateById(list);
    }
}
