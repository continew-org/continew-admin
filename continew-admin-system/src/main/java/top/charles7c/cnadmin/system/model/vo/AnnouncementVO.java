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

package top.charles7c.cnadmin.system.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import top.charles7c.cnadmin.common.base.BaseVO;
import top.charles7c.cnadmin.system.enums.AnnouncementTypeEnum;

/**
 * 公告信息
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
@Data
@Schema(description = "公告信息")
public class AnnouncementVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    /**
     * 标题
     */
    @Schema(description = "标题", example = "这是公告标题")
    private String title;

    /**
     * 类型
     */
    @Schema(description = "类型", type = "Integer", allowableValues = {"1", "2", "3"}, example = "1")
    private AnnouncementTypeEnum type;

    /**
     * 生效时间
     */
    @Schema(description = "生效时间", example = "2023-08-08 00:00:00", type = "string")
    private LocalDateTime effectiveTime;

    /**
     * 终止时间
     */
    @Schema(description = "终止时间", example = "2023-08-08 23:59:59", type = "string")
    private LocalDateTime terminateTime;

    /**
     * 状态
     * 
     * @return 公告状态
     */
    @Schema(description = "状态（1：已发布，2：已过期）", example = "1")
    public Integer getStatus() {
        int status = 1;
        if (null != this.effectiveTime) {
            if (this.effectiveTime.isAfter(LocalDateTime.now())) {
                status = 2;
            }
        }
        if (null != this.terminateTime) {
            if (this.terminateTime.isBefore(LocalDateTime.now())) {
                status = 2;
            }
        }
        return status;
    }
}