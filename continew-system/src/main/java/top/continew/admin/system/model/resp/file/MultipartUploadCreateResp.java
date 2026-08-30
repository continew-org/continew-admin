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
import java.util.Set;

/**
 * 分片上传初始化结果
 *
 * @author echo
 * @since 2.14.0
 */
@Data
@Schema(description = "分片初始化响应参数")
public class MultipartUploadCreateResp implements Serializable {

    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    private String fileId;

    /**
     * 上传ID（S3返回的uploadId）
     */
    @Schema(description = "上传ID")
    private String uploadId;

    /**
     * 存储桶
     */
    @Schema(description = "存储桶")
    private String bucket;

    /**
     * 存储平台
     */
    @Schema(description = "存储平台")
    private String platform;

    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String fileName;

    /**
     * 文件MD5
     */
    @Schema(description = "文件MD5")
    private String fileMd5;

    /**
     * 文件大小
     */
    @Schema(description = "文件大小")
    private long fileSize;

    /**
     * 扩展名
     */
    @Schema(description = "扩展名")
    private String extension;

    /**
     * 内容类型
     */
    @Schema(description = "内容类型")
    private String contentType;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型")
    private String type;

    /**
     * 文件父路径
     */
    @Schema(description = "文件父路径")
    private String parentPath;

    /**
     * 文件路径
     */
    @Schema(description = "文件路径")
    private String path;

    /**
     * 分片大小
     */
    @Schema(description = "分片大小")
    private Long partSize;

    /**
     * 已上传分片编号集合
     */
    @Schema(description = "已上传分片编号集合")
    private Set<Integer> uploadedPartNumbers;

}
