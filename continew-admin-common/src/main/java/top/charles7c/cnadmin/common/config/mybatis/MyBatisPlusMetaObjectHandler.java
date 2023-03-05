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

import java.time.LocalDateTime;

import org.apache.ibatis.reflection.MetaObject;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

import cn.hutool.core.util.ObjectUtil;

import top.charles7c.cnadmin.common.base.BaseDO;
import top.charles7c.cnadmin.common.exception.ServiceException;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;

/**
 * MyBatis Plus 元对象处理器配置（插入或修改时自动填充）
 *
 * @author Charles7c
 * @since 2022/12/22 19:52
 */
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {

    /** 创建人 */
    private static final String CREATE_USER = "createUser";
    /** 创建时间 */
    private static final String CREATE_TIME = "createTime";
    /** 修改人 */
    private static final String UPDATE_USER = "updateUser";
    /** 修改时间 */
    private static final String UPDATE_TIME = "updateTime";

    /**
     * 插入数据时填充
     *
     * @param metaObject
     *            元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNull(metaObject)) {
                return;
            }

            Long createUser = LoginHelper.getUserId();
            LocalDateTime createTime = LocalDateTime.now();
            if (metaObject.getOriginalObject() instanceof BaseDO) {
                // 继承了 BaseDO 的类，填充创建信息
                BaseDO baseDO = (BaseDO)metaObject.getOriginalObject();
                baseDO.setCreateUser(ObjectUtil.defaultIfNull(baseDO.getCreateUser(), createUser));
                baseDO.setCreateTime(ObjectUtil.defaultIfNull(baseDO.getCreateTime(), createTime));
                baseDO.setUpdateUser(ObjectUtil.defaultIfNull(baseDO.getUpdateUser(), createUser));
                baseDO.setUpdateTime(ObjectUtil.defaultIfNull(baseDO.getUpdateTime(), createTime));
            } else {
                // 未继承 BaseDO 的类，根据类中拥有的创建信息进行填充，不存在创建信息不进行填充
                this.fillFieldValue(metaObject, CREATE_USER, createUser, false);
                this.fillFieldValue(metaObject, CREATE_TIME, createTime, false);
                this.fillFieldValue(metaObject, UPDATE_USER, createUser, false);
                this.fillFieldValue(metaObject, UPDATE_TIME, createTime, false);
            }
        } catch (Exception e) {
            throw new ServiceException("插入数据时自动填充异常：" + e.getMessage());
        }
    }

    /**
     * 修改数据时填充
     *
     * @param metaObject
     *            元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNull(metaObject)) {
                return;
            }

            Long updateUser = LoginHelper.getUserId();
            LocalDateTime updateTime = LocalDateTime.now();
            if (metaObject.getOriginalObject() instanceof BaseDO) {
                // 继承了 BaseDO 的类，填充修改信息
                BaseDO baseDO = (BaseDO)metaObject.getOriginalObject();
                baseDO.setUpdateUser(updateUser);
                baseDO.setUpdateTime(updateTime);
            } else {
                // 未继承 BaseDO 的类，根据类中拥有的修改信息进行填充，不存在修改信息不进行填充
                this.fillFieldValue(metaObject, UPDATE_USER, updateUser, true);
                this.fillFieldValue(metaObject, UPDATE_TIME, updateTime, true);
            }
        } catch (Exception e) {
            throw new ServiceException("修改数据时自动填充异常：" + e.getMessage());
        }
    }

    /**
     * 填充字段值
     *
     * @param metaObject
     *            元数据对象
     * @param fieldName
     *            要填充的字段名
     * @param fillFieldValue
     *            要填充的字段值
     * @param isOverride
     *            如果字段值不为空，是否覆盖（true 覆盖、false 不覆盖）
     */
    private void fillFieldValue(MetaObject metaObject, String fieldName, Object fillFieldValue, boolean isOverride) {
        if (metaObject.hasSetter(fieldName)) {
            Object fieldValue = metaObject.getValue(fieldName);
            setFieldValByName(fieldName, fieldValue != null && !isOverride ? fieldValue : fillFieldValue, metaObject);
        }
    }
}
