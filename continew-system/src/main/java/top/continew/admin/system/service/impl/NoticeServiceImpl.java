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

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.system.enums.*;
import top.continew.admin.system.mapper.NoticeMapper;
import top.continew.admin.system.model.entity.NoticeDO;
import top.continew.admin.system.model.query.NoticeQuery;
import top.continew.admin.system.model.req.MessageReq;
import top.continew.admin.system.model.req.NoticeReq;
import top.continew.admin.system.model.resp.dashboard.DashboardNoticeResp;
import top.continew.admin.system.model.resp.notice.NoticeDetailResp;
import top.continew.admin.system.model.resp.notice.NoticeResp;
import top.continew.admin.system.service.MessageService;
import top.continew.admin.system.service.NoticeLogService;
import top.continew.admin.system.service.NoticeService;
import top.continew.starter.core.validation.CheckUtils;
import top.continew.starter.core.validation.ValidationUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.extension.crud.service.BaseServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告业务实现
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends BaseServiceImpl<NoticeMapper, NoticeDO, NoticeResp, NoticeDetailResp, NoticeQuery, NoticeReq> implements NoticeService {

    private final NoticeLogService noticeLogService;
    private final MessageService messageService;

    @Override
    public PageResp<NoticeResp> page(NoticeQuery query, PageQuery pageQuery) {
        IPage<NoticeResp> page = baseMapper.selectNoticePage(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), query);
        PageResp<NoticeResp> pageResp = PageResp.build(page);
        pageResp.getList().forEach(this::fill);
        return pageResp;
    }

    @Override
    public void beforeCreate(NoticeReq req) {
        // 校验定时发布
        if (Boolean.TRUE.equals(req.getIsTiming())) {
            ValidationUtils.throwIf(req.getPublishTime() == null, "定时发布时间不能为空");
            ValidationUtils.throwIf(req.getPublishTime().isBefore(LocalDateTime.now()), "定时发布时间不能早于当前时间");
        }
        if (!NoticeStatusEnum.DRAFT.equals(req.getStatus())) {
            if (Boolean.TRUE.equals(req.getIsTiming())) {
                // 待发布
                req.setStatus(NoticeStatusEnum.PENDING);
            } else {
                // 已发布
                req.setStatus(NoticeStatusEnum.PUBLISHED);
                req.setPublishTime(LocalDateTime.now());
            }
        }
    }

    @Override
    public void afterCreate(NoticeReq req, NoticeDO entity) {
        // 发送消息
        if (NoticeStatusEnum.PUBLISHED.equals(entity.getStatus())) {
            this.publish(entity);
        }
    }

    @Override
    public void beforeUpdate(NoticeReq req, Long id) {
        NoticeDO oldNotice = super.getById(id);
        switch (oldNotice.getStatus()) {
            case PUBLISHED -> {
                CheckUtils.throwIfNotEqual(req.getStatus(), oldNotice.getStatus(), "公告已发布，不允许修改状态");
                CheckUtils.throwIfNotEqual(req.getIsTiming(), oldNotice.getIsTiming(), "公告已发布，不允许修改定时发布信息");
                CheckUtils.throwIfNotEqual(req.getNoticeScope(), oldNotice.getNoticeScope(), "公告已发布，不允许修改通知范围");
                if (NoticeScopeEnum.USER.equals(oldNotice.getNoticeScope())) {
                    CheckUtils.throwIfNotEmpty(CollUtil.disjunction(req.getNoticeUsers(), oldNotice
                        .getNoticeUsers()), "公告已发布，不允许修改通知用户");
                }
                CheckUtils.throwIf(!CollUtil.isEqualList(req.getNoticeMethods(), oldNotice
                    .getNoticeMethods()), "公告已发布，不允许修改通知方式");
                // 修正定时发布信息
                if (Boolean.TRUE.equals(oldNotice.getIsTiming())) {
                    CheckUtils.throwIfNotEqual(req.getPublishTime(), oldNotice.getPublishTime(), "公告已发布，不允许修改定时发布信息");
                }
                req.setPublishTime(oldNotice.getPublishTime());
            }
            case DRAFT, PENDING -> {
                // 校验定时发布
                if (Boolean.TRUE.equals(req.getIsTiming())) {
                    ValidationUtils.throwIf(req.getPublishTime() == null, "定时发布时间不能为空");
                    ValidationUtils.throwIf(req.getPublishTime().isBefore(LocalDateTime.now()), "定时发布时间不能早于当前时间");
                }
                // 已发布
                if (NoticeStatusEnum.PUBLISHED.equals(req.getStatus())) {
                    if (Boolean.TRUE.equals(req.getIsTiming())) {
                        // 待发布
                        req.setStatus(NoticeStatusEnum.PENDING);
                    } else {
                        // 已发布
                        req.setStatus(NoticeStatusEnum.PUBLISHED);
                        req.setPublishTime(LocalDateTime.now());
                    }
                }
            }
            default -> throw new IllegalArgumentException("状态无效");
        }
    }

    @Override
    public void afterUpdate(NoticeReq req, NoticeDO entity) {
        // 重置定时发布时间
        if (!NoticeStatusEnum.PUBLISHED.equals(entity.getStatus()) && Boolean.FALSE.equals(entity
            .getIsTiming()) && entity.getPublishTime() != null) {
            baseMapper.lambdaUpdate().set(NoticeDO::getPublishTime, null).eq(NoticeDO::getId, entity.getId()).update();
        }
        // 发送消息
        if (Boolean.FALSE.equals(entity.getIsTiming()) && NoticeStatusEnum.PUBLISHED.equals(entity.getStatus())) {
            this.publish(entity);
        }
    }

    @Override
    public void afterDelete(List<Long> ids) {
        // 删除公告日志
        noticeLogService.deleteByNoticeIds(ids);
    }

    @Override
    public void publish(NoticeDO notice) {
        List<Integer> noticeMethods = notice.getNoticeMethods();
        if (CollUtil.isNotEmpty(noticeMethods) && noticeMethods.contains(NoticeMethodEnum.SYSTEM_MESSAGE.getValue())) {
            MessageTemplateEnum template = MessageTemplateEnum.NOTICE_PUBLISH;
            MessageReq req = new MessageReq(MessageTypeEnum.SYSTEM);
            req.setTitle(template.getTitle());
            req.setContent(template.getContent().formatted(notice.getTitle()));
            req.setPath(template.getPath().formatted(notice.getId()));
            // 新增消息
            messageService.add(req, notice.getNoticeUsers());
        }
    }

    @Override
    public List<Long> listUnreadIdsByUserId(NoticeMethodEnum method, Long userId) {
        return baseMapper.selectUnreadIdsByUserId(method != null ? method.getValue() : null, userId);
    }

    @Override
    public void readNotice(Long id, Long userId) {
        noticeLogService.add(List.of(userId), id);
    }

    @Override
    public List<DashboardNoticeResp> listDashboard() {
        Long userId = UserContextHolder.getUserId();
        return baseMapper.selectDashboardList(userId);
    }
}