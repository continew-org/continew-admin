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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.admin.common.constant.RegexConstants;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.system.model.entity.UserDO;
import top.continew.admin.system.service.OptionService;
import top.continew.admin.system.service.UserPasswordHistoryService;
import top.continew.starter.core.util.validate.ValidationUtils;

import java.util.Map;

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
     * 登录密码错误锁定账号的次数
     */
    PASSWORD_ERROR_LOCK_COUNT("登录密码错误锁定账号的次数取值范围为 %d-%d", SysConstants.NO, 10, null),

    /**
     * 登录密码错误锁定账号的时间（min）
     */
    PASSWORD_ERROR_LOCK_MINUTES("登录密码错误锁定账号的时间取值范围为 %d-%d 分钟", 1, 1440, null),

    /**
     * 密码有效期（天）
     */
    PASSWORD_EXPIRATION_DAYS("密码有效期取值范围为 %d-%d 天", SysConstants.NO, 999, null),

    /**
     * 密码到期提前提示（天）
     */
    PASSWORD_EXPIRATION_WARNING_DAYS("密码到期提前提示取值范围为 %d-%d 天", SysConstants.NO, 998, null) {
        @Override
        public void validateRange(int value, Map<String, String> policyMap) {
            if (CollUtil.isEmpty(policyMap)) {
                super.validateRange(value, policyMap);
                return;
            }
            Integer passwordExpirationDays = ObjUtil.defaultIfNull(Convert.toInt(policyMap.get(PASSWORD_EXPIRATION_DAYS
                .name())), SpringUtil.getBean(OptionService.class).getValueByCode2Int(PASSWORD_EXPIRATION_DAYS.name()));
            if (passwordExpirationDays > SysConstants.NO) {
                ValidationUtils.throwIf(value >= passwordExpirationDays, "密码到期前的提示时间应小于密码有效期");
                return;
            }
            super.validateRange(value, policyMap);
        }
    },

    /**
     * 密码最小长度
     */
    PASSWORD_MIN_LENGTH("密码最小长度取值范围为 %d-%d", 8, 32, "密码最小长度为 %d 个字符") {
        @Override
        public void validate(String password, int value, UserDO user) {
            // 最小长度校验
            ValidationUtils.throwIf(StrUtil.length(password) < value, this.getMsg().formatted(value));
            // 完整校验
            int passwordMaxLength = this.getMax();
            ValidationUtils.throwIf(!ReUtil.isMatch(RegexConstants.PASSWORD_TEMPLATE
                .formatted(value, passwordMaxLength), password), "密码长度为 {}-{} 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字", value, passwordMaxLength);
        }
    },

    /**
     * 密码是否必须包含特殊字符
     */
    PASSWORD_REQUIRE_SYMBOLS("密码是否必须包含特殊字符取值只能为是（%d）或否（%d）", SysConstants.NO, SysConstants.YES, "密码必须包含特殊字符") {
        @Override
        public void validateRange(int value, Map<String, String> policyMap) {
            ValidationUtils.throwIf(value != SysConstants.YES && value != SysConstants.NO, this.getDescription()
                .formatted(SysConstants.YES, SysConstants.NO));
        }

        @Override
        public void validate(String password, int value, UserDO user) {
            ValidationUtils.throwIf(value == SysConstants.YES && !ReUtil
                .isMatch(RegexConstants.SPECIAL_CHARACTER, password), this.getMsg());
        }
    },

    /**
     * 密码是否允许包含正反序账号名
     */
    PASSWORD_ALLOW_CONTAIN_USERNAME("密码是否允许包含正反序账号名取值只能为是（%d）或否（%d）", SysConstants.NO, SysConstants.YES, "密码不允许包含正反序账号名") {
        @Override
        public void validateRange(int value, Map<String, String> policyMap) {
            ValidationUtils.throwIf(value != SysConstants.YES && value != SysConstants.NO, this.getDescription()
                .formatted(SysConstants.YES, SysConstants.NO));
        }

        @Override
        public void validate(String password, int value, UserDO user) {
            if (value <= SysConstants.NO) {
                String username = user.getUsername();
                ValidationUtils.throwIf(StrUtil.containsAnyIgnoreCase(password, username, StrUtil
                    .reverse(username)), this.getMsg());
            }
        }
    },

    /**
     * 密码重复使用次数
     */
    PASSWORD_REPETITION_TIMES("密码重复使用规则取值范围为 %d-%d", 3, 32, "新密码不得与历史前 %d 次密码重复") {
        @Override
        public void validate(String password, int value, UserDO user) {
            UserPasswordHistoryService userPasswordHistoryService = SpringUtil
                .getBean(UserPasswordHistoryService.class);
            ValidationUtils.throwIf(userPasswordHistoryService.isPasswordReused(user.getId(), password, value), this
                .getMsg()
                .formatted(value));
        }
    },;

    /**
     * 描述
     */
    private final String description;

    /**
     * 最小值
     */
    private final Integer min;

    /**
     * 最大值
     */
    private final Integer max;

    /**
     * 提示信息
     */
    private final String msg;

    /**
     * 策略类别
     */
    public static final String CATEGORY = "PASSWORD";

    /**
     * 校验取值范围
     *
     * @param value     值
     * @param policyMap 策略集合
     */
    public void validateRange(int value, Map<String, String> policyMap) {
        Integer minValue = this.getMin();
        Integer maxValue = this.getMax();
        ValidationUtils.throwIf(value < minValue || value > maxValue, this.getDescription()
            .formatted(minValue, maxValue));
    }

    /**
     * 校验
     *
     * @param password 密码
     * @param value    策略值
     * @param user     用户信息
     */
    public void validate(String password, int value, UserDO user) {
        // 无需校验
    }
}
