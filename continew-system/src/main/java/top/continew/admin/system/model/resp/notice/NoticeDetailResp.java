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

package top.continew.admin.system.model.resp.notice;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.common.config.excel.DictExcelProperty;
import top.continew.admin.common.config.excel.ExcelDictConverter;
import top.continew.admin.common.model.resp.BaseDetailResp;
import top.continew.admin.system.enums.NoticeScopeEnum;
import top.continew.admin.system.enums.NoticeStatusEnum;
import top.continew.starter.file.excel.converter.ExcelBaseEnumConverter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告详情响应参数
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "公告详情响应参数")
public class NoticeDetailResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标题
     */
    @Schema(description = "标题", example = "这是公告标题")
    @ExcelProperty(value = "标题", order = 2)
    private String title;

    /**
     * 分类（取值于字典 notice_type）
     */
    @Schema(description = "分类（取值于字典 notice_type）", example = "1")
    @ExcelProperty(value = "分类", converter = ExcelDictConverter.class, order = 3)
    @DictExcelProperty("notice_type")
    private String type;

    /**
     * 内容
     */
    @Schema(description = "内容", example = "这是公告内容")
    private String content;

    /**
     * 通知范围
     */
    @Schema(description = "通知范围", example = "2")
    @ExcelProperty(value = "通知范围", converter = ExcelBaseEnumConverter.class, order = 4)
    private NoticeScopeEnum noticeScope;

    /**
     * 通知用户
     */
    @Schema(description = "通知用户", example = "[1,2,3]")
    private List<String> noticeUsers;

    /**
     * 通知方式
     */
    @Schema(description = "通知方式", example = "[1,2]")
    private List<Integer> noticeMethods;

    /**
     * 是否定时
     */
    @Schema(description = "是否定时", example = "false")
    @ExcelProperty(value = "是否定时", order = 5)
    private Boolean isTiming;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间", example = "2023-08-08 00:00:00", type = "string")
    @ExcelProperty(value = "发布时间", order = 6)
    private LocalDateTime publishTime;

    /**
     * 是否置顶
     */
    @Schema(description = "是否置顶", example = "false")
    @ExcelProperty(value = "是否置顶", order = 7)
    private Boolean isTop;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "3")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class, order = 8)
    private NoticeStatusEnum status;
}