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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import top.continew.admin.system.model.entity.FileDO;

import java.util.List;
import java.util.Set;

/**
 * 回收站缩略图清理测试
 *
 * @author luoqiz
 * @since 4.2.0
 */
class FileRecycleServiceImplTest {

    @Test
    void shouldSkipCandidateUsedAsMainPath() {
        String candidate = "/dir/thumb.jpg";
        FileDO file = new FileDO();
        file.setPath(candidate);
        Set<String> skipPaths =
            FileRecycleServiceImpl.resolveSkipPaths(Set.of(candidate), List.of(file), List.of());
        assertEquals(Set.of(candidate), skipPaths);
    }

    @Test
    void shouldSkipCandidateUsedAsThumbnail() {
        String candidate = "/dir/thumb.jpg";
        FileDO file = new FileDO();
        file.setParentPath("/dir");
        file.setThumbnailName("thumb.jpg");
        Set<String> skipPaths =
            FileRecycleServiceImpl.resolveSkipPaths(Set.of(candidate), List.of(), List.of(file));
        assertTrue(skipPaths.contains(candidate));
    }

    @Test
    void shouldNotSkipSameThumbnailNameInDifferentDirectory() {
        String candidate = "/dir/thumb.jpg";
        FileDO file = new FileDO();
        file.setParentPath("/other");
        file.setThumbnailName("thumb.jpg");
        Set<String> skipPaths =
            FileRecycleServiceImpl.resolveSkipPaths(Set.of(candidate), List.of(), List.of(file));
        assertTrue(skipPaths.isEmpty());
    }
}
