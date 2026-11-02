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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

/**
 * 存储文件名生成测试
 *
 * @author luoqiz
 * @since 5.0.0
 */
class FileNameGeneratorTest {

    /**
     * 32 位小写十六进制串，可选带一个扩展名
     */
    private static final Pattern STORED_NAME_PATTERN = Pattern
        .compile("^[0-9a-f]{32}(\\.[0-9a-zA-Z]+)?$");

    @Test
    void shouldGenerateUuidWithExtension() {
        String storedName = FileNameGenerator.generateStoredName("报告.pdf");

        assertTrue(STORED_NAME_PATTERN.matcher(storedName).matches());
        assertEquals(32 + ".pdf".length(), storedName.length());
        assertTrue(storedName.endsWith(".pdf"));
    }

    @Test
    void shouldKeepExtensionCase() {
        String storedName = FileNameGenerator.generateStoredName("截图.PNG");

        assertTrue(storedName.endsWith(".PNG"));
        assertTrue(STORED_NAME_PATTERN.matcher(storedName).matches());
    }

    @Test
    void shouldUseLastExtensionSegment() {
        assertTrue(FileNameGenerator.generateStoredName("archive.tar.gz").endsWith(".gz"));
    }

    @Test
    void shouldNotAppendDotWhenExtensionAbsent() {
        String storedName = FileNameGenerator.generateStoredName("README");

        assertEquals(32, storedName.length());
        assertFalse(storedName.contains("."));
    }

    @Test
    void shouldTolerateNullAndBlankFileName() {
        assertTrue(STORED_NAME_PATTERN.matcher(FileNameGenerator.generateStoredName(null))
            .matches());
        assertTrue(STORED_NAME_PATTERN.matcher(FileNameGenerator.generateStoredName("")).matches());
    }

    @Test
    void shouldGenerateDifferentNamesForSameFileName() {
        assertNotEquals(FileNameGenerator.generateStoredName("a.png"), FileNameGenerator
            .generateStoredName("a.png"));
    }
}
