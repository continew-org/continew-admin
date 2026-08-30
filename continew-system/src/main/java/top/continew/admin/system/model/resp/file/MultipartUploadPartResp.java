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

package top.continew.admin.system.model.resp.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分片上传结果
 *
 * @author echo
 * @since 2.14.0
 */
@Data
@Schema(description = "分片上传响应参数")
public class MultipartUploadPartResp implements Serializable {

    /**
     * 分片编号
     */
    @Schema(description = "分片编号")
    private Integer partNumber;

    /**
     * 分片ETag
     */
    @Schema(description = "分片ETag")
    private String partETag;

    /**
     * 分片大小
     */
    @Schema(description = "分片大小")
    private Long partSize;

    /**
     * 是否成功
     */
    @Schema(description = "是否成功")
    private boolean success;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

}
