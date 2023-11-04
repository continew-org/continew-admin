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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;

import top.charles7c.cnadmin.common.annotation.TreeField;
import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.query.SortQuery;
import top.charles7c.cnadmin.common.model.resp.PageDataResp;
import top.charles7c.cnadmin.common.service.CommonUserService;
import top.charles7c.cnadmin.common.util.ExcelUtils;
import top.charles7c.cnadmin.common.util.ExceptionUtils;
import top.charles7c.cnadmin.common.util.ReflectUtils;
import top.charles7c.cnadmin.common.util.TreeUtils;
import top.charles7c.cnadmin.common.util.helper.QueryHelper;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;

/**
 * 业务实现基类
 *
 * @param <M>
 *            Mapper 接口
 * @param <T>
 *            实体类
 * @param <L>
 *            列表信息
 * @param <D>
 *            详情信息
 * @param <Q>
 *            查询条件
 * @param <C>
 *            创建或修改信息
 * @author Charles7c
 * @since 2023/1/26 21:52
 */
public abstract class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseDO, L, D, Q, C extends BaseReq>
    implements BaseService<L, D, Q, C> {

    @Autowired
    protected M baseMapper;

    private final Class<T> entityClass;
    private final Class<L> listClass;
    private final Class<D> detailClass;

    protected BaseServiceImpl() {
        this.entityClass = (Class<T>)ClassUtil.getTypeArgument(this.getClass(), 1);
        this.listClass = (Class<L>)ClassUtil.getTypeArgument(this.getClass(), 2);
        this.detailClass = (Class<D>)ClassUtil.getTypeArgument(this.getClass(), 3);
    }

    @Override
    public PageDataResp<L> page(Q query, PageQuery pageQuery) {
        QueryWrapper<T> queryWrapper = QueryHelper.build(query);
        IPage<T> page = baseMapper.selectPage(pageQuery.toPage(), queryWrapper);
        PageDataResp<L> pageDataResp = PageDataResp.build(page, listClass);
        pageDataResp.getList().forEach(this::fill);
        return pageDataResp;
    }

    @Override
    public List<Tree<Long>> tree(Q query, SortQuery sortQuery, boolean isSimple) {
        List<L> list = this.list(query, sortQuery);
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>(0);
        }

        // 如果构建简单树结构，则不包含基本树结构之外的扩展字段
        TreeNodeConfig treeNodeConfig = TreeUtils.DEFAULT_CONFIG;
        TreeField treeField = listClass.getDeclaredAnnotation(TreeField.class);
        if (!isSimple) {
            // 根据 @TreeField 配置生成树结构配置
            treeNodeConfig = TreeUtils.genTreeNodeConfig(treeField);
        }

        // 构建树
        return TreeUtils.build(list, treeNodeConfig, (node, tree) -> {
            // 转换器
            tree.setId(ReflectUtil.invoke(node, StrUtil.genGetter(treeField.value())));
            tree.setParentId(ReflectUtil.invoke(node, StrUtil.genGetter(treeField.parentIdKey())));
            tree.setName(ReflectUtil.invoke(node, StrUtil.genGetter(treeField.nameKey())));
            tree.setWeight(ReflectUtil.invoke(node, StrUtil.genGetter(treeField.weightKey())));
            if (!isSimple) {
                List<Field> fieldList = ReflectUtils.getNonStaticFields(listClass);
                fieldList.removeIf(f -> StrUtil.containsAnyIgnoreCase(f.getName(), treeField.value(),
                    treeField.parentIdKey(), treeField.nameKey(), treeField.weightKey(), treeField.childrenKey()));
                fieldList
                    .forEach(f -> tree.putExtra(f.getName(), ReflectUtil.invoke(node, StrUtil.genGetter(f.getName()))));
            }
        });
    }

    @Override
    public List<L> list(Q query, SortQuery sortQuery) {
        List<L> list = this.list(query, sortQuery, listClass);
        list.forEach(this::fill);
        return list;
    }

    /**
     * 查询列表
     *
     * @param query
     *            查询条件
     * @param sortQuery
     *            排序查询条件
     * @param targetClass
     *            指定类型
     * @return 列表信息
     */
    protected <E> List<E> list(Q query, SortQuery sortQuery, Class<E> targetClass) {
        QueryWrapper<T> queryWrapper = QueryHelper.build(query);
        // 设置排序
        this.sort(queryWrapper, sortQuery);
        List<T> entityList = baseMapper.selectList(queryWrapper);
        return BeanUtil.copyToList(entityList, targetClass);
    }

    /**
     * 设置排序
     *
     * @param queryWrapper
     *            查询 Wrapper
     * @param sortQuery
     *            排序查询条件
     */
    protected void sort(QueryWrapper<T> queryWrapper, SortQuery sortQuery) {
        Sort sort = Opt.ofNullable(sortQuery).orElseGet(SortQuery::new).getSort();
        for (Sort.Order order : sort) {
            if (null != order) {
                queryWrapper.orderBy(true, order.isAscending(), StrUtil.toUnderlineCase(order.getProperty()));
            }
        }
    }

    @Override
    public D get(Long id) {
        T entity = this.getById(id);
        D detail = BeanUtil.copyProperties(entity, detailClass);
        this.fillDetail(detail);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(C req) {
        if (null == req) {
            return 0L;
        }
        T entity = BeanUtil.copyProperties(req, entityClass);
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(C req, Long id) {
        T entity = this.getById(id);
        BeanUtil.copyProperties(req, entity, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.deleteBatchIds(ids);
    }

    @Override
    public void export(Q query, SortQuery sortQuery, HttpServletResponse response) {
        List<D> list = this.list(query, sortQuery, detailClass);
        list.forEach(this::fillDetail);
        ExcelUtils.export(list, "导出数据", detailClass, response);
    }

    /**
     * 根据 ID 查询
     *
     * @param id
     *            ID
     * @return 实体信息
     */
    protected T getById(Object id) {
        T entity = baseMapper.selectById(Convert.toStr(id));
        CheckUtils.throwIfNotExists(entity, ClassUtil.getClassName(entityClass, true), "ID", id);
        return entity;
    }

    /**
     * 填充数据
     *
     * @param baseObj
     *            待填充列表信息
     */
    protected void fill(Object baseObj) {
        if (baseObj instanceof BaseResp baseResp) {
            Long createUser = baseResp.getCreateUser();
            if (null == createUser) {
                return;
            }
            CommonUserService userService = SpringUtil.getBean(CommonUserService.class);
            baseResp.setCreateUserString(ExceptionUtils.exToNull(() -> userService.getNicknameById(createUser)));
        }
    }

    /**
     * 填充详情数据
     *
     * @param detailObj
     *            待填充详情信息
     */
    public void fillDetail(Object detailObj) {
        if (detailObj instanceof BaseDetailResp detail) {
            this.fill(detail);

            Long updateUser = detail.getUpdateUser();
            if (null == updateUser) {
                return;
            }
            CommonUserService userService = SpringUtil.getBean(CommonUserService.class);
            detail.setUpdateUserString(ExceptionUtils.exToNull(() -> userService.getNicknameById(updateUser)));
        }
    }
}
