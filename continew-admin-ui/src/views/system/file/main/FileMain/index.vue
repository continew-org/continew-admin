<template>
  <div class="file-main">
    <a-row justify="space-between" class="row-operate">
      <!-- 左侧区域 -->
      <a-space wrap>
        <a-button type="primary" shape="round">
          <template #icon><icon-upload /></template>
          <template #default>上传</template>
        </a-button>
        <a-input-group>
          <a-space>
            <a-input placeholder="输入文件名称搜索" allow-clear> </a-input>
            <a-button type="primary">
              <template #icon><icon-search /></template>查询
            </a-button>
            <a-button>
              <template #icon><icon-refresh /></template>重置
            </a-button>
          </a-space>
        </a-input-group>
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
  import { fileTypeList, imageTypeList } from '@/constant/file';
  import { useFileStore } from '@/store/modules/file';
  import type { FileItem } from '@/api/system/file';
  import { getFileList } from '@/api/system/file';
  import { onBeforeRouteUpdate, useRoute, useRouter } from 'vue-router';
  import { onMounted, ref } from 'vue';
  import FileGrid from './FileGrid.vue';
  import FileList from './FileList.vue';
  import {
    openFileMoveModal,
    openFileRenameModal,
    previewFileVideoModal,
    previewFileAudioModal,
  } from '../../components/index';

  const route = useRoute();
  const router = useRouter();

  const fileStore = useFileStore();

  const loading = ref(false);
  // 文件列表数据
  const fileList = ref<FileItem[]>([]);
  const fileType = ref('0');
  // 批量操作
  const isBatchMode = ref(false);
  fileType.value = route.query.fileType?.toString() || '0';

  const getListData = async () => {
    try {
      loading.value = true;
      isBatchMode.value = false;
      // const res = await getFileList({ fileType: fileType.value });
      // fileList.value = res.data;
    } finally {
      loading.value = false;
    }
  };

  onMounted(() => {
    getListData();
  });

  onBeforeRouteUpdate((to) => {
    if (!to.query.fileType) return;
    fileType.value = to.query.fileType?.toString();
    getListData();
  });

  // 点击文件
  const handleClickFile = (item: FileItem) => {
    Message.success(`点击了文件-${item.name}`);
    if (imageTypeList.includes(item.extendName)) {
      if (item.src) {
        const imgList: string[] = fileList.value
          .filter((i) => imageTypeList.includes(i.extendName))
          .map((a) => a.src || '');
        const index = imgList.findIndex((i) => i === item.src);
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
    if (item.extendName === 'mp4') {
      previewFileVideoModal(item);
    }
    if (item.extendName === 'mp3') {
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
    if (mode === 'move') {
      openFileMoveModal(fileInfo);
    }
    if (mode === 'detail') {
      router.push({ path: '/file/detail' });
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
      margin: 20px 16px;
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
</style>
