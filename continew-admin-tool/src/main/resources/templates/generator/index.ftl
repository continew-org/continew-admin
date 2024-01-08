<script lang="ts" setup>
  import {
    DataRecord,
    ListParam,
    list,
    get,
    add,
    update,
    del,
  } from '@/api/${apiModuleName}/${apiName}';
  import checkPermission from '@/utils/permission';

  const { proxy } = getCurrentInstance() as any;
  // const { dis_enable_status_enum } = proxy.useDict('dis_enable_status_enum');

  const dataList = ref<DataRecord[]>([]);
  const dataDetail = ref<DataRecord>({
    // TODO 待补充详情字段默认值
  });
  const total = ref(0);
  const ids = ref<Array<string>>([]);
  const title = ref('');
  const single = ref(true);
  const multiple = ref(true);
  const showQuery = ref(true);
  const loading = ref(false);
  const detailLoading = ref(false);
  const exportLoading = ref(false);
  const visible = ref(false);
  const detailVisible = ref(false);

  const data = reactive({
    // 查询参数
    queryParams: {
      <#list fieldConfigs as fieldConfig>
      <#if fieldConfig.showInQuery>
      ${fieldConfig.fieldName}: undefined,
      </#if>
      </#list>
      page: 1,
      size: 10,
      sort: ['createTime,desc'],
    },
    // 表单数据
    form: {} as DataRecord,
    // 表单验证规则
    rules: {
      <#list fieldConfigs as fieldConfig>
      <#if fieldConfig.showInForm && fieldConfig.isRequired>
      ${fieldConfig.fieldName}: [{ required: true, message: '${fieldConfig.comment}不能为空' }],
      </#if>
      </#list>
    },
  });
  const { queryParams, form, rules } = toRefs(data);

  /**
   * 查询列表
   *
   * @param params 查询参数
   */
  const getList = (params: ListParam = { ...queryParams.value }) => {
    loading.value = true;
    list(params)
      .then((res) => {
        dataList.value = res.data.list;
        total.value = res.data.total;
      })
      .finally(() => {
        loading.value = false;
      });
  };
  getList();

  /**
   * 打开新增对话框
   */
  const toAdd = () => {
    reset();
    title.value = '新增${businessName}';
    visible.value = true;
  };

  /**
   * 打开修改对话框
   *
   * @param id ID
   */
  const toUpdate = (id: string) => {
    reset();
    get(id).then((res) => {
      form.value = res.data;
      title.value = '修改${businessName}';
      visible.value = true;
    });
  };

  /**
   * 重置表单
   */
  const reset = () => {
    form.value = {
      // TODO 待补充需要重置的字段默认值，详情请参考其他模块 index.vue
    };
    proxy.$refs.formRef?.resetFields();
  };

  /**
   * 取消
   */
  const handleCancel = () => {
    visible.value = false;
    proxy.$refs.formRef.resetFields();
  };

  /**
   * 确定
   */
  const handleOk = () => {
    proxy.$refs.formRef.validate((valid: any) => {
      if (!valid) {
        if (form.value.id !== undefined) {
          update(form.value, form.value.id).then((res) => {
            handleCancel();
            getList();
            proxy.$message.success(res.msg);
          });
        } else {
          add(form.value).then((res) => {
            handleCancel();
            getList();
            proxy.$message.success(res.msg);
          });
        }
      }
    });
  };

  /**
   * 查看详情
   *
   * @param id ID
   */
  const toDetail = async (id: string) => {
    if (detailLoading.value) return;
    detailLoading.value = true;
    detailVisible.value = true;
    get(id)
      .then((res) => {
        dataDetail.value = res.data;
      })
      .finally(() => {
        detailLoading.value = false;
      });
  };

  /**
   * 关闭详情
   */
  const handleDetailCancel = () => {
    detailVisible.value = false;
  };

  /**
   * 批量删除
   */
  const handleBatchDelete = () => {
    if (ids.value.length === 0) {
      proxy.$message.info('请选择要删除的数据');
    } else {
      proxy.$modal.warning({
        title: '警告',
        titleAlign: 'start',
        content: `是否确定删除所选的${r'${ids.value.length}'}条数据？`,
        hideCancel: false,
        onOk: () => {
          handleDelete(ids.value);
        },
      });
    }
  };

  /**
   * 删除
   *
   * @param ids ID 列表
   */
  const handleDelete = (ids: Array<string>) => {
    del(ids).then((res) => {
      proxy.$message.success(res.msg);
      getList();
    });
  };

  /**
   * 已选择的数据行发生改变时触发
   *
   * @param rowKeys ID 列表
   */
  const handleSelectionChange = (rowKeys: Array<any>) => {
    ids.value = rowKeys;
    single.value = rowKeys.length !== 1;
    multiple.value = !rowKeys.length;
  };

  /**
   * 导出
   */
  const handleExport = () => {
    if (exportLoading.value) return;
    exportLoading.value = true;
    proxy
      .download('/${apiModuleName}/${apiName}/export', { ...queryParams.value }, '${businessName}数据')
      .finally(() => {
        exportLoading.value = false;
      });
  };

  /**
   * 查询
   */
  const handleQuery = () => {
    getList();
  };

  /**
   * 重置
   */
  const resetQuery = () => {
    proxy.$refs.queryRef.resetFields();
    handleQuery();
  };

  /**
   * 切换页码
   *
   * @param current 页码
   */
  const handlePageChange = (current: number) => {
    queryParams.value.page = current;
    getList();
  };

  /**
   * 切换每页条数
   *
   * @param pageSize 每页条数
   */
  const handlePageSizeChange = (pageSize: number) => {
    queryParams.value.size = pageSize;
    getList();
  };
