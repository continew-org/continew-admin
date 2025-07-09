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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.admin.common.constant.RegexConstants;
import top.continew.admin.system.model.req.StorageReq;
import top.continew.admin.system.validation.ValidationGroup;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.enums.BaseEnum;
import top.continew.starter.core.util.URLUtils;
import top.continew.starter.core.validation.ValidationUtils;

/**
 * 存储类型枚举
 *
 * @author Charles7c
 * @since 2023/12/27 21:45
 */
@Getter
@RequiredArgsConstructor
public enum StorageTypeEnum implements BaseEnum<Integer> {

    /**
     * 本地存储
     */
    LOCAL(1, "本地存储") {
        @Override
        public void validate(StorageReq req) {
            ValidationUtils.validate(req, ValidationGroup.Storage.Local.class);
            ValidationUtils.throwIf(StrUtil.isNotBlank(req.getDomain()) && !URLUtils.isHttpUrl(req
                .getDomain()), "访问路径格式不正确");
        }

        @Override
        public void pretreatment(StorageReq req) {
            super.pretreatment(req);
            // 本地存储路径需要以 “/” 结尾
            req.setBucketName(StrUtil.appendIfMissing(req.getBucketName(), StringConstants.SLASH));
        }
    },

    /**
     * 对象存储
     */
    OSS(2, "对象存储") {
        @Override
        public void validate(StorageReq req) {
            ValidationUtils.validate(req, ValidationGroup.Storage.OSS.class);
            ValidationUtils.throwIf(StrUtil.isNotBlank(req.getDomain()) && !ReUtil
                .isMatch(RegexConstants.HTTP_DOMAIN_URL, req.getDomain()), "域名格式不正确");
        }
    };

    private final Integer value;
    private final String description;

    /**
     * 校验
     *
     * @param req 请求参数
     */
    public abstract void validate(StorageReq req);

    /**
     * 处理参数
     *
     * @param req 请求参数
     */
    public void pretreatment(StorageReq req) {
        // 域名需要以 “/” 结尾（x-file-storage 在拼接路径时都是直接 + 拼接，所以规范要求每一级都要以 “/” 结尾，且后面路径不能以 “/” 开头）
        if (StrUtil.isNotBlank(req.getDomain())) {
            req.setDomain(StrUtil.appendIfMissing(req.getDomain(), StringConstants.SLASH));
        }
    }
}
