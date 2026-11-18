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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.system.enums.OptionCategoryEnum;
import top.continew.admin.system.enums.PasswordPolicyEnum;
import top.continew.admin.system.mapper.user.UserMapper;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.OptionService;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserPasswordHistoryService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.admin.system.service.UserSocialService;
import top.continew.starter.core.exception.BadRequestException;
import top.continew.starter.core.exception.BusinessException;

import jakarta.validation.Validator;
import java.util.Map;

/**
 * 用户修改密码单元测试。
 *
 * <p>覆盖新旧密码相同、旧密码错误、空密码用户跳过旧密码校验、
 * 密码策略不通过、历史密码复用与成功后的更新/历史落库/全端登出编排。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class UserServiceImplUpdatePasswordTest {

    private static final Long USER_ID = 9L;
    private static final String RAW_OLD = "Old@1234";
    private static final String ENCODED_OLD = "encoded-old";
    private static final String VALID_NEW = "Abc@1234";
    private static final String OLD_MISMATCH_MSG = "当前密码不正确";

    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private UserPasswordHistoryService passwordHistoryService;
    private OnlineUserService onlineUserService;
    private UserServiceImpl userService;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(mock(Validator.class));
        passwordEncoder = mock(PasswordEncoder.class);
        passwordHistoryService = mock(UserPasswordHistoryService.class);
        onlineUserService = mock(OnlineUserService.class);
        OptionService optionService = mock(OptionService.class);
        when(optionService.getByCategory(OptionCategoryEnum.PASSWORD)).thenReturn(policyValues());
        // 历史密码重复策略经 SpringUtil 取服务，供给恒不重复的桩
        springUtilMock.when(() -> SpringUtil.getBean(UserPasswordHistoryService.class))
            .thenReturn(passwordHistoryService);
        lenient().when(passwordHistoryService.isPasswordReused(anyLong(), anyString(), anyInt()))
            .thenReturn(false);

        userMapper = mock(UserMapper.class);
        userService = new UserServiceImpl(passwordEncoder, passwordHistoryService,
            mock(UserSocialService.class), mock(UserRoleService.class), optionService,
            mock(RoleService.class), onlineUserService, mock(FileService.class));
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        // 父类 getById 经实体类缓存推导类型，mock Mapper 缺代理元数据，预置绕开
        ReflectionTestUtils.setField(userService, "entityClass", UserDO.class);
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldRejectSamePassword() {
        BusinessException exception = assertThrows(BusinessException.class,
            () -> userService.updatePassword(VALID_NEW, VALID_NEW, USER_ID));

        assertEquals("新密码不能与当前密码相同", exception.getMessage());
        verify(userMapper, never()).lambdaUpdate();
    }

    @Test
    void shouldRejectWrongOldPassword() {
        when(userMapper.selectById(USER_ID)).thenReturn(user(ENCODED_OLD));
        when(passwordEncoder.matches(RAW_OLD, ENCODED_OLD)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> userService.updatePassword(RAW_OLD, VALID_NEW, USER_ID));

        assertEquals(OLD_MISMATCH_MSG, exception.getMessage());
        verify(userMapper, never()).lambdaUpdate();
    }

    @Test
    void shouldRejectBlankOldPassword() {
        when(userMapper.selectById(USER_ID)).thenReturn(user(ENCODED_OLD));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> userService.updatePassword("  ", VALID_NEW, USER_ID));

        assertEquals(OLD_MISMATCH_MSG, exception.getMessage());
    }

    @Test
    void shouldSkipOldPasswordCheckWhenUserHasNoPassword() {
        when(userMapper.selectById(USER_ID)).thenReturn(user(null));
        stubUpdateChain();

        userService.updatePassword(null, VALID_NEW, USER_ID);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(onlineUserService).kickOut(USER_ID);
    }

    @Test
    void shouldRejectPasswordViolatingPolicy() {
        when(userMapper.selectById(USER_ID)).thenReturn(user(ENCODED_OLD));
        when(passwordEncoder.matches(RAW_OLD, ENCODED_OLD)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> userService.updatePassword(RAW_OLD, "abc", USER_ID));

        assertEquals("密码最小长度为 8 个字符", exception.getMessage());
        verify(userMapper, never()).lambdaUpdate();
    }

    @Test
    void shouldRejectReusedPassword() {
        when(userMapper.selectById(USER_ID)).thenReturn(user(ENCODED_OLD));
        when(passwordEncoder.matches(RAW_OLD, ENCODED_OLD)).thenReturn(true);
        when(passwordHistoryService.isPasswordReused(USER_ID, VALID_NEW, 3)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> userService.updatePassword(RAW_OLD, VALID_NEW, USER_ID));

        assertEquals("新密码不得与历史前 3 次密码重复", exception.getMessage());
        verify(userMapper, never()).lambdaUpdate();
    }

    @Test
    void shouldUpdatePasswordSaveHistoryAndKickOut() {
        when(userMapper.selectById(USER_ID)).thenReturn(user(ENCODED_OLD));
        when(passwordEncoder.matches(RAW_OLD, ENCODED_OLD)).thenReturn(true);
        LambdaUpdateChainWrapper<UserDO> chain = stubUpdateChain();

        userService.updatePassword(RAW_OLD, VALID_NEW, USER_ID);

        verify(chain).set(any(SFunction.class), eq(VALID_NEW));
        verify(chain).update();
        verify(passwordHistoryService).add(USER_ID, ENCODED_OLD, 3);
        verify(onlineUserService).kickOut(USER_ID);
    }

    private UserDO user(String encodedPassword) {
        UserDO user = new UserDO();
        user.setId(USER_ID);
        user.setUsername("tester");
        user.setPassword(encodedPassword);
        return user;
    }

    private Map<String, String> policyValues() {
        return Map.of(
            PasswordPolicyEnum.PASSWORD_MIN_LENGTH.name(), "8",
            PasswordPolicyEnum.PASSWORD_REQUIRE_SYMBOLS.name(), "0",
            PasswordPolicyEnum.PASSWORD_ALLOW_CONTAIN_USERNAME.name(), "0",
            PasswordPolicyEnum.PASSWORD_REPETITION_TIMES.name(), "3");
    }

    @SuppressWarnings("unchecked")
    private LambdaUpdateChainWrapper<UserDO> stubUpdateChain() {
        LambdaUpdateChainWrapper<UserDO> chain = mock(LambdaUpdateChainWrapper.class);
        when(userMapper.lambdaUpdate()).thenReturn(chain);
        lenient().when(chain.set(any(SFunction.class), any())).thenReturn(chain);
        lenient().when(chain.eq(any(SFunction.class), any())).thenReturn(chain);
        when(chain.update()).thenReturn(true);
        return chain;
    }
}
