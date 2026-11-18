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

package top.continew.admin.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.open.mapper.AppMapper;
import top.continew.admin.system.mapper.ClientMapper;
import top.continew.admin.system.mapper.DeptMapper;
import top.continew.admin.system.mapper.DictItemMapper;
import top.continew.admin.system.mapper.DictMapper;
import top.continew.admin.system.mapper.MenuMapper;
import top.continew.admin.system.mapper.MessageLogMapper;
import top.continew.admin.system.mapper.MessageMapper;
import top.continew.admin.system.mapper.NoticeLogMapper;
import top.continew.admin.system.mapper.NoticeMapper;
import top.continew.admin.system.mapper.RoleDeptMapper;
import top.continew.admin.system.mapper.RoleMapper;
import top.continew.admin.system.mapper.RoleMenuMapper;
import top.continew.admin.system.mapper.StorageMapper;
import top.continew.admin.system.mapper.UserRoleMapper;
import top.continew.admin.system.mapper.user.UserMapper;
import top.continew.admin.system.mapper.user.UserSocialMapper;
import top.continew.admin.tenant.mapper.PackageMapper;
import top.continew.admin.tenant.mapper.PackageMenuMapper;
import top.continew.admin.tenant.mapper.TenantMapper;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.data.mapper.BaseMapper;

