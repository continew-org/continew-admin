<template>
  <div class="gi_page">
    <a-card title="${businessName}管理" class="general-card">
      <GiTable
        ref="tableRef"
        row-key="id"
        :data="dataList"
        :columns="columns"
        :loading="loading"
        :scroll="{ x: '100%', y: '100%', minWidth: 1000 }"
        :pagination="pagination"
        :disabledColumnKeys="['name']"
        @refresh="search"
      >
        <template #custom-left>
          <#list fieldConfigs as fieldConfig>
          <#if fieldConfig.showInQuery>
          <a-input v-model="queryForm.${fieldConfig.fieldName}" placeholder="请输入${fieldConfig.comment}" allow-clear @change="search">
            <template #prefix><icon-search /></template>
          </a-input>
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
            <a-button v-permission="['${apiModuleName}:${apiName}:export']" @click="onExport">
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
    </a-card>

    <${classNamePrefix}AddModal ref="${classNamePrefix}AddModalRef" @save-success="search" />
    <${classNamePrefix}DetailDrawer ref="${classNamePrefix}DetailDrawerRef" />
  </div>
</template>

<script setup lang="ts">
import { list${classNamePrefix}, delete${classNamePrefix}, export${classNamePrefix}, type ${classNamePrefix}Resp, type ${classNamePrefix}Query } from '@/apis'
import ${classNamePrefix}AddModal from './${classNamePrefix}AddModal.vue'
import ${classNamePrefix}DetailDrawer from './${classNamePrefix}DetailDrawer.vue'
import type { TableInstanceColumns } from '@/components/GiTable/type'
import { useTable, useDownload } from '@/hooks'
import { isMobile } from '@/utils'
import has from '@/utils/has'

defineOptions({ name: '${classNamePrefix}' })

const columns: TableInstanceColumns[] = [
<#if fieldConfigs??>
  <#list fieldConfigs as fieldConfig>
  <#if fieldConfig.showInList>
  { title: '${fieldConfig.comment}', dataIndex: '${fieldConfig.fieldName}' },
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

const queryForm: ${classNamePrefix}Query = reactive({
<#list fieldConfigs as fieldConfig>
<#if fieldConfig.showInQuery>
  ${fieldConfig.fieldName}: undefined,
</#if>
</#list>
  sort: ['createTime,desc']
})

const {
  tableData: dataList,
  loading,
  pagination,
  search,
  handleDelete
} = useTable((p) => list${classNamePrefix}({ ...queryForm, page: p.page, size: p.size }), { immediate: true })

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
