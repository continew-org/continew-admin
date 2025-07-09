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

package top.continew.admin.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import top.continew.admin.common.model.entity.BaseDO;
import top.continew.admin.system.enums.NoticeScopeEnum;
import top.continew.admin.system.enums.NoticeStatusEnum;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告实体
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
@Data
@TableName(value = "sys_notice", autoResultMap = true)
public class NoticeDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 分类（取值于字典 notice_type）
     */
    private String type;

    /**
     * 通知范围
     */
    private NoticeScopeEnum noticeScope;

    /**
     * 通知用户
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> noticeUsers;

    /**
     * 通知方式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> noticeMethods;

    /**
     * 是否定时
     */
    private Boolean isTiming;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 状态
     */
    private NoticeStatusEnum status;
}