/**
 * 演示环境重置任务单元测试。
 *
 * <p>覆盖无待清理数据时跳过清理与缓存清空、存在待清理数据时全量清理并按缓存名清空，
 * 以及拦截器忽略策略在任务收尾必被还原。各 Mapper 的查询/更新链以 Mockito 模拟。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class DemoEnvironmentJobTest {

    private static final Long NON_ZERO_COUNT = 3L;

    @Test
    void shouldSkipCleaningAndCacheEvictionWhenNoRemovableData() {
        DemoEnvironmentJob job = new DemoEnvironmentJob(zeroCountMapper(DictItemMapper.class),
            zeroCountMapper(DictMapper.class), zeroCountMapper(StorageMapper.class),
            zeroCountMapper(NoticeMapper.class), removableMapper(NoticeLogMapper.class),
            zeroCountMapper(MessageMapper.class), removableMapper(MessageLogMapper.class),
            zeroCountMapper(UserMapper.class), removableMapper(UserRoleMapper.class),
            removableMapper(UserSocialMapper.class), zeroCountMapper(RoleMapper.class),
            removableMapper(RoleDeptMapper.class), removableMapper(RoleMenuMapper.class),
            zeroCountMapper(MenuMapper.class), zeroCountMapper(DeptMapper.class),
            zeroCountMapper(AppMapper.class), zeroCountMapper(ClientMapper.class),
            zeroCountMapper(TenantMapper.class), zeroCountMapper(PackageMapper.class),
            removableMapper(PackageMenuMapper.class));

        try (MockedStatic<CacheUtils> cacheUtils = mockStatic(CacheUtils.class);
            MockedStatic<InterceptorIgnoreHelper> ignoreHelper =
                mockStatic(InterceptorIgnoreHelper.class)) {
            job.resetEnvironmentData();

            ignoreHelper.verify(InterceptorIgnoreHelper::clearIgnoreStrategy);
            cacheUtils.verifyNoInteractions();
        }
    }

    @Test
    void shouldCleanAllDataAndEvictDictAndMenuCachesWhenDataExists() {
        DemoEnvironmentJob job = new DemoEnvironmentJob(cleanableMapper(DictItemMapper.class),
            cleanableMapper(DictMapper.class), cleanableMapper(StorageMapper.class),
            cleanableMapper(NoticeMapper.class), removableMapper(NoticeLogMapper.class),
            cleanableMapper(MessageMapper.class), removableMapper(MessageLogMapper.class),
            cleanableMapper(UserMapper.class), removableMapper(UserRoleMapper.class),
            removableMapper(UserSocialMapper.class), cleanableMapper(RoleMapper.class),
            removableMapper(RoleDeptMapper.class), removableMapper(RoleMenuMapper.class),
            cleanableMapper(MenuMapper.class), cleanableMapper(DeptMapper.class),
            cleanableMapper(AppMapper.class), cleanableMapper(ClientMapper.class),
            cleanableMapper(TenantMapper.class), cleanableMapper(PackageMapper.class),
            removableMapper(PackageMenuMapper.class));

        try (MockedStatic<CacheUtils> cacheUtils = mockStatic(CacheUtils.class);
            MockedStatic<InterceptorIgnoreHelper> ignoreHelper =
                mockStatic(InterceptorIgnoreHelper.class)) {
            job.resetEnvironmentData();

            ignoreHelper.verify(() -> InterceptorIgnoreHelper.handle(any(IgnoreStrategy.class)));
            ignoreHelper.verify(InterceptorIgnoreHelper::clearIgnoreStrategy);
            cacheUtils.verify(() -> CacheUtils.evictAll(CacheConstants.DICT_KEY_PREFIX));
            cacheUtils.verify(() -> CacheUtils.evictAll(CacheConstants.ROLE_MENU_KEY_PREFIX));
        }
    }

    @Test
    void shouldContinueCleaningWhenSupplierReportsFailure() {
        // clean() 的第二个条件是 supplier 返回值；remove 失败（返回 false）时仅跳过该资源的日志与缓存清空
        DemoEnvironmentJob job = new DemoEnvironmentJob(failingRemoveMapper(DictItemMapper.class),
            zeroCountMapper(DictMapper.class), zeroCountMapper(StorageMapper.class),
            zeroCountMapper(NoticeMapper.class), removableMapper(NoticeLogMapper.class),
            zeroCountMapper(MessageMapper.class), removableMapper(MessageLogMapper.class),
            zeroCountMapper(UserMapper.class), removableMapper(UserRoleMapper.class),
            removableMapper(UserSocialMapper.class), zeroCountMapper(RoleMapper.class),
            removableMapper(RoleDeptMapper.class), removableMapper(RoleMenuMapper.class),
            zeroCountMapper(MenuMapper.class), zeroCountMapper(DeptMapper.class),
            zeroCountMapper(AppMapper.class), zeroCountMapper(ClientMapper.class),
            zeroCountMapper(TenantMapper.class), zeroCountMapper(PackageMapper.class),
            removableMapper(PackageMenuMapper.class));

        try (MockedStatic<CacheUtils> cacheUtils = mockStatic(CacheUtils.class);
            MockedStatic<InterceptorIgnoreHelper> ignoreHelper =
                mockStatic(InterceptorIgnoreHelper.class)) {
            job.resetEnvironmentData();

            ignoreHelper.verify(InterceptorIgnoreHelper::clearIgnoreStrategy);
            cacheUtils.verifyNoInteractions();
        }
    }

    /**
     * 桩定计数查询返回零。
     *
     * @param <M>         Mapper 类型
     * @param mapperClass Mapper 接口
     * @return 计数恒为零的 Mapper 模拟
     */
    private <M> M zeroCountMapper(Class<M> mapperClass) {
        M mapper = mock(mapperClass);
        LambdaQueryChainWrapper<Object> query = chainQuery();
        doReturn(query).when((BaseMapper) mapper)
            .lambdaQuery();
        when(query.count()).thenReturn(0L);
        return mapper;
    }

    /**
     * 桩定计数查询返回非零且清理动作执行成功。
     *
     * @param <M>         Mapper 类型
     * @param mapperClass Mapper 接口
     * @return 检测非零且清理成功的 Mapper 模拟
     */
    private <M> M cleanableMapper(Class<M> mapperClass) {
        M mapper = mock(mapperClass);
        LambdaQueryChainWrapper<Object> query = chainQuery();
        LambdaUpdateChainWrapper<Object> update = chainUpdate(true);
        doReturn(query).when((BaseMapper) mapper)
            .lambdaQuery();
        doReturn(update).when((BaseMapper) mapper)
            .lambdaUpdate();
        when(query.count()).thenReturn(NON_ZERO_COUNT);
        return mapper;
    }

    /**
     * 桩定关联数据清理链，remove 返回 true。
     *
     * @param <M>         Mapper 类型
     * @param mapperClass Mapper 接口
     * @return 清理恒成功的 Mapper 模拟
     */
    private <M> M removableMapper(Class<M> mapperClass) {
        M mapper = mock(mapperClass);
        LambdaUpdateChainWrapper<Object> update = chainUpdate(true);
        doReturn(update).when((BaseMapper) mapper)
            .lambdaUpdate();
        return mapper;
    }

    /**
     * 桩定清理动作返回 false，模拟清理失败路径。
     *
     * @param <M>         Mapper 类型
     * @param mapperClass Mapper 接口
     * @return 清理恒失败的 Mapper 模拟
     */
    private <M> M failingRemoveMapper(Class<M> mapperClass) {
        M mapper = mock(mapperClass);
        LambdaQueryChainWrapper<Object> query = chainQuery();
        LambdaUpdateChainWrapper<Object> update = chainUpdate(false);
        doReturn(query).when((BaseMapper) mapper)
            .lambdaQuery();
        doReturn(update).when((BaseMapper) mapper)
            .lambdaUpdate();
        when(query.count()).thenReturn(NON_ZERO_COUNT);
        return mapper;
    }

    /**
     * 链条件方法统一透传自身，仅终止方法（count/remove）按用例桩定。
     *
     * @return 条件方法自链返回的查询链模拟
     */
    @SuppressWarnings("unchecked")
    private LambdaQueryChainWrapper<Object> chainQuery() {
        LambdaQueryChainWrapper<Object> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(query).gt(any(), any());
        doReturn(query).when(query).notIn(any(), anyCollection());
        return query;
    }

    /**
     * 构造条件自链返回且 remove 结果固定的更新链模拟。
     *
     * @param removeResult remove 终止方法返回值
     * @return 更新链模拟
     */
    @SuppressWarnings("unchecked")
    private LambdaUpdateChainWrapper<Object> chainUpdate(boolean removeResult) {
        LambdaUpdateChainWrapper<Object> update = mock(LambdaUpdateChainWrapper.class);
        doReturn(update).when(update).gt(any(), any());
        doReturn(update).when(update).notIn(any(), anyCollection());
        doReturn(removeResult).when(update).remove();
        return update;
    }
}
