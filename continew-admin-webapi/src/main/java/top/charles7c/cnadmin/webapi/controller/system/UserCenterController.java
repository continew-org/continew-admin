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

package top.charles7c.cnadmin.webapi.controller.system;

import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.util.ReUtil;

import top.charles7c.cnadmin.common.constant.CacheConsts;
import top.charles7c.cnadmin.common.constant.RegexConsts;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.common.util.ExceptionUtils;
import top.charles7c.cnadmin.common.util.RedisUtils;
import top.charles7c.cnadmin.common.util.SecureUtils;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;
import top.charles7c.cnadmin.common.util.validate.ValidationUtils;
import top.charles7c.cnadmin.system.model.request.UpdateBasicInfoRequest;
import top.charles7c.cnadmin.system.model.request.UpdateEmailRequest;
import top.charles7c.cnadmin.system.model.request.UpdatePasswordRequest;
import top.charles7c.cnadmin.system.model.vo.AvatarVO;
import top.charles7c.cnadmin.system.service.UserService;

/**
 * 个人中心 API
 *
 * @author Charles7c
 * @since 2023/1/2 11:41
 */
@Tag(name = "个人中心 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user/center")
public class UserCenterController {

    private final UserService userService;

    @Operation(summary = "上传头像", description = "用户上传个人头像")
    @PostMapping("/avatar")
    public R<AvatarVO> uploadAvatar(@NotNull(message = "头像不能为空") MultipartFile avatarFile) {
        ValidationUtils.throwIf(avatarFile::isEmpty, "头像不能为空");

        // 上传头像
        String newAvatar = userService.uploadAvatar(avatarFile, LoginHelper.getUserId());
        return R.ok("上传成功", new AvatarVO().setAvatar(newAvatar));
    }

    @Operation(summary = "修改基础信息", description = "修改用户基础信息")
    @PatchMapping("/basic/info")
    public R updateBasicInfo(@Validated @RequestBody UpdateBasicInfoRequest updateBasicInfoRequest) {
        userService.updateBasicInfo(updateBasicInfoRequest, LoginHelper.getUserId());
        return R.ok("修改成功");
    }

    @Operation(summary = "修改密码", description = "修改用户登录密码")
    @PatchMapping("/password")
    public R updatePassword(@Validated @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        String rawOldPassword =
            ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(updatePasswordRequest.getOldPassword()));
        ValidationUtils.throwIfBlank(rawOldPassword, "当前密码解密失败");
        String rawNewPassword =
            ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(updatePasswordRequest.getNewPassword()));
        ValidationUtils.throwIfBlank(rawNewPassword, "新密码解密失败");
        ValidationUtils.throwIf(!ReUtil.isMatch(RegexConsts.PASSWORD, rawNewPassword), "密码长度 6 到 32 位，同时包含字母和数字");

        // 修改密码
        userService.updatePassword(rawOldPassword, rawNewPassword, LoginHelper.getUserId());
        return R.ok("修改成功");
    }

    @Operation(summary = "修改邮箱", description = "修改用户邮箱")
    @PatchMapping("/email")
    public R updateEmail(@Validated @RequestBody UpdateEmailRequest updateEmailRequest) {
        String rawCurrentPassword =
            ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(updateEmailRequest.getCurrentPassword()));
        ValidationUtils.throwIfBlank(rawCurrentPassword, "当前密码解密失败");

        // 校验验证码
        String captchaKey = RedisUtils.formatKey(CacheConsts.CAPTCHA_KEY_PREFIX, updateEmailRequest.getNewEmail());
        String captcha = RedisUtils.getCacheObject(captchaKey);
        ValidationUtils.throwIfBlank(captcha, "验证码已失效");
        ValidationUtils.throwIfNotEqualIgnoreCase(updateEmailRequest.getCaptcha(), captcha, "验证码错误");
        RedisUtils.deleteCacheObject(captchaKey);

        // 修改邮箱
        userService.updateEmail(updateEmailRequest.getNewEmail(), rawCurrentPassword, LoginHelper.getUserId());
        return R.ok("修改成功");
    }
}
