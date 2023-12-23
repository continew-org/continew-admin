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

package top.charles7c.continew.admin.system.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import top.charles7c.continew.admin.system.mapper.FileMapper;
import top.charles7c.continew.admin.system.model.entity.FileDO;
import top.charles7c.continew.admin.system.model.query.FileQuery;
import top.charles7c.continew.admin.system.model.req.FileReq;
import top.charles7c.continew.admin.system.model.resp.FileDetailResp;
import top.charles7c.continew.admin.system.model.resp.FileResp;
import top.charles7c.continew.admin.system.service.FileService;
import top.charles7c.continew.starter.extension.crud.base.BaseServiceImpl;

/**
 * 文件业务实现
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends BaseServiceImpl<FileMapper, FileDO, FileResp, FileDetailResp, FileQuery, FileReq>
    implements FileService {}