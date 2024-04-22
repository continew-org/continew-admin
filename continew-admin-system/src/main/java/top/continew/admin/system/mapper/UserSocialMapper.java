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

import org.apache.ibatis.annotations.Param;
import top.continew.admin.system.model.entity.UserSocialDO;
import top.continew.starter.data.mybatis.plus.base.BaseMapper;

/**
 * 用户社会化关联 Mapper
 *
 * @author Charles7c
 * @since 2023/10/11 22:10
 */
public interface UserSocialMapper extends BaseMapper<UserSocialDO> {

    /**
     * 根据来源和开放 ID 查询
     *
     * @param source 来源
     * @param openId 开放 ID
     * @return 用户社会化关联信息
     */
    UserSocialDO selectBySourceAndOpenId(@Param("source") String source, @Param("openId") String openId);
}
