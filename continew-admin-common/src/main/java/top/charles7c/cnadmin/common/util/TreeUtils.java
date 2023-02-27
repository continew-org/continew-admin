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

import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.lang.tree.parser.NodeParser;
import cn.hutool.core.util.ReflectUtil;

import top.charles7c.cnadmin.common.annotation.TreeField;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;

/**
 * 树工具类
 *
 * @author Charles7c
 * @since 2023/1/22 22:11
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TreeUtils {

    /** 默认属性配置对象（根据前端树结构灵活调整名称） */
    public static final TreeNodeConfig DEFAULT_CONFIG =
        TreeNodeConfig.DEFAULT_CONFIG.setNameKey("title").setIdKey("key").setWeightKey("sort");

    /**
     * 树构建
     *
     * @param <T>
     *            转换的实体 为数据源里的对象类型
     * @param <E>
     *            ID类型
     * @param list
     *            源数据集合
     * @param nodeParser
     *            转换器
     * @return List 树列表
     */
    public static <T, E> List<Tree<E>> build(List<T> list, NodeParser<T, E> nodeParser) {
        return build(list, DEFAULT_CONFIG, nodeParser);
    }

    /**
     * 树构建
     *
     * @param <T>
     *            转换的实体 为数据源里的对象类型
     * @param <E>
     *            ID类型
     * @param list
     *            源数据集合
     * @param treeNodeConfig
     *            配置
     * @param nodeParser
     *            转换器
     * @return List 树列表
     */
    public static <T, E> List<Tree<E>> build(List<T> list, TreeNodeConfig treeNodeConfig, NodeParser<T, E> nodeParser) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        E parentId = (E)ReflectUtil.getFieldValue(list.get(0), treeNodeConfig.getParentIdKey());
        return TreeUtil.build(list, parentId, treeNodeConfig, nodeParser);
    }

    /**
     * 根据 @TreeField 配置生成树结构配置
     *
     * @param treeField
     *            树结构字段注解
     * @return 树结构配置
     */
    public static TreeNodeConfig genTreeNodeConfig(TreeField treeField) {
        CheckUtils.throwIfNull(treeField, "请添加并配置 @TreeField 树结构信息");
        return new TreeNodeConfig().setIdKey(treeField.value()).setParentIdKey(treeField.parentIdKey())
            .setNameKey(treeField.nameKey()).setWeightKey(treeField.weightKey()).setChildrenKey(treeField.childrenKey())
            .setDeep(treeField.deep() < 0 ? null : treeField.deep());
    }
}
