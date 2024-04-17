package ${packageName}.${subPackageName};

import java.io.Serial;
<#if hasLocalDateTime>
import java.time.LocalDateTime;
</#if>
<#if hasBigDecimal>
import java.math.BigDecimal;
</#if>

<#if hasRequiredField>
import jakarta.validation.constraints.*;
</#if>

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.hibernate.validator.constraints.Length;

import top.continew.starter.extension.crud.model.req.BaseReq;

/**
 * 创建或修改${businessName}信息
 *
 * @author ${author}
 * @since ${date}
 */
@Data
@Schema(description = "创建或修改${businessName}信息")
public class ${className} extends BaseReq {

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
    @Length(max = ${fieldConfig.columnSize}, message = "${fieldConfig.comment}长度不能超过 {max} 个字符")
    </#if>
    private ${fieldConfig.fieldType} ${fieldConfig.fieldName};
    </#if>
  </#list>
</#if>
}