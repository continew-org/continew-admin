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

package top.charles7c.cnadmin.common.model.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 操作日志
 *
 * @author Charles7c
 * @since 2022/12/25 8:59
 */
@Data
public class OperationLog {

    /**
     * 操作人
     */
    private Long createUser;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;

    /**
     * 异常
     */
    private Exception exception;

}
