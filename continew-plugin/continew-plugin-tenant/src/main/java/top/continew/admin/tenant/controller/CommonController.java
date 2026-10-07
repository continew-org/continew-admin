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

package top.continew.admin.tenant.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.tenant.service.TenantService;
import top.continew.starter.extension.tenant.annotation.TenantIgnore;
import top.continew.starter.log.annotation.Log;

/**
 * 公共 API
 *
 * @author Charles7c
 * @since 4.0.0
 */
@Tag(name = "公共 API")
@Log(ignore = true)
@Validated
@RequiredArgsConstructor
@RestController("tenantCommonController")
@RequestMapping("/tenant/common")
public class CommonController {

    private final TenantService tenantService;

    @SaIgnore
    @TenantIgnore
    @Operation(summary = "根据域名查询租户 ID", description = "根据域名查询租户 ID")
    @GetMapping("/id")
    public Long getTenantIdByDomain(@RequestParam String domain) {
        return tenantService.getIdByDomain(domain);
    }
}
