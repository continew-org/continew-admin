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
      </div>

      <!-- 列表区域 -->
      <a-table
        ref="tableRef"
        row-key="tableName"
        :data="tableList"
        :loading="loading"
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
      >
        <template #columns>
          <a-table-column title="序号">
            <template #cell="{ rowIndex }">
              {{ rowIndex + 1 + (queryParams.page - 1) * queryParams.size }}
            </template>
          </a-table-column>
          <a-table-column title="表名称" data-index="tableName" :width="225" />
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
                :title="record.isConfiged ? '生成预览' : '请先进行生成配置'"
                :disabled="!record.isConfiged"
                @click="handlePreview(record.tableName)"
              >
                <template #icon><icon-eye /></template>预览
              </a-button>
              <a-button
                type="text"
                size="small"
                :title="record.isConfiged ? '生成' : '请先进行生成配置'"
                :disabled="!record.isConfiged"
                @click="handleGenerate(record.tableName)"
              >
                <template #icon><icon-robot-add /></template>生成
              </a-button>
            </template>
          </a-table-column>
        </template>
        <template #pagination-left>
          <a-tooltip content="刷新">
            <div class="action-icon" @click="handleQuery">
              <icon-refresh size="18" />
            </div>
          </a-tooltip>
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
            <a-popconfirm
              content="确定要同步最新数据表结构吗？同步后只要不点击确定保存，则不影响原有配置数据。"
              type="warning"
              @ok="handleRefresh(form.tableName)"
            >
              <a-tooltip content="同步最新数据表结构">
                <a-button
                  type="primary"
                  status="success"
                  size="small"
                  title="同步"
                  :disabled="
                    fieldConfigList.length !== 0 &&
                    fieldConfigList[0].createTime === null
                  "
                >
                  <template #icon><icon-sync /></template>同步
                </a-button>
              </a-tooltip>
            </a-popconfirm>
          </template>
          <a-table
            ref="fieldConfigRef"
            :data="fieldConfigList"
            :loading="fieldConfigLoading"
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
                :width="125"
                ellipsis
                tooltip
              />
              <a-table-column title="类型" :width="95" ellipsis tooltip>
                <template #cell="{ record }">
                  <span v-if="record.fieldType">{{ record.fieldType }}</span>
                  <a-tooltip v-else content="请检查 generator.properties 配置">
                    <icon-exclamation-circle-fill
                      size="large"
                      style="color: #f53f3f"
                    />
                  </a-tooltip>
                </template>
              </a-table-column>
              <a-table-column title="描述" :width="170">
                <template #cell="{ record }">
                  <a-input v-model="record.comment" />
                </template>
              </a-table-column>
              <a-table-column title="列表" :width="60" align="center">
                <template #cell="{ record }">
                  <a-checkbox v-model="record.showInList" value="true" />
                </template>
              </a-table-column>
              <a-table-column title="表单" :width="60" align="center">
                <template #cell="{ record }">
                  <a-checkbox v-model="record.showInForm" value="true" />
                </template>
              </a-table-column>
              <a-table-column title="必填" :width="60" align="center">
                <template #cell="{ record }">
                  <a-checkbox
                    v-if="record.showInForm"
                    v-model="record.isRequired"
                    value="true"
                  />
                  <a-checkbox v-else disabled />
                </template>
              </a-table-column>
              <a-table-column title="查询" :width="60" align="center">
                <template #cell="{ record }">
                  <a-checkbox v-model="record.showInQuery" value="true" />
                </template>
              </a-table-column>
              <a-table-column title="表单类型" :width="150">
                <template #cell="{ record }">
                  <a-select
                    v-if="record.showInForm || record.showInQuery"
                    v-model="record.formType"
                    :options="form_type_enum"
                    placeholder="请选择表单类型"
                  />
                  <span v-else>无需设置</span>
                </template>
              </a-table-column>
              <a-table-column title="查询方式">
                <template #cell="{ record }">
                  <a-select
                    v-if="record.showInQuery"
                    v-model="record.queryType"
                    :options="query_type_enum"
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
                placeholder="项目模块包名，例如：top.charles7c.continew.admin.system"
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

  <!-- 生成预览区域 -->
  <a-modal
    :body-style="{
      paddingTop: 0,
    }"
    title="生成预览"
    :visible="previewVisible"
    width="70%"
    :footer="false"
    unmount-on-close
    render-to-body
    @cancel="handlePreviewCancel"
  >
    <div>
      <a-scrollbar style="height: 700px; overflow: auto">
        <a-link
          style="position: absolute; right: 20px; top: 50px; z-index: 999"
          @click="handleCopy"
        >
          <template #icon>
            <icon-copy size="large" />
          </template>
          复制
        </a-link>
        <a-tabs size="large" @tab-click="handleTabClick">
          <a-tab-pane
            v-for="item in generatePreviewList"
            :key="item.fileName"
            :title="item.fileName"
          >
            <codemirror
              v-model="item.content"
              :autofocus="true"
              :extensions="extensions"
            />
          </a-tab-pane>
        </a-tabs>
      </a-scrollbar>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive, watch } from 'vue';
  import { useClipboard } from '@vueuse/core';
  import { Codemirror } from 'vue-codemirror';
  import { java } from '@codemirror/lang-java';
  import { javascript } from '@codemirror/lang-javascript';

  import {
    TableRecord,
    TableParam,
    FieldConfigRecord,
    GenConfigRecord,
    listTable,
    listFieldConfig,
    getGenConfig,
    GeneratorConfigRecord,
    saveConfig,
    GeneratePreviewRecord,
    preview,
    generate,
  } from '@/api/tool/generator';

  const { proxy } = getCurrentInstance() as any;
  const { form_type_enum, query_type_enum } = proxy.useDict(
    'form_type_enum',
    'query_type_enum',
  );

  const { copy, copied } = useClipboard();
  const extensions = [java(), javascript()];
  const tableList = ref<TableRecord[]>([]);
  const fieldConfigList = ref<FieldConfigRecord[]>([]);
  const total = ref(0);
  const title = ref('');
  const showQuery = ref(true);
  const loading = ref(false);
  const visible = ref(false);
  const fieldConfigLoading = ref(false);
  const copyCodeContent = ref();
  const generatePreviewList = ref<GeneratePreviewRecord[]>([]);
  const previewVisible = ref(false);

  const data = reactive({
    // 查询参数
    queryParams: {
      tableName: undefined,
      page: 1,
      size: 10,
    },
    // 表单数据
    form: {} as GenConfigRecord,
    // 代码生成配置数据
    config: {} as GeneratorConfigRecord,
    // 表单验证规则
    rules: {
      author: [{ required: true, message: '请输入作者名称' }],
      moduleName: [{ required: true, message: '请输入所属模块' }],
      packageName: [{ required: true, message: '请输入模块包名' }],
      businessName: [{ required: true, message: '请输入业务名称' }],
    },
  });
  const { queryParams, form, rules, config } = toRefs(data);

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
      (t) => t.tableName === tableName,
    )[0].comment;
    tableComment = tableComment ? `（${tableComment}）` : ' ';
    title.value = `${tableName}${tableComment}配置`;
    visible.value = true;
    // 查询字段配置
    getFieldConfig(tableName, false);
    // 查询生成配置
    getGenConfig(tableName).then((res) => {
      form.value = res.data;
      form.value.isOverride = false;
    });
  };

  /**
   * 同步
   *
   * @param tableName 表名称
   */
  const handleRefresh = (tableName: string) => {
    getFieldConfig(tableName, true);
  };

  /**
   * 查询字段配置
   *
   * @param tableName 表名称
   * @param requireSync 是否需要同步
   */
  const getFieldConfig = (tableName: string, requireSync: boolean) => {
    fieldConfigLoading.value = true;
    listFieldConfig(tableName, requireSync)
      .then((res) => {
        fieldConfigList.value = res.data;
      })
      .finally(() => {
        fieldConfigLoading.value = false;
      });
  };

  /**
   * 确定
   */
  const handleOk = () => {
    proxy.$refs.formRef.validate((valid: any) => {
      if (!valid) {
        config.value.fieldConfigs = fieldConfigList.value;
        config.value.genConfig = form.value;
        saveConfig(form.value.tableName, config.value).then((res) => {
          handleCancel();
          getList();
          proxy.$message.success(res.msg);
        });
      }
    });
  };

  /**
   * 关闭配置
   */
  const handleCancel = () => {
    visible.value = false;
    proxy.$refs.formRef?.resetFields();
    fieldConfigList.value = [];
  };

  /**
   * 生成预览
   *
   * @param tableName 表名称
   */
  const handlePreview = (tableName: string) => {
    preview(tableName).then((res) => {
      generatePreviewList.value = res.data;
      copyCodeContent.value = generatePreviewList.value[0].content;
      previewVisible.value = true;
    });
  };

  /**
   * 关闭预览
   */
  const handlePreviewCancel = () => {
    generatePreviewList.value = [];
    copyCodeContent.value = '';
    previewVisible.value = false;
  };

  /**
   * 点击 Tab
   *
   * @param key Tab 键
   */
  const handleTabClick = (key: any) => {
    copyCodeContent.value = generatePreviewList.value.filter(
      (p) => p.fileName === key,
    )[0].content;
  };

  /**
   * 复制代码
   *
   */
  const handleCopy = () => {
    copy(copyCodeContent.value);
  };
  watch(copied, () => {
    if (copied.value) {
      proxy.$message.success('复制成功');
    }
  });

  /**
   * 生成代码
   *
   * @param tableName 表名称
   */
  const handleGenerate = (tableName: string) => {
    generate(tableName).then((res) => {
      proxy.$message.success(res.msg);
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
    name: 'Generator',
  };
</script>

<style scoped lang="less">
  :deep(.arco-tabs-content) {
    padding-top: 0;
  }

  .field-config :deep(.arco-card-body) {
    padding: 0;
  }

  :deep(.gen-config.arco-form) {
    width: 50%;
  }

  .action-icon {
    cursor: pointer;
    margin-right: 10px;
  }

  .action-icon:hover {
    color: #0960bd;
  }
</style>
