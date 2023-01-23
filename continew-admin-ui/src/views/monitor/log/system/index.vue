<template>
  <div class="container">
    <Breadcrumb :items="['menu.monitor', 'menu.log.system.list']" />
    <a-card class="general-card" :title="$t('menu.log.system.list')">
      <!-- 查询 -->
      <a-row style="margin-bottom: 15px">
        <a-col :span="24">
          <a-form ref="queryFormRef" :model="queryFormData" layout="inline">
            <a-form-item field="createTime" hide-label>
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

      <!-- 表格 -->
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
        <template #statusCode="{ record }">
          <a-space v-if="record.statusCode >= 400">
            <a-tag color="red">{{ record.statusCode }}</a-tag>
          </a-space>
          <a-space v-else-if="record.statusCode === 200">
            <a-tag color="green">{{ record.statusCode }}</a-tag>
          </a-space>
          <a-space v-else>
            <a-tag color="orange">{{ record.statusCode }}</a-tag>
          </a-space>
        </template>
        <template #requestUrl="{ record }">
          <a-space :title="decodeURIComponent(record.requestUrl)">{{ record.requestUrl.match(/(\w+):\/\/([^/:]+)(:\d*)?([^#|\?|\n]*)(\?.*)?/)[4] }}</a-space>
        </template>
        <template #elapsedTime="{ record }">
          <a-space v-if="record.elapsedTime > 500">
            <a-tag color="red">{{ record.elapsedTime }} ms</a-tag>
          </a-space>
          <a-space v-else-if="record.elapsedTime > 200">
            <a-tag color="orange">{{ record.elapsedTime }} ms</a-tag>
          </a-space>
          <a-space v-else>
            <a-tag color="green">{{ record.elapsedTime }} ms</a-tag>
          </a-space>
        </template>
        <template #operations="{ record }">
          <a-button v-permission="['admin']" type="text" size="small" @click="handleClick(record.logId)">详情</a-button>
          <a-button v-if="record.exceptionDetail" v-permission="['admin']" type="text" size="small" @click="handleExceptionDetail(record)">异常详情</a-button>
        </template>
      </a-table>

      <!-- 窗口 -->
      <a-drawer
        :width="570"
        :visible="visible"
        :footer="false"
        unmount-on-close
        @ok="handleOk"
        @cancel="handleCancel"
      >
        <template #title>日志详情</template>
        <div style="margin: 10px 0 0 10px">
          <a-descriptions title="基础信息" :column="2" bordered>
            <a-descriptions-item label="客户端 IP">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>{{ renderDetailData.clientIp }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="浏览器">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>{{ renderDetailData.browser }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="IP 归属地">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>{{ renderDetailData.location }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="请求耗时">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>
                <a-tag v-if="renderDetailData.elapsedTime > 500" color="red">
                  {{ renderDetailData.elapsedTime }} ms
                </a-tag>
                <a-tag v-else-if="renderDetailData.elapsedTime > 200" color="orange">
                  {{ renderDetailData.elapsedTime }} ms
                </a-tag>
                <a-tag v-else color="green">{{ renderDetailData.elapsedTime }} ms</a-tag>
              </span>
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>{{ renderDetailData.createTime }}</span>
            </a-descriptions-item>
          </a-descriptions>
          <a-descriptions
            title="协议信息"
            style="margin-top: 25px"
            :column="2"
            bordered
          >
            <a-descriptions-item label="状态码">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :rows="1" />
              </a-skeleton>
              <span v-else>
                <a-tag v-if="renderDetailData.statusCode >= 400" color="red">{{ renderDetailData.statusCode }}</a-tag>
                <a-tag v-else-if="renderDetailData.statusCode === 200" color="green">{{ renderDetailData.statusCode }}</a-tag>
                <a-tag v-else color="orange">{{ renderDetailData.statusCode }}</a-tag>
              </span>
            </a-descriptions-item>
            <a-descriptions-item label="请求方式">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :rows="1" />
              </a-skeleton>
              <span v-else>{{ renderDetailData.requestMethod }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="请求 URL" :span="2">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :rows="1" />
              </a-skeleton>
              <span v-else>{{ renderDetailData.requestUrl }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="响应体" :span="2">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :rows="3" />
              </a-skeleton>
              <a-space v-else>
                <VueJsonPretty
                  v-if="renderDetailData.responseBody"
                  :path="'res'"
                  :data="JSON.parse(renderDetailData.responseBody)"
                  :show-length="true" />
                <span v-else>无</span>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="响应头" :span="2">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :rows="3" />
              </a-skeleton>
              <a-space v-else>
                <VueJsonPretty
                  v-if="renderDetailData.responseHeaders"
                  :path="'res'"
                  :data="JSON.parse(renderDetailData.responseHeaders)"
                  :show-length="true" />
                <span v-else>无</span>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="请求体" :span="2">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :rows="3" />
              </a-skeleton>
              <a-space v-else>
                <VueJsonPretty
                  v-if="renderDetailData.requestBody"
                  :path="'res'"
                  :data="JSON.parse(renderDetailData.requestBody)"
                  :show-length="true" />
                <span v-else>无</span>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="请求头" :span="2">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :rows="3" />
              </a-skeleton>
              <a-space v-else>
                <VueJsonPretty
                  v-if="renderDetailData.requestHeaders"
                  :data="JSON.parse(renderDetailData.requestHeaders)"
                  :show-length="true" />
                <span v-else>无</span>
              </a-space>
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </a-drawer>

      <a-modal
        title="异常详情"
        render-to-body
        top="30px"
        width="83%"
        :visible="exceptionDetailVisible"
        :footer="false"
        unmount-on-close
        @ok="handleExceptionDetailOk"
        @cancel="handleExceptionDetailCancel"
      >
        <pre>{{ exceptionDetail }}</pre>
      </a-modal>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, reactive } from 'vue';
  import useLoading from '@/hooks/loading';
  import {
    getSystemLogDetail,
    SystemLogDetailRecord,
    getSystemLogList,
    SystemLogRecord,
    SystemLogParams,
  } from '@/api/monitor/log';
  import { Pagination } from '@/types/global';
  import { PaginationProps } from '@arco-design/web-vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { FormInstance } from '@arco-design/web-vue/es/form';
  import VueJsonPretty from 'vue-json-pretty';
  import 'vue-json-pretty/lib/styles.css';

  const { loading, setLoading } = useLoading(true);
  const visible = ref(false);
  const exceptionDetailVisible = ref(false);
  const detailLoading = ref(false);
  const exceptionDetail = ref();
  const queryFormRef = ref<FormInstance>();
  const queryFormData = ref({
    createTime: [],
  });
  // 查询
  const toQuery = () => {
    fetchData({
      page: pagination.current,
      size: pagination.pageSize,
      sort: ['createTime,desc'],
      ...queryFormData.value,
    } as unknown as SystemLogParams);
  };
  // 重置
  const resetQuery = async () => {
    await queryFormRef.value?.resetFields();
    await fetchData();
  };

  const renderData = ref<SystemLogRecord[]>([]);
  const renderDetailData = ref<SystemLogDetailRecord>({
    logId: '',
    requestUrl: '',
    requestMethod: '',
    requestHeaders: '',
    requestBody: '',
    statusCode: 200,
    responseHeaders: '',
    responseBody: '',
    elapsedTime: 0,
    clientIp: '',
    location: '',
    browser: '',
    createTime: '',
  });
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
      title: '状态码',
      dataIndex: 'statusCode',
      slotName: 'statusCode',
      align: 'center',
    },
    {
      title: '请求方式',
      dataIndex: 'requestMethod',
      align: 'center',
    },
    {
      title: '请求 URI',
      dataIndex: 'requestUrl',
      slotName: 'requestUrl',
    },
    {
      title: '客户端 IP',
      dataIndex: 'clientIp',
    },
    {
      title: 'IP 归属地',
      dataIndex: 'location',
    },
    {
      title: '浏览器',
      dataIndex: 'browser',
    },
    {
      title: '请求耗时',
      dataIndex: 'elapsedTime',
      slotName: 'elapsedTime',
      align: 'center',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
    },
    {
      title: '操作',
      slotName: 'operations',
      align: 'center',
    },
  ]);

  // 分页查询列表
  const fetchData = async (
    params: SystemLogParams = { page: 1, size: 10, sort: ['createTime,desc'] }
  ) => {
    setLoading(true);
    try {
      const { data } = await getSystemLogList(params);
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

  // 查看详情
  const handleClick = async (logId: string) => {
    visible.value = true;
    detailLoading.value = true;
    try {
      const { data } = await getSystemLogDetail(logId);
      renderDetailData.value = data;
    } finally {
      detailLoading.value = false;
    }
  };
  const handleOk = () => {
    visible.value = false;
  };
  const handleCancel = () => {
    visible.value = false;
  }

  // 查看异常详情
  const handleExceptionDetail = async (record: SystemLogRecord) => {
    exceptionDetailVisible.value = true;
    exceptionDetail.value = record.exceptionDetail;
  };
  const handleExceptionDetailOk = () => {
    exceptionDetailVisible.value = false;
    exceptionDetail.value = null;
  };
  const handleExceptionDetailCancel = () => {
    exceptionDetailVisible.value = false;
    exceptionDetail.value = null;
  }
</script>

<script lang="ts">
  export default {
    name: 'SystemLog',
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
