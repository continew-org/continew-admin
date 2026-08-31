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

package top.continew.admin.common.constant;

import java.time.ZoneId;

/**
 * 全局常量
 *
 * @author Charles7c
 * @since 2023/2/9 22:11
 */
public class GlobalConstants {

    /**
     * 根父级 ID
     */
    public static final Long ROOT_PARENT_ID = 0L;

    /**
     * 默认业务时区（与数据库连接、雪花算法配置保持一致，统一为 Asia/Shanghai）
     */
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");

    /**
     * 布尔值常量
     */
    public static class Boolean {

        /**
         * 否
         */
        public static final Integer NO = 0;

        /**
         * 是
         */
        public static final Integer YES = 1;

        private Boolean() {
        }
    }

    private GlobalConstants() {
    }
}
