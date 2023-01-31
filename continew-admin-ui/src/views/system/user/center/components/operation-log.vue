<template>
  <div class="container">
    <a-table
      :columns="columns"
      :data="renderData"
      :pagination="paginationProps"
      row-key="logId"
      :bordered="false"
      :stripe="true"
      :loading="loading"
      size="large"
      @page-change="handlePageChange"
      @page-size-change="handlePageSizeChange"
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
          <a-tooltip :content="record.errorMsg">
            <a-tag color="red" style="cursor: pointer">
              <span class="circle fail"></span>
              失败
            </a-tag>
          </a-tooltip>
        </a-space>
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
  import { computed, ref, reactive } from 'vue';
  import useLoading from '@/hooks/loading';
  import { useLoginStore } from '@/store';
  import { getOperationLogList, OperationLogRecord, OperationLogParams } from '@/api/monitor/log';
  import { Pagination } from '@/types/global';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { PaginationProps } from "@arco-design/web-vue";

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
  const paginationProps = computed((): PaginationProps => {
    return {
      showTotal: true,
      showPageSize: true,
      total: pagination.total,
      current: pagination.current,
    }
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
      title: '所属模块',
      dataIndex: 'module',
    },
    {
      title: '操作状态',
      dataIndex: 'status',
      slotName: 'status',
      align: 'center',
    },
    {
      title: '操作 IP',
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

  // 分页查询列表
  const fetchData = async (
    params: OperationLogParams = { uid: loginStore.userId, page: 1, size: 10, sort: ['createTime,desc'] }
  ) => {
    setLoading(true);
    try {
      const { data } = await getOperationLogList(params);
      renderData.value = data.list;
      pagination.current = params.page;
      pagination.total = data.total;
    } finally {
      setLoading(false);
    }
  };
  const handlePageChange = (current: number) => {
    fetchData({ uid: loginStore.userId, page: current, size: pagination.pageSize, sort: ['createTime,desc'] });
  };
  const handlePageSizeChange = (pageSize: number) => {
    fetchData({ uid: loginStore.userId, page: pagination.current, size: pageSize, sort: ['createTime,desc'] });
  };
  const handleRefresh = () => {
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
    margin-right: 10px;
  }

  .action-icon:hover {
    color: #0960bd;
  }
</style>
