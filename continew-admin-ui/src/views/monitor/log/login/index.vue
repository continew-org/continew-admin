<template>
  <div class="container">
    <Breadcrumb :items="['menu.monitor', 'menu.log.login.list']" />
    <a-card class="general-card" :title="$t('menu.log.login.list')">
      <!-- 头部区域 -->
      <div class="head-container">
        <!-- 搜索栏 -->
        <div class="query-container">
          <a-form ref="queryRef" :model="queryParams" layout="inline">
            <a-form-item field="status" hide-label>
              <a-select
                v-model="queryParams.status"
                :options="statusOptions"
                placeholder="登录状态搜索"
                allow-clear
                style="width: 150px;"
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
        row-key="logId"
        :loading="loading"
        :pagination="{
          showTotal: true,
          showPageSize: true,
          total: total,
          current: queryParams.page,
        }"
        :data="loginLogList"
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
          <a-table-column title="用户昵称" data-index="createUserString" />
          <a-table-column title="登录行为" data-index="description" />
          <a-table-column title="登录状态" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.status === 1" color="green"><span class="circle pass" />成功</a-tag>
              <a-tooltip v-else :content="record.errorMsg">
                <a-tag color="red" style="cursor: pointer">
                  <span class="circle fail" />失败
                </a-tag>
              </a-tooltip>
            </template>
          </a-table-column>
          <a-table-column title="登录 IP" data-index="clientIp" />
          <a-table-column title="登录地点" data-index="location" />
          <a-table-column title="浏览器" data-index="browser" />
          <a-table-column title="登录时间" data-index="createTime" />
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive } from 'vue';
  import { SelectOptionData } from '@arco-design/web-vue';
  import {
    LoginLogRecord,
    LoginLogParam,
    listLoginLogList,
  } from '@/api/monitor/log';

  const { proxy } = getCurrentInstance() as any;

  const loginLogList = ref<LoginLogRecord[]>([]);
  const total = ref(0);
  const loading = ref(false);
  const statusOptions = ref<SelectOptionData[]>([
    { label: '启用', value: 1 },
    { label: '禁用', value: 2 },
  ]);

  const data = reactive({
    // 查询参数
    queryParams: {
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
  const getList = (params: LoginLogParam = { ...queryParams.value }) => {
    loading.value = true;
    listLoginLogList(params).then((res) => {
      loginLogList.value = res.data.list;
      total.value = res.data.total;
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
    name: 'LoginLog',
  };
</script>

<style scoped lang="less">
  .container {
    padding: 0 20px 20px 20px;
    .head-container {
      margin-bottom: 16px
    }
  }
  :deep(.arco-table-th) {
    &:last-child {
      .arco-table-th-item-title {
        margin-left: 16px;
      }
    }
  }
</style>
