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

package top.continew.admin.system.enums;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.admin.common.constant.RegexConstants;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.system.model.entity.UserDO;
import top.continew.admin.system.service.UserPasswordHistoryService;
import top.continew.starter.core.util.validate.ValidationUtils;

/**
 * 密码策略枚举
 *
 * @author Kils
 * @author Charles7c
 * @since 2024/5/9 11:25
 */
@Getter
@RequiredArgsConstructor
public enum PasswordPolicyEnum {

    /**
     * 密码最小长度
     */
    PASSWORD_MIN_LENGTH("密码最小长度为 %s 个字符", 8, 32) {
        @Override
        public void validate(String password, int policyValue, UserDO user) {
            // 最小长度校验
            ValidationUtils.throwIf(StrUtil.length(password) < policyValue, this.getDescription()
                .formatted(policyValue));
            // 完整校验
            int passwordMaxLength = this.getMax();
            ValidationUtils.throwIf(!ReUtil.isMatch(RegexConstants.PASSWORD_TEMPLATE
                .formatted(policyValue, passwordMaxLength), password), "密码长度为 {}-{} 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字", policyValue, passwordMaxLength);
        }
    },

    /**
     * 密码是否必须包含特殊字符
     */
    PASSWORD_CONTAIN_SPECIAL_CHARACTERS("密码必须包含特殊字符", SysConstants.NO, SysConstants.YES) {
        @Override
        public void validate(String password, int policyValue, UserDO user) {
            ValidationUtils.throwIf(policyValue == SysConstants.YES && !ReUtil
                .isMatch(RegexConstants.SPECIAL_CHARACTER, password), this.getDescription());
        }
    },

    /**
     * 密码是否允许包含正反序账号名
     */
    PASSWORD_ALLOW_CONTAIN_USERNAME("密码不允许包含正反序账号名", SysConstants.NO, SysConstants.YES) {
        @Override
        public void validate(String password, int policyValue, UserDO user) {
            if (policyValue <= SysConstants.NO) {
                String username = user.getUsername();
                ValidationUtils.throwIf(StrUtil.containsAnyIgnoreCase(password, username, StrUtil
                    .reverse(username)), this.getDescription());
            }
        }
    },

    /**
     * 密码重复使用规则
     */
    PASSWORD_REUSE_POLICY("不允许使用最近 %s 次的历史密码", 3, 32) {
        @Override
        public void validate(String password, int policyValue, UserDO user) {
            UserPasswordHistoryService userPasswordHistoryService = SpringUtil
                .getBean(UserPasswordHistoryService.class);
            ValidationUtils.throwIf(userPasswordHistoryService.isPasswordReused(user
                .getId(), password, policyValue), this.getDescription().formatted(policyValue));
        }
    },

    /**
     * 登录密码错误锁定账号的次数
     */
    PASSWORD_ERROR_LOCK_COUNT(null, SysConstants.NO, 10) {
        @Override
        public void validate(String password, int policyValue, UserDO user) {
            // 无需此处校验
        }
    },

    /**
     * 登录密码错误锁定账号的时间（min）
     */
    PASSWORD_ERROR_LOCK_MINUTES(null, 1, 1440) {
        @Override
        public void validate(String password, int policyValue, UserDO user) {
            // 无需此处校验
        }
    },

    /**
     * 密码到期提前提示（天）
     */
    PASSWORD_EXPIRATION_WARNING_DAYS(null, SysConstants.NO, Integer.MAX_VALUE) {
        @Override
        public void validate(String password, int policyValue, UserDO user) {
            // 无需此处校验
        }
    },

    /**
     * 密码有效期（天）
     */
    PASSWORD_EXPIRATION_DAYS(null, SysConstants.NO, 999) {
        @Override
        public void validate(String password, int policyValue, UserDO user) {
            // 无需此处校验
        }
    },;

    private final String description;
    private final Integer min;
    private final Integer max;

    /**
     * 校验
     *
     * @param password    密码
     * @param policyValue 策略值
     * @param user        用户信息
     */
    public abstract void validate(String password, int policyValue, UserDO user);
}
