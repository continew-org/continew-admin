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

package top.continew.admin.common.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import top.continew.admin.common.constant.GlobalConstants;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 密码有效期计算测试
 *
 * @author luoqiz
 * @since 5.0.0
 */
class UserContextPasswordExpiryTest {

    /**
     * 当前时间
     * <p>
     * 与 UserContext 用同一时区，避免 CI 跑在 UTC 时把边界用例判错。
     * </p>
     *
     * @return 当前时间
     */
    private static LocalDateTime now() {
        return LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID);
    }

    private static UserContext context(Integer expirationDays, LocalDateTime pwdResetTime) {
        UserContext context = new UserContext(Set.of(), Set.of(), expirationDays);
        context.setPwdResetTime(pwdResetTime);
        return context;
    }

    @Test
    void shouldNeverExpireWhenPolicyDisabled() {
        UserContext context = context(null, now().minusYears(1));

        assertThat(context.isPasswordExpired()).isFalse();
        assertThat(context.getPwdExpireDays()).isNull();
    }

    @Test
    void shouldNeverExpireWhenPolicyIsZero() {
        UserContext context = context(0, now().minusYears(1));

        assertThat(context.isPasswordExpired()).isFalse();
        assertThat(context.getPwdExpireDays()).isNull();
    }

    @Test
    void shouldNeverExpireWhenPwdResetTimeIsNull() {
        UserContext context = context(30, null);

        assertThat(context.isPasswordExpired()).isFalse();
        assertThat(context.getPwdExpireDays()).isNull();
    }

    @Test
    void shouldReturnRemainingDays() {
        UserContext context = context(6, now().minusDays(3));

        assertThat(context.isPasswordExpired()).isFalse();
        assertThat(context.getPwdExpireDays()).isEqualTo(3);
    }

    @Test
    void shouldReturnOneDayWhenLessThanOneDayLeft() {
        UserContext context = context(1, now().minusDays(1).plusSeconds(30));

        assertThat(context.isPasswordExpired()).isFalse();
        // 剩余不足 1 天时要向上取整为 1，不能返回 0（0 表示已过期）
        assertThat(context.getPwdExpireDays()).isEqualTo(1);
    }

    @Test
    void shouldReturnOneDayWhenOnlyHoursLeft() {
        UserContext context = context(1, now().minusHours(23));

        assertThat(context.isPasswordExpired()).isFalse();
        assertThat(context.getPwdExpireDays()).isEqualTo(1);
    }

    @Test
    void shouldReturnZeroWhenExpired() {
        UserContext context = context(1, now().minusDays(2));

        assertThat(context.isPasswordExpired()).isTrue();
        assertThat(context.getPwdExpireDays()).isZero();
    }
}
