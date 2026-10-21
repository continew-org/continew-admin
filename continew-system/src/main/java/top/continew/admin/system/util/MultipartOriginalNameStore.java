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

package top.continew.admin.system.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import top.continew.admin.system.constant.MultipartUploadConstants;
import top.continew.starter.cache.redisson.util.RedisUtils;

import java.time.Duration;
import java.util.Map;

/**
 * 分片上传原始文件名暂存工具
 *
 * <p>
 * 文件落盘名统一使用唯一 ID，而分片上传完成时启动器会用会话中的文件名覆盖展示名（见
 * {@code FileStorageService#completeMultipartUpload}），因此需要在初始化阶段把用户上传时的原始文件名单独暂存，待完成上传时回填到文件记录。
 * </p>
 *
 * <p>
 * 采用独立缓存键而非对象元数据：既不污染存储引擎的对象元数据，也不用担心非 ASCII 文件名进入 HTTP 头。缓存不可用时整体降级为「展示名退化为存储名」，不影响上传本身。
 * </p>
 *
 * @author luoqiz
 * @since 5.0.0
 */
@Slf4j
public class MultipartOriginalNameStore {

    // TODO 待 continew-starter 支持分片会话携带原始文件名后，删除本类，改为向 MultipartUploadInitReq 传入 originalFileName

    /**
     * 文件元数据中的分片上传 ID 键名
     *
     * <p>
     * 由 {@code FileStorageService#completeMultipartUpload} 在调用记录器前写入。
     * </p>
     */
    private static final String METADATA_UPLOAD_ID = "uploadId";

    private MultipartOriginalNameStore() {
    }

    /**
     * 暂存原始文件名
     *
     * <p>
     * 缓存键已存在时保留首次写入的值：同一 MD5 的分片上传会话会被复用，此时上传 ID 相同，以首次初始化的文件名为准。
     * </p>
     *
     * <p>
     * 写入失败不向上抛出：调用方此时已创建好存储会话，抛出会让用户看到失败提示，但会话与分片数据已经产生，反而制造孤儿数据。
     * </p>
     *
     * @param uploadId     分片上传 ID
     * @param originalName 原始文件名
     */
    public static void save(String uploadId, String originalName) {
        if (StrUtil.isBlank(uploadId) || StrUtil.isBlank(originalName)) {
            return;
        }
        try {
            // TODO 会话 TTL 目前是固定常量，若后续改为可配置，需与此处保持同步
            RedisUtils.setIfAbsent(key(uploadId), originalName, Duration
                .ofHours(MultipartUploadConstants.DEFAULT_EXPIRE_HOURS));
        } catch (Exception e) {
            log.warn("暂存原始文件名失败，文件展示名将退化为存储名：uploadId={}", uploadId, e);
        }
    }

    /**
     * 查询暂存的原始文件名
     *
     * @param uploadId 分片上传 ID
     * @return 原始文件名（未暂存或查询失败时返回 {@code null}）
     */
    public static String resolve(String uploadId) {
        if (StrUtil.isBlank(uploadId)) {
            return null;
        }
        try {
            String originalName = RedisUtils.get(key(uploadId));
            if (StrUtil.isBlank(originalName)) {
                log.debug("未查询到暂存的原始文件名：uploadId={}", uploadId);
                return null;
            }
            return originalName;
        } catch (Exception e) {
            log.warn("查询原始文件名失败，文件展示名将退化为存储名：uploadId={}", uploadId, e);
            return null;
        }
    }

    /**
     * 基于文件元数据查询暂存的原始文件名
     *
     * @param metadata 文件元数据
     * @return 原始文件名（元数据中无分片上传 ID 或未暂存时返回 {@code null}）
     */
    public static String resolveByMetadata(Map<String, String> metadata) {
        if (metadata == null) {
            return null;
        }
        return resolve(metadata.get(METADATA_UPLOAD_ID));
    }

    /**
     * 删除暂存的原始文件名
     *
     * @param uploadId 分片上传 ID
     */
    public static void delete(String uploadId) {
        if (StrUtil.isBlank(uploadId)) {
            return;
        }
        try {
            RedisUtils.delete(key(uploadId));
        } catch (Exception e) {
            log.warn("删除暂存的原始文件名失败：uploadId={}", uploadId, e);
        }
    }

    private static String key(String uploadId) {
        return MultipartUploadConstants.MULTIPART_ORIGINAL_NAME_PREFIX + uploadId;
    }
}
