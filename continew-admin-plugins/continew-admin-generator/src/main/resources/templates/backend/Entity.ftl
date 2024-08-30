package ${packageName}.${subPackageName};

import java.io.Serial;
<#if hasTimeField>
import java.time.*;
</#if>
<#if hasBigDecimalField>
import java.math.BigDecimal;
</#if>

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableName;

import top.continew.starter.extension.crud.model.entity.BaseDO;

/**
 * ${businessName}实体
 *
 * @author ${author}
 * @since ${datetime}
 */
@Data
@TableName("${tableName}")
public class ${className} extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;
<#if fieldConfigs??>
  <#list fieldConfigs as fieldConfig>

    /**
     * ${fieldConfig.comment}
     */
    private ${fieldConfig.fieldType} ${fieldConfig.fieldName};
  </#list>
</#if>
}