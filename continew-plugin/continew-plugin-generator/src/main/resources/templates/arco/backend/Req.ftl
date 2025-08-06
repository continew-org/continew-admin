package ${packageName}.${subPackageName};

<#if hasRequiredField>
import jakarta.validation.constraints.*;
</#if>

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.hibernate.validator.constraints.Length;
<#if imports??>
    <#list imports as className>
import ${className};
    </#list>
</#if>
import java.io.Serial;
import java.io.Serializable;
<#if hasTimeField>
import java.time.*;
</#if>
<#if hasBigDecimalField>
import java.math.BigDecimal;
</#if>

/**
 * ${businessName}创建或修改参数
 *
 * @author ${author}
 * @since ${datetime}
 */
@Data
@Schema(description = "${businessName}创建或修改参数")
public class ${className} implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
<#if fieldConfigs??>
  <#list fieldConfigs as fieldConfig>
    <#if fieldConfig.showInForm>

    /**
     * ${fieldConfig.comment}
     */
    @Schema(description = "${fieldConfig.comment}")
    <#if fieldConfig.isRequired>
    <#if fieldConfig.fieldType = 'String'>
    @NotBlank(message = "${fieldConfig.comment}不能为空")
    <#else>
    @NotNull(message = "${fieldConfig.comment}不能为空")
    </#if>
    </#if>
    <#if fieldConfig.fieldType = 'String' && fieldConfig.columnSize??>
    @Length(max = ${fieldConfig.columnSize?c}, message = "${fieldConfig.comment}长度不能超过 {max} 个字符")
    </#if>
    private ${fieldConfig.fieldType} ${fieldConfig.fieldName};
    </#if>
  </#list>
</#if>
}