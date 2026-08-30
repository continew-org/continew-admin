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

package top.continew.admin.system.service;

import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.req.MultipartUploadCreateReq;
import top.continew.admin.system.model.resp.file.MultipartUploadCreateResp;
import top.continew.admin.system.model.resp.file.MultipartUploadPartResp;

/**
 * 分片上传业务接口
 *
 * @author KAI
 * @since 2025/7/3 8:42
 */
public interface MultipartUploadService {

    MultipartUploadCreateResp initMultipartUpload(MultipartUploadCreateReq multiPartUploadInitReq);

    MultipartUploadPartResp uploadPart(MultipartFile file, String uploadId, Integer partNumber,
        String path);

    FileDO completeMultipartUpload(String uploadId);

    void cancelMultipartUpload(String uploadId);
}
