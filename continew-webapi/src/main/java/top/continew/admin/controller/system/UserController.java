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

package top.continew.admin.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ReUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.common.config.properties.CaptchaProperties;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.controller.BaseController;
import top.continew.admin.common.constant.RegexConstants;
import top.continew.admin.common.util.SecureUtils;
import top.continew.admin.system.model.entity.UserDO;
import top.continew.admin.system.model.query.UserQuery;
import top.continew.admin.system.model.req.user.*;
import top.continew.admin.system.model.resp.user.UserDetailResp;
import top.continew.admin.system.model.resp.user.UserImportParseResp;
import top.continew.admin.system.model.resp.user.UserImportResp;
import top.continew.admin.system.model.resp.user.UserResp;
import top.continew.admin.system.service.UserService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.util.ExceptionUtils;
import top.continew.starter.core.validation.ValidationUtils;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;
import top.continew.starter.extension.crud.model.resp.BaseIdResp;
import top.continew.starter.extension.crud.validation.CrudValidationGroup;

import java.io.IOException;

/**
 * 用户管理 API
 *
 * @author Charles7c
 * @since 2023/2/20 21:00
 */
@Tag(name = "用户管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/system/user", api = {Api.PAGE, Api.LIST, Api.DETAIL, Api.ADD, Api.UPDATE, Api.DELETE,
    Api.EXPORT})
public class UserController extends BaseController<UserService, UserResp, UserDetailResp, UserQuery, UserReq> {

    private final UserService userService;
    private final CaptchaProperties captchaProperties;

    @Operation(summary = "用户注册", description = "用户注册")
    @PostMapping(value = "/signup")
    public BaseIdResp<Long> signup(@Validated(CrudValidationGroup.Add.class) @RequestBody UserReq req) {
        String captcha = req.getCaptcha();
        if (!StringUtil.equals(captcha, captchaProperties.getSms().getCode())) {
            String key = StringUtil.isNotBlank(req.getUuid())
                ? req.getUuid()
                : StringUtil.isNotBlank(req.getPhone())
                    ? req.getPhone()
                    : StringUtil.isNotBlank(req.getEmail()) ? req.getEmail() : "";
            ValidationUtils.throwIfBlank(captcha, "验证码不能为空");
            ValidationUtils.throwIfBlank(key, "验证码标识不能为空");
            String captchaKey = CacheConstants.CAPTCHA_KEY_PREFIX + key;
            String captcha1 = RedisUtils.get(captchaKey);
            ValidationUtils.throwIfBlank(captcha1, "验证码已失效");
            ValidationUtils.throwIfNotEqualIgnoreCase(captcha, captcha1, "验证码错误");
            //            RedisUtils.delete(captchaKey);
        }
        String rawPassword = ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(req.getPassword()));
        ValidationUtils.throwIfNull(rawPassword, "密码解密失败");
        ValidationUtils.throwIf(!ReUtil
            .isMatch(RegexConstants.PASSWORD, rawPassword), "密码长度为 8-32 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字");
        req.setPassword(rawPassword);
        return super.add(req);
    }

    @Operation(summary = "修改密码", description = "修改用户登录密码")
    @PostMapping("/password")
    public void updatePassword(@Validated @RequestBody UserPasswordUpdateReq updateReq) {
        String captcha = updateReq.getCaptcha();
        if (!StringUtil.equals(captcha, captchaProperties.getSms().getCode())) {
            String captchaKey = CacheConstants.CAPTCHA_KEY_PREFIX + updateReq.getEmail();
            String captcha1 = RedisUtils.get(captchaKey);
            ValidationUtils.throwIfBlank(captcha1, "验证码已失效");
            ValidationUtils.throwIfNotEqualIgnoreCase(captcha, captcha1, "验证码错误");
            RedisUtils.delete(captchaKey);
        }
        String newPassword = ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(updateReq
            .getNewPassword()));
        ValidationUtils.throwIfNull(newPassword, "新密码解密失败");
        ValidationUtils.throwIf(!ReUtil
            .isMatch(RegexConstants.PASSWORD, newPassword), "密码长度为 8-32 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字");
        UserDO user = userService.getByUsername(updateReq.getUsername());
        ValidationUtils.throwIfEmpty(user, "用户名错误或不存在");
        userService.updatePassword("", newPassword, user.getId());
    }

    @Override
    @Operation(summary = "新增数据", description = "新增数据")
    public BaseIdResp<Long> add(@Validated(CrudValidationGroup.Add.class) @RequestBody UserReq req) {
        String rawPassword = ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(req.getPassword()));
        ValidationUtils.throwIfNull(rawPassword, "密码解密失败");
        ValidationUtils.throwIf(!ReUtil
            .isMatch(RegexConstants.PASSWORD, rawPassword), "密码长度为 8-32 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字");
        req.setPassword(rawPassword);
        return super.add(req);
    }

    @Operation(summary = "下载导入模板", description = "下载导入模板")
    @SaCheckPermission("system:user:import")
    @GetMapping(value = "/import/template", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        baseService.downloadImportTemplate(response);
    }

    @Operation(summary = "解析导入数据", description = "解析导入数据")
    @SaCheckPermission("system:user:import")
    @PostMapping("/import/parse")
    public UserImportParseResp parseImport(@NotNull(message = "文件不能为空") MultipartFile file) {
        ValidationUtils.throwIf(file::isEmpty, "文件不能为空");
        return baseService.parseImport(file);
    }

    @Operation(summary = "导入数据", description = "导入数据")
    @SaCheckPermission("system:user:import")
    @PostMapping(value = "/import")
    public UserImportResp importUser(@Validated @RequestBody UserImportReq req) {
        return baseService.importUser(req);
    }

    @Operation(summary = "重置密码", description = "重置用户登录密码")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:user:resetPwd")
    @PatchMapping("/{id}/password")
    public void resetPassword(@Validated @RequestBody UserPasswordResetReq req, @PathVariable Long id) {
        String rawNewPassword = ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(req.getNewPassword()));
        ValidationUtils.throwIfNull(rawNewPassword, "新密码解密失败");
        ValidationUtils.throwIf(!ReUtil
            .isMatch(RegexConstants.PASSWORD, rawNewPassword), "密码长度为 8-32 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字");
        req.setNewPassword(rawNewPassword);
        baseService.resetPassword(req, id);
    }

    @Operation(summary = "分配角色", description = "为用户新增或移除角色")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:user:updateRole")
    @PatchMapping("/{id}/role")
    public void updateRole(@Validated @RequestBody UserRoleUpdateReq updateReq, @PathVariable Long id) {
        baseService.updateRole(updateReq, id);
    }
}
