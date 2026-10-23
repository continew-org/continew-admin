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

package top.continew.admin.common.exception;

import top.continew.starter.core.exception.BusinessException;

import java.io.Serial;

/**
 * 密码过期异常
 * <p>
 * 与普通业务异常区分，便于前端识别后跳转到修改密码页。
 * </p>
 *
 * @author luoqiz
 * @since 5.0.0
 */
public class PasswordExpiredException extends BusinessException {

    /**
     * 稳定的错误码，前端按此值识别，不要依赖提示文案。
     * <p>
     * 取 428（Precondition Required，要求先满足前置条件再发起请求）。{@code R.code} 是 String，
     * 前端会按数值解析，所以必须是数字形态的字符串；换成符号型错误码（如 PASSWORD_EXPIRED）
     * 会被解析成 NaN，按 code 分流的逻辑就静默失效了。
     * </p>
     */
    public static final String CODE = "428";

    @Serial
    private static final long serialVersionUID = 1L;

    public PasswordExpiredException() {
        super("密码已过期，请先修改密码");
    }
}
