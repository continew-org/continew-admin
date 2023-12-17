<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.monitor', 'menu.log.operation.list']" />
    <a-card class="general-card" :title="$t('menu.log.operation.list')">
      <!-- 头部区域 -->
      <div class="header">
        <!-- 搜索栏 -->
        <div class="header-query">
          <a-form ref="queryRef" :model="queryParams" layout="inline">
            <a-form-item field="description" hide-label>
              <a-input
                v-model="queryParams.description"
                placeholder="输入操作内容搜索"
                allow-clear
                style="width: 150px"
                @press-enter="handleQuery"
              />
            </a-form-item>
            <a-form-item field="status" hide-label>
              <a-select
                v-model="queryParams.status"
                :options="success_failure_status_enum"
                placeholder="操作状态搜索"
                allow-clear
                style="width: 150px"
              />
            </a-form-item>
            <a-form-item field="createTime" hide-label>
              <date-range-picker v-model="queryParams.createTime" />
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
        row-key="id"
        :data="operationLogList"
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
          <a-table-column title="操作时间" data-index="createTime" />
          <a-table-column title="操作人" data-index="createUserString" />
          <a-table-column title="操作内容" data-index="description" />
          <a-table-column title="所属模块" data-index="module" />
          <a-table-column title="操作状态" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.status === 1" color="green"
                ><span class="circle pass" />成功</a-tag
              >
              <a-tooltip v-else :content="record.errorMsg">
                <a-tag color="red" style="cursor: pointer">
                  <span class="circle fail" />失败
                </a-tag>
              </a-tooltip>
            </template>
          </a-table-column>
          <a-table-column title="操作 IP" data-index="ip" />
          <a-table-column title="操作地点" data-index="address" />
          <a-table-column title="浏览器" data-index="browser" />
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive } from 'vue';
  import {
    OperationLogParam,
    OperationLogRecord,
    listOperationLog,
  } from '@/api/monitor/log';

  const { proxy } = getCurrentInstance() as any;
  const { success_failure_status_enum } = proxy.useDict(
    'success_failure_status_enum'
  );

  const operationLogList = ref<OperationLogRecord[]>([]);
  const total = ref(0);
  const loading = ref(false);

  const data = reactive({
    // 查询参数
    queryParams: {
      description: undefined,
      status: undefined,
      createTime: undefined,
      page: 1,
      size: 10,
      sort: ['createTime,desc'],
    },
  });
  const { queryParams } = toRefs(data);

  /**
   * 查询列表
   *
   * @param params 查询参数
   */
  const getList = (params: OperationLogParam = { ...queryParams.value }) => {
    loading.value = true;
    listOperationLog(params)
      .then((res) => {
        operationLogList.value = res.data.list;
        total.value = res.data.total;
      })
      .finally(() => {
        loading.value = false;
      });
  };
  getList();

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
    name: 'OperationLog',
  };
</script>

<style scoped lang="less"></style>
