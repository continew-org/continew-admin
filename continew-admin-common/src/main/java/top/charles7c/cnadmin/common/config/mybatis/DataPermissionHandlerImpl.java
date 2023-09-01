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

package top.charles7c.cnadmin.common.config.mybatis;

import java.lang.reflect.Method;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;

import top.charles7c.cnadmin.common.annotation.DataPermission;
import top.charles7c.cnadmin.common.constant.StringConsts;
import top.charles7c.cnadmin.common.enums.DataScopeEnum;
import top.charles7c.cnadmin.common.model.dto.LoginUser;
import top.charles7c.cnadmin.common.model.dto.RoleDTO;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * 数据权限处理器实现
 * <p>
 * 来源：<a href="https://gitee.com/baomidou/mybatis-plus/issues/I37I90">DataPermissionInterceptor 如何使用？</a>
 * </p>
 *
 * @author Charles7c
 * @since 2023/3/6 23:19
 */
@Slf4j
public class DataPermissionHandlerImpl implements DataPermissionHandler {

    /** ID */
    private static final String ID = "id";
    /** 部门 ID */
    private static final String DEPT_ID = "dept_id";
    /** 创建人 */
    private static final String CREATE_USER = "create_user";
    /** 部门表 */
    private static final String DEPT_TABLE = "sys_dept";
    /** 角色和部门关联表 */
    private static final String ROLE_DEPT_TABLE = "sys_role_dept";
    /** 角色和部门关联表：角色 ID */
    private static final String ROLE_ID = "role_id";

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        try {
            Class<?> clazz =
                Class.forName(mappedStatementId.substring(0, mappedStatementId.lastIndexOf(StringConsts.DOT)));
            String methodName = mappedStatementId.substring(mappedStatementId.lastIndexOf(StringConsts.DOT) + 1);
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                DataPermission dataPermission = method.getAnnotation(DataPermission.class);
                if (null != dataPermission
                    && (method.getName().equals(methodName) || (method.getName() + "_COUNT").equals(methodName))) {
                    // 获取当前登录用户
                    LoginUser loginUser = LoginHelper.getLoginUser();
                    if (null != loginUser && !loginUser.isAdmin()) {
                        return buildDataScopeFilter(loginUser, dataPermission.value(), where);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("Data permission handler build data scope filter occurred an error: {}.", e.getMessage(), e);
        }
        return where;
    }

    /**
     * 构建数据范围过滤条件
     *
     * @param user
     *            当前登录用户
     * @param tableAlias
     *            表别名
     * @param where
     *            当前查询条件
     * @return 构建后查询条件
     */
    private static Expression buildDataScopeFilter(LoginUser user, String tableAlias, Expression where) {
        Expression expression = null;
        for (RoleDTO role : user.getRoles()) {
            DataScopeEnum dataScope = role.getDataScope();
            if (DataScopeEnum.ALL.equals(dataScope)) {
                return where;
            }
            if (DataScopeEnum.DEPT_AND_CHILD.equals(dataScope)) {
                // select t1.* from table as t1 where t1.`dept_id` in (select `id` from `sys_dept` where `id` = xxx or
                // find_in_set(xxx, `ancestors`));
                // 构建子查询
                SubSelect subSelect = new SubSelect();
                PlainSelect select = new PlainSelect();
                select.setSelectItems(Collections.singletonList(new SelectExpressionItem(new Column(ID))));
                select.setFromItem(new Table(DEPT_TABLE));
                EqualsTo equalsTo = new EqualsTo();
                equalsTo.setLeftExpression(new Column(ID));
                equalsTo.setRightExpression(new LongValue(user.getDeptId()));
                Function function = new Function();
                function.setName("find_in_set");
                function.setParameters(new ExpressionList(new LongValue(user.getDeptId()), new Column("ancestors")));
                select.setWhere(new OrExpression(equalsTo, function));
                subSelect.setSelectBody(select);
                // 构建父查询
                InExpression inExpression = new InExpression();
                inExpression.setLeftExpression(buildColumn(tableAlias, DEPT_ID));
                inExpression.setRightExpression(subSelect);
                expression = null != expression ? new OrExpression(expression, inExpression) : inExpression;
            } else if (DataScopeEnum.DEPT.equals(dataScope)) {
                // select t1.* from table as t1 where t1.`dept_id` = xxx;
                EqualsTo equalsTo = new EqualsTo();
                equalsTo.setLeftExpression(buildColumn(tableAlias, DEPT_ID));
                equalsTo.setRightExpression(new LongValue(user.getDeptId()));
                expression = null != expression ? new OrExpression(expression, equalsTo) : equalsTo;
            } else if (DataScopeEnum.SELF.equals(dataScope)) {
                // select t1.* from table as t1 where t1.`create_user` = xxx;
                EqualsTo equalsTo = new EqualsTo();
                equalsTo.setLeftExpression(buildColumn(tableAlias, CREATE_USER));
                equalsTo.setRightExpression(new LongValue(user.getId()));
                expression = null != expression ? new OrExpression(expression, equalsTo) : equalsTo;
            } else if (DataScopeEnum.CUSTOM.equals(dataScope)) {
                // select t1.* from table as t1 where t1.`dept_id` in (select `dept_id` from `sys_role_dept` where
                // `role_id` = xxx);
                // 构建子查询
                SubSelect subSelect = new SubSelect();
                PlainSelect select = new PlainSelect();
                select.setSelectItems(Collections.singletonList(new SelectExpressionItem(new Column(DEPT_ID))));
                select.setFromItem(new Table(ROLE_DEPT_TABLE));
                EqualsTo equalsTo = new EqualsTo();
                equalsTo.setLeftExpression(new Column(ROLE_ID));
                equalsTo.setRightExpression(new LongValue(role.getId()));
                select.setWhere(equalsTo);
                subSelect.setSelectBody(select);
                // 构建父查询
                InExpression inExpression = new InExpression();
                inExpression.setLeftExpression(buildColumn(tableAlias, DEPT_ID));
                inExpression.setRightExpression(subSelect);
                expression = null != expression ? new OrExpression(expression, inExpression) : inExpression;
            }
        }
        return null != where ? new AndExpression(where, new Parenthesis(expression)) : expression;
    }

    /**
     * 构建 Column
     *
     * @param tableAlias
     *            表别名
     * @param columnName
     *            字段名称
     * @return 带表别名字段
     */
    private static Column buildColumn(String tableAlias, String columnName) {
        if (StringUtils.isNotEmpty(tableAlias)) {
            columnName = String.format("%s.%s", tableAlias, columnName);
        }
        return new Column(columnName);
    }
}
