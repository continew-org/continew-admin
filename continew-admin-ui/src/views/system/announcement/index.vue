<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.system', 'menu.system.announcement.list']" />
    <a-card class="general-card" :title="$t('menu.system.announcement.list')">
      <!-- 头部区域 -->
      <div class="header">
        <!-- 搜索栏 -->
        <div v-if="showQuery" class="header-query">
          <a-form ref="queryRef" :model="queryParams" layout="inline">
            <a-form-item field="title" hide-label>
              <a-input
                v-model="queryParams.title"
                placeholder="输入标题搜索"
                allow-clear
                style="width: 230px"
                @press-enter="handleQuery"
              />
            </a-form-item>
            <a-form-item field="type" hide-label>
              <a-select
                v-model="queryParams.type"
                :options="announcement_type"
                placeholder="类型搜索"
                allow-clear
                style="width: 150px"
              />
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
                  v-permission="['system:announcement:add']"
                  type="primary"
                  @click="toAdd"
                >
                  <template #icon><icon-plus /></template>新增
                </a-button>
                <a-button
                  v-permission="['system:announcement:update']"
                  type="primary"
                  status="success"
                  :disabled="single"
                  :title="single ? '请选择一条要修改的数据' : ''"
                  @click="toUpdate(ids[0])"
                >
                  <template #icon><icon-edit /></template>修改
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
                <a-button
                  v-permission="['system:announcement:export']"
                  :loading="exportLoading"
                  type="primary"
                  status="warning"
                  @click="handleExport"
                >
                  <template #icon><icon-download /></template>导出
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
          <a-table-column title="标题">
            <template #cell="{ record }">
              <a-link @click="toDetail(record.id)">{{ record.title }}</a-link>
            </template>
          </a-table-column>
          <a-table-column title="类型" align="center">
            <template #cell="{ record }">
              <dict-tag :value="record.type" :dict="announcement_type" />
            </template>
          </a-table-column>
          <a-table-column title="状态" align="center">
            <template #cell="{ record }">
              <dict-tag
                :value="record.status"
                :dict="announcement_status_enum"
              />
            </template>
          </a-table-column>
          <a-table-column title="生效时间">
            <template #cell="{ record }">
              {{ record.effectiveTime ? record.effectiveTime : '无' }}
            </template>
          </a-table-column>
          <a-table-column title="终止时间">
            <template #cell="{ record }">
              {{ record.terminateTime ? record.terminateTime : '无' }}
            </template>
          </a-table-column>
          <a-table-column title="创建人" data-index="createUserString" />
          <a-table-column title="创建时间" data-index="createTime" />
          <a-table-column
            v-if="
              checkPermission([
                'system:announcement:update',
                'system:announcement:delete',
              ])
            "
            title="操作"
            align="center"
          >
            <template #cell="{ record }">
              <a-button
                v-permission="['system:announcement:update']"
                type="text"
                size="small"
                title="修改"
                @click="toUpdate(record.id)"
              >
                <template #icon><icon-edit /></template>修改
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

      <!-- 表单区域 -->
      <a-modal
        :title="title"
        :visible="visible"
        width="80%"
        :mask-closable="false"
        :esc-to-close="false"
        unmount-on-close
        render-to-body
        @ok="handleOk"
        @cancel="handleCancel"
      >
        <a-form
          ref="formRef"
          :model="form"
          :rules="rules"
          layout="vertical"
          :label-col-style="{ width: '75px' }"
          size="large"
        >
          <a-row :gutter="16">
            <a-col :span="24">
              <a-form-item label="标题" field="title">
                <a-input
                  v-model="form.title"
                  placeholder="请输入标题"
                  :max-length="150"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="类型" field="type">
                <a-select
                  v-model="form.type"
                  :options="announcement_type"
                  placeholder="请选择类型"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                label="生效时间"
                field="effectiveTime"
                tooltip="默认立即生效"
              >
                <a-date-picker
                  v-model="form.effectiveTime"
                  placeholder="请选择生效时间"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                label="终止时间"
                field="terminateTime"
                tooltip="默认无终止"
              >
                <a-date-picker
                  v-model="form.terminateTime"
                  placeholder="请选择终止时间"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="24">
              <a-form-item label="内容" field="content">
                <v-md-editor
                  v-model="form.content"
                  height="400px"
                  left-toolbar="undo redo clear | h bold italic strikethrough quote | ul ol table hr | link image code"
                  placeholder="请输入内容"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-modal>

      <!-- 详情区域 -->
      <a-modal
        :visible="detailVisible"
        modal-class="detail"
        width="70%"
        :footer="false"
        unmount-on-close
        render-to-body
        @cancel="handleDetailCancel"
      >
        <a-spin
          :loading="detailLoading"
          tip="公告正在赶来..."
          style="width: 100%"
        >
          <template #icon>
            <icon-sync />
          </template>
          <a-typography :style="{ marginTop: '-40px', textAlign: 'center' }">
            <a-typography-title>
              {{ dataDetail.title }}
            </a-typography-title>
            <a-typography-paragraph>
              <div class="meta-data">
                <a-space>
                  <span>
                    <icon-user class="icon" />
                    <span class="label">发布人：</span>
                    <span>{{ dataDetail.createUserString }}</span>
                  </span>
                  <a-divider direction="vertical" />
                  <span>
                    <svg-icon icon-class="clock-circle" class="icon" />
                    <span class="label">发布时间：</span>
                    <span>{{
                      dataDetail.effectiveTime
                        ? dataDetail.effectiveTime
                        : dataDetail.createTime
                    }}</span>
                  </span>
                </a-space>
              </div>
            </a-typography-paragraph>
          </a-typography>
          <a-divider />
          <v-md-preview :text="dataDetail.content"></v-md-preview>
          <a-divider />
          <div v-if="dataDetail.updateTime" class="update-time-row">
            <span>
              <icon-schedule class="icon" />
              <span>最后更新于：</span>
              <span>{{ dataDetail.updateTime }}</span>
            </span>
          </div>
        </a-spin>
      </a-modal>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive } from 'vue';
  import {
    DataRecord,
    ListParam,
    list,
    get,
    add,
    update,
    del,
  } from '@/api/system/announcement';
  import checkPermission from '@/utils/permission';

  const { proxy } = getCurrentInstance() as any;
  const { announcement_type, announcement_status_enum } = proxy.useDict(
    'announcement_type',
    'announcement_status_enum'
  );

  const dataList = ref<DataRecord[]>([]);
  const dataDetail = ref<DataRecord>({
    // TODO 待补充详情字段默认值
  });
  const total = ref(0);
  const ids = ref<Array<number>>([]);
  const title = ref('');
  const single = ref(true);
  const multiple = ref(true);
  const showQuery = ref(true);
  const loading = ref(false);
  const detailLoading = ref(false);
  const exportLoading = ref(false);
  const visible = ref(false);
  const detailVisible = ref(false);

  const data = reactive({
    // 查询参数
    queryParams: {
      title: undefined,
      status: undefined,
      type: undefined,
      page: 1,
      size: 10,
      sort: ['createTime,desc'],
    },
    // 表单数据
    form: {} as DataRecord,
    // 表单验证规则
    rules: {
      title: [{ required: true, message: '请输入标题' }],
      content: [{ required: true, message: '请输入内容' }],
      type: [{ required: true, message: '请选择类型' }],
    },
  });
  const { queryParams, form, rules } = toRefs(data);

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
   * 打开新增对话框
   */
  const toAdd = () => {
    reset();
    title.value = '新增公告';
    visible.value = true;
  };

  /**
   * 打开修改对话框
   *
   * @param id ID
   */
  const toUpdate = (id: number) => {
    reset();
    get(id).then((res) => {
      form.value = res.data;
      title.value = '修改公告';
      visible.value = true;
    });
  };

  /**
   * 重置表单
   */
  const reset = () => {
    form.value = {
      // TODO 待补充需要重置的字段默认值，详情请参考其他模块 index.vue
    };
    proxy.$refs.formRef?.resetFields();
  };

  /**
   * 取消
   */
  const handleCancel = () => {
    visible.value = false;
    proxy.$refs.formRef.resetFields();
  };

  /**
   * 确定
   */
  const handleOk = () => {
    proxy.$refs.formRef.validate((valid: any) => {
      if (!valid) {
        if (form.value.id !== undefined) {
          update(form.value, form.value.id).then((res) => {
            handleCancel();
            getList();
            proxy.$message.success(res.msg);
          });
        } else {
          add(form.value).then((res) => {
            handleCancel();
            getList();
            proxy.$message.success(res.msg);
          });
        }
      }
    });
  };

  /**
   * 查看详情
   *
   * @param id ID
   */
  const toDetail = async (id: number) => {
    if (detailLoading.value) return;
    detailLoading.value = true;
    detailVisible.value = true;
    get(id)
      .then((res) => {
        dataDetail.value = res.data;
      })
      .finally(() => {
        detailLoading.value = false;
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
    });
  };

  /**
   * 已选择的数据行发生改变时触发
   *
   * @param rowKeys ID 列表
   */
  const handleSelectionChange = (rowKeys: Array<any>) => {
    ids.value = rowKeys;
    single.value = rowKeys.length !== 1;
    multiple.value = !rowKeys.length;
  };

  /**
   * 导出
   */
  const handleExport = () => {
    if (exportLoading.value) return;
    exportLoading.value = true;
    proxy
      .download(
        '/system/announcement/export',
        { ...queryParams.value },
        '公告数据'
      )
      .finally(() => {
        exportLoading.value = false;
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
