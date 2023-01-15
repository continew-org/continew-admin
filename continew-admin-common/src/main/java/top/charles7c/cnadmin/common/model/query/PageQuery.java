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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 分页查询条件
 *
 * @author Charles7c
 * @since 2023/1/15 10:59
 */
@Data
@ParameterObject
@Schema(description = "分页查询条件")
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @Schema(description = "页码")
    private int page;

    /**
     * 每页记录数
     */
    @Schema(description = "每页记录数")
    private int size;

    /**
     * 排序条件
     */
    @Schema(description = "排序条件", example = "sort=published,desc&sort=title,asc")
    private String[] sort;

    /** 默认页码：1 */
    private static final int DEFAULT_PAGE = 1;

    /** 默认每页记录数：int 最大值 */
    private static final int DEFAULT_SIZE = Integer.MAX_VALUE;
    private static final String DELIMITER = ",";

    public PageQuery() {
        this.page = DEFAULT_PAGE;
        this.size = DEFAULT_SIZE;
    }

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
        if (sort[0].contains(DELIMITER)) {
            // e.g "sort=published,desc&sort=title,asc"
            for (String s : sort) {
                String[] sortArr = s.split(DELIMITER);
                Sort.Order order = new Sort.Order(Sort.Direction.valueOf(sortArr[1].toUpperCase()), sortArr[0]);
                orders.add(order);
            }
        } else {
            // e.g "sort=published,desc"
            Sort.Order order = new Sort.Order(Sort.Direction.valueOf(sort[1].toUpperCase()), sort[0]);
            orders.add(order);
        }
        return Sort.by(orders);
    }

    /**
     * 基于分页查询条件转换为 MyBatis Plus 分页条件
     *
     * @param <T>
     *            列表数据类型
     * @return MyBatis Plus 分页条件
     */
    public <T> IPage<T> toPage() {
        Page<T> mybatisPage = new Page<>(this.getPage(), this.getSize());
        Sort pageSort = this.getSort();
        if (CollUtil.isNotEmpty(pageSort)) {
            for (Sort.Order order : pageSort) {
                OrderItem orderItem = new OrderItem();
                orderItem.setAsc(order.isAscending());
                orderItem.setColumn(StrUtil.toUnderlineCase(order.getProperty()));
                mybatisPage.addOrder(orderItem);
            }
        }
        return mybatisPage;
    }
}
