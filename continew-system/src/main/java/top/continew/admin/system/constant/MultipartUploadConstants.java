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

package top.continew.admin.system.constant;

/**
 * 分片上传常量
 *
 * @author KAI
 * @since 2025/7/30 17:40
 */
public class MultipartUploadConstants {

    //todo 后续改为从配置文件读取
    /**
     * MD5到uploadId的映射前缀
     * <p>
     * 用于存储文件MD5到uploadId的映射关系，实现基于MD5的双列Map结构。
     * 键格式：multipart:md5_to_upload:{md5}
     * 值格式：Hash结构，包含uploadId和fileInfo
     * </p>
     */
    public static final String MD5_TO_UPLOAD_ID_PREFIX = "multipart:md5_to_upload:";

    /**
     * 分片上传信息前缀
     * <p>
     * 用于存储分片上传的初始化信息，包含uploadId、bucket、path等基本信息。
     * 键格式：multipart:upload:{uploadId}
     * 值格式：JSON字符串，包含MultipartInitResp的序列化数据
     * </p>
     */
    public static final String MULTIPART_UPLOAD_PREFIX = "multipart:upload:";

    /**
     * 分片信息前缀
     * <p>
     * 用于存储所有分片的上传信息，使用Hash结构存储。
     * 键格式：multipart:parts:{uploadId}
     * 值格式：Hash结构，field为分片编号，value为FilePartInfo的JSON序列化数据
     * </p>
     */
    public static final String MULTIPART_PARTS_PREFIX = "multipart:parts:";

    /**
     * 元数据前缀
     * <p>
     * 用于存储分片上传的元数据信息，如文件名、大小、类型等。
     * 键格式：multipart:metadata:{uploadId}
     * 值格式：Hash结构，field为元数据键，value为元数据值
     * </p>
     */
    public static final String MULTIPART_METADATA_PREFIX = "multipart:metadata:";

    /**
     * 过期时间前缀
     * <p>
     * 用于存储分片上传的过期时间，用于定期清理过期数据。
     * 键格式：multipart:expire:{uploadId}
     * 值格式：ISO格式的时间字符串
     * </p>
     */
    public static final String MULTIPART_EXPIRE_PREFIX = "multipart:expire:";

    /**
     * 默认过期时间（小时）
     * <p>
     * 分片上传缓存数据的默认过期时间，超过此时间的数据会被自动清理。
     * 设置为24小时，平衡存储空间和用户体验。
     * </p>
     */
    public static final long DEFAULT_EXPIRE_HOURS = 24;
}
