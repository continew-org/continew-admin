<template>
  <div class="container">
    <Breadcrumb :items="['menu.monitor', 'menu.log.login.list']" />
    <a-card class="general-card" :title="$t('menu.log.login.list')">
      <a-row style="margin-bottom: 15px">
        <a-col :span="24">
          <a-form
            ref="queryFormRef"
            :model="queryFormData"
            layout="inline"
          >
            <a-form-item
              field="status"
              hide-label
            >
              <a-select
                v-model="queryFormData.status"
                :options="statusOptions"
                placeholder="登录状态搜索"
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
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, reactive } from 'vue';
  import useLoading from '@/hooks/loading';
  import { queryLoginLogList, LoginLogRecord, LoginLogParams } from '@/api/monitor/log';
  import { Pagination } from '@/types/global';
  import { PaginationProps } from '@arco-design/web-vue';
  import type { SelectOptionData } from '@arco-design/web-vue/es/select/interface';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { FormInstance } from '@arco-design/web-vue/es/form';

  const { loading, setLoading } = useLoading(true);
  const queryFormRef = ref<FormInstance>();
  const queryFormData = ref({
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

  // 查询
  const toQuery = () => {
    fetchData({
      page: pagination.current,
      size: pagination.pageSize,
      sort: ['createTime,desc'],
      ...queryFormData.value,
    } as unknown as LoginLogParams);
  };

  // 重置
  const resetQuery = async () => {
    await queryFormRef.value?.resetFields();
    await fetchData();
  };

  const renderData = ref<LoginLogRecord[]>([]);
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
      title: '用户昵称',
      dataIndex: 'createUserString',
    },
    {
      title: '登录行为',
      dataIndex: 'description',
    },
    {
      title: '登录状态',
      dataIndex: 'status',
      slotName: 'status',
      align: 'center',
    },
    {
      title: '登录 IP',
      dataIndex: 'clientIp',
    },
    {
      title: '登录地点',
      dataIndex: 'location',
    },
    {
      title: '浏览器',
      dataIndex: 'browser',
    },
    {
      title: '登录时间',
      dataIndex: 'createTime',
    },
  ]);

  // 分页查询列表
  const fetchData = async (
    params: LoginLogParams = { page: 1, size: 10, sort: ['createTime,desc'] }
  ) => {
    setLoading(true);
    try {
      const { data } = await queryLoginLogList(params);
      renderData.value = data.list;
      pagination.current = params.page;
      pagination.total = data.total;
    } finally {
      setLoading(false);
    }
  };
  const handlePageChange = (current: number) => {
    fetchData({ page: current, size: pagination.pageSize, sort: ['createTime,desc'] });
  };
  const handlePageSizeChange = (pageSize: number) => {
    fetchData({ page: pagination.current, size: pageSize, sort: ['createTime,desc'] });
  };
  fetchData();
</script>

<script lang="ts">
  export default {
    name: 'LoginLog',
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
