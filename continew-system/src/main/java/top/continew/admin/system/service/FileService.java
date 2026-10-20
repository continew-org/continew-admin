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

package top.continew.admin.system.service;

import cn.hutool.core.util.StrUtil;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.common.base.service.BaseService;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.model.query.FileQuery;
import top.continew.admin.system.model.req.FileReq;
import top.continew.admin.system.model.resp.file.FileResp;
import top.continew.admin.system.model.resp.file.FileStatisticsResp;
import top.continew.admin.system.model.resp.file.FileUploadProgressResp;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.data.service.IService;
import top.continew.starter.storage.domain.model.resp.FileInfo;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 文件业务接口
 *
 * @author Charles7c
 * @since 2.2.0
 */
public interface FileService
    extends BaseService<FileResp, FileResp, FileQuery, FileReq>, IService<FileDO> {

    /**
     * 上传到默认存储
     *
     * @param file 文件信息
     * @return 文件信息
     * @throws IOException /
     */
    default FileInfo upload(MultipartFile file) throws IOException {
        return upload(file, getDefaultParentPath(), null);
    }

    /**
     * 上传到默认存储
     *
     * @param file       文件信息
     * @param parentPath 上级目录
     * @return 文件信息
     * @throws IOException /
     */
    default FileInfo upload(MultipartFile file, String parentPath) throws IOException {
        return upload(file, StrUtil.blankToDefault(parentPath, getDefaultParentPath()), null);
    }

    /**
     * 上传到指定存储
     *
     * @param file        文件信息
     * @param parentPath  上级目录
     * @param storageCode 存储编码
     * @return 文件信息
     * @throws IOException /
     */
    FileInfo upload(MultipartFile file, String parentPath, String storageCode) throws IOException;

    /**
     * 上传到指定存储（支持上传任务 ID 追踪进度）
     *
     * @param file         文件信息
     * @param parentPath   上级目录
     * @param storageCode  存储编码
     * @param uploadTaskId 上传任务 ID
     * @return 文件信息
     * @throws IOException /
     */
    default FileInfo upload(MultipartFile file,
        String parentPath,
        String storageCode,
        String uploadTaskId) throws IOException {
        return upload(file, parentPath, storageCode);
    }

    /**
     * 上传到默认存储
     *
     * @param file 文件信息
     * @return 文件信息
     * @throws IOException /
     */
    default FileInfo upload(File file) throws IOException {
        return upload(file, getDefaultParentPath(), null);
    }

    /**
     * 上传到默认存储
     *
     * @param file       文件信息
     * @param parentPath 上级目录
     * @return 文件信息
     * @throws IOException /
     */
    default FileInfo upload(File file, String parentPath) throws IOException {
        return upload(file, StrUtil.blankToDefault(parentPath, getDefaultParentPath()), null);
    }

    /**
     * 上传到指定存储
     *
     * @param file        文件信息
     * @param parentPath  上级目录
     * @param storageCode 存储编码
     * @return 文件信息
     * @throws IOException /
     */
    FileInfo upload(File file, String parentPath, String storageCode) throws IOException;

    /**
     * 创建目录
     *
     * @param req 请求参数
     * @return ID
     */
    Long createDir(FileReq req);

    /**
     * 查询文件资源统计信息
     *
     * @return 资源统计信息
     */
    FileStatisticsResp statistics();

    /**
     * 检查文件是否存在
     *
     * @param fileHash 文件 Hash
     * @return 响应参数
     */
    FileResp check(String fileHash);

    /**
     * 计算文件夹大小
     *
     * @param id ID
     * @return 文件夹大小（字节）
     */
    Long calcDirSize(Long id);

    /**
     * 查询单文件上传进度
     *
     * @param uploadTaskId 上传任务 ID
     * @return 上传进度
     */
    FileUploadProgressResp getUploadProgress(String uploadTaskId);

    /**
     * 根据存储 ID 列表查询
     *
     * @param storageIds 存储 ID 列表
     * @return 文件数量
     */
    Long countByStorageIds(List<Long> storageIds);

    /**
     * 解析文件完整访问 URL
     *
     * <p>
     * 头像等业务字段允许两种形态：http(s) 开头的第三方完整链接原样返回；本系统库内路径
     * （形如 {@code /user/avatar/x.png}，前导斜杠可选）统一按默认存储的 urlPrefix 装配。
     * 未配置默认存储或前缀为空时返回原路径。data:、file: 等非 HTTP 外部地址同样原样返回。
     * </p>
     *
     * @param path 库内路径或 http(s) 完整链接
     * @return 完整访问 URL；空值返回 null
     * @since 4.2.0
     */
    String resolveUrl(String path);

    /**
     * 批量解析文件完整访问 URL
     *
     * <p>行为同 {@link #resolveUrl(String)}，供分页等批量出口一次性装配，避免逐行查询。
     * 仅在确实存在库内路径时才加载默认存储配置，全外链场景不产生任何查询。</p>
     *
     * @param paths 路径或完整链接集合
     * @return 原始入参到完整访问 URL 的映射（不含空值）
     * @since 4.2.0
     */
    Map<String, String> resolveUrls(Collection<String> paths);

    /**
     * 按存储路径删除物理文件及记录（含回收站语义）
     *
     * <p>
     * 头像等业务字段允许保存 http(s) 第三方完整链接，此类非本系统存储值不做删除处理。
     * 库内路径按 path 精确匹配记录，记录不存在或同路径存在多条时跳过并告警，避免误删。
     * </p>
     *
     * @param path 库内路径（前导斜杠可选）或 http(s) 完整链接
     * @return 是否执行了删除
     * @since 4.2.0
     */
    boolean deleteByPath(String path);

    /**
     * 创建上级文件夹（支持多级）
     *
     * <p>
     * user/avatar/ => user（path：/user）、avatar（path：/user/avatar）
     * </p>
     *
     * @param parentPath 上级目录
     * @param storage    存储配置
     */
    void createParentDir(String parentPath, StorageDO storage);

    /**
     * 获取默认上级目录
     *
     * <p>
     * 默认上级目录：yyyy/MM/dd/
     * </p>
     *
     * @return 默认上级目录
     */
    default String getDefaultParentPath() {
        LocalDate today = LocalDate.now(GlobalConstants.DEFAULT_ZONE_ID);
        return today.getYear() + StringConstants.SLASH + today.getMonthValue()
            + StringConstants.SLASH + today
                .getDayOfMonth()
            + StringConstants.SLASH;
    }
}
