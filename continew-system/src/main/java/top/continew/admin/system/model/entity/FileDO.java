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

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.x.file.storage.core.FileInfo;
import top.continew.admin.common.model.entity.BaseDO;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.starter.core.constant.StringConstants;

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
     * 基于 {@link FileInfo} 构建文件信息对象
     *
     * @param fileInfo {@link FileInfo} 文件信息
     */
    public FileDO(FileInfo fileInfo) {
        this.name = fileInfo.getFilename();
        this.originalName = fileInfo.getOriginalFilename();
        this.size = fileInfo.getSize();
        // 如果为空，则为 /；如果不为空，则调整格式为：/xxx
        this.parentPath = StrUtil.isEmpty(fileInfo.getPath())
            ? StringConstants.SLASH
            : StrUtil.removeSuffix(StrUtil.prependIfMissing(fileInfo
                .getPath(), StringConstants.SLASH), StringConstants.SLASH);
        this.path = StrUtil.prependIfMissing(fileInfo.getUrl(), StringConstants.SLASH);
        this.extension = fileInfo.getExt();
        this.contentType = fileInfo.getContentType();
        this.type = FileTypeEnum.getByExtension(this.extension);
        this.sha256 = fileInfo.getHashInfo().getSha256();
        this.metadata = JSONUtil.toJsonStr(fileInfo.getMetadata());
        this.thumbnailName = fileInfo.getThFilename();
        this.thumbnailSize = fileInfo.getThSize();
        this.thumbnailMetadata = JSONUtil.toJsonStr(fileInfo.getThMetadata());
        this.setCreateTime(DateUtil.toLocalDateTime(fileInfo.getCreateTime()));
    }

    /**
     * 转换为 {@link FileInfo} 文件信息对象
     *
     * @param storage 存储配置信息
     * @return {@link FileInfo} 文件信息对象
     */
    public FileInfo toFileInfo(StorageDO storage) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPlatform(storage.getCode());
        fileInfo.setFilename(this.name);
        fileInfo.setOriginalFilename(this.originalName);
        // 暂不使用，所以保持空
        fileInfo.setBasePath(StringConstants.EMPTY);
        fileInfo.setSize(this.size);
        fileInfo.setPath(StringConstants.SLASH.equals(this.parentPath)
            ? StringConstants.EMPTY
            : StrUtil.appendIfMissing(StrUtil
                .removePrefix(this.parentPath, StringConstants.SLASH), StringConstants.SLASH));
        fileInfo.setExt(this.extension);
        fileInfo.setContentType(this.contentType);
        if (StrUtil.isNotBlank(this.metadata)) {
            fileInfo.setMetadata(JSONUtil.toBean(this.metadata, Map.class));
        }
        fileInfo.setUrl(StrUtil.removePrefix(this.path, StringConstants.SLASH));
        // 缩略图信息
        fileInfo.setThFilename(this.thumbnailName);
        fileInfo.setThSize(this.thumbnailSize);
        fileInfo.setThUrl(fileInfo.getPath() + fileInfo.getThFilename());
        if (StrUtil.isNotBlank(this.thumbnailMetadata)) {
            fileInfo.setThMetadata(JSONUtil.toBean(this.thumbnailMetadata, Map.class));
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
