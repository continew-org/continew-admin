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

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.continew.admin.common.base.model.entity.BaseDO;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.storage.domain.model.resp.FileInfo;

import java.io.Serial;
import java.util.Map;

/**
 * 文件实体
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
@Data
@NoArgsConstructor
@TableName("sys_file")
public class FileDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 原始名称
     */
    private String originalName;

    /**
     * 大小（字节）
     */
    private Long size;

    /**
     * 上级目录
     */
    private String parentPath;

    /**
     * 路径
     */
    private String path;

    /**
     * 扩展名
     */
    private String extension;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 类型
     */
    private FileTypeEnum type;

    /**
     * SHA256 值
     */
    private String sha256;

    /**
     * 元数据
     */
    private String metadata;

    /**
     * 缩略图名称
     */
    private String thumbnailName;

    /**
     * 缩略图大小（字节)
     */
    private Long thumbnailSize;

    /**
     * 缩略图元数据
     */
    private String thumbnailMetadata;

    /**
     * 存储 ID
     */
    private Long storageId;

    /**
     * 是否已删除（0：否；1：回收站）
     */
    @TableLogic(value = "0", delval = "1")
    private Long deleted;

    /**
     * 基于 {@link FileInfo} 构建文件信息对象
     *
     * @param fileInfo {@link FileInfo} 文件信息
     */
    public FileDO(FileInfo fileInfo) {
        String normalizedPath = StrUtil.blankToDefault(fileInfo.getPath(), fileInfo.getFullPath());
        normalizedPath = normalizedPath.replace("\\", StringConstants.SLASH).replaceAll("/+", StringConstants.SLASH);
        normalizedPath = StrUtil.removePrefix(normalizedPath, StringConstants.SLASH);
        normalizedPath = StrUtil.removeSuffix(normalizedPath, StringConstants.SLASH);
        String fileName = StrUtil.blankToDefault(fileInfo.getName(), FileNameUtil.getName(normalizedPath));
        if (StrUtil.isBlank(normalizedPath)) {
            normalizedPath = fileName;
        }
        this.name = fileName;
        this.originalName = StrUtil.blankToDefault(fileInfo.getOriginalFileName(), fileName);
        this.size = fileInfo.getSize();
        int lastSlash = normalizedPath.lastIndexOf(StringConstants.SLASH);
        String parent = lastSlash < 0 ? StringConstants.EMPTY : normalizedPath.substring(0, lastSlash);
        this.parentPath = StrUtil.isBlank(parent) ? StringConstants.SLASH : StringConstants.SLASH + parent;
        this.path = StringConstants.SLASH + normalizedPath;
        this.extension = FileNameUtil.extName(fileName);
        this.contentType = fileInfo.getContentType();
        this.type = FileTypeEnum.getByExtension(this.extension);
        Map<String, String> metadataMap = fileInfo.getMetadata();
        this.sha256 = metadataMap != null
            ? StrUtil.blankToDefault(metadataMap.get("sha256"), metadataMap.get("etag"))
            : null;
        this.metadata = JSONUtil.toJsonStr(metadataMap);
        String thumbnailPath = fileInfo.getThumbnailPath();
        if (StrUtil.isNotBlank(thumbnailPath)) {
            String normalizedThumbnailPath = thumbnailPath.replace("\\", StringConstants.SLASH);
            if (!normalizedThumbnailPath.startsWith("http://") && !normalizedThumbnailPath.startsWith("https://")) {
                normalizedThumbnailPath = StrUtil.removePrefix(normalizedThumbnailPath, StringConstants.SLASH);
            }
            this.thumbnailName = FileNameUtil.getName(normalizedThumbnailPath);
        }
        this.thumbnailSize = fileInfo.getThumbnailSize();
        this.thumbnailMetadata = null;
        this.setCreateTime(fileInfo.getUploadTime());
    }

    /**
     * 转换为 {@link FileInfo} 文件信息对象
     *
     * @param storage 存储配置
     * @return {@link FileInfo} 文件信息对象
     */
    public FileInfo toFileInfo(StorageDO storage) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPlatform(storage.getCode());
        fileInfo.setBucket(storage.getBucketName());
        fileInfo.setFileId(this.getId() == null ? null : String.valueOf(this.getId()));
        fileInfo.setName(this.name);
        fileInfo.setOriginalFileName(this.originalName);
        fileInfo.setSize(this.size);
        String normalizedPath = StrUtil.removePrefix(this.path, StringConstants.SLASH);
        fileInfo.setPath(normalizedPath);
        fileInfo.setFullPath(normalizedPath);
        fileInfo.setContentType(this.contentType);
        if (StrUtil.isNotBlank(this.metadata)) {
            fileInfo.setMetadata(JSONUtil.toBean(this.metadata, Map.class));
        }
        fileInfo.setUrl(URLUtil.normalize(storage.getUrlPrefix() + normalizedPath, false, true));
        // 缩略图信息
        if (StrUtil.isNotBlank(this.thumbnailName)) {
            String normalizedParentPath = StringConstants.SLASH.equals(this.parentPath)
                ? StringConstants.EMPTY
                : StrUtil.appendIfMissing(StrUtil
                    .removePrefix(this.parentPath, StringConstants.SLASH), StringConstants.SLASH);
            fileInfo.setThumbnailPath(normalizedParentPath + this.thumbnailName);
            fileInfo.setThumbnailSize(this.thumbnailSize);
        }
        return fileInfo;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
        this.path = StringConstants.SLASH.equals(parentPath)
            ? parentPath + this.name
            : parentPath + StringConstants.SLASH + this.name;
    }
}
