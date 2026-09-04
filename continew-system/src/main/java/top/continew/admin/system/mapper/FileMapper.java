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

package top.continew.admin.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.resp.file.FileStatisticsResp;
import top.continew.starter.data.mapper.BaseMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件 Mapper
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
public interface FileMapper extends BaseMapper<FileDO> {

    /**
     * 查询文件资源统计信息
     *
     * @return 文件资源统计信息
     */
    @Select("SELECT type, COUNT(1) number, SUM(size) size FROM sys_file WHERE deleted = 0 AND type != 0 GROUP BY type")
    List<FileStatisticsResp> statistics();

    /**
     * 分页查询回收站列表
     *
     * @param page         分页条件
     * @param queryWrapper 查询条件
     * @return 回收站分页列表信息
     */
    @Select("SELECT * FROM sys_file ${ew.customSqlSegment}")
    Page<FileDO> selectPageInRecycleBin(@Param("page") IPage<FileDO> page,
        @Param(Constants.WRAPPER) LambdaQueryWrapper<FileDO> queryWrapper);

    /**
     * 根据 ID 查询（文件已进入回收站）
     *
     * @param id ID
     * @return 文件信息
     */
    @Select("SELECT * FROM sys_file WHERE id = #{id} AND deleted = 1")
    FileDO selectByIdInRecycleBin(@Param("id") Long id);

    /**
     * 查询回收站文件列表
     *
     * @return 回收站文件列表
     */
    @Select("SELECT * FROM sys_file WHERE deleted = 1")
    List<FileDO> selectListInRecycleBin();

    /**
     * 从回收站恢复文件
     *
     * @param id         ID
     * @param userId     用户 ID
     * @param updateTime 修改时间
     */
    @Update("UPDATE sys_file SET deleted = 0, update_user = #{userId}, update_time = #{updateTime} WHERE id = #{id}")
    void restoreInRecycleBin(@Param("id") Long id, @Param("userId") Long userId,
        @Param("updateTime") LocalDateTime updateTime);

    /**
     * 删除文件（不进入回收站）
     *
     * @param ids        ID 列表
     * @param userId     用户 ID
     * @param updateTime 修改时间
     */
    void deleteWithoutRecycleBin(@Param("ids") List<Long> ids, @Param("userId") Long userId,
        @Param("updateTime") LocalDateTime updateTime);

    /**
     * 清空文件回收站
     *
     * @param userId     用户 ID
     * @param updateTime 修改时间
     */
    @Update("UPDATE sys_file SET deleted = id, update_user = #{userId}, update_time = #{updateTime} WHERE deleted = 1")
    void cleanRecycleBin(@Param("userId") Long userId,
        @Param("updateTime") LocalDateTime updateTime);
}
