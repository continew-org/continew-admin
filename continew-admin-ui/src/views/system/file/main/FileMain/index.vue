<template>
  <div class="file-main">
    <a-row justify="space-between" class="row-operate">
      <!-- 左侧区域 -->
      <a-space wrap>
        <a-form ref="queryRef" :model="queryParams" layout="inline">
          <a-form-item hide-label>
            <a-button type="primary" shape="round">
              <template #icon><icon-upload /></template>
              <template #default>上传</template>
            </a-button>
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
        <a-button type="primary" @click="isBatchMode = !isBatchMode">
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
        @rightMenuClick="handleRightMenuClick"
      ></FileGrid>

      <!-- 文件列表-列表模式 -->
      <FileList
        v-show="fileList.length && fileStore.viewMode == 'list'"
        :data="fileList"
        :is-batch-mode="isBatchMode"
        @click="handleClickFile"
        @rightMenuClick="handleRightMenuClick"
      ></FileList>

      <a-empty v-show="!fileList.length"></a-empty>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import { Message, Modal } from '@arco-design/web-vue';
  import { api as viewerApi } from 'v-viewer';
  import { imageTypeList } from '@/constant/file';
  import { useFileStore } from '@/store/modules/file';
  import type { ListParam, FileItem } from '@/api/system/file';
  import { list } from '@/api/system/file';
  import { onBeforeRouteUpdate, useRoute, useRouter } from 'vue-router';
  import { getCurrentInstance, onMounted, reactive, ref, toRefs } from 'vue';
  import FileGrid from './FileGrid.vue';
  import FileList from './FileList.vue';
  import {
    openFileRenameModal,
    previewFileVideoModal,
    previewFileAudioModal,
  } from '../../components/index';

  const { proxy } = getCurrentInstance() as any;
  const route = useRoute();
  const router = useRouter();
  const fileStore = useFileStore();
  const loading = ref(false);
  // 文件列表数据
  const fileList = ref<FileItem[]>([]);
  // 批量操作
  const isBatchMode = ref(false);

  const data = reactive({
    // 查询参数
    queryParams: {
      name: undefined,
      type: route.query.type?.toString() || undefined,
      sort: ['updateTime,desc'],
    },
  });
  const { queryParams } = toRefs(data);

  const getList = async (params: ListParam = { ...queryParams.value }) => {
    try {
      loading.value = true;
      isBatchMode.value = false;
      params.type = params.type === '0' ? undefined : params.type;
      const res = await list(params);
      fileList.value = res.data.list;
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
    Message.success(`点击了文件-${item.name}`);
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
    Message.success(`点击了${mode}`);
    if (mode === 'delete') {
      Modal.warning({
        title: '提示',
        content: '是否删除该文件？',
        hideCancel: false,
      });
    }
    if (mode === 'rename') {
      openFileRenameModal(fileInfo);
    }
    if (mode === 'detail') {
      // TODO
    }
  };

  // 批量删除
  const handleMulDelete = () => {
    Modal.warning({
      title: '提示',
      content: '是否确认删除？',
      hideCancel: false,
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
