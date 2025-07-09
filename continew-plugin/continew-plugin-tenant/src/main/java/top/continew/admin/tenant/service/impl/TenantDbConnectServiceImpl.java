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

import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.tenant.mapper.TenantDbConnectMapper;
import top.continew.admin.tenant.mapper.TenantMapper;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.admin.tenant.model.entity.TenantDbConnectDO;
import top.continew.admin.tenant.model.enums.TenantConnectTypeEnum;
import top.continew.admin.tenant.model.query.TenantDbConnectQuery;
import top.continew.admin.tenant.model.req.TenantDbConnectReq;
import top.continew.admin.tenant.model.resp.TenantDbConnectDetailResp;
import top.continew.admin.tenant.model.resp.TenantDbConnectResp;
import top.continew.admin.tenant.service.TenantDbConnectService;
import top.continew.admin.tenant.util.DbConnectUtil;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.util.validation.CheckUtils;

import javax.sql.DataSource;
import java.util.List;

/**
 * 租户数据连接业务实现
 *
 * @author 小熊
 * @since 2024/12/12 19:13
 */
@Service
@RequiredArgsConstructor
public class TenantDbConnectServiceImpl extends BaseServiceImpl<TenantDbConnectMapper, TenantDbConnectDO, TenantDbConnectResp, TenantDbConnectDetailResp, TenantDbConnectQuery, TenantDbConnectReq> implements TenantDbConnectService {

    private final TenantMapper tenantMapper;

    @Override
    @Cached(name = CacheConstants.DB_CONNECT_KEY_PREFIX, key = "#id")
    public TenantDbConnectDetailResp get(Long id) {
        return super.get(id);
    }

    @Override
    protected void beforeCreate(TenantDbConnectReq req) {
        TenantConnectTypeEnum connectTypeEnum = TenantConnectTypeEnum.getByOrdinal(req.getType());
        if (TenantConnectTypeEnum.MYSQL.equals(connectTypeEnum)) {
            DbConnectUtil.getMysqlDataSource(req.getHost(), req.getPort(), req.getUsername(), req
                .getPassword(), null, null);
            checkRepeat(req, null);
        }
    }

    /**
     * 验证重复数据
     */
    private void checkRepeat(TenantDbConnectReq req, Long id) {
        CheckUtils.throwIf(baseMapper.exists(Wrappers.lambdaQuery(TenantDbConnectDO.class)
            .eq(TenantDbConnectDO::getHost, req.getHost())
            .eq(TenantDbConnectDO::getPort, req.getPort())
            .eq(TenantDbConnectDO::getUsername, req.getUsername())
            .ne(id != null, TenantDbConnectDO::getId, id)), "数据库连接已存在");
    }

    @Override
    protected void beforeUpdate(TenantDbConnectReq req, Long id) {
        TenantConnectTypeEnum connectTypeEnum = TenantConnectTypeEnum.getByOrdinal(req.getType());
        if (TenantConnectTypeEnum.MYSQL.equals(connectTypeEnum)) {
            DbConnectUtil.getMysqlDataSource(req.getHost(), req.getPort(), req.getUsername(), req
                .getPassword(), null, null);
            checkRepeat(req, id);
        }
    }

    @Override
    protected void beforeDelete(List<Long> ids) {
        CheckUtils.throwIf(tenantMapper.selectCount(Wrappers.lambdaQuery(TenantDO.class)
            .in(TenantDO::getDbConnectId, ids)) > 0, "存在关联租户无法删除");
    }

    @Override
    protected void afterUpdate(TenantDbConnectReq req, TenantDbConnectDO entity) {
        RedisUtils.delete(CacheConstants.DB_CONNECT_KEY_PREFIX + entity.getId());
    }

    @Override
    protected void afterDelete(List<Long> ids) {
        ids.forEach(id -> RedisUtils.delete(CacheConstants.DB_CONNECT_KEY_PREFIX + id));
    }

    @Override
    public JdbcTemplate getConnectJdbcTemplateById(Long id) {
        TenantDbConnectDetailResp dbConnectReq = get(id);
        TenantConnectTypeEnum connectTypeEnum = TenantConnectTypeEnum.getByOrdinal(dbConnectReq.getType());
        if (TenantConnectTypeEnum.MYSQL.equals(connectTypeEnum)) {
            DataSource dataSource = DbConnectUtil.getMysqlDataSource(dbConnectReq.getHost(), dbConnectReq
                .getPort(), dbConnectReq.getUsername(), dbConnectReq.getPassword(), null, null);
            return new JdbcTemplate(dataSource);
        }
        return null;
    }

}