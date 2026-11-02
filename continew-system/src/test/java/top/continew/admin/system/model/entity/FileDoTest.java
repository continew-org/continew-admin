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

package top.continew.admin.system.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import top.continew.starter.storage.domain.model.resp.FileInfo;

/**
 * 文件实体缩略图路径测试
 *
 * @author luoqiz
 * @since 4.2.0
 */
class FileDoTest {

    @Test
    void shouldBuildThumbnailPathWithLeadingSlash() {
        FileDO file = new FileDO();
        file.setParentPath("/user/avatar");
        file.setThumbnailName("x.thumb.png");
        assertEquals("/user/avatar/x.thumb.png", file.getThumbnailPath());
    }

    @Test
    void shouldReturnNullThumbnailPathWhenNameBlank() {
        FileDO file = new FileDO();
        file.setParentPath("/user/avatar");
        file.setThumbnailName(" ");
        assertNull(file.getThumbnailPath());
    }

    @Test
    void shouldExposeThumbnailPathWithoutLeadingSlashInFileInfo() {
        FileDO file = new FileDO();
        file.setName("x.png");
        file.setParentPath("/user/avatar");
        file.setPath("/user/avatar/x.png");
        file.setThumbnailName("x.thumb.png");
        file.setThumbnailSize(128L);
        StorageDO storage = new StorageDO();
        storage.setCode("local");
        storage.setBucketName("data");
        storage.setDomain("http://localhost:8000/file/");
        FileInfo fileInfo = file.toFileInfo(storage);
        assertEquals("user/avatar/x.thumb.png", fileInfo.getThumbnailPath());
        assertEquals(128L, fileInfo.getThumbnailSize());
    }
}
