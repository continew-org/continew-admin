<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.monitor', 'menu.online.user.list']" />
    <a-card class="general-card" :title="$t('menu.online.user.list')">
      <!-- 头部区域 -->
      <div class="header">
        <!-- 搜索栏 -->
        <div class="header-query">
          <a-form ref="queryRef" :model="queryParams" layout="inline">
            <a-form-item field="nickname" hide-label>
              <a-input
                v-model="queryParams.nickname"
                placeholder="输入用户昵称搜索"
                allow-clear
                style="width: 150px"
                @press-enter="handleQuery"
              />
            </a-form-item>
            <a-form-item field="loginTime" hide-label>
              <date-range-picker v-model="queryParams.loginTime" />
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
        row-key="token"
        :data="dataList"
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
          <a-table-column title="用户昵称">
            <template #cell="{ record }">
              {{ record.nickname }}（{{ record.username }}）
            </template>
          </a-table-column>
          <a-table-column title="登录 IP" data-index="clientIp" />
          <a-table-column title="登录地点" data-index="location" />
          <a-table-column title="浏览器" data-index="browser" />
          <a-table-column title="登录时间" data-index="loginTime" />
          <a-table-column
            v-if="checkPermission(['monitor:online:user:delete'])"
            title="操作"
            align="center"
          >
            <template #cell="{ record }">
              <a-popconfirm
                content="确定要强退该用户吗？"
                type="warning"
                @ok="handleKickout(record.token)"
              >
                <a-button
                  v-permission="['monitor:online:user:delete']"
                  type="text"
                  size="small"
                  :disabled="currentToken === record.token"
                  :title="
                    currentToken === record.token
                      ? '不能强退当前登录用户'
                      : '强退'
                  "
                >
                  <template #icon><icon-delete /></template>强退
                </a-button>
              </a-popconfirm>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive } from 'vue';
  import {
    DataRecord,
    ListParam,
    list,
    kickout,
  } from '@/api/monitor/online';
  import { getToken } from '@/utils/auth';
  import checkPermission from '@/utils/permission';

  const { proxy } = getCurrentInstance() as any;

  const dataList = ref<DataRecord[]>([]);
  const total = ref(0);
  const loading = ref(false);
  const currentToken = getToken();

  const data = reactive({
    // 查询参数
    queryParams: {
      nickname: undefined,
      loginTime: undefined,
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
  const getList = (params: ListParam = { ...queryParams.value }) => {
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
   * 强退
   *
   * @param token Token
   */
  const handleKickout = (token: string) => {
    kickout(token).then((res) => {
      getList();
      proxy.$message.success(res.msg);
    });
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
    name: 'OnlineUser',
  };
</script>

<style scoped lang="less"></style>
