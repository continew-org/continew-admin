package ${packageName}.${subPackageName};

import java.io.Serial;
<#if hasLocalDateTime>
import java.time.LocalDateTime;
</#if>
<#if hasBigDecimal>
import java.math.BigDecimal;
</#if>

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import top.continew.starter.extension.crud.model.resp.BaseResp;

/**
 * ${businessName}信息
 *
 * @author ${author}
 * @since ${date}
 */
@Data
@Schema(description = "${businessName}信息")
public class ${className} extends BaseResp {

    @Serial
    private static final long serialVersionUID = 1L;
<#if fieldConfigs??>
  <#list fieldConfigs as fieldConfig>
    <#if fieldConfig.showInList>

    /**
     * ${fieldConfig.comment}
     */
    @Schema(description = "${fieldConfig.comment}")
    private ${fieldConfig.fieldType} ${fieldConfig.fieldName};
    </#if>
  </#list>
</#if>
}