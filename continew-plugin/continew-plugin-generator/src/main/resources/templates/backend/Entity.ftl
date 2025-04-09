package ${packageName}.${subPackageName};

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableName;

import top.continew.admin.common.model.entity.BaseDO;

<#if imports??>
    <#list imports as className>
import ${className};
    </#list>
</#if>

import java.io.Serial;
<#if hasTimeField>
import java.time.*;
</#if>
<#if hasBigDecimalField>
import java.math.BigDecimal;
</#if>

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
