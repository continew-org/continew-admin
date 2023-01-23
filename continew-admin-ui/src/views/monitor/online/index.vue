<template>
  <div class="container">
    <Breadcrumb :items="['menu.monitor', 'menu.online.user.list']" />
    <a-card class="general-card" :title="$t('menu.online.user.list')">
      <a-row style="margin-bottom: 15px">
        <a-col :span="24">
          <a-form ref="queryFormRef" :model="queryFormData" layout="inline">
            <a-form-item field="nickname" hide-label>
              <a-input
                v-model="queryFormData.nickname"
                placeholder="输入用户昵称搜索"
                allow-clear
                style="width: 150px;"
                @press-enter="toQuery"
              />
            </a-form-item>
            <a-form-item field="loginTime" hide-label>
              <date-range-picker v-model="queryFormData.loginTime" />
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
        <template #nickname="{ record }">
          {{ record.nickname }}（{{record.username}}）
        </template>
        <template #operations="{ record }">
          <a-button
            v-permission="['admin']"
            type="text"
            size="small"
            :title="currentToken === record.token ? '不能强退当前登录' : ''"
            :disabled="currentToken === record.token"
            @click="handleClick(record.token)"
          >
            强退
          </a-button>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, reactive } from 'vue';
  import useLoading from '@/hooks/loading';
  import { Message } from '@arco-design/web-vue';
  import { getOnlineUserList, OnlineUserRecord, OnlineUserParams, kickout } from '@/api/monitor/online';
  import { Pagination } from '@/types/global';
  import { PaginationProps } from '@arco-design/web-vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { FormInstance } from '@arco-design/web-vue/es/form';
  import { getToken } from '@/utils/auth';

  const { loading, setLoading } = useLoading(true);
  const currentToken = computed(() => getToken());
  const queryFormRef = ref<FormInstance>();
  const queryFormData = ref({
    nickname: '',
    loginTime: [],
  });

  // 查询
  const toQuery = () => {
    fetchData({
      page: pagination.current,
      size: pagination.pageSize,
      sort: ['createTime,desc'],
      ...queryFormData.value,
    } as unknown as OnlineUserParams);
  };

  // 重置
  const resetQuery = async () => {
    await queryFormRef.value?.resetFields();
    await fetchData();
  };

  const renderData = ref<OnlineUserRecord[]>([]);
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
      dataIndex: 'nickname',
      slotName: 'nickname',
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
      dataIndex: 'loginTime',
    },
    {
      title: '操作',
      slotName: 'operations',
      align: 'center',
    },
  ]);

  // 分页查询列表
  const fetchData = async (
    params: OnlineUserParams = { page: 1, size: 10, sort: ['createTime,desc'] }
  ) => {
    setLoading(true);
    try {
      const { data } = await getOnlineUserList(params);
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

  // 强退
  const handleClick = async (token: string) => {
    const res = await kickout(token);
    if (res.success) Message.success(res.msg);
  };
</script>

<script lang="ts">
  export default {
    name: 'OnlineUser',
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
