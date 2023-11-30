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

package top.charles7c.continew.admin.common.config.properties;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;

/**
 * 本地存储配置属性
 *
 * @author Charles7c
 * @since 2023/1/2 19:43
 */
@Data
@Component
@ConfigurationProperties(prefix = "local-storage")
public class LocalStorageProperties {

    /** 文件模式 */
    private String filePattern;

    /** 头像模式 */
    private String avatarPattern;

    /** 文件大小限制 */
    private Long maxSizeInMb;

    /** 头像大小限制 */
    private Long avatarMaxSizeInMb;

    /** Windows 系统本地存储路径 */
    private LocalStoragePath windows;

    /** Linux 系统本地存储路径 */
    private LocalStoragePath linux;

    /** MAC 系统本地存储路径 */
    private LocalStoragePath mac;

    /**
     * 获取存储路径
     * 
     * @return /
     */
    public LocalStoragePath getPath() {
        OsInfo osInfo = SystemUtil.getOsInfo();
        if (osInfo.isWindows()) {
            return windows;
        }
        if (osInfo.isMac()) {
            return mac;
        }
        return linux;
    }

    /**
     * 本地存储路径
     */
    @Data
    public static class LocalStoragePath {

        /** 文件存储路径 */
        private String file;

        /** 头像存储路径 */
        private String avatar;
    }
}
