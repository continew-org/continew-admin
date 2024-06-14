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

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.x.file.storage.core.FileInfo;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.StrUtils;
import top.continew.starter.core.util.URLUtils;
import top.continew.starter.extension.crud.model.entity.BaseDO;

import java.io.Serial;

/**
 * 文件实体
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
@Data
@TableName("sys_file")
public class FileDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 大小（字节）
     */
    private Long size;

    /**
     * URL
     */
    private String url;

    /**
     * 扩展名
     */
    private String extension;

    /**
     * 类型
     */
    private FileTypeEnum type;

    /**
     * 缩略图大小（字节)
     */
    private Long thumbnailSize;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 存储 ID
     */
    private Long storageId;

    /**
     * 转换为 X-File-Storage 文件信息对象
     *
     * @param storageCode 存储编码
     * @return X-File-Storage 文件信息对象
     */
    public FileInfo toFileInfo(String storageCode) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOriginalFilename(StrUtils
            .blankToDefault(this.extension, this.name, ex -> this.name + StringConstants.DOT + ex));
        fileInfo.setSize(this.size);
        fileInfo.setUrl(this.url);
        fileInfo.setExt(this.extension);
        fileInfo.setBasePath(StringConstants.EMPTY);
        fileInfo.setPath(StringConstants.EMPTY);
        fileInfo.setFilename(URLUtils.isHttpUrl(this.url)
            ? StrUtil.subAfter(this.url, StringConstants.SLASH, true)
            : this.url);
        fileInfo.setThFilename(this.thumbnailUrl);
        fileInfo.setPlatform(storageCode);
        return fileInfo;
    }
}
