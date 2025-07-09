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

package top.continew.admin.tenant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.common.config.properties.TenantProperties;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.tenant.mapper.TenantMapper;
import top.continew.admin.tenant.mapper.TenantPackageMapper;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.admin.tenant.model.entity.TenantPackageDO;
import top.continew.admin.tenant.model.query.TenantQuery;
import top.continew.admin.tenant.model.req.TenantReq;
import top.continew.admin.tenant.model.resp.TenantAvailableResp;
import top.continew.admin.tenant.model.resp.TenantDetailResp;
import top.continew.admin.tenant.model.resp.TenantResp;
import top.continew.admin.tenant.service.TenantDbConnectService;
import top.continew.admin.tenant.service.TenantService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.extension.crud.model.entity.BaseIdDO;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.extension.tenant.context.TenantContextHolder;
import top.continew.starter.extension.tenant.enums.TenantIsolationLevel;

import java.util.Arrays;
import java.util.List;

/**
 * 租户业务实现
 *
 * @author 小熊
 * @since 2024/11/26 17:20
 */
@Service
@RequiredArgsConstructor
public class TenantServiceImpl extends BaseServiceImpl<TenantMapper, TenantDO, TenantResp, TenantDetailResp, TenantQuery, TenantReq> implements TenantService {

    private final TenantPackageMapper packageMapper;
    private final TenantProperties tenantProperties;
    private final TenantDbConnectService dbConnectService;

    @Override
    protected void beforeCreate(TenantReq req) {
        //租户名称不能重复
        ValidationUtils.throwIf(baseMapper.exists(Wrappers.lambdaQuery(TenantDO.class)
            .eq(TenantDO::getName, req.getName())), "重复的租户名称");
        //录入随机的六位租户编号
        req.setTenantSn(generateTenantSn());
    }

    /**
     * 生成六位随机不重复的编号
     */
    private String generateTenantSn() {
        String tenantSn;
        do {
            tenantSn = RandomUtil.randomString(RandomUtil.BASE_CHAR_NUMBER_LOWER, 6);
        } while (baseMapper.exists(Wrappers.lambdaQuery(TenantDO.class).eq(TenantDO::getTenantSn, tenantSn)));
        return tenantSn;
    }

    @Override
    protected void afterCreate(TenantReq req, TenantDO entity) {
        //数据源级别的租户需要创建数据库
        if (entity.getIsolationLevel().equals(TenantIsolationLevel.DATASOURCE.ordinal())) {
            JdbcTemplate jdbcTemplate = dbConnectService.getConnectJdbcTemplateById(entity.getDbConnectId());
            String dbName = SysConstants.TENANT_DB_PREFIX + entity.getTenantSn();
            //建库
            jdbcTemplate.execute(StrUtil
                .format("CREATE DATABASE {} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;", dbName));
            jdbcTemplate.execute(StrUtil.format("USE {};", dbName));
            //建表
            Resource resource = new ClassPathResource("db/changelog/mysql/tenant_table.sql");
            String tableSql = resource.readUtf8Str();
            Arrays.stream(tableSql.split(";"))
                .map(String::trim)
                .filter(sql -> !sql.isEmpty())
                .forEach(jdbcTemplate::execute);
        }
    }

    @Override
    public List<TenantAvailableResp> getAvailableList() {
        List<TenantDO> tenantDOS = baseMapper.selectList(Wrappers.lambdaQuery(TenantDO.class)
            .select(TenantDO::getName, BaseIdDO::getId, TenantDO::getDomain)
            .eq(TenantDO::getStatus, DisEnableStatusEnum.ENABLE.getValue())
            .and(t -> t.isNull(TenantDO::getExpireTime).or().ge(TenantDO::getExpireTime, DateUtil.date())));
        return BeanUtil.copyToList(tenantDOS, TenantAvailableResp.class);
    }

    @Override
    public PageResp<TenantResp> page(TenantQuery query, PageQuery pageQuery) {
        QueryWrapper queryWrapper = Wrappers.query(TenantQuery.class)
            .eq(query.getPackageId() != null, "package_id", query.getPackageId())
            .like(StrUtil.isNotEmpty(query.getName()), "sys_tenant.name", query.getName());
        this.sort(queryWrapper, pageQuery);
        IPage<TenantResp> list = baseMapper.listTenant(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), queryWrapper);
        PageResp<TenantResp> pageResp = PageResp.build(list, TenantResp.class);
        return pageResp;
    }

    @Override
    public TenantDetailResp get(Long id) {
        TenantDetailResp detailResp = new TenantDetailResp();
        TenantDO tenantDO = getById(id);
        if (tenantDO != null) {
            BeanUtil.copyProperties(tenantDO, detailResp);
            TenantPackageDO packageDO = packageMapper.selectById(tenantDO.getPackageId());
            if (packageDO != null) {
                detailResp.setPackageName(packageDO.getName());
                detailResp.setMenuIds(new JSONArray(packageDO.getMenuIds()).toList(Long.class));
            }
        }
        fill(detailResp);
        return detailResp;
    }

    @Override
    public void bindUser(Long tenantId, Long userId) {
        update(Wrappers.lambdaUpdate(TenantDO.class).set(TenantDO::getUserId, userId).eq(BaseIdDO::getId, tenantId));
        TenantDO entity = getById(tenantId);
        RedisUtils.set(CacheConstants.TENANT_KEY + tenantId, entity);
    }

    @Override
    public void checkStatus() {
        if (tenantProperties.isEnabled()) {
            Long tenantId = TenantContextHolder.getTenantId();
            CheckUtils.throwIfNull(tenantId, "未选择租户");
            if (tenantId != 0) {
                TenantDO tenantDO = baseMapper.selectById(tenantId);
                CheckUtils.throwIfNull(tenantDO, "租户不存在");
                CheckUtils.throwIfNotEqual(DisEnableStatusEnum.ENABLE.getValue(), tenantDO.getStatus(), "此租户已被禁用");
                //租户过期
                CheckUtils.throwIf(tenantDO.getExpireTime() != null && tenantDO.getExpireTime()
                    .isBefore(DateUtil.date().toLocalDateTime()), "租户已过期");
                //套餐状态
                TenantPackageDO packageDO = packageMapper.selectById(tenantDO.getPackageId());
                CheckUtils.throwIfNull(tenantDO, "套餐不存在");
                CheckUtils.throwIfNotEqual(DisEnableStatusEnum.ENABLE.getValue(), packageDO.getStatus(), "此租户套餐已被禁用");
            }
        }
    }

    @Override
    @Cached(name = CacheConstants.TENANT_KEY, key = "#id")
    public TenantDO getTenantById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    protected void afterUpdate(TenantReq req, TenantDO entity) {
        RedisUtils.set(CacheConstants.TENANT_KEY + entity.getId(), entity);
    }

    @Override
    protected void afterDelete(List<Long> ids) {
        ids.forEach(id -> RedisUtils.delete(CacheConstants.TENANT_KEY + id));

    }

    @Override
    public TenantDO getTenantByUserId(Long userId) {
        return baseMapper.selectOne(Wrappers.lambdaQuery(TenantDO.class).eq(TenantDO::getUserId, userId));
    }

}