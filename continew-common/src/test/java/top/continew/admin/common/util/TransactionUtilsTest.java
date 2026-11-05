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

package top.continew.admin.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 事务提交后回调注册的测试。
 *
 * <p>回归：管理员变更角色/用户权限时，在事务内直接同步在线用户会话快照，数据库回滚后已写入
 * Redis 的快照不随之回滚（授权场景放大权限窗口、撤销场景错误拒权）。快照同步必须等数据库
 * 提交后再执行。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class TransactionUtilsTest {

    private List<String> executed;

    @BeforeEach
    void setUp() {
        executed = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void shouldRunImmediatelyWithoutActiveTransaction() {
        TransactionUtils.afterCommit(() -> executed.add("run"));
        assertEquals(List.of("run"), executed);
    }

    @Test
    void shouldDeferActionUntilAfterCommit() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionUtils.afterCommit(() -> executed.add("run"));
        // 事务未提交（含回滚）时不执行，动作仍待注册表触发
        assertTrue(executed.isEmpty());

        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager
            .getSynchronizations();
        assertEquals(1, synchronizations.size());
        synchronizations.get(0).afterCommit();
        assertEquals(List.of("run"), executed);
    }

    @Test
    void shouldNotRunActionWhenTransactionRollsBack() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionUtils.afterCommit(() -> executed.add("run"));
        // 回滚场景：Spring 不会触发 afterCommit，同步随事务清理一并丢弃
        TransactionSynchronizationManager.clearSynchronization();
        assertTrue(executed.isEmpty());
    }
}
