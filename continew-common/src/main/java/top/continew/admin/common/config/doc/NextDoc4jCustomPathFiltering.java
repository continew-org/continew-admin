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

package top.continew.admin.common.config.doc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.continew.starter.auth.satoken.autoconfigure.SaTokenExtensionProperties;
import top.nextdoc4j.security.core.enhancer.NextDoc4jPathExcluder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * NextDoc4j 自定义路径过滤
 *
 * @author echo
 * @since 2025/12/18
 */
@Component
@RequiredArgsConstructor
public class NextDoc4jCustomPathFiltering implements NextDoc4jPathExcluder {

    private final SaTokenExtensionProperties saTokenExtensionProperties;

    @Override
    public Set<String> getExcludedPaths() {
        Set<String> paths = new HashSet<>();
        this.addConfiguredExcludes(paths);
        return paths;
    }

    /**
     * 添加 Sa-Token 配置中的排除路径
     */
    private void addConfiguredExcludes(Set<String> paths) {
        if (saTokenExtensionProperties == null || saTokenExtensionProperties
            .getSecurity() == null
            || saTokenExtensionProperties.getSecurity().getExcludes() == null) {
            return;
        }

        paths.addAll(Arrays.asList(saTokenExtensionProperties.getSecurity().getExcludes()));
    }

    @Override
    public int getOrder() {
        // 在 RequestMappingHandlerMapping Excluder 之后执行
        return 200;
    }
}
