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

import javax.validation.constraints.Min;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.hibernate.validator.constraints.Range;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Sort;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.collection.CollUtil;
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
public class PageQuery extends SortQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @Schema(description = "页码")
    @Min(value = 1, message = "页码最小值为 1")
    private Integer page;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数")
    @Range(min = 1, max = 1000, message = "每页条数（取值范围 1-1000）")
    private Integer size;

    /** 默认页码：1 */
    private static final int DEFAULT_PAGE = 1;
    /** 默认每页条数：10 */
    private static final int DEFAULT_SIZE = 10;
    /** 默认每页最大条数：1000 */
    private static final int DEFAULT_MAX_SIZE = 1000;

    public PageQuery() {
        this(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public PageQuery(Integer page, Integer size) {
        this.setPage(page);
        this.setSize(size);
    }

    public void setPage(Integer page) {
        this.page = page == null ? DEFAULT_PAGE : page;
    }

    public void setSize(Integer size) {
        if (size == null) {
            this.size = DEFAULT_SIZE;
        } else if (size > DEFAULT_MAX_SIZE) {
            this.size = DEFAULT_MAX_SIZE;
        } else {
            this.size = size;
        }
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
