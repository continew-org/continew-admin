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

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import top.continew.starter.ratelimiter.annotation.RateLimiter;
import top.continew.starter.web.model.R;

/**
 * OCR 管理 API
 *
 * @author Charles7c
 * @since 2025/02/01
 */
@Tag(name = "OCR 管理 API")
@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    @Value("${ocr.service.url:http://localhost:8080}")
    private String ocrServiceUrl;

    /**
     * 调用 OCR 服务进行文本识别
     *
     * @param requestBody OCR 请求参数
     * @return OCR 识别结果
     */
    @Operation(summary = "OCR 识别", description = "调用 OCR 服务进行文本识别")
    @RateLimiter(interval = 5)
    @PostMapping("/infer")
    @SuppressWarnings("unchecked")
    public R<String> infer(@RequestBody JSONObject requestBody) {
        try {
            String url = ocrServiceUrl + "/ocr";
            HttpResponse response = HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(30000)
                    .execute();

            if (response.isOk()) {
                String body = response.body();
                // 返回原始 JSON 字符串，让前端处理
                return (R<String>) R.ok(body);
            } else {
                return (R<String>) R.fail("500", "OCR 服务请求失败: " + response.getStatus());
            }
        } catch (Exception e) {
            return (R<String>) R.fail("500", "OCR 服务连接失败: " + e.getMessage());
        }
    }
}
