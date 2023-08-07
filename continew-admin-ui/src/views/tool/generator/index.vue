<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.tool', 'menu.tool.generator.list']" />
    <a-card class="general-card" :title="$t('menu.tool.generator.list')">
      <!-- 头部区域 -->
      <div class="header">
        <!-- 搜索栏 -->
        <div v-if="showQuery" class="header-query">
          <a-form ref="queryRef" :model="queryParams" layout="inline">
            <a-form-item field="tableName" hide-label>
              <a-input
                v-model="queryParams.tableName"
                placeholder="输入表名称搜索"
                allow-clear
                style="width: 150px"
                @press-enter="handleQuery"
              />
            </a-form-item>
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
                <a-button type="primary" @click="toGenerate(ids[0])">
                  <template #icon><icon-robot-add /></template>代码生成
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
        row-key="tableName"
        :data="tableList"
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
          <a-table-column title="序号">
            <template #cell="{ rowIndex }">
              {{ rowIndex + 1 + (queryParams.page - 1) * queryParams.size }}
            </template>
          </a-table-column>
          <a-table-column
            title="表名称"
            data-index="tableName"
            :width="200"
            tooltip
          />
          <a-table-column title="描述" data-index="comment" tooltip />
          <a-table-column title="存储引擎" data-index="engine" align="center" />
          <a-table-column title="字符集" data-index="charset" />
          <a-table-column title="创建时间" data-index="createTime" />
          <a-table-column title="操作" align="center">
            <template #cell="{ record }">
              <a-button
                type="text"
                size="small"
                title="配置"
                @click="toConfig(record.tableName)"
              >
                <template #icon><icon-settings /></template>配置
              </a-button>
              <a-button
                type="text"
                size="small"
                title="生成"
                @click="toGenerate(record.tableName)"
              >
                <template #icon><icon-robot-add /></template>生成
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>

      <!-- 表单区域 -->
      <a-drawer
        :title="title"
        :visible="visible"
        :width="1000"
        :mask-closable="false"
        :esc-to-close="false"
        unmount-on-close
        render-to-body
        @ok="handleOk"
        @cancel="handleCancel"
      >
        <a-card title="字段配置" class="field-config">
          <template #extra>
            <a-space>
              <a-button
                type="primary"
                status="success"
                size="small"
                title="同步"
                disabled
              >
                <template #icon><icon-sync /></template>同步
              </a-button>
            </a-space>
          </template>
          <a-table
            ref="columnMappingRef"
            :data="columnMappingList"
            :loading="columnMappingLoading"
            :pagination="false"
            :bordered="false"
            size="large"
            :scroll="{
              y: 400,
            }"
          >
            <template #columns>
              <a-table-column
                title="名称"
                data-index="fieldName"
                :width="130"
                ellipsis
                tooltip
              />
              <a-table-column
                title="类型"
                data-index="fieldType"
                :width="90"
                ellipsis
                tooltip
              />
              <a-table-column title="描述" data-index="comment" :width="170">
                <template #cell="{ record }">
                  <a-input v-model="record.comment" />
                </template>
              </a-table-column>
              <a-table-column
                title="列表"
                data-index="showInList"
                :width="60"
                align="center"
              >
                <template #cell="{ record }">
                  <a-checkbox v-model="record.showInList" value="true" />
                </template>
              </a-table-column>
              <a-table-column
                title="表单"
                data-index="showInForm"
                :width="60"
                align="center"
              >
                <template #cell="{ record }">
                  <a-checkbox v-model="record.showInForm" value="true" />
                </template>
              </a-table-column>
              <a-table-column
                title="必填"
                data-index="isRequired"
                :width="60"
                align="center"
              >
                <template #cell="{ record }">
                  <a-checkbox v-if="record.showInForm" v-model="record.isRequired" value="true" />
                  <a-checkbox v-else disabled />
                </template>
              </a-table-column>
              <a-table-column
                title="查询"
                data-index="showInQuery"
                :width="60"
                align="center"
              >
                <template #cell="{ record }">
                  <a-checkbox v-model="record.showInQuery" value="true" />
                </template>
              </a-table-column>
              <a-table-column
                title="表单类型"
                data-index="formType"
                :width="150"
              >
                <template #cell="{ record }">
                  <a-select
                    v-if="record.showInForm || record.showInQuery"
                    v-model="record.formType"
                    :options="FormTypeEnum"
                    placeholder="请选择表单类型"
                  />
                  <span v-else>无需设置</span>
                </template>
              </a-table-column>
              <a-table-column title="查询方式" data-index="queryType">
                <template #cell="{ record }">
                  <a-select
                    v-if="record.showInQuery"
                    v-model="record.queryType"
                    :options="QueryTypeEnum"
                    placeholder="请选择查询方式"
                  />
                  <span v-else>无需设置</span>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-card>
        <a-card title="生成配置" style="margin-top: 10px">
          <a-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="gen-config"
            size="large"
          >
            <a-form-item label="作者名称" field="author">
              <a-input v-model="form.author" placeholder="请输入作者名称" />
            </a-form-item>
            <a-form-item label="业务名称" field="businessName">
              <a-input
                v-model="form.businessName"
                placeholder="自定义业务名称，例如：用户"
              />
            </a-form-item>
            <a-form-item label="所属模块" field="moduleName">
              <a-input
                v-model="form.moduleName"
                placeholder="项目模块名称，例如：continew-admin-system"
              />
            </a-form-item>
            <a-form-item label="模块包名" field="packageName">
              <a-input
                v-model="form.packageName"
                placeholder="项目模块包名，例如：top.charles7c.cnadmin.system"
              />
            </a-form-item>
            <a-form-item label="前端路径" field="frontendPath">
              <a-input
                v-model="form.frontendPath"
                placeholder="前端项目 views 目录地址"
              />
            </a-form-item>
            <a-form-item label="去表前缀" field="tablePrefix">
              <a-input
                v-model="form.tablePrefix"
                placeholder="数据库表前缀，例如：sys_"
              />
            </a-form-item>
            <a-form-item label="是否覆盖" field="isOverride">
              <a-radio-group v-model="form.isOverride" type="button">
                <a-radio :value="true">是</a-radio>
                <a-radio :value="false">否</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-form>
        </a-card>
      </a-drawer>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive } from 'vue';
  import {
    TableRecord,
    TableParam,
    ColumnMappingRecord,
    GenConfigRecord,
    listTable,
    listColumnMapping,
    getGenConfig,
  } from '@/api/tool/generator';

  const { proxy } = getCurrentInstance() as any;
  const { FormTypeEnum, QueryTypeEnum } = proxy.useDict(
    'FormTypeEnum',
    'QueryTypeEnum'
  );

  const tableList = ref<TableRecord[]>([]);
  const columnMappingList = ref<ColumnMappingRecord[]>([]);
  const total = ref(0);
  const ids = ref<Array<string>>([]);
  const title = ref('');
  const single = ref(true);
  const multiple = ref(true);
  const showQuery = ref(true);
  const loading = ref(false);
  const visible = ref(false);
  const columnMappingLoading = ref(false);

  const data = reactive({
    // 查询参数
    queryParams: {
      tableName: undefined,
      page: 1,
      size: 10,
      sort: ['createTime,desc', 'updateTime,desc'],
    },
    // 表单数据
    form: {} as GenConfigRecord,
    // 表单验证规则
    rules: {
      author: [{ required: true, message: '请输入作者名称' }],
      moduleName: [{ required: true, message: '请输入模块名称' }],
      packageName: [{ required: true, message: '请输入包名称' }],
      businessName: [{ required: true, message: '请输入业务名称' }],
    },
  });
  const { queryParams, form, rules } = toRefs(data);

  /**
   * 查询列表
   *
   * @param params 查询参数
   */
  const getList = (params: TableParam = { ...queryParams.value }) => {
    loading.value = true;
    listTable(params)
      .then((res) => {
        tableList.value = res.data.list;
        total.value = res.data.total;
      })
      .finally(() => {
        loading.value = false;
      });
  };
  getList();

  /**
   * 打开配置对话框
   *
   * @param tableName 表名称
   */
  const toConfig = (tableName: string) => {
    let tableComment = tableList.value.filter(
      (t) => t.tableName === tableName
    )[0].comment;
    tableComment = tableComment ? `（${tableComment}）` : ' ';
    title.value = `${tableName}${tableComment}配置`;
    visible.value = true;
    // 查询列映射信息
    columnMappingLoading.value = true;
    listColumnMapping(tableName)
      .then((res) => {
        columnMappingList.value = res.data;
      })
      .finally(() => {
        columnMappingLoading.value = false;
      });
    // 查询生成配置
    getGenConfig(tableName).then((res) => {
      form.value = res.data;
      form.value.isOverride = false;
    });
  };

  /**
   * 确定
   */
  const handleOk = () => {
    visible.value = false;
    proxy.$message.info('功能尚在开发中');
  };

  /**
   * 关闭配置
   */
  const handleCancel = () => {
    visible.value = false;
  };

  /**
   * 代码生成
   *
   * @param tableName 表名称
   */
  const toGenerate = (tableName: string) => {
    proxy.$message.info(tableName);
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
    name: 'Generator',
  };
</script>

<style scoped lang="less">
  .field-config :deep(.arco-card-body) {
    padding: 0;
  }

  :deep(.gen-config.arco-form) {
    width: 50%;
  }
</style>
