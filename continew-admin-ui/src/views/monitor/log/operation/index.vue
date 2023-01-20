<template>
  <div class="container">
    <Breadcrumb :items="['menu.monitor', 'menu.log.operation.list']" />
    <a-card class="general-card" :title="$t('menu.log.operation.list')">
      <a-row style="margin-bottom: 15px">
        <a-col :span="24">
          <a-form
            ref="queryFormRef"
            :model="queryFormData"
            layout="inline"
          >
            <a-form-item
              field="description"
              hide-label
            >
              <a-input
                v-model="queryFormData.description"
                placeholder="输入操作内容搜索"
                allow-clear
                style="width: 150px;"
                @press-enter="toQuery"
              />
            </a-form-item>
            <a-form-item
              field="status"
              hide-label
            >
              <a-select
                v-model="queryFormData.status"
                :options="statusOptions"
                placeholder="操作状态搜索"
                allow-clear
                style="width: 150px;"
              />
            </a-form-item>
            <a-form-item
              field="createTime"
              hide-label
            >
              <date-range-picker v-model="queryFormData.createTime" />
            </a-form-item>
            <a-button type="primary" @click="toQuery">
              <template #icon>
                <icon-search />
              </template>
              查询
            </a-button>
            <a-button @click="resetQuery">
              <template #icon>
                <icon-refresh />
              </template>
              重置
            </a-button>
          </a-form>
        </a-col>
      </a-row>
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
            <a-tooltip :content="record.errorMsg">
              <a-tag color="red" style="cursor: pointer">
                <span class="circle fail"></span>
                失败
              </a-tag>
            </a-tooltip>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, reactive } from 'vue';
  import useLoading from '@/hooks/loading';
  import { queryOperationLogList, OperationLogRecord, OperationLogParams } from '@/api/monitor/log';
  import { Pagination } from '@/types/global';
  import type { SelectOptionData } from '@arco-design/web-vue/es/select/interface';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { FormInstance } from '@arco-design/web-vue/es/form';

  const { loading, setLoading } = useLoading(true);
  const queryFormRef = ref<FormInstance>();
  const renderData = ref<OperationLogRecord[]>([]);

  const queryFormData = ref({
    description: '',
    status: undefined,
    createTime: [],
  });
  const statusOptions = computed<SelectOptionData[]>(() => [
    {
      label: '成功',
      value: 1,
    },
    {
      label: '失败',
      value: 2,
    },
  ]);

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
      title: '操作人',
      dataIndex: 'createUserString',
    },
    {
      title: '操作内容',
      dataIndex: 'description',
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

  // 查询列表
  const fetchData = async (
    params: OperationLogParams = { page: 1, size: 10, sort: ['createTime,desc'] }
  ) => {
    setLoading(true);
    try {
      const { data } = await queryOperationLogList(params);
      renderData.value = data.list;
      pagination.current = params.page;
      pagination.total = data.total;
    } finally {
      setLoading(false);
    }
  };

  const onPageChange = (current: number) => {
    fetchData({ page: current, size: pagination.pageSize, sort: ['createTime,desc'] });
  };

  // 查询
  const toQuery = () => {
    fetchData({
      page: pagination.current,
      size: pagination.pageSize,
      sort: ['createTime,desc'],
      ...queryFormData.value,
    } as unknown as OperationLogParams);
  };

  // 重置
  const resetQuery = async () => {
    await queryFormRef.value?.resetFields();
    await fetchData();
  };
  fetchData();
</script>

<script lang="ts">
  export default {
    name: 'OperationLog',
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
</style>
