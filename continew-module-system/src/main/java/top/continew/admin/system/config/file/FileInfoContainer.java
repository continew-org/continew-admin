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

package top.continew.admin.system.config.file;

import cn.crane4j.core.container.Container;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.continew.admin.common.constant.ContainerConstants;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.service.FileService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件信息填充容器
 *
 * @author luoqiz
 * @since 2025/3/12 18:11
 */
@Component
@RequiredArgsConstructor
public class FileInfoContainer implements Container<Long> {

    private final FileService fileService;

    @Override
    public String getNamespace() {
        return ContainerConstants.FILE_INFO;
    }

    @Override
    public Map<Long, FileDO> get(Collection<Long> ids) {
        List<FileDO> list = fileService.listByIds(ids);
        return list.stream().collect(Collectors.toMap(FileDO::getId, Function.identity()));
    }
}