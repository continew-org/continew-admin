<template>
  <div class="file-main">
    <a-row justify="space-between" class="row-operate">
      <!-- 左侧区域 -->
      <a-space wrap>
        <a-form ref="queryRef" :model="queryParams" layout="inline">
          <a-form-item hide-label>
            <a-upload
              v-permission="['system:file:upload']"
              :show-file-list="false"
              :custom-request="handleUpload"
            >
              <template #upload-button>
                <a-button type="primary" shape="round">
                  <template #icon><icon-upload /></template>
                  <template #default>上传</template>
                </a-button>
              </template>
            </a-upload>
          </a-form-item>
          <a-form-item field="name" hide-label>
            <a-input
              v-model="queryParams.name"
              placeholder="输入文件名称搜索"
              allow-clear
              @press-enter="handleQuery"
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
      </a-space>

      <!-- 右侧区域 -->
      <a-space wrap>
        <a-button
          v-if="isBatchMode"
          :disabled="!fileStore.selectedFileIds.length"
          type="primary"
          status="danger"
          @click="handleMulDelete"
          ><template #icon><icon-delete /></template
        ></a-button>
        <a-button
          v-permission="['system:file:delete']"
          type="primary"
          @click="isBatchMode = !isBatchMode"
        >
          <template #icon><icon-select-all /></template>
          <template #default>{{
            isBatchMode ? '取消批量' : '批量操作'
          }}</template>
        </a-button>
        <a-button-group>
          <a-tooltip content="视图" position="bottom">
            <a-button @click="fileStore.changeViewMode">
              <template #icon>
                <icon-apps v-if="fileStore.viewMode === 'grid'" />
                <icon-list v-else />
              </template>
            </a-button>
          </a-tooltip>
          <a-tooltip content="配置存储库" position="bottom">
            <a-button
              v-permission="['system:storage:list']"
              @click="handleConfig"
            >
              <template #icon>
                <icon-settings />
              </template>
            </a-button>
          </a-tooltip>
        </a-button-group>
      </a-space>
    </a-row>

    <!-- 文件列表-宫格模式 -->
    <a-spin class="file-wrap" :loading="loading">
      <FileGrid
        v-show="fileList.length && fileStore.viewMode == 'grid'"
        :data="fileList"
        :is-batch-mode="isBatchMode"
        :selected-file-ids="fileStore.selectedFileIds"
        @click="handleClickFile"
        @check="handleCheckFile"
        @right-menu-click="handleRightMenuClick"
      ></FileGrid>

      <!-- 文件列表-列表模式 -->
      <FileList
        v-show="fileList.length && fileStore.viewMode == 'list'"
        :data="fileList"
        :is-batch-mode="isBatchMode"
        @click="handleClickFile"
        @right-menu-click="handleRightMenuClick"
      ></FileList>

      <a-empty v-show="!fileList.length"></a-empty>
    </a-spin>

    <!-- 配置存储库 -->
    <a-drawer
      title="配置存储库"
      :visible="storageVisible"
      :width="1070"
      :mask-closable="false"
      :esc-to-close="false"
      unmount-on-close
      render-to-body
      :footer="false"
      @cancel="handleCancelConfig"
    >
      <!-- 操作栏 -->
      <div class="header-operation" style="margin-bottom: 16px">
        <a-row>
          <a-col :span="12">
            <a-space>
              <a-button
                v-permission="['system:storage:add']"
                type="primary"
                @click="toAddStorage"
              >
                <template #icon><icon-plus /></template>新增
              </a-button>
              <a-button
                v-permission="['system:storage:export']"
                :loading="exportStorageLoading"
                type="primary"
                status="warning"
                @click="handleStorageExport"
              >
                <template #icon><icon-download /></template>导出
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </div>
      <a-table
        row-key="id"
        :data="storageList"
        :loading="storageLoading"
        :bordered="false"
        :pagination="{
          showTotal: true,
          showPageSize: true,
          total: totalStorage,
          current: storageQueryParams.page,
        }"
        size="large"
        column-resizable
        stripe
        @page-change="handleStoragePageChange"
        @page-size-change="handleStoragePageSizeChange"
      >
        <template #columns>
          <a-table-column title="名称" :width="135">
            <template #cell="{ record }">
              {{ record.name }}
              <a-tag v-if="record.isDefault" color="arcoblue">默认</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="编码" data-index="code" />
          <a-table-column title="类型" align="center">
            <template #cell="{ record }">
              <dict-tag :value="record.type" :dict="storage_type_enum" />
            </template>
          </a-table-column>
          <a-table-column
            title="访问密钥"
            data-index="accessKey"
            ellipsis
            tooltip
          />
          <a-table-column
            title="终端节点"
            data-index="endpoint"
            ellipsis
            tooltip
          />
          <a-table-column
            title="桶名称"
            data-index="bucketName"
            ellipsis
            tooltip
          />
          <a-table-column title="域名" data-index="domain" ellipsis tooltip />
          <a-table-column title="状态" align="center">
            <template #cell="{ record }">
              <a-switch
                v-model="record.status"
                :checked-value="1"
                :unchecked-value="2"
                :disabled="!checkPermission(['system:storage:update'])"
                @change="handleStorageChangeStatus(record)"
              />
            </template>
          </a-table-column>
          <a-table-column
            title="描述"
            data-index="description"
            ellipsis
            tooltip
          />
          <a-table-column
            v-if="
              checkPermission([
                'system:storage:update',
                'system:storage:delete',
              ])
            "
            title="操作"
            align="center"
            fixed="right"
            :width="90"
          >
            <template #cell="{ record }">
              <a-button
                v-permission="['system:storage:update']"
                type="text"
                size="small"
                title="修改"
                @click="toUpdateStorage(record.id)"
              >
                <template #icon><icon-edit /></template>
              </a-button>
              <a-popconfirm
                content="确定要删除当前选中的数据吗？"
                type="warning"
                @ok="handleDeleteStorage([record.id])"
              >
                <a-button
                  v-permission="['system:storage:delete']"
                  type="text"
                  size="small"
                  :title="record.isDefault ? '默认存储库不能删除' : '删除'"
                  :disabled="record.disabled"
                >
                  <template #icon><icon-delete /></template>
                </a-button>
              </a-popconfirm>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-drawer>
    <!-- 表单区域 -->
    <a-modal
      :title="storageFormTitle"
      :visible="storageFormVisible"
      :width="580"
      :mask-closable="false"
      :esc-to-close="false"
      unmount-on-close
      render-to-body
      @ok="handleStorageFormOk"
      @cancel="handleStorageFormCancel"
    >
      <a-form
        ref="storageFormRef"
        :model="storageForm"
        :rules="storageRules"
        size="large"
        :label-col-style="{ width: '84px' }"
        layout="inline"
      >
        <a-form-item label="名称" field="name">
          <a-input
            v-model="storageForm.name"
            placeholder="请输入名称"
            style="width: 162px"
          />
        </a-form-item>
        <a-form-item label="编码" field="code">
          <a-input
            v-model="storageForm.code"
            placeholder="请输入编码"
            style="width: 162px"
          />
        </a-form-item>
        <a-form-item label="类型" field="type">
          <a-select
            v-model="storageForm.type"
            :options="storage_type_enum"
            placeholder="请选择存储类型"
            style="width: 416px"
          />
        </a-form-item>
        <a-form-item
          v-if="storageForm.type === 1"
          label="访问密钥"
          field="accessKey"
        >
          <a-input
            v-model="storageForm.accessKey"
            placeholder="请输入访问密钥"
            style="width: 416px"
          />
        </a-form-item>
        <a-form-item
          v-if="storageForm.type === 1"
          label="私有密钥"
          field="secretKey"
        >
          <a-input
            v-model="storageForm.secretKey"
            placeholder="请输入私有密钥"
            style="width: 416px"
          />
        </a-form-item>
        <a-form-item
          v-if="storageForm.type === 1"
          label="终端节点"
          field="endpoint"
        >
          <a-input
            v-model="storageForm.endpoint"
            placeholder="请输入终端节点"
            style="width: 416px"
          />
        </a-form-item>
        <a-form-item label="桶名称" field="bucketName">
          <a-input
            v-model="storageForm.bucketName"
            placeholder="请输入桶名称"
            style="width: 416px"
          />
        </a-form-item>
        <a-form-item v-if="storageForm.type === 1" label="域名" field="domain">
          <a-input
            v-model="storageForm.domain"
            placeholder="请输入域名"
            style="width: 416px"
          />
        </a-form-item>
        <a-form-item
          v-if="storageForm.type === 2"
          label="域名"
          field="domain"
          :rules="[
            {
              required: true,
              message: '请输入域名',
            },
          ]"
        >
          <a-input
            v-model="storageForm.domain"
            placeholder="请输入域名"
            style="width: 416px"
          />
        </a-form-item>
        <a-form-item label="排序" field="sort">
          <a-input-number
            v-model="storageForm.sort"
            placeholder="请输入排序"
            style="width: 416px"
            :min="1"
            mode="button"
          />
        </a-form-item>
        <a-form-item label="描述" field="description">
          <a-textarea
            v-model="storageForm.description"
            :max-length="200"
            placeholder="请输入描述"
            style="width: 416px"
            :auto-size="{
              minRows: 3,
            }"
            show-word-limit
          />
        </a-form-item>
        <a-form-item label="默认存储" field="isDefault">
          <a-switch v-model="storageForm.isDefault" type="round" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { Modal, RequestOption } from '@arco-design/web-vue';
  import checkPermission from '@/utils/permission';
  import { api as viewerApi } from 'v-viewer';
  import { imageTypeList } from '@/constant/file';
  import { useFileStore } from '@/store/modules/file';
  import type { ListParam, FileItem } from '@/api/system/file';
  import { list, del } from '@/api/system/file';
  import type { DataRecord as StorageDataRecord } from '@/api/system/storage';
  import {
    list as listStorage,
    get as getStorage,
    add as addStorage,
    update as updateStorage,
    del as delStorage,
  } from '@/api/system/storage';
  import { upload } from '@/api/common';
  import { onBeforeRouteUpdate, useRoute } from 'vue-router';
  import { getCurrentInstance, onMounted, reactive, ref, toRefs } from 'vue';
  import FileGrid from './FileGrid.vue';
  import FileList from './FileList.vue';
  import {
    openFileRenameModal,
    openFileDetailModal,
    previewFileVideoModal,
    previewFileAudioModal,
  } from '../../components/index';
  import 'viewerjs/dist/viewer.css';

  const { proxy } = getCurrentInstance() as any;
  const route = useRoute();
  const fileStore = useFileStore();
  const loading = ref(false);
  // 文件列表数据
  const fileList = ref<FileItem[]>([]);
  // 批量操作
  const isBatchMode = ref(false);
  const storageVisible = ref(false);
  const storageLoading = ref(false);
  const exportStorageLoading = ref(false);
  const storageList = ref<StorageDataRecord[]>([]);
  const totalStorage = ref(0);
  const storageFormTitle = ref();
  const storageFormVisible = ref(false);
  const { storage_type_enum } = proxy.useDict('storage_type_enum');

  const data = reactive({
    // 查询参数
    queryParams: {
      name: undefined,
      type: route.query.type?.toString() || undefined,
      sort: ['updateTime,desc'],
    },
    storageQueryParams: {
      page: 1,
      size: 10,
      sort: ['updateTime,desc'],
    },
    storageForm: {} as StorageDataRecord,
    storageRules: {
      name: [{ required: true, message: '请输入名称' }],
      code: [{ required: true, message: '请输入编码' }],
      type: [{ required: true, message: '请选择类型' }],
      accessKey: [{ required: true, message: '请输入访问密钥' }],
      secretKey: [{ required: true, message: '请输入私有密钥' }],
      endpoint: [{ required: true, message: '请输入终端节点' }],
      bucketName: [{ required: true, message: '请输入桶名称' }],
    },
  });
  const { queryParams, storageQueryParams, storageForm, storageRules } =
    toRefs(data);

  const getList = async (params: ListParam = { ...queryParams.value }) => {
    try {
      loading.value = true;
      isBatchMode.value = false;
      params.type = params.type === '0' ? undefined : params.type;
      const res = await list(params);
      fileList.value = res.data;
    } finally {
      loading.value = false;
    }
  };

  onMounted(() => {
    getList();
  });

  onBeforeRouteUpdate((to) => {
    if (!to.query.type) return;
    queryParams.value.type = to.query.type?.toString();
    getList();
  });

  // 点击文件
  const handleClickFile = (item: FileItem) => {
    if (imageTypeList.includes(item.extension)) {
      if (item.url) {
        const imgList: string[] = fileList.value
          .filter((i) => imageTypeList.includes(i.extension))
          .map((a) => a.url || '');
        const index = imgList.findIndex((i) => i === item.url);
        if (imgList.length) {
          viewerApi({
            options: {
              initialViewIndex: index,
            },
            images: imgList,
          });
        }
      }
    }
    if (item.extension === 'mp4') {
      previewFileVideoModal(item);
    }
    if (item.extension === 'mp3') {
      previewFileAudioModal(item);
    }
  };

  // 勾选文件
  const handleCheckFile = (item: FileItem) => {
    fileStore.addSelectedFileItem(item);
  };
  // 鼠标右键
  const handleRightMenuClick = (mode: string, fileInfo: FileItem) => {
    if (mode === 'delete') {
      Modal.warning({
        title: '提示',
        content: `是否确定删除文件 [${fileInfo.name}]？`,
        hideCancel: false,
        onOk: () => {
          del(fileInfo.id).then((res) => {
            proxy.$message.success(res.msg);
            getList();
          });
        },
      });
    }
    if (mode === 'download') {
      const link = document.createElement('a');
      link.href = fileInfo.url;
      link.download = fileInfo.name;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
    if (mode === 'rename') {
      openFileRenameModal(fileInfo);
    }
    if (mode === 'detail') {
      openFileDetailModal(fileInfo);
    }
  };

  // 批量删除
  const handleMulDelete = () => {
    Modal.warning({
      title: '提示',
      content: `是否确定删除所选的${fileStore.selectedFileIds.length}个文件？`,
      hideCancel: false,
      onOk: () => {
        del(fileStore.selectedFileIds).then((res) => {
          proxy.$message.success(res.msg);
          getList();
        });
      },
    });
  };

  /**
   * 上传
   *
   * @param options /
   */
  const handleUpload = (options: RequestOption) => {
    const controller = new AbortController();
    (async function requestWrap() {
      const {
        onProgress,
        onError,
        onSuccess,
        fileItem,
        name = 'file',
      } = options;
      onProgress(20);
      const formData = new FormData();
      formData.append(name as string, fileItem.file as Blob);
      upload(formData)
        .then((res) => {
          onSuccess(res);
          getList();
          proxy.$message.success(res.msg);
        })
        .catch((error) => {
          onError(error);
        });
    })();
    return {
      abort() {
        controller.abort();
      },
    };
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
   * 查询存储库列表
   *
   * @param params 参数
   */
  const getStorageList = async (
    params: ListParam = { ...storageQueryParams.value },
  ) => {
    try {
      storageLoading.value = true;
      const res = await listStorage(params);
      storageList.value = res.data.list;
      totalStorage.value = res.data.total;
    } finally {
      storageLoading.value = false;
    }
  };

  /**
   * 重置表单
   */
  const resetStorage = () => {
    storageForm.value = {
      type: 1,
      sort: 999,
      isDefault: false,
    };
    proxy.$refs.storageFormRef?.resetFields();
  };

  /**
   * 打开修改对话框
   *
   * @param id ID
   */
  const toUpdateStorage = (id: number) => {
    resetStorage();
    getStorage(id).then((res) => {
      storageForm.value = res.data;
      storageFormTitle.value = '修改存储库';
      storageFormVisible.value = true;
    });
  };

  /**
   * 打开新增对话框
   */
  const toAddStorage = () => {
    resetStorage();
    storageFormTitle.value = '新增存储库';
    storageFormVisible.value = true;
  };

  /**
   * 删除
   *
   * @param ids ID 列表
   */
  const handleDeleteStorage = (ids: Array<number>) => {
    delStorage(ids).then((res) => {
      proxy.$message.success(res.msg);
      getStorageList();
    });
  };

  /**
   * 配置
   */
  const handleConfig = () => {
    getStorageList();
    storageVisible.value = true;
  };

  /**
   * 取消配置
   */
  const handleCancelConfig = () => {
    storageVisible.value = false;
    storageList.value = [];
  };

  /**
   * 切换页码
   *
   * @param current 页码
   */
  const handleStoragePageChange = (current: number) => {
    storageQueryParams.value.page = current;
    getStorageList();
  };

  /**
   * 切换每页条数
   *
   * @param pageSize 每页条数
   */
  const handleStoragePageSizeChange = (pageSize: number) => {
    storageQueryParams.value.size = pageSize;
    getStorageList();
  };

  /**
   * 修改状态
   *
   * @param record 记录信息
   */
  const handleStorageChangeStatus = (record: StorageDataRecord) => {
    const { id } = record;
    if (id) {
      const tip = record.status === 1 ? '启用' : '禁用';
      getStorage(id)
        .then((res) => {
          res.data.status = record.status;
          updateStorage(res.data, id)
            .then(() => {
              proxy.$message.success(`${tip}成功`);
            })
            .catch(() => {
              record.status = record.status === 1 ? 2 : 1;
            });
        })
        .catch(() => {
          record.status = record.status === 1 ? 2 : 1;
        });
    }
  };

  /**
   * 取消
   */
  const handleStorageFormCancel = () => {
    storageFormVisible.value = false;
    proxy.$refs.storageFormRef?.resetFields();
  };

  /**
   * 确定
   */
  const handleStorageFormOk = () => {
    proxy.$refs.storageFormRef.validate((valid: any) => {
      if (!valid) {
        if (storageForm.value.id !== undefined) {
          updateStorage(storageForm.value, storageForm.value.id).then((res) => {
            handleStorageFormCancel();
            getStorageList();
            proxy.$message.success(res.msg);
          });
        } else {
          addStorage(storageForm.value).then((res) => {
            handleStorageFormCancel();
            getStorageList();
            proxy.$message.success(res.msg);
          });
        }
      }
    });
  };

  /**
   * 导出
   */
  const handleStorageExport = () => {
    if (exportStorageLoading.value) return;
    exportStorageLoading.value = true;
    proxy
      .download('/system/storage/export', { ...storageQueryParams.value }, '存储库数据')
      .finally(() => {
        exportStorageLoading.value = false;
      });
  };
</script>

<style lang="less" scoped>
  .file-main {
    height: 100%;
    background: var(--color-bg-1);
    border-radius: 2px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    .row-operate {
      border-bottom: 1px dashed var(--color-border-3);
      margin: 20px 16px 0;
    }
    .file-wrap {
      flex: 1;
      padding: 0 16px 16px;
      box-sizing: border-box;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }
  }

  :deep(.arco-form-item-layout-inline) {
    margin-right: 8px;
  }
</style>
