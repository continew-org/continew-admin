<template>
  <div class="container">
    <a-table
      row-key="logId"
      :loading="loading"
      :pagination="pagination"
      :columns="columns"
      :data="renderData"
      :bordered="false"
      :stripe="true"
      size="large"
      @page-change="onPageChange"
    >
      <template #index="{ rowIndex }">
        {{ rowIndex + 1 + (pagination.current - 1) * pagination.pageSize }}
      </template>
      <template #status="{ record }">
        <a-space v-if="record.status === 1">
          <a-tag color="green">
            <span class="circle pass"></span>
            成功
          </a-tag>
        </a-space>
        <a-space v-else>
          <a-tag color="red">
            <span class="circle fail"></span>
            失败
          </a-tag>
        </a-space>
      </template>
      <template #pagination-left>
        <a-tooltip content="刷新">
          <div class="action-icon" @click="onRefresh">
            <icon-refresh size="18" />
          </div>
        </a-tooltip>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, reactive } from "vue";
  import { useLoginStore } from '@/store';
  import useLoading from '@/hooks/loading';
  import { queryOperationLogList, OperationLogRecord, OperationLogParams } from '@/api/monitor/operation-log';
  import { Pagination } from '@/types/global';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';

  const { loading, setLoading } = useLoading(true);
  const loginStore = useLoginStore();
  const renderData = ref<OperationLogRecord[]>([]);

  const basePagination: Pagination = {
    current: 1,
    pageSize: 10,
  };
  const pagination = reactive({
    ...basePagination,
  });
  const columns = computed<TableColumnData[]>(() => [
    {
      title: '序号',
      dataIndex: 'index',
      slotName: 'index',
    },
    {
      title: '操作时间',
      dataIndex: 'createTime',
    },
    {
      title: '操作内容',
      dataIndex: 'description',
    },
    {
      title: '操作状态',
      dataIndex: 'status',
      slotName: 'status',
    },
    {
      title: '操作IP',
      dataIndex: 'clientIp',
    },
    {
      title: '操作地点',
      dataIndex: 'location',
    },
    {
      title: '浏览器',
      dataIndex: 'browser',
    },
  ]);
  const fetchData = async (
    params: OperationLogParams = { uid: loginStore.userId, page: 1, size: 10, sort: ['createTime,desc'] }
  ) => {
    setLoading(true);
    try {
      const { data } = await queryOperationLogList(params);
      renderData.value = data.list;
      pagination.current = params.page;
      pagination.total = data.total;
    } catch (err) {
      // you can report use errorHandler or other
    } finally {
      setLoading(false);
    }
  };

  const onPageChange = (current: number) => {
    fetchData({ uid: loginStore.userId, page: current, size: pagination.pageSize, sort: ['createTime,desc'] });
  };

  const onRefresh = () => {
    fetchData({
      uid: loginStore.userId,
      page: pagination.current,
      size: pagination.pageSize,
      sort: ['createTime,desc'],
    } as unknown as OperationLogParams);
  };

  fetchData();
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
  }

  .action-icon:hover {
    color: #0960bd;
  }
</style>
