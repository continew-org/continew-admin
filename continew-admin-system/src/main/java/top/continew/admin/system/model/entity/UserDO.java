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

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.continew.admin.common.config.mybatis.BCryptEncryptor;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.enums.GenderEnum;
import top.continew.starter.extension.crud.model.entity.BaseDO;
import top.continew.starter.security.crypto.annotation.FieldEncrypt;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author Charles7c
 * @since 2022/12/21 20:42
 */
@Data
@TableName("sys_user")
public class UserDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 密码
     */
    @FieldEncrypt(encryptor = BCryptEncryptor.class)
    private String password;

    /**
     * 性别
     */
    private GenderEnum gender;

    /**
     * 邮箱
     */
    @FieldEncrypt
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
    private String email;

    /**
     * 手机号码
     */
    @FieldEncrypt
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
    private String phone;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private DisEnableStatusEnum status;

    /**
     * 是否为系统内置数据
     */
    private Boolean isSystem;

    /**
     * 最后一次修改密码时间
     */
    private LocalDateTime pwdResetTime;

    /**
     * 部门 ID
     */
    private Long deptId;
}
