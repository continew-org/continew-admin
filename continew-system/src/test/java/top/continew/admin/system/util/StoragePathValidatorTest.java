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

package top.continew.admin.system.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import top.continew.starter.core.exception.BaseException;

/**
 * 存储路径与名称校验测试
 *
 * @author luoqiz
 * @since 5.0.0
 */
class StoragePathValidatorTest {

    @Test
    void shouldAcceptNameAtMaxLength() {
        assertDoesNotThrow(() -> StoragePathValidator.validateName("a".repeat(255)));
    }

    @Test
    void shouldRejectNameOverMaxLength() {
        assertThrows(BaseException.class, () -> StoragePathValidator.validateName("a".repeat(256)));
    }

    @Test
    void shouldRejectUnsafeNames() {
        assertThrows(BaseException.class, () -> StoragePathValidator.validateName("a/b.pdf"));
        assertThrows(BaseException.class, () -> StoragePathValidator.validateName("a\\b.pdf"));
        assertThrows(BaseException.class, () -> StoragePathValidator.validateName("a:b.pdf"));
        assertThrows(BaseException.class, () -> StoragePathValidator.validateName("a\u0000b.pdf"));
        assertThrows(BaseException.class, () -> StoragePathValidator.validateName("."));
        assertThrows(BaseException.class, () -> StoragePathValidator.validateName(".."));
    }

    @Test
    void shouldRejectUnsafeParentPaths() {
        assertThrows(BaseException.class, () -> StoragePathValidator.validate("/docs/../secret"));
        assertThrows(BaseException.class, () -> StoragePathValidator.validate("docs//sub"));
        assertThrows(BaseException.class, () -> StoragePathValidator.validate("C:/windows"));
        assertDoesNotThrow(() -> StoragePathValidator.validate("/docs/sub/"));
    }

    @Test
    void shouldNormalizeParentPath() {
        assertEquals("/", StoragePathValidator.normalizeParentPath(null));
        assertEquals("/", StoragePathValidator.normalizeParentPath(" "));
        assertEquals("/", StoragePathValidator.normalizeParentPath("/"));
        assertEquals("/docs", StoragePathValidator.normalizeParentPath("docs/"));
        assertEquals("/docs", StoragePathValidator.normalizeParentPath("/docs/"));
    }

    @Test
    void shouldCompareParentPathAfterNormalization() {
        assertTrue(StoragePathValidator.isSameParentPath("docs/", "/docs"));
        assertTrue(StoragePathValidator.isSameParentPath(null, "/"));
        assertFalse(StoragePathValidator.isSameParentPath("/docs", "/archive"));
    }
}
