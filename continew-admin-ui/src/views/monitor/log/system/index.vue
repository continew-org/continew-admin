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
                      /(\w+):\/\/([^/:]+)(:\d*)?([^#|\?|\n]*)(\?.*)?/,
                  )[4]
              }}</span>
            </template>
          </a-table-column>
          <a-table-column title="IP" data-index="ip" />
          <a-table-column title="地址" data-index="address" />
          <a-table-column title="浏览器" data-index="browser" />
          <a-table-column title="耗时">
            <template #cell="{ record }">
              <a-tag v-if="record.timeTaken > 500" color="red"
                >{{ record.timeTaken }} ms</a-tag
              >
              <a-tag v-else-if="record.timeTaken > 200" color="orange"
                >{{ record.timeTaken }} ms</a-tag
              >
              <a-tag v-else color="green">{{ record.timeTaken }} ms</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="请求时间" data-index="createTime" />
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
          <a-descriptions :column="2" bordered>
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
            <a-descriptions-item label="IP">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>{{ systemLog.ip }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="地址">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>{{ systemLog.address }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="浏览器">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>{{ systemLog.browser }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="操作系统">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>{{ systemLog.os }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="请求时间">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>{{ systemLog.createTime }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="耗时">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :widths="['200px']" :rows="1" />
              </a-skeleton>
              <span v-else>
                <a-tag v-if="systemLog.timeTaken > 500" color="red">
                  {{ systemLog.timeTaken }} ms
                </a-tag>
                <a-tag v-else-if="systemLog.timeTaken > 200" color="orange">
                  {{ systemLog.timeTaken }} ms
                </a-tag>
                <a-tag v-else color="green">{{ systemLog.timeTaken }} ms</a-tag>
              </span>
            </a-descriptions-item>
            <a-descriptions-item label="请求 URI" :span="2">
              <a-skeleton v-if="loading" :animation="true">
                <a-skeleton-line :rows="1" />
              </a-skeleton>
              <span v-else>
                {{ systemLog.requestUrl }}
                <icon-copy
                  class="copy-btn"
                  @click="handleCopy(systemLog.requestUrl)"
                />
              </span>
            </a-descriptions-item>
          </a-descriptions>
          <a-descriptions
            layout="inline-vertical"
            :column="2"
            style="margin-top: 10px; position: relative"
          >
            <a-descriptions-item :span="2">
              <a-tabs type="card">
                <a-tab-pane key="1" title="响应头">
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
                </a-tab-pane>
                <a-tab-pane key="2" title="响应体">
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
                </a-tab-pane>
              </a-tabs>
            </a-descriptions-item>
          </a-descriptions>
          <a-descriptions
            layout="inline-vertical"
            :column="2"
            style="margin-top: 10px; position: relative"
          >
            <a-descriptions-item :span="2">
              <a-tabs type="card">
                <a-tab-pane key="1" title="请求头">
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
                </a-tab-pane>
                <a-tab-pane key="2" title="请求体">
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
                </a-tab-pane>
              </a-tabs>
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </a-drawer>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive, watch } from 'vue';
  import { useClipboard } from '@vueuse/core';
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
  const { copy, copied } = useClipboard();
  const systemLogList = ref<SystemLogRecord[]>([]);
  const systemLog = ref<SystemLogDetailRecord>({
    requestUrl: '',
    requestMethod: '',
    requestHeaders: '',
    requestBody: '',
    statusCode: 200,
    responseHeaders: '',
    responseBody: '',
    timeTaken: 0,
    ip: '',
    address: '',
    browser: '',
    os: '',
    createTime: '',
  });
  const total = ref(0);
  const loading = ref(false);
  const visible = ref(false);

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
   * 复制内容
   *
   * @param content 内容
   */
  const handleCopy = (content: string) => {
    copy(content);
  };
  watch(copied, () => {
    if (copied.value) {
      proxy.$message.success('复制成功');
    }
  });

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

<style scoped lang="less">
  .copy-btn {
    color: gray;
    cursor: pointer;
  }

  .copy-btn:hover {
    color: rgb(var(--arcoblue-6));
  }

  :deep(.arco-tabs-content) {
    padding-top: 5px;
    padding-left: 15px;
  }
</style>
