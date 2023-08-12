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

package top.charles7c.cnadmin.common.model.vo;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.HttpStatus;

/**
 * 响应信息
 *
 * @author Charles7c
 * @since 2022/12/10 23:31
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "响应信息")
public class R<V> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否成功 */
    @Schema(description = "是否成功", example = "true")
    private boolean success;

    /** 状态码 */
    @Schema(description = "状态码", example = "200")
    private int code;

    /** 状态信息 */
    @Schema(description = "状态信息", example = "操作成功")
    private String msg;

    /** 返回数据 */
    @Schema(description = "返回数据")
    private V data;

    /** 时间戳 */
    @Schema(description = "时间戳", example = "1691453288000")
    private long timestamp = System.currentTimeMillis();

    /** 成功状态码 */
    private static final int SUCCESS_CODE = HttpStatus.OK.value();
    /** 失败状态码 */
    private static final int FAIL_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    private R(boolean success, int code, String msg, V data) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <V> R<V> ok() {
        return new R<>(true, SUCCESS_CODE, "操作成功", null);
    }

    public static <V> R<V> ok(V data) {
        return new R<>(true, SUCCESS_CODE, "操作成功", data);
    }

    public static <V> R<V> ok(String msg) {
        return new R<>(true, SUCCESS_CODE, msg, null);
    }

    public static <V> R<V> ok(String msg, V data) {
        return new R<>(true, SUCCESS_CODE, msg, data);
    }

    public static <V> R<V> fail() {
        return new R<>(false, FAIL_CODE, "操作失败", null);
    }

    public static <V> R<V> fail(String msg) {
        return new R<>(false, FAIL_CODE, msg, null);
    }

    public static <V> R<V> fail(V data) {
        return new R<>(false, FAIL_CODE, "操作失败", data);
    }

    public static <V> R<V> fail(String msg, V data) {
        return new R<>(false, FAIL_CODE, msg, data);
    }

    public static <V> R<V> fail(int code, String msg) {
        return new R<>(false, code, msg, null);
    }
}
