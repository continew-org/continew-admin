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

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.common.model.resp.BaseDetailResp;
import top.continew.admin.system.enums.FileTypeEnum;

import java.io.Serial;

/**
 * 文件响应参数
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "文件响应参数")
public class FileResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "6824afe8408da079832dcfb6.jpg")
    private String name;

    /**
     * 原始名称
     */
    @Schema(description = "原始名称", example = "example.jpg")
    private String originalName;

    /**
     * 大小（字节）
     */
    @Schema(description = "大小（字节）", example = "4096")
    private Long size;

    /**
     * URL
     */
    @Schema(description = "URL", example = "https://examplebucket.oss-cn-hangzhou.aliyuncs.com/2025/2/25/6824afe8408da079832dcfb6.jpg")
    private String url;

    /**
     * 上级目录
     */
    @Schema(description = "上级目录", example = "/2025/2/25")
    private String parentPath;

    /**
     * 路径
     */
    @Schema(description = "路径", example = "/2025/2/25/6824afe8408da079832dcfb6.jpg")
    private String path;

    /**
     * 扩展名
     */
    @Schema(description = "扩展名", example = "jpg")
    private String extension;

    /**
     * 内容类型
     */
    @Schema(description = "内容类型", example = "image/jpeg")
    private String contentType;

    /**
     * 类型
     */
    @Schema(description = "类型", example = "2")
    private FileTypeEnum type;

    /**
     * SHA256 值
     */
    @Schema(description = "SHA256 值", example = "722f185c48bed892d6fa12e2b8bf1e5f8200d4a70f522fb62112b6caf13cb74e")
    private String sha256;

    /**
     * 元数据
     */
    @Schema(description = "元数据", example = "{width:1024,height:1024}")
    private String metadata;

    /**
     * 缩略图名称
     */
    @Schema(description = "缩略图名称", example = "example.jpg.min.jpg")
    private String thumbnailName;

    /**
     * 缩略图大小（字节)
     */
    @Schema(description = "缩略图大小（字节)", example = "1024")
    private Long thumbnailSize;

    /**
     * 缩略图元数据
     */
    @Schema(description = "缩略图文件元数据", example = "{width:100,height:100}")
    private String thumbnailMetadata;

    /**
     * 缩略图 URL
     */
    @Schema(description = "缩略图 URL", example = "https://examplebucket.oss-cn-hangzhou.aliyuncs.com/2025/2/25/example.jpg.min.jpg")
    private String thumbnailUrl;

    /**
     * 存储 ID
     */
    @Schema(description = "存储 ID", example = "1")
    private Long storageId;

    /**
     * 存储名称
     */
    @Schema(description = "存储名称", example = "MinIO")
    private String storageName;
}