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

package top.continew.admin.common.base.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import top.continew.starter.extension.crud.enums.Api;

/**
 * 控制器基类的 API 名称映射测试。
 *
 * <p>覆盖分页/树/列表统一映射为 LIST、删除类接口统一映射为 DELETE、其余接口
 * 保持原名的权限码组接口径。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class BaseControllerTest {

    @Test
    void shouldMapListLikeApisToList() {
        assertEquals("LIST", BaseController.getApiName(Api.PAGE));
        assertEquals("LIST", BaseController.getApiName(Api.TREE));
        assertEquals("LIST", BaseController.getApiName(Api.LIST));
    }

    @Test
    void shouldMapDeleteLikeApisToDelete() {
        assertEquals("DELETE", BaseController.getApiName(Api.DELETE));
        assertEquals("DELETE", BaseController.getApiName(Api.BATCH_DELETE));
    }

    @Test
    void shouldKeepApiNameForOthers() {
        assertEquals("CREATE", BaseController.getApiName(Api.CREATE));
        assertEquals("UPDATE", BaseController.getApiName(Api.UPDATE));
        assertEquals("GET", BaseController.getApiName(Api.GET));
    }
}
