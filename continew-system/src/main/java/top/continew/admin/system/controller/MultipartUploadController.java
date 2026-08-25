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

package top.continew.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.req.MultipartUploadInitReq;
import top.continew.admin.system.model.resp.file.MultipartUploadInitResp;
import top.continew.admin.system.model.resp.file.MultipartUploadResp;
import top.continew.admin.system.service.MultipartUploadService;

/**
 * 分片上传控制器
 *
 * @author KAI
 * @since 2025/7/30 16:38
 */
@RestController
@RequestMapping("/system/multipart-upload")
@RequiredArgsConstructor
public class MultipartUploadController {

    private final MultipartUploadService multipartUploadService;

    /**
     * 初始化分片上传
     *
     * @param multiPartUploadInitReq 分片上传信息
     * @return 初始化响应
     */
    @Operation(summary = "初始化分片上传", description = "初始化分片上传，返回uploadId等信息")
    @SaCheckPermission("system:file:upload")
    @PostMapping("/init")
    public MultipartUploadInitResp initMultipartUpload(@RequestBody @Valid MultipartUploadInitReq multiPartUploadInitReq) {
        return multipartUploadService.initMultipartUpload(multiPartUploadInitReq);
    }

    /**
     * 上传分片
     *
     * @param file       分片文件
     * @param uploadId   上传ID
     * @param partNumber 分片编号
     * @param path       文件路径
     * @return 上传结果
     */
    @Operation(summary = "上传分片", description = "上传单个分片")
    @SaCheckPermission("system:file:upload")
    @PostMapping("/part")
    public MultipartUploadResp uploadPart(@RequestPart("file") MultipartFile file,
                                          @RequestParam("uploadId") String uploadId,
                                          @RequestParam("partNumber") Integer partNumber,
                                          @RequestParam("path") String path) {
        return multipartUploadService.uploadPart(file, uploadId, partNumber, path);
    }

    /**
     * 合并分片
     *
     * @param uploadId 上传ID
     */
    @Operation(summary = "完成分片上传", description = "合并所有分片，完成上传")
    @SaCheckPermission("system:file:upload")
    @GetMapping("/complete/{uploadId}")
    public FileDO completeMultipartUpload(@PathVariable String uploadId) {
        return multipartUploadService.completeMultipartUpload(uploadId);
    }

    /**
     * 取消分片上传
     *
     * @param uploadId 上传ID
     */
    @Operation(summary = "取消分片上传", description = "删除缓存信息,分片数据")
    @SaCheckPermission("system:file:upload")
    @GetMapping("/cancel/{uploadId}")
    public void cancelMultipartUpload(@PathVariable String uploadId) {
        multipartUploadService.cancelMultipartUpload(uploadId);
    }

}
