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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import me.ahoo.cosid.IdGenerator;
import me.ahoo.cosid.provider.DefaultIdGeneratorProvider;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.enums.ImportPolicyEnum;
import top.continew.admin.system.mapper.user.UserMapper;
import top.continew.admin.system.model.entity.DeptDO;
import top.continew.admin.system.model.entity.RoleDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.req.user.UserImportReq;
import top.continew.admin.system.model.req.user.UserImportRowReq;
import top.continew.admin.system.service.DeptService;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.OptionService;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserPasswordHistoryService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.admin.system.service.UserSocialService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.encrypt.field.util.EncryptHelper;
import top.continew.starter.storage.core.FileStorageService;

import jakarta.validation.Validator;
import java.util.List;

/**
 * 用户导入时角色与内置用户保护的守卫测试。
 *
 * @author Charles7c
 * @since 5.0.0
 */
class UserServiceImplImportRoleGuardTest {

    private static final long SYSTEM_ROLE_ID = 1L;
    private static final long NORMAL_ROLE_ID = 3L;

    private UserRoleService userRoleService;
    private RoleService roleService;
    private DeptService deptService;
    private UserMapper userMapper;
    private UserServiceImpl userService;
    private MockedStatic<SpringUtil> springUtilMock;
    private MockedStatic<RedisUtils> redisUtilsMock;
    private MockedStatic<EncryptHelper> encryptHelperMock;

    @BeforeEach
    void setUp() {
        // 初始化 UserDO 的 MyBatis-Plus Lambda 列缓存，避免构造 LambdaQueryWrapper 时找不到实体元数据
        MapperBuilderAssistant assistant =
            new MapperBuilderAssistant(new Configuration(), UserDO.class.getName());
        TableInfoHelper.initTableInfo(assistant, UserDO.class);

        springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(mock(Validator.class));

        redisUtilsMock = mockStatic(RedisUtils.class);
        encryptHelperMock = mockStatic(EncryptHelper.class);
        encryptHelperMock.when(() -> EncryptHelper.encrypt(any()))
            .thenAnswer(inv -> inv.getArgument(0));

        // 导入走雪花 ID 生成器，单测环境向单例注入一个固定生成器
        IdGenerator idGenerator = mock(IdGenerator.class);
        when(idGenerator.generate()).thenReturn(100L);
        DefaultIdGeneratorProvider.INSTANCE.setShare(idGenerator);

        userRoleService = mock(UserRoleService.class);
        roleService = mock(RoleService.class);
        deptService = mock(DeptService.class);
        userService = new UserServiceImpl(mock(PasswordEncoder.class),
            mock(UserPasswordHistoryService.class),
            mock(UserSocialService.class),
            userRoleService,
            mock(OptionService.class),
            roleService,
            mock(OnlineUserService.class),
            mock(FileService.class),
            mock(FileStorageService.class));
        userMapper = mock(UserMapper.class);
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        ReflectionTestUtils.setField(userService, "deptService", deptService);

        // 部门解析
        LambdaQueryChainWrapper<DeptDO> deptChain = mock(LambdaQueryChainWrapper.class);
        when(deptChain.eq(any(SFunction.class), any())).thenReturn(deptChain);
        when(deptChain.list()).thenReturn(List.of(this.dept()));
        when(deptService.lambdaQuery()).thenReturn(deptChain);
        // 已存在用户查询（用户名/邮箱/手机）均为空；查询经 this.list(wrapper) 落到 baseMapper.selectList
        when(userMapper.selectList(any())).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() {
        DefaultIdGeneratorProvider.INSTANCE.removeShare();
        encryptHelperMock.close();
        redisUtilsMock.close();
        springUtilMock.close();
    }

    private DeptDO dept() {
        DeptDO dept = new DeptDO();
        dept.setId(10L);
        dept.setName("研发部");
        return dept;
    }

    private UserImportReq req() {
        UserImportReq req = new UserImportReq();
        req.setImportKey("test-key");
        req.setDuplicateUser(ImportPolicyEnum.UPDATE);
        req.setDuplicateEmail(ImportPolicyEnum.SKIP);
        req.setDuplicatePhone(ImportPolicyEnum.SKIP);
        req.setDefaultStatus(DisEnableStatusEnum.ENABLE);
        return req;
    }

    private UserImportRowReq row(String username, String roleName) {
        UserImportRowReq row = new UserImportRowReq();
        row.setUsername(username);
        row.setNickname("测试员");
        row.setPassword("test123");
        row.setDeptName("研发部");
        row.setRoleName(roleName);
        return row;
    }

    private RoleDO role(Long id, boolean isSystem) {
        RoleDO role = new RoleDO();
        role.setId(id);
        role.setName(isSystem ? "超级管理员" : "普通用户");
        role.setIsSystem(isSystem);
        return role;
    }

    @Test
    void shouldRejectImportingIntoSystemRole() {
        redisUtilsMock.when(() -> RedisUtils.get(CacheConstants.DATA_IMPORT_KEY + "test-key"))
            .thenReturn("[{\"username\":\"newuser\",\"nickname\":\"测试员\",\"password\":\"test123\","
                + "\"deptName\":\"研发部\",\"roleName\":\"超级管理员\"}]");
        when(roleService.listByNames(List.of("超级管理员")))
            .thenReturn(List.of(this.role(SYSTEM_ROLE_ID, true)));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> userService.importUser(req()));

        assertEquals("角色 [超级管理员] 是系统内置角色，不允许通过导入分配", exception.getMessage());
        verify(userRoleService, never()).saveBatch(any());
    }

    @Test
    void shouldImportIntoNormalRole() {
        redisUtilsMock.when(() -> RedisUtils.get(CacheConstants.DATA_IMPORT_KEY + "test-key"))
            .thenReturn("[{\"username\":\"newuser\",\"nickname\":\"测试员\",\"password\":\"test123\","
                + "\"deptName\":\"研发部\",\"roleName\":\"普通用户\"}]");
        when(roleService.listByNames(List.of("普通用户")))
            .thenReturn(List.of(this.role(NORMAL_ROLE_ID, false)));

        userService.importUser(req());

        verify(userRoleService).saveBatch(any());
    }

    @Test
    void shouldSkipSystemRoleCheckForSkippedRow() {
        // 重复用户名 + SKIP 策略：该行被跳过，不应触发系统角色校验
        UserImportReq skipReq = req();
        skipReq.setDuplicateUser(ImportPolicyEnum.SKIP);
        when(userMapper.selectList(any())).thenReturn(List.of(this.existingUser("admin")));
        redisUtilsMock.when(() -> RedisUtils.get(CacheConstants.DATA_IMPORT_KEY + "test-key"))
            .thenReturn("[{\"username\":\"admin\",\"nickname\":\"测试员\",\"password\":\"test123\","
                + "\"deptName\":\"研发部\",\"roleName\":\"超级管理员\"}]");
        when(roleService.listByNames(List.of("超级管理员")))
            .thenReturn(List.of(this.role(SYSTEM_ROLE_ID, true)));

        userService.importUser(skipReq);

        verify(userRoleService, never()).saveBatch(any());
    }

    private UserDO existingUser(String username) {
        UserDO user = new UserDO();
        user.setId(1L);
        user.setUsername(username);
        return user;
    }
}
