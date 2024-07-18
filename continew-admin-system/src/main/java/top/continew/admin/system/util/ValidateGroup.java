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

import jakarta.validation.groups.Default;

/**
 * 分组校验
 *
 * @author Charles7c
 * @since 2024/7/3 22:01
 */
public interface ValidateGroup extends Default {

    /**
     * 分组校验-增删改查
     */
    interface Storage extends ValidateGroup {
        /**
         * 本地存储
         */
        interface Local extends Storage {
        }

        /**
         * 兼容S3协议存储
         */
        interface S3 extends Storage {
        }
    }
}