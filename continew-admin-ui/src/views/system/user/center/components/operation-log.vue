<template>
  <div class="container">
    <!-- 列表区域 -->
    <a-table
      ref="tableRef"
      row-key="logId"
      :loading="loading"
      :pagination="{
          showTotal: true,
          showPageSize: true,
          total: total,
          current: queryParams.page,
        }"
      :data="operationLogList"
      :bordered="false"
      :stripe="true"
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
        <a-table-column title="操作内容" data-index="description" />
        <a-table-column title="所属模块" data-index="module" />
        <a-table-column title="操作状态" align="center">
          <template #cell="{ record }">
            <a-tag v-if="record.status === 1" color="green"><span class="circle pass" />成功</a-tag>
            <a-tooltip v-else :content="record.errorMsg">
              <a-tag color="red" style="cursor: pointer">
                <span class="circle fail" />失败
              </a-tag>
            </a-tooltip>
          </template>
        </a-table-column>
        <a-table-column title="操作 IP" data-index="clientIp" />
        <a-table-column title="操作地点" data-index="location" />
        <a-table-column title="浏览器" data-index="browser" />
      </template>
      <template #pagination-left>
        <a-tooltip content="刷新">
          <div class="action-icon" @click="handleRefresh">
            <icon-refresh size="18" />
          </div>
        </a-tooltip>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
  import { ref, toRefs, reactive } from 'vue';
  import {
    OperationLogParam,
    OperationLogRecord,
    listOperationLog,
  } from '@/api/monitor/log';
  import { useLoginStore } from '@/store';

  const loginStore = useLoginStore();

  const operationLogList = ref<OperationLogRecord[]>([]);
  const total = ref(0);
  const loading = ref(false);

  const data = reactive({
    // 查询参数
    queryParams: {
      uid: loginStore.userId,
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
    listOperationLog(params).then((res) => {
      operationLogList.value = res.data.list;
      total.value = res.data.total;
    }).finally(() => {
      loading.value = false;
    });
  };
  getList();

  /**
   * 刷新
   */
  const handleRefresh = () => {
    getList();
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

<style scoped lang="less">
  .container {
    padding: 0 20px 20px 20px;
  }

  :deep(.arco-table-th) {
    &:last-child {
      .arco-table-th-item-title {
        margin-left: 16px;
      }
    }
  }

  .action-icon {
    cursor: pointer;
    margin-right: 10px;
  }

  .action-icon:hover {
    color: #0960bd;
  }
</style>
