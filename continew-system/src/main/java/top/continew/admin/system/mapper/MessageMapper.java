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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import top.continew.admin.system.model.entity.MessageDO;
import top.continew.admin.system.model.query.MessageQuery;
import top.continew.admin.system.model.resp.message.MessageDetailResp;
import top.continew.admin.system.model.resp.message.MessageResp;
import top.continew.starter.data.mp.base.BaseMapper;

import java.util.List;

/**
 * 消息 Mapper
 *
 * @author Bull-BCLS
 * @since 2023/10/15 19:05
 */
public interface MessageMapper extends BaseMapper<MessageDO> {

    /**
     * 分页查询消息列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 消息列表
     */
    IPage<MessageResp> selectMessagePage(@Param("page") Page<MessageDO> page, @Param("query") MessageQuery query);

    /**
     * 查询消息详情
     *
     * @param id ID
     * @return 消息详情
     */
    MessageDetailResp selectMessageById(@Param("id") Long id);

    /**
     * 查询未读消息列表
     *
     * @param userId 用户 ID
     * @return 消息列表
     */
    List<MessageDO> selectUnreadListByUserId(@Param("userId") Long userId);

    /**
     * 查询未读消息数量
     *
     * @param userId 用户 ID
     * @param type   消息类型
     * @return 未读消息数量
     */
    Long selectUnreadCountByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);
}