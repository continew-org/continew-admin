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
 * 文件信息
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "文件详情信息")
public class FileResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "example")
    private String name;

    /**
     * 大小（字节）
     */
    @Schema(description = "大小（字节）", example = "4096")
    private Long size;

    /**
     * URL
     */
    @Schema(description = "URL", example = "https://examplebucket.oss-cn-hangzhou.aliyuncs.com/example/example.jpg")
    private String url;

    /**
     * 上级目录
     */
    @Schema(description = "上级目录", example = "25")
    private String parentPath;

    /**
     * 绝对路径
     */
    @Schema(description = "绝对路径", example = "/2025/2/25")
    private String absPath;

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
     * MD5 值
     */
    @Schema(description = "MD5值", example = "193572f83684128a0d0f993e97100f8a")
    private String md5;

    /**
     * 元数据
     */
    @Schema(description = "元数据", example = "{width:1024,height:1024}")
    private String metadata;

    /**
     * 缩略图大小（字节)
     */
    @Schema(description = "缩略图大小（字节)", example = "1024")
    private Long thumbnailSize;

    /**
     * 缩略图 URL
     */
    @Schema(description = "缩略图 URL", example = "https://examplebucket.oss-cn-hangzhou.aliyuncs.com/example/example.jpg.min.jpg")
    private String thumbnailUrl;

    /**
     * 缩略图元数据
     */
    @Schema(description = "缩略图文件元数据", example = "{width:100,height:100}")
    private String thumbnailMetadata;

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