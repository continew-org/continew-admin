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

package top.continew.admin.system.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分片上传请求参数
 *
 * @author KAI
 * @since 2025/7/30 16:40
 */
@Data
@Schema(description = "分片上传请求参数")
public class MultipartUploadReq {

    /**
     * 上传ID
     */
    @Schema(description = "上传ID")
    private String uploadId;

    /**
     * 分片序号
     */
    @Schema(description = "分片序号")
    private Integer partNumber;

    /**
     * 分片ETag
     */
    @Schema(description = "分片ETag")
    private String eTag;

    /**
     * 存储编码
     */
    @Schema(description = "存储编码")
    private String storageCode;
}
