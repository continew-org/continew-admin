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

package top.continew.admin.controller.schedule;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.system.mapper.*;
import top.continew.admin.system.model.entity.*;
import top.continew.starter.cache.redisson.util.RedisUtils;

import java.util.function.BooleanSupplier;

/**
 * 演示环境任务（任务示例）
 *
 * @author Charles7c
 * @since 2024/8/4 15:30
 */
@Component
@RequiredArgsConstructor
public class DemoEnvironmentJob {

    private final DictItemMapper dictItemMapper;
    private final DictMapper dictMapper;
    private final StorageMapper storageMapper;
    private final NoticeMapper noticeMapper;
    private final MessageMapper messageMapper;
    private final MessageUserMapper messageUserMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserSocialMapper userSocialMapper;
    private final RoleMapper roleMapper;
    private final RoleDeptMapper roleDeptMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;
    private final DeptMapper deptMapper;

    private static final Long DICT_ITEM_FLAG = 4L;
    private static final Long DICT_FLAG = 2L;
    private static final Long STORAGE_FLAG = 2L;
    private static final Long NOTICE_FLAG = 7L;
    private static final Long MESSAGE_FLAG = 0L;
    private static final Long[] USER_FLAG = {1L, 547889293968801831L};
    private static final Long ROLE_FLAG = 547888897925840928L;
    private static final Long MENU_FLAG = 10003L;
    private static final Long DEPT_FLAG = 547888580614160409L;

    /**
     * 重置演示环境数据
     */
    @JobExecutor(name = "ResetEnvironmentData")
    @Transactional(rollbackFor = Exception.class)
    public void resetEnvironmentData() {
        try {
            SnailJobLog.REMOTE.info("定时任务 [重置演示环境数据] 开始执行。");
            // 检测待清理数据
            SnailJobLog.REMOTE.info("开始检测演示环境待清理数据项，请稍候...");
            Long dictItemCount = dictItemMapper.lambdaQuery().gt(DictItemDO::getId, DICT_ITEM_FLAG).count();
            this.log(dictItemCount, "字典项");
            Long dictCount = dictMapper.lambdaQuery().gt(DictDO::getId, DICT_FLAG).count();
            this.log(dictCount, "字典");
            Long storageCount = storageMapper.lambdaQuery().gt(StorageDO::getId, STORAGE_FLAG).count();
            this.log(storageCount, "存储");
            Long noticeCount = noticeMapper.lambdaQuery().gt(NoticeDO::getId, NOTICE_FLAG).count();
            this.log(noticeCount, "公告");
            Long messageCount = messageMapper.lambdaQuery().count();
            this.log(messageCount, "通知");
            Long userCount = userMapper.lambdaQuery().notIn(UserDO::getId, USER_FLAG).count();
            this.log(userCount, "用户");
            Long roleCount = roleMapper.lambdaQuery().gt(RoleDO::getId, ROLE_FLAG).count();
            this.log(roleCount, "角色");
            Long menuCount = menuMapper.lambdaQuery().gt(MenuDO::getId, MENU_FLAG).count();
            this.log(menuCount, "菜单");
            Long deptCount = deptMapper.lambdaQuery().gt(DeptDO::getId, DEPT_FLAG).count();
            this.log(deptCount, "部门");
            // 清理数据
            InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().blockAttack(true).build());
            SnailJobLog.REMOTE.info("演示环境待清理数据项检测完成，开始执行清理。");
            this.clean(dictItemCount, "字典项", null, () -> dictItemMapper.lambdaUpdate()
                .gt(DictItemDO::getId, DICT_ITEM_FLAG)
                .remove());
            this.clean(dictCount, "字典", null, () -> dictMapper.lambdaUpdate().gt(DictDO::getId, DICT_FLAG).remove());
            this.clean(storageCount, "存储", null, () -> storageMapper.lambdaUpdate()
                .gt(StorageDO::getId, STORAGE_FLAG)
                .remove());
            this.clean(noticeCount, "公告", null, () -> noticeMapper.lambdaUpdate()
                .gt(NoticeDO::getId, NOTICE_FLAG)
                .remove());
            this.clean(messageCount, "通知", null, () -> {
                messageUserMapper.lambdaUpdate().gt(MessageUserDO::getMessageId, MESSAGE_FLAG).remove();
                return messageMapper.lambdaUpdate().gt(MessageDO::getId, MESSAGE_FLAG).remove();
            });
            this.clean(userCount, "用户", null, () -> {
                userRoleMapper.lambdaUpdate().notIn(UserRoleDO::getUserId, USER_FLAG).remove();
                userSocialMapper.lambdaUpdate().notIn(UserSocialDO::getUserId, USER_FLAG).remove();
                return userMapper.lambdaUpdate().notIn(UserDO::getId, USER_FLAG).remove();
            });
            this.clean(roleCount, "角色", null, () -> {
                roleDeptMapper.lambdaUpdate().ne(RoleDeptDO::getRoleId, ROLE_FLAG).remove();
                roleMenuMapper.lambdaUpdate().ne(RoleMenuDO::getRoleId, ROLE_FLAG).remove();
                return roleMapper.lambdaUpdate().gt(RoleDO::getId, ROLE_FLAG).remove();
            });
            this.clean(menuCount, "菜单", CacheConstants.MENU_KEY_PREFIX, () -> menuMapper.lambdaUpdate()
                .gt(MenuDO::getId, MENU_FLAG)
                .remove());
            this.clean(deptCount, "部门", null, () -> deptMapper.lambdaUpdate().gt(DeptDO::getId, DEPT_FLAG).remove());
            SnailJobLog.REMOTE.info("演示环境数据已清理完成。");
            SnailJobLog.REMOTE.info("定时任务 [重置演示环境数据] 执行结束。");
        } finally {
            InterceptorIgnoreHelper.clearIgnoreStrategy();
        }
    }

    /**
     * 输出日志
     *
     * @param count    待清理数据项数量
     * @param resource 资源名称
     */
    private void log(Long count, String resource) {
        if (count > 0) {
            SnailJobLog.REMOTE.info("检测到 [{}] 待清理数据项：{}条", resource, count);
        }
    }

    /**
     * 清理数据
     *
     * @param count    待清理数据项数量
     * @param resource 资源名称
     * @param cacheKey 缓存键
     * @param supplier 清理数据项函数
     */
    private void clean(Long count, String resource, String cacheKey, BooleanSupplier supplier) {
        if (count > 0 && supplier.getAsBoolean()) {
            SnailJobLog.REMOTE.info("[{}] 数据项清理完成。", resource);
            if (StrUtil.isNotBlank(cacheKey)) {
                RedisUtils.deleteByPattern(cacheKey);
                SnailJobLog.REMOTE.info("[{}] 数据项缓存清理完成。", resource);
            }
        }
    }
}
