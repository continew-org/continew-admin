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

package top.continew.admin.common.config.doc;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.models.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局描述定制器 - 处理 sa-token 的注解权限码
 *
 * @author echo
 * @since 2025/1/24 14:59
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalDescriptionCustomizer implements GlobalOperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        // 将 sa-token 注解数据添加到 operation 的描述中
        // 权限
        List<String> noteList = new ArrayList<>(new OperationDescriptionCustomizer().getPermission(handlerMethod));

        // 如果注解数据列表为空，直接返回原 operation
        if (noteList.isEmpty()) {
            return operation;
        }
        // 拼接注解数据为字符串
        String noteStr = StrUtil.join("<br/>", noteList);
        // 获取原描述
        String originalDescription = operation.getDescription();
        // 根据原描述是否为空，更新描述
        String newDescription = StrUtil.isNotEmpty(originalDescription)
            ? originalDescription + "<br/>" + noteStr
            : noteStr;

        // 设置新描述
        operation.setDescription(newDescription);
        return operation;
    }
}
