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

package top.continew.admin.system.model.resp;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.starter.extension.crud.model.resp.BaseDetailResp;

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
     * 扩展名
     */
    @Schema(description = "扩展名", example = "jpg")
    private String extension;

    /**
     * 类型
     */
    @Schema(description = "类型（1：其他；2：图片；3：文档；4：视频；5：音频）", type = "Integer", allowableValues = {"1", "2", "3", "4",
        "5"}, example = "2")
    private FileTypeEnum type;

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