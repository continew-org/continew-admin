<template>
  <a-modal
    v-model:visible="visible"
    title="视频播放"
    width="auto"
    draggable
    @close="close"
  >
    <div id="videoId"></div>
  </a-modal>
</template>

<script setup lang="ts">
  import Player from 'xgplayer';
  import type { FileItem } from '@/api/system/file';
  import { nextTick, onMounted, ref } from 'vue';

  interface Props {
    fileInfo: FileItem;
    onClose: () => void;
  }
  const props = withDefaults(defineProps<Props>(), {});

  const visible = ref(false);

  onMounted(() => {
    visible.value = true;
    nextTick(() => {
      // eslint-disable-next-line no-new
      new Player({
        id: 'videoId',
        url: props.fileInfo?.src || '',
        lang: 'zh-cn',
        autoplay: true,
        closeVideoClick: true,
        videoInit: true,
      });
    });
  });

  const close = () => {
    visible.value = false;
    props.onClose();
  };
</script>
