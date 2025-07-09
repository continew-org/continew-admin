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

import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.model.entity.BaseDO;
import top.continew.admin.system.enums.StorageTypeEnum;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.security.crypto.annotation.FieldEncrypt;

import java.io.Serial;
import java.net.URL;

/**
 * 存储实体
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
@Data
@TableName("sys_storage")
public class StorageDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 类型
     */
    private StorageTypeEnum type;

    /**
     * Access Key
     */
    @FieldEncrypt
    private String accessKey;

    /**
     * Secret Key
     */
    @FieldEncrypt
    private String secretKey;

    /**
     * Endpoint
     */
    private String endpoint;

    /**
     * Bucket
     */
    private String bucketName;

    /**
     * 域名
     */
    private String domain;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否为默认存储
     */
    private Boolean isDefault;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    private DisEnableStatusEnum status;

    /**
     * 获取 URL 前缀
     * <p>
     * LOCAL：{@link #domain}/ <br />
     * OSS：域名不为空：{@link #domain}/；Endpoint 不是
     * IP：http(s)://{@link #bucketName}.{@link #endpoint}/；否则：{@link #endpoint}/{@link #bucketName}/
     * </p>
     *
     * @return URL 前缀
     */
    public String getUrlPrefix() {
        if (StrUtil.isNotBlank(this.domain) || StorageTypeEnum.LOCAL.equals(this.type)) {
            return StrUtil.appendIfMissing(this.domain, StringConstants.SLASH);
        }
        URL url = URLUtil.url(this.endpoint);
        String host = url.getHost();
        // IP（MinIO） 则拼接 BucketName
        if (ReUtil.isMatch(RegexPool.IPV4, host) || ReUtil.isMatch(RegexPool.IPV6, host)) {
            return StrUtil
                .appendIfMissing(this.endpoint, StringConstants.SLASH) + this.bucketName + StringConstants.SLASH;
        }
        return "%s://%s.%s/".formatted(url.getProtocol(), this.bucketName, host);
    }
}