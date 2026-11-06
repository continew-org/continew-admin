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
        // 注册后动作不立即执行
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
        // 清除事务同步后动作不执行
        TransactionSynchronizationManager.clearSynchronization();
        assertTrue(executed.isEmpty());
    }
}
