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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 分片上传原始文件名暂存测试
 *
 * <p>
 * 仅覆盖不访问 Redis 的兜底分支：{@code RedisUtils} 的静态初始化依赖 Spring 容器，纯单元测试环境下无法加载，Redis 读写路径需由集成测试覆盖。
 * </p>
 *
 * <p>
 * 这些分支并非边角料：正常上传时 {@code FileRecorderImpl#update} 会无条件调用
 * {@link MultipartOriginalNameStore#resolveByMetadata}，一旦此处抛异常，所有上传都会失败，因此「元数据缺失时安静地返回
 * null」是需要被固定下来的契约。
 * </p>
 *
 * @author luoqiz
 * @since 5.0.0
 */
class MultipartOriginalNameStoreTest {

    @Test
    void shouldReturnNullWhenMetadataAbsent() {
        assertNull(MultipartOriginalNameStore.resolveByMetadata(null));
        assertNull(MultipartOriginalNameStore.resolveByMetadata(new HashMap<>()));
    }

    @Test
    void shouldReturnNullWhenMetadataHasNoUploadId() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("sha256", "e3b0c44298fc1c149afbf4c8996fb924");
        metadata.put("uploadId", " ");

        assertNull(MultipartOriginalNameStore.resolveByMetadata(metadata));
    }

    @Test
    void shouldIgnoreBlankUploadId() {
        assertNull(MultipartOriginalNameStore.resolve(null));
        assertNull(MultipartOriginalNameStore.resolve(" "));
        assertDoesNotThrow(() -> MultipartOriginalNameStore.save(null, "年度报告.PDF"));
        assertDoesNotThrow(() -> MultipartOriginalNameStore.save(" ", "年度报告.PDF"));
        assertDoesNotThrow(() -> MultipartOriginalNameStore.delete(null));
        assertDoesNotThrow(() -> MultipartOriginalNameStore.delete(" "));
    }

    @Test
    void shouldIgnoreBlankOriginalName() {
        assertDoesNotThrow(() -> MultipartOriginalNameStore.save("upload-1", null));
        assertDoesNotThrow(() -> MultipartOriginalNameStore.save("upload-1", " "));
    }
}
