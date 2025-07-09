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

package top.continew.admin.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.system.mapper.*;
import top.continew.admin.system.mapper.user.UserMapper;
import top.continew.admin.system.mapper.user.UserPasswordHistoryMapper;
import top.continew.admin.system.mapper.user.UserSocialMapper;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.TenantSysDataService;
import top.continew.starter.extension.crud.model.entity.BaseIdDO;

import java.util.List;

/**
 * @description: 多租户系统数据接口
 * @author: 小熊
 * @create: 2024-12-02 20:12
 */
@RequiredArgsConstructor
@Service
public class TenantSysDataServiceImpl implements TenantSysDataService {

    private final DeptMapper deptMapper;
    private final FileService fileService;
    private final LogMapper logMapper;
    private final MenuMapper menuMapper;
    private final MessageMapper messageMapper;
    private final MessageMapper messageUserMapper;
    private final NoticeMapper noticeMapper;
    private final RoleMapper roleMapper;
    private final RoleDeptMapper roleDeptMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserMapper userMapper;
    private final UserPasswordHistoryMapper userPasswordHistoryMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserSocialMapper userSocialMapper;

    @Override
    @Transactional
    public void clear() {
        //所有用户退出
        List<UserDO> userDOS = userMapper.selectList(null);
        for (UserDO userDO : userDOS) {
            StpUtil.logout(userDO.getId());
        }
        Wrapper dw = Wrappers.query().eq("1", 1);
        //部门清除
        deptMapper.delete(dw);
        //文件清除
        List<Long> fileIds = fileService.list().stream().map(BaseIdDO::getId).toList();
        if (!fileIds.isEmpty()) {
            fileService.delete(fileIds);
        }
        //日志清除
        logMapper.delete(dw);
        //菜单清除
        menuMapper.delete(dw);
        //消息清除
        messageMapper.delete(dw);
        messageUserMapper.delete(dw);
        //通知清除
        noticeMapper.delete(dw);
        //角色相关数据清除
        roleMapper.delete(dw);
        roleDeptMapper.delete(dw);
        roleMenuMapper.delete(dw);
        //用户数据清除
        userMapper.delete(dw);
        userPasswordHistoryMapper.delete(dw);
        userRoleMapper.delete(dw);
        userSocialMapper.delete(dw);
    }

}
