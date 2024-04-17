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

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;

import top.continew.starter.extension.crud.model.resp.BaseDetailResp;

/**
 * ${businessName}详情信息
 *
 * @author ${author}
 * @since ${date}
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "${businessName}详情信息")
public class ${className} extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;
<#if fieldConfigs??>
  <#list fieldConfigs as fieldConfig>

    /**
     * ${fieldConfig.comment}
     */
    @Schema(description = "${fieldConfig.comment}")
    @ExcelProperty(value = "${fieldConfig.comment}")
    private ${fieldConfig.fieldType} ${fieldConfig.fieldName};
  </#list>
</#if>
}