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
        :data="tableList"
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
        row-key="tableName"
        :bordered="false"
        :stripe="true"
        :loading="loading"
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
          <a-table-column title="注释" data-index="comment" tooltip />
          <a-table-column title="存储引擎" data-index="engine" align="center" />
          <a-table-column title="字符集" data-index="charset" />
          <a-table-column title="创建时间" data-index="createTime" />
          <a-table-column title="操作" align="center">
            <template #cell="{ record }">
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
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive } from 'vue';
  import { TableRecord, TableParam, listTable } from '@/api/tool/generator';

  const { proxy } = getCurrentInstance() as any;

  const tableList = ref<TableRecord[]>([]);
  const total = ref(0);
  const ids = ref<Array<string>>([]);
  const single = ref(true);
  const multiple = ref(true);
  const showQuery = ref(true);
  const loading = ref(false);

  const data = reactive({
    // 查询参数
    queryParams: {
      tableName: undefined,
      page: 1,
      size: 10,
      sort: ['createTime,desc', 'updateTime,desc'],
    },
  });
  const { queryParams } = toRefs(data);

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
    name: 'Role',
  };
</script>

<style scoped lang="less"></style>
