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

package top.continew.admin.job.constant;

/**
 * 任务调度常量
 *
 * @author KAI
 * @since 2024/6/26 9:19
 */
public class JobConstants {

    /**
     * 请求头：命名空间 ID
     */
    public static final String NAMESPACE_ID_HEADER = "SNAIL-JOB-NAMESPACE-ID";

    /**
     * 请求头：认证令牌
     */
    public static final String AUTH_TOKEN_HEADER = "Snail-Job-Auth";

    private JobConstants() {
    }
}
