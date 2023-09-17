<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.monitor', 'menu.log.system.list']" />
    <a-card class="general-card" :title="$t('menu.log.system.list')">
      <!-- 头部区域 -->
      <div class="header">
        <!-- 搜索栏 -->
        <div class="header-query">
          <a-form ref="queryRef" :model="queryParams" layout="inline">
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
        :data="systemLogList"
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
          <a-table-column title="状态码" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.statusCode >= 400" color="red">{{
                record.statusCode
              }}</a-tag>
              <a-tag v-else-if="record.statusCode === 200" color="green">{{
                record.statusCode
              }}</a-tag>
              <a-tag v-else color="orange">{{ record.statusCode }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column
            title="请求方式"
            align="center"
            data-index="requestMethod"
          />
          <a-table-column title="请求 URI">
            <template #cell="{ record }">
              <span :title="decodeURIComponent(record.requestUrl)">{{
                record.requestUrl.match(
                  /(\w+):\/\/([^/:]+)(:\d*)?([^#|\?|\n]*)(\?.*)?/
                )[4]
              }}</span>
            </template>
          </a-table-column>
          <a-table-column title="客户端 IP" data-index="clientIp" />
          <a-table-column title="IP 归属地" data-index="location" />
          <a-table-column title="浏览器" data-index="browser" />
          <a-table-column title="请求耗时">
            <template #cell="{ record }">
              <a-tag v-if="record.elapsedTime > 500" color="red"
                >{{ record.elapsedTime }} ms</a-tag
              >
              <a-tag v-else-if="record.elapsedTime > 200" color="orange"
                >{{ record.elapsedTime }} ms</a-tag
              >
              <a-tag v-else color="green">{{ record.elapsedTime }} ms</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" data-index="createTime" />
          <a-table-column title="操作" align="center">
            <template #cell="{ record }">
              <a-button
                type="text"
                size="small"
                title="查看详情"
                @click="toDetail(record.id)"
              >
                <template #icon><icon-eye /></template>详情
              </a-button>
              <a-button
                v-if="record.exceptionDetail"
                type="text"
                size="small"
                title="查看异常详情"
                @click="toExceptionDetail(record)"
              >
                <template #icon><icon-bug /></template>异常
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>

      <!-- 详情区域 -->
      <a-drawer
        title="日志详情"
        :visible="visible"
        :width="660"
        :footer="false"
        unmount-on-close
        render-to-body
        @cancel="handleCancel"
      >
        <div style="margin: 10px 0 0 10px">
          <a-descriptions title="基础信息" :column="2" bordered>
            <a-descriptions-item label="客户端 IP">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>{{ systemLog.clientIp }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="浏览器">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>{{ systemLog.browser }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="IP 归属地">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>{{ systemLog.location }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="请求耗时">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>
                <a-tag v-if="systemLog.elapsedTime > 500" color="red">
                  {{ systemLog.elapsedTime }} ms
                </a-tag>
                <a-tag v-else-if="systemLog.elapsedTime > 200" color="orange">
                  {{ systemLog.elapsedTime }} ms
                </a-tag>
                <a-tag v-else color="green"
                  >{{ systemLog.elapsedTime }} ms</a-tag
                >
              </span>
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>{{ systemLog.createTime }}</span>
            </a-descriptions-item>
          </a-descriptions>
          <a-descriptions
            title="协议信息"
            :column="2"
            bordered
            style="margin-top: 25px"
          >
            <a-descriptions-item label="状态码">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :rows="1" />
              </a-skeleton>
              <span v-else>
                <a-tag v-if="systemLog.statusCode >= 400" color="red">{{
                  systemLog.statusCode
                }}</a-tag>
                <a-tag v-else-if="systemLog.statusCode === 200" color="green">{{
                  systemLog.statusCode
                }}</a-tag>
                <a-tag v-else color="orange">{{ systemLog.statusCode }}</a-tag>
              </span>
            </a-descriptions-item>
            <a-descriptions-item label="请求方式">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :rows="1" />
              </a-skeleton>
              <span v-else>{{ systemLog.requestMethod }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="请求 URL" :span="2">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :rows="1" />
              </a-skeleton>
              <span v-else>{{ systemLog.requestUrl }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="响应体" :span="2">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :rows="3" />
              </a-skeleton>
              <a-space v-else>
                <VueJsonPretty
                  v-if="systemLog.responseBody"
                  :path="'res'"
                  :data="JSON.parse(systemLog.responseBody)"
                  :show-length="true"
                />
                <span v-else>无</span>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="响应头" :span="2">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :rows="3" />
              </a-skeleton>
              <a-space v-else>
                <VueJsonPretty
                  v-if="systemLog.responseHeaders"
                  :path="'res'"
                  :data="JSON.parse(systemLog.responseHeaders)"
                  :show-length="true"
                />
                <span v-else>无</span>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="请求体" :span="2">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :rows="3" />
              </a-skeleton>
              <a-space v-else>
                <VueJsonPretty
                  v-if="systemLog.requestBody"
                  :path="'res'"
                  :data="JSON.parse(systemLog.requestBody)"
                  :show-length="true"
                />
                <span v-else>无</span>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="请求头" :span="2">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :rows="3" />
              </a-skeleton>
              <a-space v-else>
                <VueJsonPretty
                  v-if="systemLog.requestHeaders"
                  :data="JSON.parse(systemLog.requestHeaders)"
                  :show-length="true"
                />
                <span v-else>无</span>
              </a-space>
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </a-drawer>

      <!-- 异常详情区域 -->
      <a-modal
        title="异常详情"
        :visible="exceptionDetailVisible"
        width="83%"
        :footer="false"
        top="30px"
        unmount-on-close
        render-to-body
        @cancel="handleExceptionDetailCancel"
      >
        <pre>{{ exceptionDetail }}</pre>
      </a-modal>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive } from 'vue';
  import {
    SystemLogParam,
    SystemLogRecord,
    SystemLogDetailRecord,
    listSystemLog,
    getSystemLog,
  } from '@/api/monitor/log';
  import VueJsonPretty from 'vue-json-pretty';
  import 'vue-json-pretty/lib/styles.css';

  const { proxy } = getCurrentInstance() as any;

  const systemLogList = ref<SystemLogRecord[]>([]);
  const systemLog = ref<SystemLogDetailRecord>({
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
  const total = ref(0);
  const exceptionDetail = ref('');
  const loading = ref(false);
  const visible = ref(false);
  const exceptionDetailVisible = ref(false);

  const data = reactive({
    // 查询参数
    queryParams: {
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
  const getList = (params: SystemLogParam = { ...queryParams.value }) => {
    loading.value = true;
    listSystemLog(params)
      .then((res) => {
        systemLogList.value = res.data.list;
        total.value = res.data.total;
      })
      .finally(() => {
        loading.value = false;
      });
  };
  getList();

  /**
   * 查看详情
   *
   * @param id ID
   */
  const toDetail = async (id: number) => {
    visible.value = true;
    loading.value = true;
    getSystemLog(id)
      .then((res) => {
        systemLog.value = res.data;
      })
      .finally(() => {
        loading.value = false;
      });
  };

  /**
   * 关闭详情
   */
  const handleCancel = () => {
    visible.value = false;
  };

  /**
   * 查看异常详情
   *
   * @param record 记录信息
   */
  const toExceptionDetail = async (record: SystemLogRecord) => {
    exceptionDetail.value = record.exceptionDetail || '';
    exceptionDetailVisible.value = true;
  };

  /**
   * 关闭异常详情
   */
  const handleExceptionDetailCancel = () => {
    exceptionDetail.value = '';
    exceptionDetailVisible.value = false;
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
    name: 'SystemLog',
  };
</script>

<style scoped lang="less"></style>
