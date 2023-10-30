<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.system', 'menu.system.message.list']" />
    <a-card class="general-card" :title="$t('menu.system.message.list')">
      <!-- 头部区域 -->
      <div class="header">
        <!-- 搜索栏 -->
        <div v-if="showQuery" class="header-query">
          <a-form ref="queryRef" :model="queryParams" layout="inline">
            <a-form-item field="title" hide-label>
              <a-input
                v-model="queryParams.title"
                placeholder="输入主题搜索"
                allow-clear
                style="width: 230px"
                @press-enter="handleQuery"
              />
            </a-form-item>
            <a-form-item field="type" hide-label>
              <a-select
                v-model="queryParams.type"
                :options="message_type"
                placeholder="类型搜索"
                allow-clear
                style="width: 150px"
              />
            </a-form-item>

            <a-form-item field="type" hide-label>
              <a-select :style="{width:'150px'}" placeholder="是否已读" allow-clear v-model="queryParams.readStatus">
                <a-option :value="true">是</a-option>
                <a-option :value="false">否</a-option>
              </a-select>
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
        <!-- 操作栏 -->
        <div class="header-operation">
          <a-row>
            <a-col :span="12">
              <a-space>
                <a-button
                  type="primary"
                  status="success"
                  :disabled="readMultiple"
                  :title="readMultiple ? '请选择要读取的数据' : ''"
                  @click="handleBatchRedaMessage"
                >
                  <template #icon><icon-check /></template>标记已读
                </a-button>
                <a-button
                  type="primary"
                  status="success"
                  @click="handleAllRedaMessage"
                >
                  <template #icon><icon-check /></template>全部已读
                </a-button>
                <a-button
                  v-permission="['system:announcement:delete']"
                  type="primary"
                  status="danger"
                  :disabled="multiple"
                  :title="multiple ? '请选择要删除的数据' : ''"
                  @click="handleBatchDelete"
                >
                  <template #icon><icon-delete /></template>删除
                </a-button>
              </a-space>
            </a-col>
            <a-col :span="12">
              <right-toolbar
                v-model:show-query="showQuery"
                @refresh="getList"
              />
            </a-col>
          </a-row>
        </div>
      </div>

      <!-- 列表区域 -->
      <a-table
        ref="tableRef"
        row-key="id"
        :data="dataList"
        :loading="loading"
        :row-selection="{
          type: 'checkbox',
          showCheckedAll: true,
          onlyCurrent: false,
        }"
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
        @selection-change="handleSelectionChange"
      >
        <template #columns>
          <a-table-column title="序号">
            <template #cell="{ rowIndex }">
              {{ rowIndex + 1 + (queryParams.page - 1) * queryParams.size }}
            </template>
          </a-table-column>
          <a-table-column title="主题">
            <template #cell="{ record }">
              <a-link @click="toDetail(record.id)">{{ record.title }}</a-link>
            </template>
          </a-table-column>
          <a-table-column title="类型" align="center">
            <template #cell="{ record }">
              <dict-tag :value="record.type" :dict="message_type" />
            </template>
          </a-table-column>
          <a-table-column title="是否已读" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.readStatus" color="green">是</a-tag>
              <a-tag v-else color="red">否</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="发送时间" data-index="createTime" />
          <a-table-column
            v-if="checkPermission(['system:message:delete'])"
            title="操作"
            align="center"
          >
            <template #cell="{ record }">
              <a-button
                :disabled="record.readStatus"
                type="text"
                size="small"
                title="标记已读"
                @click="handleReadMessage([record.id])"
              >
                <template #icon><icon-check /></template>标记已读
              </a-button>

              <a-popconfirm
                content="确定要删除当前选中的数据吗？"
                type="warning"
                @ok="handleDelete([record.id])"
              >
                <a-button
                  v-permission="['system:announcement:delete']"
                  type="text"
                  size="small"
                  title="删除"
                  :disabled="record.disabled"
                >
                  <template #icon><icon-delete /></template>删除
                </a-button>
              </a-popconfirm>
            </template>
          </a-table-column>
        </template>
      </a-table>

      <!-- 详情区域 -->
      <a-drawer
        title="消息详情"
        :visible="detailVisible"
        :width="580"
        :footer="false"
        unmount-on-close
        render-to-body
        @cancel="handleDetailCancel"
      >
        <a-descriptions :column="2" bordered size="large">
          <a-descriptions-item label="主题">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ dataDetail.title }}</span>
          </a-descriptions-item>

          <a-descriptions-item label="类型">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>
              <dict-tag :value="dataDetail.type" :dict="message_type" />
            </span>
          </a-descriptions-item>

          <a-descriptions-item label="发送人">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else-if="dataDetail.createUserString">{{ dataDetail.createUserString }}</span>
            <dict-tag
              v-if="dataDetail.createUserString == null"
              :value="dataDetail.type"
              :dict="message_type"
            />
          </a-descriptions-item>

          <a-descriptions-item label="发送时间">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ dataDetail.createTime }}</span>
          </a-descriptions-item>

          <a-descriptions-item label="是否已读">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span color="green" v-else>
               <a-tag v-if="dataDetail.readStatus" color="green">已读</a-tag>
               <a-tag v-else color="red">未读</a-tag>
            </span>
          </a-descriptions-item>

          <a-descriptions-item label="读取时间">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ dataDetail.readTime }}</span>
          </a-descriptions-item>

          <a-descriptions-item label="内容">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ dataDetail.content || '无' }}</span>
          </a-descriptions-item>
        </a-descriptions>
      </a-drawer>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive } from 'vue';
  import {
    MessageRecord,
    page,
    ListParam,
    get,
    del,
    read,
  } from '@/api/system/message';
  import checkPermission from '@/utils/permission';

  const { proxy } = getCurrentInstance() as any;
  const { message_type } = proxy.useDict('message_type');

  const dataList = ref<MessageRecord[]>([]);
  const dataDetail = ref<MessageRecord>({});
  const total = ref(0);
  const ids = ref<Array<number>>([]);
  const single = ref(true);
  const multiple = ref(true);
  const readMultiple = ref(true);
  const showQuery = ref(true);
  const loading = ref(false);
  const detailVisible = ref(false);
  const detailLoading = ref(false);

  const data = reactive({
    // 查询参数
    queryParams: {
      title: undefined,
      readStatus: undefined,
      type: undefined,
      page: 1,
      size: 10,
      sort: ['createTime,desc'],
    },
  });
  const { queryParams } = toRefs(data);
  /**
   * 查询列表
   *
   */
  const getList = (params: ListParam = { ...queryParams.value }) => {
    loading.value = true;
    page(params)
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
   * 查看详情
   *
   * @param id ID
   */
  const toDetail = async (id: number) => {
    detailVisible.value = true;
    get(id).then((res) => {
      dataDetail.value = res.data;
    });
  };

  /**
   * 关闭详情
   */
  const handleDetailCancel = () => {
    detailVisible.value = false;
    dataDetail.value = {};
  };

  /**
   * 批量删除
   */
  const handleBatchDelete = () => {
    if (ids.value.length === 0) {
      proxy.$message.info('请选择要删除的数据');
    } else {
      proxy.$modal.warning({
        title: '警告',
        titleAlign: 'start',
        content: '确定要删除当前选中的数据吗？',
        hideCancel: false,
        onOk: () => {
          handleDelete(ids.value);
        },
      });
    }
  };

  /**
   * 删除
   *
   * @param ids ID 列表
   */
  const handleDelete = (ids: Array<number>) => {
    del(ids).then((res) => {
      proxy.$message.success(res.msg);
      getList();
      proxy.$refs.tableRef.selectAll(false);
    });
  };

  /**
   * 批量将消息设置为已读
   */
  const handleBatchRedaMessage = () => {
    if (ids.value.length === 0) {
      proxy.$message.info('请选择要读取的数据');
    } else {
      handleReadMessage(ids.value);
    }
  };

  /**
   * 批量所以消息设置为已读
   */
  const handleAllRedaMessage = () => {
      handleReadMessage([]);
  };

  /**
   * 将消息设置为已读
   *
   * @param ids ID 列表
   */
  const handleReadMessage = (ids: Array<number>) => {
    read(ids).then((res) => {
      proxy.$message.success(res.msg);
      getList();
      proxy.$refs.tableRef.selectAll(false);
    });
  };

  /**
   * 已选择的数据行发生改变时触发
   *
   * @param rowKeys ID 列表
   */
  const handleSelectionChange = (rowKeys: Array<any>) => {
   const unReadMessageList = dataList.value.filter(
      (item) => rowKeys.indexOf(item.id)!==-1 && !item.readStatus
    );
    readMultiple.value=!unReadMessageList.length
    ids.value = rowKeys;
    single.value = rowKeys.length !== 1;
    multiple.value = !rowKeys.length;

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
    name: 'Announcement',
  };
</script>

<style scoped lang="less">
  :deep(.github-markdown-body) {
    padding: 16px 32px 5px;
  }

  :deep(.arco-form-item-label-tooltip) {
    margin-left: 3px;
  }

  .meta-data {
    font-size: 15px;
    text-align: center;
  }

  .icon {
    margin-right: 3px;
  }

  .update-time-row {
    text-align: right;
  }
</style>
