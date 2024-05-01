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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.system.enums.FileTypeEnum;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 文件资源统计信息
 *
 * @author Kils
 * @since 2024/4/30 14:30
 */
@Data
@Schema(description = "文件资源统计信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileStatisticsResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件类型
     */
    @Schema(description = "类型（1：其他；2：图片；3：文档；4：视频；5：音频）", type = "Integer", allowableValues = {"1", "2", "3", "4",
        "5"}, example = "2")
    private FileTypeEnum type;

    /**
     * 大小（字节）
     */
    @Schema(description = "大小（字节）", example = "4096")
    private Long size;

    /**
     * 数量
     */
    @Schema(description = "数量", example = "1000")
    private Long number;

    /**
     * 分类数据
     */
    @Schema(description = "分类数据")
    private List<FileStatisticsResp> data;
}