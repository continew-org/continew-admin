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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.system.mapper.user.UserMapper;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.OptionService;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserPasswordHistoryService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.admin.system.service.UserSocialService;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.core.exception.BusinessException;

import jakarta.validation.Validator;
import java.util.List;

/**
 * 用户删除编排单元测试。
 *
 * <p>覆盖删除当前用户、用户不存在、系统内置用户三类守卫，
 * 以及成功删除时的关联清理、缓存逐一失效与在线用户踢出编排。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class UserServiceImplDeleteTest {

    private static final Long CURRENT_USER_ID = 1L;
    private static final Long USER_ID = 9L;

    private UserMapper userMapper;
    private UserRoleService userRoleService;
    private UserPasswordHistoryService passwordHistoryService;
    private UserSocialService userSocialService;
    private OnlineUserService onlineUserService;
    private UserServiceImpl userService;
    private MockedStatic<SpringUtil> springUtilMock;
    private MockedStatic<UserContextHolder> userContextMock;
    private MockedStatic<CacheUtils> cacheUtilsMock;

    @BeforeEach
    void setUp() {
        springUtilMock = mockStatic(SpringUtil.class);
        Validator validator = mock(Validator.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(validator);
        userContextMock = mockStatic(UserContextHolder.class);
        userContextMock.when(UserContextHolder::getUserId).thenReturn(CURRENT_USER_ID);
        cacheUtilsMock = mockStatic(CacheUtils.class);

        userMapper = mock(UserMapper.class);
        userRoleService = mock(UserRoleService.class);
        passwordHistoryService = mock(UserPasswordHistoryService.class);
        userSocialService = mock(UserSocialService.class);
        onlineUserService = mock(OnlineUserService.class);
        userService = new UserServiceImpl(mock(PasswordEncoder.class), passwordHistoryService,
            userSocialService, userRoleService, mock(OptionService.class), mock(RoleService.class),
            onlineUserService, mock(FileService.class));
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @AfterEach
    void tearDown() {
        cacheUtilsMock.close();
        userContextMock.close();
        springUtilMock.close();
    }

    @Test
    void shouldRejectDeleteCurrentUser() {
        List<Long> ids = List.of(CURRENT_USER_ID, USER_ID);
        BusinessException exception = assertThrows(BusinessException.class,
            () -> userService.delete(ids));

        assertEquals("不允许删除当前用户", exception.getMessage());
        verify(userMapper, never()).lambdaQuery();
    }

    @Test
    void shouldRejectDeleteWhenSomeUserMissing() {
        stubExistingUsers(List.of(user(USER_ID)));

        List<Long> ids = List.of(USER_ID, 99L);
        BusinessException exception = assertThrows(BusinessException.class,
            () -> userService.delete(ids));

        assertEquals("所选用户 [99] 不存在", exception.getMessage());
        verify(userRoleService, never()).deleteByUserIds(any());
    }

    @Test
    void shouldRejectDeleteOfSystemUser() {
        UserDO systemUser = user(USER_ID);
        systemUser.setIsSystem(true);
        stubExistingUsers(List.of(systemUser));

        List<Long> ids = List.of(USER_ID);
        BusinessException exception = assertThrows(BusinessException.class,
            () -> userService.delete(ids));

        assertEquals("所选用户 [测试用户] 是系统内置用户，不允许删除", exception.getMessage());
        verify(userMapper, never()).deleteByIds(any());
    }

    @Test
    void shouldDeleteAssociationsEvictCacheAndKickOut() {
        stubExistingUsers(List.of(user(USER_ID)));
        List<Long> ids = List.of(USER_ID);

        userService.delete(ids);

        verify(userRoleService).deleteByUserIds(ids);
        verify(passwordHistoryService).deleteByUserIds(ids);
        verify(userSocialService).deleteByUserIds(ids);
        verify(userMapper).deleteByIds(ids);
        cacheUtilsMock.verify(() -> CacheUtils.evict(CacheConstants.USER_KEY_PREFIX, USER_ID));
        verify(onlineUserService).kickOut(USER_ID);
    }

    private UserDO user(Long id) {
        UserDO user = new UserDO();
        user.setId(id);
        user.setNickname("测试用户");
        user.setIsSystem(false);
        return user;
    }

    @SuppressWarnings("unchecked")
    private void stubExistingUsers(List<UserDO> users) {
        LambdaQueryChainWrapper<UserDO> chain = mock(LambdaQueryChainWrapper.class);
        when(userMapper.lambdaQuery()).thenReturn(chain);
        lenient().when(chain.select(any(SFunction.class), any(SFunction.class),
            any(SFunction.class))).thenReturn(chain);
        lenient().when(chain.in(any(SFunction.class), any(java.util.Collection.class)))
            .thenReturn(chain);
        when(chain.list()).thenReturn(users);
    }
}
