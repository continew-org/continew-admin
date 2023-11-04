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

package top.charles7c.cnadmin.common.model.resp;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

/**
 * 分页信息
 *
 * @param <L>
 *            列表数据类型
 * @author Charles7c
 * @since 2023/1/14 23:40
 */
@Data
@Schema(description = "分页信息")
public class PageDataResp<L> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 列表数据
     */
    @Schema(description = "列表数据")
    private List<L> list;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "10")
    private long total;

    /**
     * 基于 MyBatis Plus 分页数据构建分页信息，并将源数据转换为指定类型数据
     *
     * @param page
     *            MyBatis Plus 分页数据
     * @param targetClass
     *            目标类型 Class 对象
     * @param <T>
     *            源列表数据类型
     * @param <L>
     *            目标列表数据类型
     * @return 分页信息
     */
    public static <T, L> PageDataResp<L> build(IPage<T> page, Class<L> targetClass) {
        if (null == page) {
            return empty();
        }
        PageDataResp<L> pageDataResp = new PageDataResp<>();
        pageDataResp.setList(BeanUtil.copyToList(page.getRecords(), targetClass));
        pageDataResp.setTotal(page.getTotal());
        return pageDataResp;
    }

    /**
     * 基于 MyBatis Plus 分页数据构建分页信息
     *
     * @param page
     *            MyBatis Plus 分页数据
     * @param <L>
     *            列表数据类型
     * @return 分页信息
     */
    public static <L> PageDataResp<L> build(IPage<L> page) {
        if (null == page) {
            return empty();
        }
        PageDataResp<L> pageDataResp = new PageDataResp<>();
        pageDataResp.setList(page.getRecords());
        pageDataResp.setTotal(page.getTotal());
        return pageDataResp;
    }

    /**
     * 基于列表数据构建分页信息
     *
     * @param page
     *            页码
     * @param size
     *            每页条数
     * @param list
     *            列表数据
     * @param <L>
     *            列表数据类型
     * @return 分页信息
     */
    public static <L> PageDataResp<L> build(int page, int size, List<L> list) {
        if (CollUtil.isEmpty(list)) {
            return empty();
        }
        PageDataResp<L> pageDataResp = new PageDataResp<>();
        pageDataResp.setTotal(list.size());
        // 对列表数据进行分页
        int fromIndex = (page - 1) * size;
        int toIndex = page * size + size;
        if (fromIndex > list.size()) {
            pageDataResp.setList(new ArrayList<>(0));
        } else if (toIndex >= list.size()) {
            pageDataResp.setList(list.subList(fromIndex, list.size()));
        } else {
            pageDataResp.setList(list.subList(fromIndex, toIndex));
        }
        return pageDataResp;
    }

    /**
     * 空分页信息
     *
     * @param <L>
     *            列表数据类型
     * @return 分页信息
     */
    private static <L> PageDataResp<L> empty() {
        PageDataResp<L> pageDataResp = new PageDataResp<>();
        pageDataResp.setList(new ArrayList<>(0));
        return pageDataResp;
    }
}
