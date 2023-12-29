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

package top.charles7c.continew.admin.system.model.entity;

import java.io.Serial;

import lombok.Data;

import org.dromara.x.file.storage.core.FileInfo;

import com.baomidou.mybatisplus.annotation.*;

import cn.hutool.core.util.StrUtil;

import top.charles7c.continew.admin.system.enums.FileTypeEnum;
import top.charles7c.continew.starter.core.constant.StringConstants;
import top.charles7c.continew.starter.core.util.URLUtils;
import top.charles7c.continew.starter.extension.crud.base.BaseDO;

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

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

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
     * 存储库 ID
     */
    private Long storageId;

    /**
     * 转换为 X-File-Storage 文件信息对象
     *
     * @param storageCode
     *            存储库编码
     * @return X-File-Storage 文件信息对象
     */
    public FileInfo toFileInfo(String storageCode) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOriginalFilename(
            StrUtil.isNotBlank(this.extension) ? this.name + StringConstants.DOT + this.extension : this.name);
        fileInfo.setSize(this.size);
        fileInfo.setUrl(this.url);
        fileInfo.setExt(this.extension);
        fileInfo.setBasePath(StringConstants.EMPTY);
        fileInfo.setPath(StringConstants.EMPTY);
        fileInfo.setFilename(
            URLUtils.isHttpUrl(this.url) ? StrUtil.subAfter(this.url, StringConstants.SLASH, true) : this.url);
        fileInfo.setPlatform(storageCode);
        return fileInfo;
    }
}