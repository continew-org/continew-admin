<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.system', 'menu.system.dict.list']" />
    <a-card class="general-card" :title="$t('menu.system.dict.list')">
      <a-row>
        <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <!-- 头部区域 -->
          <div class="header">
            <!-- 搜索栏 -->
            <div v-if="showQuery" class="header-query">
              <a-form ref="queryRef" :model="queryParams" layout="inline">
                <a-form-item field="name" hide-label>
                  <a-input
                    v-model="queryParams.name"
                    placeholder="输入字典名称搜索"
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
                    <a-button
                      v-permission="['system:dict:add']"
                      type="primary"
                      @click="toAdd"
                    >
                      <template #icon><icon-plus /></template>新增
                    </a-button>
                    <a-button
                      v-permission="['system:dict:update']"
                      type="primary"
                      status="success"
                      :disabled="single"
                      :title="single ? '请选择一条要修改的数据' : ''"
                      @click="toUpdate(ids[0])"
                    >
                      <template #icon><icon-edit /></template>修改
                    </a-button>
                    <a-button
                      v-permission="['system:dict:delete']"
                      type="primary"
                      status="danger"
                      :disabled="multiple"
                      :title="multiple ? '请选择要删除的数据' : ''"
                      @click="handleBatchDelete"
                    >
                      <template #icon><icon-delete /></template>删除
                    </a-button>
                    <a-button
                      v-permission="['system:dict:export']"
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
                    @refresh="handleQuery"
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
            class="dictTable"
            size="large"
            @page-change="handlePageChange"
            @page-size-change="handlePageSizeChange"
            @selection-change="handleSelectionChange"
            @row-click="handleSelect"
          >
            <template #columns>
              <a-table-column
                title="序号"
                :width="60"
                :body-cell-style="bodyCellStyle"
              >
                <template #cell="{ rowIndex }">
                  {{ rowIndex + 1 + (queryParams.page - 1) * queryParams.size }}
                </template>
              </a-table-column>
              <a-table-column
                title="字典名称"
                data-index="name"
                :width="100"
                :body-cell-style="bodyCellStyle"
              />
              <a-table-column
                title="字典编码"
                data-index="code"
                :body-cell-style="bodyCellStyle"
              />
              <a-table-column
                title="描述"
                data-index="description"
                ellipsis
                tooltip
                :width="100"
                :body-cell-style="bodyCellStyle"
              />
              <a-table-column
                v-if="
                  checkPermission(['system:dict:update', 'system:dict:delete'])
                "
                title="操作"
                align="center"
                :body-cell-style="bodyCellStyle"
              >
                <template #cell="{ record }">
                  <a-button
                    v-permission="['system:dict:update']"
                    type="text"
                    size="small"
                    title="修改"
                    @click="toUpdate(record.id)"
                  >
                    <template #icon><icon-edit /></template>修改
                  </a-button>
                  <a-popconfirm
                    content="确定要删除当前选中的数据吗？"
                    type="warning"
                    @ok="handleDelete([record.id])"
                  >
                    <a-button
                      v-permission="['system:dict:delete']"
                      type="text"
                      size="small"
                      :title="record.isSystem ? '系统内置数据不能删除' : '删除'"
                      :disabled="record.isSystem"
                    >
                      <template #icon><icon-delete /></template>删除
                    </a-button>
                  </a-popconfirm>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-col>
        <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <dict-item ref="dictItemRef" :dict-id="dictId" />
        </a-col>
      </a-row>

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
          <a-form-item label="字典名称" field="name">
            <a-input v-model="form.name" placeholder="请输入字典名称" />
          </a-form-item>
          <a-form-item label="字典编码" field="code">
            <a-input
              v-model="form.code"
              placeholder="请输入字典编码"
              :disabled="form.isSystem"
            />
          </a-form-item>
          <a-form-item label="描述" field="description">
            <a-textarea
              v-model="form.description"
              :max-length="200"
              placeholder="请输入描述"
              :auto-size="{
                minRows: 3,
              }"
              show-word-limit
            />
          </a-form-item>
        </a-form>
      </a-modal>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive } from 'vue';
  import { TableData } from '@arco-design/web-vue';
  import {
    DataRecord,
    ListParam,
    list,
    get,
    add,
    update,
    del,
  } from '@/api/system/dict';
  import checkPermission from '@/utils/permission';
  import dictItem from './item.vue';

  const { proxy } = getCurrentInstance() as any;

  const dataList = ref<DataRecord[]>([]);
  const total = ref(0);
  const ids = ref<Array<number>>([]);
  const title = ref('');
  const single = ref(true);
  const multiple = ref(true);
  const showQuery = ref(true);
  const loading = ref(false);
  const exportLoading = ref(false);
  const visible = ref(false);
  const dictId = ref();
  const lastSelectDict = ref<TableData>();
  const bodyCellStyle = (record: TableData) => {
    if (record.click) {
      return {
        color: 'rgb(var(--arcoblue-6))',
        cursor: 'pointer',
      };
    }
    return { cursor: 'pointer' };
  };

  const data = reactive({
    // 查询参数
    queryParams: {
      name: undefined,
      page: 1,
      size: 10,
      sort: ['createTime,desc'],
    },
    // 表单数据
    form: {} as DataRecord,
    // 表单验证规则
    rules: {
      name: [
        { required: true, message: '请输入字典名称' },
        {
          match: /^[\\u4e00-\\u9fa5a-zA-Z0-9_-]{2,30}$/,
          message:
            '长度为 2 到 30 位，可以包含中文、字母、数字、下划线，短横线',
        },
      ],
      code: [
        { required: true, message: '请输入字典编码' },
        {
          match: /^[a-zA-Z][a-zA-Z0-9_]{1,29}$/,
          message: '长度为 2 到 30 位，可以包含字母、数字，下划线，以字母开头',
        },
      ],
    },
  });
  const { queryParams, form, rules } = toRefs(data);

  /**
   * 选中行
   *
   * @param record 所选行记录
   */
  const handleSelect = (record: TableData) => {
    if (lastSelectDict.value) {
      delete lastSelectDict.value.click;
    }
    lastSelectDict.value = record;
    record.click = true;
    dictId.value = record.id;
    proxy.$refs.dictItemRef.getList(record.id);
  };

  /**
   * 查询列表
   *
   * @param params 查询参数
   */
  const getList = (params: ListParam = { ...queryParams.value }) => {
    dictId.value = null;
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
    title.value = '新增字典';
    visible.value = true;
  };

  /**
   * 打开修改对话框
   *
   * @param id ID
   */
  const toUpdate = (id: number) => {
    reset();
    get(id).then((res) => {
      form.value = res.data;
      title.value = '修改字典';
      visible.value = true;
    });
  };

  /**
   * 重置表单
   */
  const reset = () => {
    form.value = {};
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
   * 批量删除
   */
  const handleBatchDelete = () => {
    if (ids.value.length === 0) {
      proxy.$message.info('请选择要删除的数据');
    } else {
      proxy.$modal.warning({
        title: '警告',
        titleAlign: 'start',
        content: '确定要删除当前选中的数据吗？',
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
  const handleDelete = (ids: Array<number>) => {
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
      .download('/system/dict/export', { ...queryParams.value }, '字典数据')
      .finally(() => {
        exportLoading.value = false;
      });
  };

  /**
   * 查询
   */
  const handleQuery = () => {
    proxy.$refs.tableRef.selectAll(false);
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
    name: 'Dict',
  };
</script>

<style scoped lang="less"></style>
