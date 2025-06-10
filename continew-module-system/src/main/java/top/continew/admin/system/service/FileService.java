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

import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.query.FileQuery;
import top.continew.admin.system.model.req.FileReq;
import top.continew.admin.system.model.resp.file.FileResp;
import top.continew.admin.system.model.resp.file.FileStatisticsResp;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.data.mp.service.IService;
import top.continew.starter.extension.crud.service.BaseService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * 文件业务接口
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
public interface FileService extends BaseService<FileResp, FileResp, FileQuery, FileReq>, IService<FileDO> {

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
        return upload(file, parentPath, null);
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
        return upload(file, parentPath, null);
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
     * 根据存储 ID 列表查询
     *
     * @param storageIds 存储 ID 列表
     * @return 文件数量
     */
    Long countByStorageIds(List<Long> storageIds);

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
        LocalDate today = LocalDate.now();
        return today.getYear() + StringConstants.SLASH + today.getMonthValue() + StringConstants.SLASH + today
            .getDayOfMonth() + StringConstants.SLASH;
    }
}