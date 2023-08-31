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

package top.charles7c.cnadmin.common.model.vo;

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
 * @param <V>
 *            列表数据类型
 * @author Charles7c
 * @since 2023/1/14 23:40
 */
@Data
@Schema(description = "分页信息")
public class PageDataVO<V> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 列表数据
     */
    @Schema(description = "列表数据")
    private List<V> list;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "10")
    private int total;

    /**
     * 基于 MyBatis Plus 分页数据构建分页信息，并将源数据转换为指定类型数据
     *
     * @param page
     *            MyBatis Plus 分页数据
     * @param targetClass
     *            目标类型 Class 对象
     * @param <T>
     *            源列表数据类型
     * @param <V>
     *            目标列表数据类型
     * @return 分页信息
     */
    public static <T, V> PageDataVO<V> build(IPage<T> page, Class<V> targetClass) {
        if (null == page) {
            return empty();
        }
        PageDataVO<V> pageDataVO = new PageDataVO<>();
        pageDataVO.setList(BeanUtil.copyToList(page.getRecords(), targetClass));
        pageDataVO.setTotal((int)page.getTotal());
        return pageDataVO;
    }

    /**
     * 基于 MyBatis Plus 分页数据构建分页信息
     *
     * @param page
     *            MyBatis Plus 分页数据
     * @param <V>
     *            列表数据类型
     * @return 分页信息
     */
    public static <V> PageDataVO<V> build(IPage<V> page) {
        if (null == page) {
            return empty();
        }
        PageDataVO<V> pageDataVO = new PageDataVO<>();
        pageDataVO.setList(page.getRecords());
        pageDataVO.setTotal((int)page.getTotal());
        return pageDataVO;
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
     * @param <V>
     *            列表数据类型
     * @return 分页信息
     */
    public static <V> PageDataVO<V> build(int page, int size, List<V> list) {
        if (CollUtil.isEmpty(list)) {
            return empty();
        }
        PageDataVO<V> pageDataVO = new PageDataVO<>();
        pageDataVO.setTotal(list.size());
        // 对列表数据进行分页
        int fromIndex = (page - 1) * size;
        int toIndex = page * size + size;
        if (fromIndex > list.size()) {
            pageDataVO.setList(new ArrayList<>(0));
        } else if (toIndex >= list.size()) {
            pageDataVO.setList(list.subList(fromIndex, list.size()));
        } else {
            pageDataVO.setList(list.subList(fromIndex, toIndex));
        }
        return pageDataVO;
    }

    /**
     * 空分页信息
     *
     * @param <V>
     *            列表数据类型
     * @return 分页信息
     */
    private static <V> PageDataVO<V> empty() {
        PageDataVO<V> pageDataVO = new PageDataVO<>();
        pageDataVO.setList(new ArrayList<>(0));
        return pageDataVO;
    }
}