</script>

<script lang="ts">
  export default {
    name: '${classNamePrefix}',
  };
</script>

<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.${apiModuleName}', 'menu.${apiModuleName}.${apiName}.list']" />
    <a-card class="general-card" :title="$t('menu.${apiModuleName}.${apiName}.list')">
      <!-- 头部区域 -->
      <div class="header">
        <!-- 搜索栏 -->
        <div v-if="showQuery" class="header-query">
          <a-form ref="queryRef" :model="queryParams" layout="inline">
            <#list fieldConfigs as fieldConfig>
            <#if fieldConfig.showInQuery>
            <a-form-item field="${fieldConfig.fieldName}" hide-label>
              <a-input
                v-model="queryParams.${fieldConfig.fieldName}"
                placeholder="输入${fieldConfig.comment}搜索"
                allow-clear
                style="width: 150px"
                @press-enter="handleQuery"
              />
            </a-form-item>
            </#if>
            </#list>
            <a-form-item hide-label>
              <a-space>
                <a-button type="primary" @click="handleQuery">
                  <template #icon><icon-search /></template>查询
                </a-button>
                <a-button @click="resetQuery">
                  <template #icon><icon-refresh /></template>重置
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>
        <!-- 操作栏 -->
        <div class="header-operation">
          <a-row>
            <a-col :span="12">
              <a-space>
                <a-button
                  v-permission="['${apiModuleName}:${apiName}:add']"
                  type="primary"
                  @click="toAdd"
                >
                  <template #icon><icon-plus /></template>新增
                </a-button>
                <a-button
                  v-permission="['${apiModuleName}:${apiName}:update']"
                  type="primary"
                  status="success"
                  :disabled="single"
                  :title="single ? '请选择一条要修改的数据' : ''"
                  @click="toUpdate(ids[0])"
                >
                  <template #icon><icon-edit /></template>修改
                </a-button>
                <a-button
                  v-permission="['${apiModuleName}:${apiName}:delete']"
                  type="primary"
                  status="danger"
                  :disabled="multiple"
                  :title="multiple ? '请选择要删除的数据' : ''"
                  @click="handleBatchDelete"
                >
                  <template #icon><icon-delete /></template>删除
                </a-button>
                <a-button
                  v-permission="['${apiModuleName}:${apiName}:export']"
                  :loading="exportLoading"
                  type="primary"
                  status="warning"
                  @click="handleExport"
                >
                  <template #icon><icon-download /></template>导出
                </a-button>
              </a-space>
            </a-col>
            <a-col :span="12">
              <right-toolbar
                v-model:show-query="showQuery"
                @refresh="getList"
              />
            </a-col>
          </a-row>
        </div>
      </div>

      <!-- 列表区域 -->
      <a-table
        ref="tableRef"
        row-key="id"
        :data="dataList"
        :loading="loading"
        :row-selection="{
          type: 'checkbox',
          showCheckedAll: true,
          onlyCurrent: false,
        }"
        :pagination="{
          showTotal: true,
          showPageSize: true,
          total: total,
          current: queryParams.page,
        }"
        :bordered="false"
        column-resizable
        stripe
        size="large"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
        @selection-change="handleSelectionChange"
      >
        <template #columns>
          <#list fieldConfigs as fieldConfig>
          <#if fieldConfig_index = 0>
          <a-table-column title="${fieldConfig.comment}" data-index="${fieldConfig.fieldName}">
            <template #cell="{ record }">
              <a-link @click="toDetail(record.id)">{{ record.${fieldConfig.fieldName} }}</a-link>
            </template>
          </a-table-column>
          <#else>
          <#if fieldConfig.showInList>
          <a-table-column title="${fieldConfig.comment}" data-index="${fieldConfig.fieldName}" />
          </#if>
          </#if>
          </#list>
          <a-table-column
            v-if="checkPermission(['${apiModuleName}:${apiName}:update', '${apiModuleName}:${apiName}:delete'])"
            title="操作"
            align="center"
          >
            <template #cell="{ record }">
              <a-button
                v-permission="['${apiModuleName}:${apiName}:update']"
                type="text"
                size="small"
                title="修改"
                @click="toUpdate(record.id)"
              >
                <template #icon><icon-edit /></template>修改
              </a-button>
              <a-popconfirm
                content="是否确定删除该数据？"
                type="warning"
                @ok="handleDelete([record.id])"
              >
                <a-button
                  v-permission="['${apiModuleName}:${apiName}:delete']"
                  type="text"
                  size="small"
                  title="删除"
                  :disabled="record.disabled"
                >
                  <template #icon><icon-delete /></template>删除
                </a-button>
              </a-popconfirm>
            </template>
          </a-table-column>
        </template>
      </a-table>

      <!-- 表单区域 -->
      <a-modal
        :title="title"
        :visible="visible"
        :mask-closable="false"
        :esc-to-close="false"
        unmount-on-close
        render-to-body
        @ok="handleOk"
        @cancel="handleCancel"
      >
        <a-form ref="formRef" :model="form" :rules="rules" size="large">
          <#list fieldConfigs as fieldConfig>
          <#if fieldConfig.showInForm>
          <a-form-item label="${fieldConfig.comment}" field="${fieldConfig.fieldName}">
            <#if fieldConfig.formType = 'TEXT'>
            <a-input v-model="form.${fieldConfig.fieldName}" placeholder="请输入${fieldConfig.comment}" />
            <#elseif fieldConfig.formType = 'TEXT_AREA'>
            <a-textarea
              v-model="form.${fieldConfig.fieldName}"
              :max-length="200"
              placeholder="请输入${fieldConfig.comment}"
              :auto-size="{
                minRows: 3,
              }"
              show-word-limit
            />
            <#elseif fieldConfig.formType = 'SELECT'>
              <#--<a-select
                v-model="form.${fieldConfig.fieldName}"
                :options="${apiName}Options"
                placeholder="请选择${fieldConfig.comment}"
                :loading="${apiName}Loading"
                multiple
                allow-clear
                :allow-search="{ retainInputValue: true }"
                style="width: 416px"
              />-->
            <#elseif fieldConfig.formType = 'RADIO'>
            <#--<a-radio-group v-model="form.${fieldConfig.fieldName}" type="button">
            </a-radio-group>-->
            <#elseif fieldConfig.formType = 'DATE'>
            <a-date-picker v-model="form.${fieldConfig.fieldName}" placeholder="请选择${fieldConfig.comment}"/>
            <#elseif fieldConfig.formType = 'DATE_TIME'>
            <a-date-picker
              v-model="form.${fieldConfig.fieldName}"
              placeholder="请选择${fieldConfig.comment}"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
            />
            </#if>
          </a-form-item>
          </#if>
          </#list>
        </a-form>
      </a-modal>

      <!-- 详情区域 -->
      <a-drawer
        title="${businessName}详情"
        :visible="detailVisible"
        :width="580"
        :footer="false"
        unmount-on-close
        render-to-body
        @cancel="handleDetailCancel"
      >
        <a-descriptions :column="2" bordered size="large">
          <#list fieldConfigs as fieldConfig>
          <a-descriptions-item label="${fieldConfig.comment}">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <#if fieldConfig.fieldName = 'createUser'>
            <span v-else>{{ dataDetail.createUserString }}</span>
            <#elseif fieldConfig.fieldName = 'updateUser'>
            <span v-else>{{ dataDetail.updateUserString }}</span>
            <#else>
            <span v-else>{{ dataDetail.${fieldConfig.fieldName} }}</span>
            </#if>
          </a-descriptions-item>
          </#list>
        </a-descriptions>
      </a-drawer>
    </a-card>
  </div>
</template>

<style scoped lang="less"></style>
