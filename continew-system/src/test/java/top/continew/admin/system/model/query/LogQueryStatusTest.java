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

package top.continew.admin.system.model.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import top.continew.admin.system.enums.LogStatusEnum;
import top.continew.admin.system.model.entity.LogDO;

/**
 * 日志查询条件状态字段的类型回归测试。
 *
 * <p>日志分页与导出都以 {@code LogQuery.status} 构建等值条件，查询条件的状态类型必须与
 * {@code LogDO.status} 一致，否则按成功或失败过滤时传入的枚举值无法与实体列对齐。测试以反射
 * 锁定两侧字段类型，并覆盖成功与失败两个取值。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class LogQueryStatusTest {

    @Test
    void shouldUseLogStatusEnumAlignedWithLogDO() throws NoSuchFieldException {
        assertEquals(LogStatusEnum.class, LogQuery.class.getDeclaredField("status").getType());
        assertEquals(LogStatusEnum.class, LogDO.class.getDeclaredField("status").getType());
    }

    @Test
    void shouldHoldSuccessAndFailure() {
        LogQuery query = new LogQuery();
        query.setStatus(LogStatusEnum.SUCCESS);
        assertEquals(LogStatusEnum.SUCCESS, query.getStatus());

        query.setStatus(LogStatusEnum.FAILURE);
        assertEquals(LogStatusEnum.FAILURE, query.getStatus());
    }
}
