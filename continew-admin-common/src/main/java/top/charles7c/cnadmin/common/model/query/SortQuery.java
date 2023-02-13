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

package top.charles7c.cnadmin.common.model.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Sort;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.common.consts.CharConstants;

/**
 * 排序查询条件
 *
 * @author Charles7c
 * @since 2023/2/12 21:30
 */
@Data
@ParameterObject
@Schema(description = "排序查询条件")
public class SortQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 排序条件
     */
    @Schema(description = "排序条件", example = "sort=published,desc&sort=title,asc")
    private String[] sort;

    /**
     * 解析排序条件为 Spring 分页排序实体
     *
     * @return Spring 分页排序实体
     */
    public Sort getSort() {
        if (ArrayUtil.isEmpty(sort)) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>(sort.length);
        if (StrUtil.contains(sort[0], CharConstants.COMMA)) {
            // e.g "sort=published,desc&sort=title,asc"
            for (String s : sort) {
                List<String> sortList = StrUtil.split(s, CharConstants.COMMA);
                Sort.Order order =
                    new Sort.Order(Sort.Direction.valueOf(sortList.get(1).toUpperCase()), sortList.get(0));
                orders.add(order);
            }
        } else {
            // e.g "sort=published,desc"
            Sort.Order order = new Sort.Order(Sort.Direction.valueOf(sort[1].toUpperCase()), sort[0]);
            orders.add(order);
        }
        return Sort.by(orders);
    }
}
