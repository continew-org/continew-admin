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

package top.charles7c.cnadmin.common.base;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;

import cn.hutool.core.bean.BeanUtil;

import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageDataVO;
import top.charles7c.cnadmin.common.util.helper.QueryHelper;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;

/**
 * 业务实现基类
 *
 * @param <M>
 *            Mapper 接口
 * @param <T>
 *            实体类
 * @author Charles7c
 * @since 2023/1/26 21:52
 */
public abstract class BaseServiceImpl<M extends BaseMapper<T>, T, V, D, Q, C, U> implements BaseService<V, D, Q, C, U> {

    @Autowired
    protected M baseMapper;

    protected Class<T> entityClass = currentEntityClass();
    protected Class<V> voClass = currentVoClass();
    protected Class<D> detailVoClass = currentDetailVoClass();

    @Override
    public PageDataVO<V> page(Q query, PageQuery pageQuery) {
        QueryWrapper<T> queryWrapper = QueryHelper.build(query);
        IPage<T> page = baseMapper.selectPage(pageQuery.toPage(), queryWrapper);
        return PageDataVO.build(page, voClass);
    }

    @Override
    public List<V> list(Q query) {
        QueryWrapper<T> queryWrapper = QueryHelper.build(query);
        List<T> entityList = baseMapper.selectList(queryWrapper);
        return BeanUtil.copyToList(entityList, voClass);
    }

    @Override
    public D detail(Long id) {
        T entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, String.format("ID为 [%s] 的记录已不存在", id));
        return BeanUtil.copyProperties(entity, detailVoClass);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public abstract Long create(C request);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, U request) {
        T entity = BeanUtil.copyProperties(request, entityClass);
        baseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.deleteBatchIds(ids);
    }

    /**
     * 获取实体类 Class 对象
     *
     * @return 实体类 Class 对象
     */
    protected Class<T> currentEntityClass() {
        return (Class<T>)ReflectionKit.getSuperClassGenericType(this.getClass(), BaseServiceImpl.class, 1);
    }

    /**
     * 获取列表信息类 Class 对象
     *
     * @return 列表信息类 Class 对象
     */
    protected Class<V> currentVoClass() {
        return (Class<V>)ReflectionKit.getSuperClassGenericType(this.getClass(), BaseServiceImpl.class, 2);
    }

    /**
     * 获取详情信息类 Class 对象
     *
     * @return 详情信息类 Class 对象
     */
    protected Class<D> currentDetailVoClass() {
        return (Class<D>)ReflectionKit.getSuperClassGenericType(this.getClass(), BaseServiceImpl.class, 3);
    }
}
