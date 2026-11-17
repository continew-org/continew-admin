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

package top.continew.admin.common.config.mybatis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

import cn.dev33.satoken.stp.StpUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.continew.admin.common.base.model.entity.BaseDO;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 审计字段填充测试。
 *
 * <p>覆盖继承 BaseDO 实体的创建/修改信息填充、未继承实体的按字段填充、
 * 已有值不被覆盖等分支。登录 ID 经 StpUtil 静态桩供给，不依赖 Spring 上下文。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class MyBatisPlusMetaObjectHandlerTest {

    private final MyBatisPlusMetaObjectHandler handler = new MyBatisPlusMetaObjectHandler();

    private MockedStatic<StpUtil> stpUtilMock;

    @BeforeEach
    void setUp() {
        stpUtilMock = mockStatic(StpUtil.class);
        // 归属校验按字符串比对登录 ID，与线程本地上下文保持同用户
        stpUtilMock.when(StpUtil::getLoginIdDefaultNull).thenReturn(9L);
        UserContext userContext = new UserContext();
        userContext.setId(9L);
        UserContextHolder.setContext(userContext, false);
    }

    @AfterEach
    void tearDown() {
        stpUtilMock.close();
        UserContextHolder.clearContext();
    }

    /**
     * 演示实体：继承 BaseDO
     */
    static class DemoDO extends BaseDO {
    }

    /**
     * 演示实体：不继承 BaseDO，自带创建/修改字段
     */
    static class PlainDO {

        private Long createUser;
        private LocalDateTime createTime;
        private Long updateUser;
        private LocalDateTime updateTime;

        public Long getCreateUser() {
            return createUser;
        }

        public void setCreateUser(Long createUser) {
            this.createUser = createUser;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public Long getUpdateUser() {
            return updateUser;
        }

        public void setUpdateUser(Long updateUser) {
            this.updateUser = updateUser;
        }

        public LocalDateTime getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
        }
    }

    /**
     * 演示实体：不含任何审计字段
     */
    static class BareDO {
    }

    @Test
    void shouldFillCreateInfoOnInsertForBaseDO() {
        DemoDO entity = new DemoDO();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.insertFill(metaObject);

        assertEquals(9L, entity.getCreateUser());
        assertNotNull(entity.getCreateTime());
    }

    @Test
    void shouldNotOverwriteExistingCreateInfoOnInsertForBaseDO() {
        DemoDO entity = new DemoDO();
        LocalDateTime existingTime = LocalDateTime.of(2020, 1, 1, 0, 0);
        entity.setCreateUser(1L);
        entity.setCreateTime(existingTime);
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.insertFill(metaObject);

        assertEquals(1L, entity.getCreateUser());
        assertEquals(existingTime, entity.getCreateTime());
    }

    @Test
    void shouldFillUpdateInfoOnUpdateForBaseDO() {
        DemoDO entity = new DemoDO();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.updateFill(metaObject);

        assertEquals(9L, entity.getUpdateUser());
        assertNotNull(entity.getUpdateTime());
    }

    @Test
    void shouldOverwriteUpdateInfoOnUpdateForBaseDO() {
        DemoDO entity = new DemoDO();
        entity.setUpdateUser(1L);
        entity.setUpdateTime(LocalDateTime.of(2020, 1, 1, 0, 0));
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.updateFill(metaObject);

        assertEquals(9L, entity.getUpdateUser());
    }

    @Test
    void shouldFillFieldsOnInsertForNonBaseDO() {
        PlainDO entity = new PlainDO();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.insertFill(metaObject);

        assertEquals(9L, entity.getCreateUser());
        assertNotNull(entity.getCreateTime());
    }

    @Test
    void shouldNotFillNonExistingFieldsOnUpdateForNonBaseDO() {
        BareDO entity = new BareDO();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.updateFill(metaObject);

        // BareDO 无 updateUser 字段：填充按 hasSetter 判断直接跳过
        assertFalse(metaObject.hasSetter("updateUser"));
    }
}
