<template>
  <a-modal
    v-model:visible="visible"
    :title="title"
    title-align="start"
    width="90%"
    modal-animation-name="el-fade"
    :footer="false"
    :modal-style="{ maxWidth: '300px' }"
    @close="cancel"
  >
    <a-row justify="center" align="center">
      <div style="height: 100px">
        <FileImg :data="fileInfo" style="border-radius: 5px"></FileImg>
      </div>
    </a-row>
    <a-row style="margin-top: 15px">
      <a-descriptions :column="1" title="详细信息" layout="inline-vertical">
        <a-descriptions-item :label="title">{{
          formatFileSize(fileInfo.size)
        }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{
          fileInfo.createTime
        }}</a-descriptions-item>
        <a-descriptions-item label="修改时间">{{
          fileInfo.updateTime
        }}</a-descriptions-item>
      </a-descriptions>
    </a-row>
  </a-modal>
</template>

<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { FileItem } from '@/api/system/file';
  import FileImg from '../../main/FileMain/FileImg.vue';

  interface Props {
    fileInfo: FileItem;
    onClose: () => void;
  }
  const props = withDefaults(defineProps<Props>(), {});
  const visible = ref(false);
  const title = ref();

  onMounted(() => {
    title.value = `${props.fileInfo.name}.${props.fileInfo.extension}`;
    visible.value = true;
  });

  const cancel = () => {
    visible.value = false;
    props.onClose();
  };

  const formatFileSize = (fileSize: number) => {
    if (fileSize == null || fileSize === 0) {
      return '0 Bytes';
    }
    const unitArr = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    let index = 0;
    const srcSize = parseFloat(fileSize.toString());
    index = Math.floor(Math.log(srcSize) / Math.log(1024));
    const size = srcSize / 1024 ** index;
    return `${size.toFixed(2)} ${unitArr[index]}`;
  };
</script>

<style lang="less" scoped>
  .label {
    color: var(--color-text-2);
  }
  :deep(.arco-form-item) {
    margin-bottom: 0;
  }
  :deep(.arco-form-item-label-col > label) {
    white-space: nowrap;
  }
  :deep(.arco-descriptions-title) {
    font-size: 14px;
  }
  :deep(.arco-descriptions-item-label-inline) {
    font-size: 12px;
  }
  :deep(.arco-descriptions-item-value-inline) {
    font-size: 12px;
    margin-bottom: 10px;
  }
</style>
