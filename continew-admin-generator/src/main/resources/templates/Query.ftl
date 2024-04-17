package ${packageName}.${subPackageName};

import java.io.Serial;
import java.io.Serializable;
<#if hasLocalDateTime>
import java.time.LocalDateTime;
</#if>
<#if hasBigDecimal>
import java.math.BigDecimal;
</#if>
<#if hasListQueryField>
import java.util.List;
</#if>

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import top.continew.starter.data.core.annotation.Query;
import top.continew.starter.data.core.enums.QueryType;

/**
 * ${businessName}查询条件
 *
 * @author ${author}
 * @since ${date}
 */
@Data
@Schema(description = "${businessName}查询条件")
public class ${className} implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
<#if fieldConfigs??>
  <#list fieldConfigs as fieldConfig>
    <#if fieldConfig.showInQuery>

    /**
     * ${fieldConfig.comment}
     */
    @Schema(description = "${fieldConfig.comment}")
    @Query(type = QueryType.${fieldConfig.queryType})
    <#if fieldConfig.queryType = 'IN' || fieldConfig.queryType = 'NOT_IN' || fieldConfig.queryType = 'BETWEEN'>
    private List<${fieldConfig.fieldType}> ${fieldConfig.fieldName};
    <#else>
    private ${fieldConfig.fieldType} ${fieldConfig.fieldName};
    </#if>
    </#if>
  </#list>
</#if>
}