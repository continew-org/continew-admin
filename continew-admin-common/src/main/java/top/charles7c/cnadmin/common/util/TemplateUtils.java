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

package top.charles7c.cnadmin.common.util;

import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;

/**
 * 模板工具类
 *
 * @author Charles7c
 * @since 2023/1/13 20:37
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplateUtils {

    private static final String TEMPLATE_PARENT_PATH = "templates";

    /**
     * 将模板与绑定参数融合后返回为字符串
     *
     * @param bindingMap
     *            绑定的参数，此Map中的参数会替换模板中的变量
     * @return 融合后的内容
     */
    public static String render(String templatePath, Map<?, ?> bindingMap) {
        TemplateEngine engine =
            TemplateUtil.createEngine(new TemplateConfig(TEMPLATE_PARENT_PATH, TemplateConfig.ResourceMode.CLASSPATH));
        Template template = engine.getTemplate(templatePath);
        return template.render(bindingMap);
    }
}
