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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * 存储路径工具测试
 *
 * @author luoqiz
 * @since 4.2.0
 */
class StoragePathUtilsTest {

    @Test
    void shouldNormalizeToPhysicalPath() {
        assertEquals("", StoragePathUtils.normalize(null));
        assertEquals("", StoragePathUtils.normalize(""));
        assertEquals("user/avatar/x.png", StoragePathUtils.normalize("/user/avatar/x.png"));
        assertEquals("user/avatar/x.png", StoragePathUtils.normalize("user//avatar///x.png"));
        assertEquals("user/avatar/x.png", StoragePathUtils.normalize("\\user\\avatar\\x.png"));
        assertEquals("user/avatar/x.png", StoragePathUtils.normalize("///user/avatar/x.png/"));
    }

    @Test
    void shouldConvertToDbPath() {
        assertEquals("/user/avatar/x.png", StoragePathUtils.toDbPath("user/avatar/x.png"));
        assertEquals("/user/avatar/x.png", StoragePathUtils.toDbPath("/user/avatar/x.png"));
        assertEquals("/", StoragePathUtils.toDbPath(""));
    }

    @Test
    void shouldBuildThumbnailPath() {
        assertEquals("/user/avatar/x.thumb.png", StoragePathUtils.thumbnailPath("/user/avatar",
            "x.thumb.png"));
        assertEquals("/x.thumb.png", StoragePathUtils.thumbnailPath("/", "x.thumb.png"));
        assertEquals("/x.thumb.png", StoragePathUtils.thumbnailPath(null, "x.thumb.png"));
        assertEquals("/user/x.thumb.png", StoragePathUtils.thumbnailPath("/user/",
            "x.thumb.png"));
        assertNull(StoragePathUtils.thumbnailPath("/user/avatar", null));
        assertNull(StoragePathUtils.thumbnailPath("/user/avatar", ""));
    }
}
