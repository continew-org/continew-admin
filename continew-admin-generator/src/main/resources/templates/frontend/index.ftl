<template>
  <div class="table-page">
    <GiTable
      row-key="id"
      title="${businessName}管理"
      :data="dataList"
      :columns="columns"
      :loading="loading"
      :scroll="{ x: '100%', y: '100%', minWidth: 1000 }"
      :pagination="pagination"
      :disabled-tools="['size']"
      :disabled-column-keys="['name']"
      @refresh="search"
    >
      <template #custom-left>
        <#list fieldConfigs as fieldConfig>
        <#if fieldConfig.showInQuery>
	        <#if fieldConfig.formType == "SELECT"><#-- 下拉框 -->
	        <a-select
	          v-model="queryForm.${fieldConfig.fieldName}"
	          :options="${fieldConfig.columnName}_enum"
	          placeholder="请选择${fieldConfig.comment}"
	          allow-clear
	          style="width: 150px"
	          @change="search"
	        />	        
	        <#elseif fieldConfig.formType == "RADIO"><#-- 单选框 -->
			<a-radio-group v-model="queryForm.${fieldConfig.fieldName}" :options="${fieldConfig.columnName}_enum" @change="search"/>	        
	        <#elseif fieldConfig.formType == "DATE"><#-- 日期框 -->
            <a-date-picker
              v-model="queryForm.${fieldConfig.fieldName}"
              placeholder="请选择${fieldConfig.comment}"
              format="YYYY-MM-DD"
              style="width: 100%"
            />
	        <#elseif fieldConfig.formType == "DATE_TIME"><#-- 日期时间框 -->
            <a-date-picker
              v-model="queryForm.${fieldConfig.fieldName}"
              placeholder="请选择${fieldConfig.comment}"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />	        
	        <#else>
	        <a-input v-model="queryForm.${fieldConfig.fieldName}" placeholder="请输入${fieldConfig.comment}" allow-clear @change="search">
	          <template #prefix><icon-search /></template>
	        </a-input>
        	</#if>
        </#if>
        </#list>
        <a-button @click="reset">重置</a-button>
      </template>
      <template #custom-right>
        <a-button v-permission="['${apiModuleName}:${apiName}:add']" type="primary" @click="onAdd">
          <template #icon><icon-plus /></template>
          <span>新增</span>
        </a-button>
        <a-tooltip content="导出">
          <a-button v-permission="['${apiModuleName}:${apiName}:export']" class="gi_hover_btn-border" @click="onExport">
            <template #icon>
              <icon-download />
            </template>
          </a-button>
        </a-tooltip>
      </template>
      <template #name="{ record }">
        <a-link @click="onDetail(record)">{{ record.name }}</a-link>
      </template>
      <template #action="{ record }">
        <a-space>
          <a-link v-permission="['${apiModuleName}:${apiName}:update']" @click="onUpdate(record)">修改</a-link>
          <a-link
            v-permission="['${apiModuleName}:${apiName}:delete']"
            status="danger"
            :disabled="record.disabled"
            @click="onDelete(record)"
          >
            删除
          </a-link>
        </a-space>
      </template>
    </GiTable>

    <${classNamePrefix}AddModal ref="${classNamePrefix}AddModalRef" @save-success="search" />
    <${classNamePrefix}DetailDrawer ref="${classNamePrefix}DetailDrawerRef" />
  </div>
</template>

<script setup lang="ts">
import ${classNamePrefix}AddModal from './${classNamePrefix}AddModal.vue'
import ${classNamePrefix}DetailDrawer from './${classNamePrefix}DetailDrawer.vue'
import { type ${classNamePrefix}Resp, type ${classNamePrefix}Query, delete${classNamePrefix}, export${classNamePrefix}, list${classNamePrefix} } from '@/apis'
import type { TableInstanceColumns } from '@/components/GiTable/type'
import { useDownload, useTable } from '@/hooks'
import { isMobile } from '@/utils'
import has from '@/utils/has'
import { useDict } from '@/hooks/app'

defineOptions({ name: '${classNamePrefix}' })

const queryForm = reactive<${classNamePrefix}Query>({
<#list fieldConfigs as fieldConfig>
<#if fieldConfig.showInQuery>
  ${fieldConfig.fieldName}: undefined,
</#if>
</#list>
  sort: ['createTime,desc']
})

<#list fieldConfigs as fieldConfig>
<#if fieldConfig.showInQuery>
<#if fieldConfig.formType == "SELECT" || fieldConfig.formType == "RADIO">
const { ${fieldConfig.columnName}_enum } = useDict('${fieldConfig.columnName}_enum')
</#if>
</#if>
</#list>

const {
  tableData: dataList,
  loading,
  pagination,
  search,
  handleDelete
} = useTable((page) => list${classNamePrefix}({ ...queryForm, ...page }), { immediate: true })

const columns: TableInstanceColumns[] = [
<#if fieldConfigs??>
  <#list fieldConfigs as fieldConfig>
  <#if fieldConfig.showInList>
  { title: '${fieldConfig.comment}', dataIndex: '${fieldConfig.fieldName}', slotName: '${fieldConfig.fieldName}' },
  </#if>
  </#list>
</#if>
  {
    title: '操作',
    slotName: 'action',
    width: 130,
    align: 'center',
    fixed: !isMobile() ? 'right' : undefined,
    show: has.hasPermOr(['${apiModuleName}:${apiName}:update', '${apiModuleName}:${apiName}:delete'])
  }
]

// 重置
const reset = () => {
<#list fieldConfigs as fieldConfig>
<#if fieldConfig.showInQuery>
  queryForm.${fieldConfig.fieldName} = undefined
</#if>
</#list>
  search()
}

// 删除
const onDelete = (item: ${classNamePrefix}Resp) => {
  return handleDelete(() => delete${classNamePrefix}(item.id), {
    content: `是否确定删除该条数据？`,
    showModal: true
  })
}

// 导出
const onExport = () => {
  useDownload(() => export${classNamePrefix}(queryForm))
}

const ${classNamePrefix}AddModalRef = ref<InstanceType<typeof ${classNamePrefix}AddModal>>()
// 新增
const onAdd = () => {
  ${classNamePrefix}AddModalRef.value?.onAdd()
}

// 修改
const onUpdate = (item: ${classNamePrefix}Resp) => {
  ${classNamePrefix}AddModalRef.value?.onUpdate(item.id)
}

const ${classNamePrefix}DetailDrawerRef = ref<InstanceType<typeof ${classNamePrefix}DetailDrawer>>()
// 详情
const onDetail = (item: ${classNamePrefix}Resp) => {
  ${classNamePrefix}DetailDrawerRef.value?.onDetail(item.id)
}
</script>

<style lang="scss" scoped></style>
