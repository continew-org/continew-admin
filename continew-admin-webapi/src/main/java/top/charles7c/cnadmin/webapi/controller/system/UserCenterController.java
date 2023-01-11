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

import javax.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.common.config.properties.LocalStorageProperties;
import top.charles7c.cnadmin.common.consts.FileConstants;
import top.charles7c.cnadmin.common.consts.RegExpConstants;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.common.util.ExceptionUtils;
import top.charles7c.cnadmin.common.util.SecureUtils;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;
import top.charles7c.cnadmin.common.util.validate.ValidationUtils;
import top.charles7c.cnadmin.system.model.entity.SysUser;
import top.charles7c.cnadmin.system.model.request.UpdateBasicInfoRequest;
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
@RequestMapping(value = "/system/user/center", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserCenterController {

    private final UserService userService;
    private final LocalStorageProperties localStorageProperties;

    @Operation(summary = "上传头像", description = "用户上传个人头像")
    @PostMapping("/avatar")
    public R<AvatarVO> uploadAvatar(@NotNull(message = "头像不能为空") MultipartFile avatarFile) {
        // 校验
        ValidationUtils.exIfCondition(avatarFile::isEmpty, "头像不能为空");
        Long avatarMaxSizeInMb = localStorageProperties.getAvatarMaxSizeInMb();
        ValidationUtils.exIfCondition(() -> avatarFile.getSize() > avatarMaxSizeInMb * 1024 * 1024,
            String.format("请上传小于 %s MB 的图片", avatarMaxSizeInMb));
        String avatarImageType = FileNameUtil.extName(avatarFile.getOriginalFilename());
        String[] avatarSupportImgTypes = FileConstants.AVATAR_SUPPORTED_IMG_TYPES;
        ValidationUtils.exIfCondition(() -> !StrUtil.equalsAnyIgnoreCase(avatarImageType, avatarSupportImgTypes),
            String.format("头像仅支持 %s 格式的图片", String.join("，", avatarSupportImgTypes)));

        // 上传头像
        String newAvatar = userService.uploadAvatar(avatarFile, LoginHelper.getUserId());
        return R.ok("上传成功", new AvatarVO().setAvatar(newAvatar));
    }

    @Operation(summary = "修改基础信息", description = "修改用户基础信息")
    @PatchMapping("/basic/info")
    public R updateBasicInfo(@Validated @RequestBody UpdateBasicInfoRequest updateBasicInfoRequest) {
        SysUser user = new SysUser();
        user.setUserId(LoginHelper.getUserId());
        BeanUtil.copyProperties(updateBasicInfoRequest, user);
        userService.update(user);
        return R.ok("修改成功");
    }

    @Operation(summary = "修改密码", description = "修改用户登录密码")
    @PatchMapping("/password")
    public R updatePassword(@Validated @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        // 解密
        String rawOldPassword =
            ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(updatePasswordRequest.getOldPassword()));
        ValidationUtils.exIfBlank(rawOldPassword, "当前密码解密失败");
        String rawNewPassword =
            ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(updatePasswordRequest.getNewPassword()));
        ValidationUtils.exIfBlank(rawNewPassword, "新密码解密失败");

        // 校验
        ValidationUtils.exIfCondition(() -> !ReUtil.isMatch(RegExpConstants.PASSWORD, rawNewPassword),
            "密码长度 6 到 32 位，同时包含数字和字母");
        ValidationUtils.exIfEqual(rawNewPassword, rawOldPassword, "新密码不能与当前密码相同");

        // 修改密码
        userService.updatePassword(rawOldPassword, rawNewPassword, LoginHelper.getUserId());
        return R.ok("修改成功");
    }
}
